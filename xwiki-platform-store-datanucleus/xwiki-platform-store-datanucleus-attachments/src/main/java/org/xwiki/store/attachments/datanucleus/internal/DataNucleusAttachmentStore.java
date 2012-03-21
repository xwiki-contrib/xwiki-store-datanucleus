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
package org.xwiki.store.attachments.datanucleus.internal;

import java.util.List;

import com.xpn.xwiki.doc.XWikiAttachment;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.jdo.PersistenceManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.store.attachments.newstore.internal.AttachmentStore;
import org.xwiki.store.TransactionRunnable;

/**
 * A means of storing the metadata of an attachment.
 * This class is designed to seperate concerns of content, metadata, and archive.
 *
 * @version $Id: 24f7cb845408a32680ef20dbd5817abdd3f41642 $
 * @since TODO
 */
@Component
@Named("datanucleus")
@Singleton
public class DataNucleusAttachmentStore implements AttachmentStore<PersistenceManager>
{
    @Override
    public TransactionRunnable<PersistenceManager> getAttachmentSaveRunnable(
        final List<XWikiAttachment> toSave)
    {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public TransactionRunnable<PersistenceManager> getAttachmentLoadRunnable(
        final List<AttachmentReference> refs,
        final List<XWikiAttachment> output)
    {
        throw new RuntimeException("Not implemented.");
    }

    /*
     * This is a no-op since the act of removing the attachment from the document
     * and saving the document will make it go away.
     */
    @Override
    public TransactionRunnable<PersistenceManager> getAttachmentDeleteRunnable(
        final List<XWikiAttachment> toDelete)
    {
        return new TransactionRunnable<PersistenceManager>();
    }
}
