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

import java.util.Date;

import com.xpn.xwiki.doc.XWikiLink;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import org.xwiki.store.objects.PersistableObject;

@PersistenceCapable
public class PersistableXWikiLink extends PersistableObject
{
    /** The ID of the document which this link comes from. */
    @Index
    private long docId;

    /** The name of the document which this link comes from. */
    private String fullName;

    /** The name of the document which this link points to. */
    @Index
    private String link;

    public PersistableXWikiLink(final XWikiLink link)
    {
        this.docId = link.getDocId();
        this.fullName = link.getFullName();
        this.link = link.getLink();
        this.setPersistableObjectId(this.docId + ":" + this.link);
    }

    public XWikiLink toXWikiLink()
    {
        return new XWikiLink(this.docId, this.link, this.fullName);
    }

    public String getFullName()
    {
        return this.fullName;
    }
}
