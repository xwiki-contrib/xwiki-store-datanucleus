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
 * A means of storing the content of an attachment in a datanucleus based store.
 *
 * @version $Id$
 * @since TODO
 */
@Component
@Named("datanucleus")
@Singleton
public class DataNucleusAttachmentContentStore implements AttachmentContentStore<PersistenceManager>
{
    @Override
    public TransactionRunnable<T> getAttachmentContentSaveRunnable(final XWikiAttachmentContent content)
    {
        
    }

    @Override
    public TransactionRunnable<T> getAttachmentContentLoadRunnable(final XWikiAttachment attachment)
    {
    }

    @Override
    public TransactionRunnable<T> getAttachmentContentDeleteRunnable(final XWikiAttachment attachment)
    {
    }

    private static class DataNucleusBlobStoreRunnable extends TransactionRunnable<PersistenceManager>
    {
        private final String key;
        private final InputStream copyFrom;

        public DataNucleusBlob(final String key, final InputStream copyFrom)
        {
            this.key = key;
            this.copyFrom = copyFrom;
        }

        @Override
        protected void onRun()
        {
            
        }
    }

    @PersistenceCapable(table = "BlobChunk")
    private static class BlobChunk
    {
        @PrimaryKey
        private String id;
        private byte[] content;

        public BlobChunk(final String id, final byte[] content)
        {
            this.id = id;
            this.content = content;
        }

        public byte[] getContent()
        {
            return this.content;
        }
    }
}
