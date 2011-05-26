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
package com.xpn.xwiki.doc;

import java.util.Date;
import java.uitl.Collection;

import org.xwiki.model.reference.EntityReference;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.LargeStringProperty;
import com.xpn.xwiki.objects.StringProperty;
import com.xpn.xwiki.objects.StringListProperty;
import com.xpn.xwiki.objects.DBStringListProperty;
import com.xpn.xwiki.objects.IntegerProperty;
import com.xpn.xwiki.objects.FloatProperty;
import com.xpn.xwiki.objects.DoubleProperty;
import com.xpn.xwiki.objects.LongProperty;
import com.xpn.xwiki.objects.DateProperty;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.NonPersistant;
//import javax.jdo.annotations.*/


import org.xwiki.model.reference.EntityReference;

/**
 * A converter which takes an XWiki class and converts it to a PersistanceCapable
 * Java class so that it can be stored using JDO.
 */
public class XClassConverter
{
    public PersistanceCapable convert(final BaseClass xwikiClass)
    {
    }

    private static void writeClass(final BaseClass xwikiClass, final StringBuilder writeTo)
    {
        final EntityReference docRef = xwikiClass.getDocumentReference();
        final EntityReference spaceRef = docRef.getParent();

        // package
        writeTo.append("package ");
        writeReference(spaceRef, writeTo);
        writeTo.append(";\n");

        writeTo.append("\n");

        // imports
        writeTo.append("import javax.jdo.annotations.IdentityType;\n");
        writeTo.append("import javax.jdo.annotations.Index;\n");
        writeTo.append("import javax.jdo.annotations.PersistenceCapable;\n");
        writeTo.append("import javax.jdo.annotations.PrimaryKey;\n");

        writeTo.append("\n");

        // persistance capaible...
        writeTo.append("@PersistanceCapable\n");

        // class name
        writeTo.append("public class ");
        writeTo.append(escape(docRef.getName()));
        writeTo.append("\n{\n");

        writeFields(xwikiClass, writeTo);

        // closer
        writeTo.append("}\n");
    }

    /** Write all of the field entries for the property. */
    private static void writeFields(final BaseClass xwikiClass, final StringBuilder writeTo)
    {
        final Collection<PropertyClass> properties = xwikiClass.getFieldList();
        for (final PropertyClass propClass : properties) {
            final BaseProperty prop = propClass.newProperty();
            writeField(prop, writeTo);
        }
    }

    private static void writeField(final BaseProperty prop, final StringBuilder writeTo)
    {
        writeTo.append("\n@Index");
        writeTo.append("\npublic ");
        switch (prop.getClass()) {
            case StringProperty.class :
            case LargeStringProperty.class :
            case StringListProperty.class :
                writeTo.append("String");
                break;
            case IntegerProperty.class :
                writeTo.append("int");
                break;
            case DateProperty.class :
                writeTo.append("Date");
                break;
            case DBStringListProperty.class :
                writeTo.append("List<String>");
                break;
            case DoubleProperty.class :
                writeTo.append("double");
                break;
            case FloatProperty.class :
                writeTo.append("float");
                break;
            case LongProperty.class :
                writeTo.append("long");
                break;
            default :
                throw new RuntimeException("Encountered a " + prop.getClass().getName()
                                           + " property which is not handled.");
        };
        writeTo.append(" ");
        writeTo.append(escape(prop.getName()));
        writeTo.append(";\n");
    }
    
    /** Write the space reference as dot delimniated, used to create the package name. */
    private static void writeReference(final EntityReference ref, final StringBuilder writeTo)
    {
        if (ref.getParent() != null) {
            writeReference(ref.getParent(), writeTo);
            writeTo.append(".");
        }
        writeTo.append(escape(ref.getName()));
    }

    /** Escape a string so that it is a valid java class/memeber name. */
    private static String escape(final String toEscape)
    {
        // TODO: Decide on an escaping scheme.
        return toEscape;
    }
}
