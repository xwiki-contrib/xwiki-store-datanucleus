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
package com.xpn.xwiki.store.datanucleus;

import java.util.Date;

import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import org.xwiki.store.objects.PersistableObject;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class PersistableXWikiAttachment extends PersistableObject
{
    private String fileName;

    private int fileSize;

    private Date date;

    private String author;

    private String version;

    private String comment;

    public PersistableXWikiAttachment(final XWikiAttachment attach)
    {
        this.fileName = attach.getFilename();
        this.fileSize = attach.getFilesize();
        this.date = attach.getDate();
        this.author = attach.getAuthor();
        this.version = attach.getVersion();
        this.comment = attach.getComment();
    }

    public XWikiAttachment toXWikiAttachment(final XWikiDocument containingDocument)
    {
        final XWikiAttachment out = new XWikiAttachment(containingDocument, this.fileName);
        out.setFilesize(this.fileSize);
        out.setDate(this.date);
        out.setAuthor(this.author);
        out.setVersion(this.version);
        out.setComment(this.comment);
        return out;
    }
}
