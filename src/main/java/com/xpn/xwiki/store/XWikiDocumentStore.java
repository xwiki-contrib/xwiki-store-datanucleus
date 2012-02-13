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
package com.xpn.xwiki.store;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface XWikiDocumentStore
{
    public void saveXWikiDoc(final XWikiDocument doc, final XWikiContext context)
        throws XWikiException;

    public void saveXWikiDoc(final XWikiDocument doc,
                             final XWikiContext context,
                             final boolean ignored)
        throws XWikiException;

    public XWikiDocument loadXWikiDoc(final XWikiDocument doc, final XWikiContext unused)
        throws XWikiException;

    public boolean exists(final XWikiDocument doc, final XWikiContext unused) throws XWikiException;

    public void deleteXWikiDoc(final XWikiDocument doc, final XWikiContext context)
        throws XWikiException;
}
