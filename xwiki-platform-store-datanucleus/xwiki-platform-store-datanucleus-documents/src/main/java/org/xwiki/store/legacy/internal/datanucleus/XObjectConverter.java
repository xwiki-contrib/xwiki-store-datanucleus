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

import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.BaseStringProperty;
import com.xpn.xwiki.objects.LargeStringProperty;
import com.xpn.xwiki.objects.StringProperty;
import com.xpn.xwiki.objects.StringListProperty;
import com.xpn.xwiki.objects.DBStringListProperty;
import com.xpn.xwiki.objects.IntegerProperty;
import com.xpn.xwiki.objects.FloatProperty;
import com.xpn.xwiki.objects.DoubleProperty;
import com.xpn.xwiki.objects.LongProperty;
import com.xpn.xwiki.objects.NumberProperty;
import com.xpn.xwiki.objects.ListProperty;
import com.xpn.xwiki.objects.DateProperty;
import com.xpn.xwiki.objects.BaseObject;
import groovy.lang.GroovyClassLoader;
import javax.jdo.JDOEnhancer;
import javax.jdo.JDOHelper;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.tools.GroovyClass;
import org.codehaus.groovy.control.CompilationUnit;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.store.datanucleus.internal.JavaIdentifierEscaper;
import org.xwiki.store.datanucleus.internal.JavaClassNameDocumentReferenceSerializer;
import org.xwiki.store.objects.PersistableObject;
import org.xwiki.store.UnexpectedException;

/**
 */
final class XObjectConverter
{
    public static AbstractXObject convertFromXObject(final BaseObject xwikiObject,
                                                     final Class<AbstractXObject> objectClass)
    {
        // Sanity check
        final String className = JavaClassNameDocumentReferenceSerializer.serializeRef(
            xwikiObject.getXClassReference(), null);
        if (!objectClass.getName().equals(className)) {
            throw new UnexpectedException("The provided class " + objectClass.getName()
                                          + " does not match the class name of the XWiki object "
                                          + className);
        }

        final AbstractXObject out;
        try {
            out = objectClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not instanciate the class " + objectClass.getName());
        }

        Map<String, Object> propertyValues = new HashMap<String, Object>();
        for (final String name : xwikiObject.getPropertyList()) {
            propertyValues.put(name, ((BaseProperty) xwikiObject.getField(name)).getValue());
        }
        out._setFields(propertyValues);

        return out;
    }

    public static BaseObject convertToXObject(final AbstractXObject persistable)
    {
        final BaseObject out = new BaseObject();
        final DocumentReference classRef =
            JavaClassNameDocumentReferenceSerializer.resolveRef(persistable.getClass().getName());
        out.setXClassReference(classRef);
        final Map<String, Class> classes = persistable._getMetaData();
        for (Map.Entry<String, Object> e : persistable._getFields().entrySet()) {
            if (e.getValue() != null) {
                setValue(e.getKey(), e.getValue(), classes.get(e.getKey()), out);
            }
        }

        return out;
    }

    private static void setValue(final String name,
                                 final Object value,
                                 final Class valueClass,
                                 final BaseObject xobj)
    {
        if (valueClass == StringProperty.class) {
            xobj.setStringValue(name, (String) value);

        } else if (valueClass == LargeStringProperty.class) {
            xobj.setLargeStringValue(name, (String) value);

        } else if (valueClass == IntegerProperty.class) {
            xobj.setIntValue(name, (Integer) value);

        } else if (valueClass == LongProperty.class) {
            xobj.setLongValue(name, (Long) value);

        } else if (valueClass == FloatProperty.class) {
            xobj.setFloatValue(name, (Float) value);

        } else if (valueClass == DoubleProperty.class) {
            xobj.setDoubleValue(name, (Double) value);

        } else if (valueClass == DateProperty.class) {
            xobj.setDateValue(name, (Date) value);

        } else if (valueClass == StringListProperty.class) {
            xobj.setStringListValue(name, (List) value);

        } else if (valueClass == DBStringListProperty.class) {
            xobj.setDBStringListValue(name, (List) value);

        } else {
            throw new UnexpectedException("Encountered an unhandled property of type " + valueClass);
        }
    }
}
