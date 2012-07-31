/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.store.legacy.internal.datanucleus;

import java.util.HashMap;
import java.util.Map;

import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.LargeStringProperty;
import com.xpn.xwiki.objects.StringProperty;
import com.xpn.xwiki.objects.ListProperty;
import com.xpn.xwiki.objects.IntegerProperty;
import com.xpn.xwiki.objects.FloatProperty;
import com.xpn.xwiki.objects.DoubleProperty;
import com.xpn.xwiki.objects.LongProperty;
import com.xpn.xwiki.objects.DateProperty;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.store.datanucleus.internal.JavaIdentifierEscaper;
import org.xwiki.store.datanucleus.internal.GroovyPersistableClassCompiler;
import org.xwiki.store.datanucleus.internal.JavaClassNameDocumentReferenceSerializer;
import org.xwiki.store.objects.PersistableClass;
import org.xwiki.store.objects.PersistableClassLoader;
import org.xwiki.store.objects.PersistableObject;

/**
 * A converter which takes an XWiki class and converts it to a PersistanceCapable
 * Java class so that it can be stored using JDO.
 */
final class XClassConverter
{
    /** Words which must not be used as identifiers. */
    public static final String[] RESERVED_WORDS = new String[] {
        // Reserved because the PersistableObject field called 'id' is used.
        "id",

        // Reserved because there is a field called 'className' which contains a
        // string representation of the class name.
        "className",

        // Used internally.
        "XCLASS_METADATA"
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(XClassConverter.class);

    private final PersistableClassLoader loader;

    private GroovyPersistableClassCompiler compiler;

    public XClassConverter(final PersistableClassLoader loader)
    {
        this.loader = loader;
    }

    /**
     * Get a class for a given XObject.
     * This will try to load the class and if unsuccessful will create it.
     * Creating the class is only necessary to support the case of objects being inserted before
     * their class is defined.
     *
     * @param xobject the object to load or define a class for.
     */
    public PersistableClass<XObject> convert(final BaseObject xobject)
    {
        final String className =
            JavaClassNameDocumentReferenceSerializer.serializeRef(xobject.getXClassReference(),
                                                                  null);
        try {
            return (PersistableClass<XObject>) this.loader.loadPersistableClass(className);
        } catch (ClassNotFoundException e) {
            // Ok then, we'll define it.
        }

        // Get fields.
        final Map<String, Class> propertyMap = new HashMap<String, Class>();
        for (final String fieldName : xobject.getPropertyList()) {
            propertyMap.put(fieldName, xobject.getField(fieldName).getClass());
        }

        return convert(xobject.getXClassReference(), propertyMap);
    }


    public PersistableClass<XObject> convert(final DocumentReference classDocReference,
                                             final Map<String, Class> propertyMap)
    {
        final StringBuilder sb = new StringBuilder();
        writeClass(classDocReference, propertyMap, sb);
        final String source = sb.toString();
        LOGGER.debug("Generated class: \n\n{}\n\n", source);
        if (this.compiler == null) {
            this.compiler = new GroovyPersistableClassCompiler(loader);
        }
        final PersistableClass<XObject> pc = this.compiler.compile(source);
        return pc;
    }

    private static void writeClass(final DocumentReference classDocReference,
                                   final Map<String, Class> propertyMap,
                                   final StringBuilder writeTo)
    {
        final EntityReference spaceRef = classDocReference.getParent();

        // package
        writeTo.append("package ");
        writeReference(spaceRef, writeTo);
        writeTo.append(";\n\n");

        // imports
        writeTo.append(
              "import java.util.Map;\n"
            + "import java.util.HashMap;\n"
            + "import javax.jdo.annotations.Element;\n"
            + "import javax.jdo.annotations.Index;\n"
            + "import javax.jdo.annotations.PersistenceCapable;\n"
            + "import javax.jdo.annotations.Persistent;\n"
            + "import org.xwiki.store.legacy.internal.datanucleus.AbstractXObject;\n"
            + "\n"
            + "@PersistenceCapable(detachable=\"true\")\n"
        );

        writeTo.append("public final class ")
            .append(JavaIdentifierEscaper.escape(classDocReference.getName()))
                .append(" extends AbstractXObject\n{\n");

        writeMetaData(propertyMap, writeTo);
        writeFields(propertyMap, writeTo);
        writeClassName(writeTo);
        writeGetMetaData(writeTo);
        writeSetFields(propertyMap, writeTo);
        writeGetFields(propertyMap, writeTo);

        writeTo.append("}\n");
    }

    private static void writeClassName(final StringBuilder writeTo)
    {
        writeTo.append("@Index\n"
                     + "private String className = this.getClass().getName();\n\n");
    }

    private static void writeMetaData(final Map<String, Class> propertyMap,
                                      final StringBuilder writeTo)
    {
        writeTo.append(
            "private static final Map<String, Class> XCLASS_METADATA =\n"
            + "    new HashMap<String, Class>();\n"
            + "\n"
            + "static {\n");
        for (final Map.Entry<String, Class> e : propertyMap.entrySet()) {
            writeTo.append("    XCLASS_METADATA.put(\"")
                .append(StringEscapeUtils.escapeJava(e.getKey())).append("\", ")
                    .append(e.getValue().getName()).append(".class);\n");
        }
        writeTo.append("}\n\n");
    }

    private static void writeGetMetaData(final StringBuilder writeTo)
    {
        writeTo.append(
            "public Map<String, Class> getMetaData()\n"
          + "{\n"
          + "    return XCLASS_METADATA;\n"
          + "}\n\n");
    }

    private static void writeSetFields(final Map<String, Class> propertyMap,
                                       final StringBuilder writeTo)
    {
        writeTo.append("public void setFields(final Map<String, Object> map)\n{\n");
        for (final String name : propertyMap.keySet()) {
            writeTo.append("    this.").append(JavaIdentifierEscaper.escape(name, RESERVED_WORDS))
                .append(" = map.get(\"").append(StringEscapeUtils.escapeJava(name))
                    .append("\");\n");
        }
        writeTo.append("}\n\n");
    }

    private static void writeGetFields(final Map<String, Class> propertyMap,
                                       final StringBuilder writeTo)
    {
        writeTo.append(
              "public Map<String, Object> getFields()\n"
            + "{\n"
            + "    final Map<String, Object> out = new HashMap<String, Object>();\n");

        for (final String name : propertyMap.keySet()) {
            writeTo.append("    out.put(\"")
                .append(StringEscapeUtils.escapeJava(name)).append("\", "
                + "this.").append(JavaIdentifierEscaper.escape(name, RESERVED_WORDS))
                .append(");\n");
        }
        writeTo.append(
              "    return out;\n"
            + "}\n\n");
    }

    private static void writeFields(final Map<String, Class> propertyMap,
                                    final StringBuilder writeTo)
    {
        for (final Map.Entry<String, Class> e : propertyMap.entrySet()) {
            writeField(e.getKey(), e.getValue(), writeTo);
        }
    }

    /**
     * Write a field corrisponding to a class property/field.
     * for example:
     * public String myName;
     *
     * @param fieldName the myName part.
     * @param propClass the class of xobject property.
     * @param writeTo the place to write the output to.
     */
    private static void writeField(final String fieldName,
                                   final Class propClass,
                                   final StringBuilder writeTo)
    {
        if (ListProperty.class.isAssignableFrom(propClass)) {
            writeTo.append("@Index\n"
                         + "@Persistent(defaultFetchGroup=\"true\")\n"
                         + "@Element(indexed=\"true\", dependent=\"true\")\n"
                         + "public List<String>");
        } else {
            writeTo.append("@Index\n"
                         + "public ");
            if (propClass == StringProperty.class
                || propClass == LargeStringProperty.class)
            {
                writeTo.append("String");

            } else if (propClass == IntegerProperty.class) {
                writeTo.append("Integer");

            } else if (propClass == DateProperty.class) {
                writeTo.append("Date");

            } else if (propClass == DoubleProperty.class) {
                writeTo.append("Double");

            } else if (propClass == FloatProperty.class) {
                writeTo.append("Float");

            } else if (propClass == LongProperty.class) {
                writeTo.append("Long");

            } else {
                throw new RuntimeException("Encountered a [" + propClass.getName()
                                           + "] property which is not handled.");
            }
        }
        writeTo.append(" ");
        writeTo.append(JavaIdentifierEscaper.escape(fieldName, RESERVED_WORDS));
        writeTo.append(";\n\n");
    }
    
    /** Write the space reference as dot delimniated, used to create the package name. */
    private static void writeReference(final EntityReference ref, final StringBuilder writeTo)
    {
        if (ref.getParent() != null) {
            writeReference(ref.getParent(), writeTo);
            writeTo.append(".");
        }
        writeTo.append(JavaIdentifierEscaper.escape(ref.getName()));
    }
}
