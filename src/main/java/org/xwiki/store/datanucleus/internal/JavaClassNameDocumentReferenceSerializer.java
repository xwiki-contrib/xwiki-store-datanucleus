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
package org.xwiki.store.datanucleus.internal;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.component.annotation.Component;

@Component("java-class-name")
public final class JavaClassNameDocumentReferenceSerializer
    implements EntityReferenceSerializer<String>, DocumentReferenceResolver<String>
{
    public String serialize(final EntityReference reference, final Object... parameters)
    {
        return serializeRef(reference, null);
    }

    public DocumentReference resolve(final String representation, final Object... parameters)
    {
        return resolveRef(representation, null);
    }

    public static String serializeRef(final EntityReference reference, final Object... parameters)
    {
        EntityReference ref = reference;
        final StringBuilder sb = new StringBuilder();
        while (ref != null) {
            sb.insert(0, JavaIdentifierEscaper.escape(ref.getName()));
            sb.insert(0, ".");
            ref = ref.getParent();
        }
        // trim the leading '.'
        return sb.substring(1);
    }

    public static DocumentReference resolveRef(final String representation, final Object... parameters)
    {
        final String[] splitClassName = representation.split(".");
        final List<String> spaceNames = new ArrayList<String>(splitClassName.length - 2);
        final String wikiName = JavaIdentifierEscaper.unescape(splitClassName[0]);
        final String pageName = JavaIdentifierEscaper.unescape(splitClassName[splitClassName.length - 1]);
        for (int i = 1; i < splitClassName.length - 1; i++) {
            spaceNames.add(JavaIdentifierEscaper.unescape(splitClassName[i]));
        }
        return new DocumentReference(wikiName, spaceNames, pageName);
    }
}
