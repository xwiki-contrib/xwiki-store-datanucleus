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
package com.xpn.xwiki.store.internal;

import java.util.ArrayList;
import java.util.List;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import javax.inject.Inject;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.store.EntityProvider;

@Component("legacy-xwikidocument")
public class LegacyXWikiDocumentProvider implements EntityProvider<XWikiDocument, DocumentReference>
{
    @Inject
    private Execution execution;

    public XWikiDocument get(final DocumentReference reference)
    {
        final XWikiContext context = (XWikiContext) execution.getContext().getProperty("xwiki-context");
        try {
            final XWikiDocument doc = context.getWiki().getDocument(reference, context);
            if (!doc.isNew()) {
                return doc;
            }
        } catch (XWikiException e) {
            throw new RuntimeException("Failed to get the document", e);
        }
        return null;
    }

    public List<XWikiDocument> get(final List<DocumentReference> references)
    {
        final List<XWikiDocument> out = new ArrayList<XWikiDocument>(references.size());
        for (final DocumentReference reference : references) {
            out.add(this.get(reference));
        }
        return out;
    }
}
