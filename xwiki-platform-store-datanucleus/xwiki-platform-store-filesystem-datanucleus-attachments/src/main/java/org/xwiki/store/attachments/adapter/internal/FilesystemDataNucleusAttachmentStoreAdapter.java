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
package org.xwiki.store.attachments.adapter.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.jdo.PersistenceManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.store.attachments.newstore.internal.AttachmentContentStore;
import org.xwiki.store.attachments.newstore.internal.AttachmentStore;
import org.xwiki.store.StartableTransactionRunnable;
import org.xwiki.store.TransactionProvider;

/**
 * AttachmentVersioningStore implementation.
 * Stores content in the filesystem and metadata in a DataNucleus store.
 *
 * @version $Id: 83f077188fcdce981f344151e51822a95552f157 $
 * @since 3.3M2
 */
@Component
@Named("file")
@Singleton
public class FilesystemDataNucleusAttachmentStoreAdapter
    extends AbstractAttachmentStoreAdapter<PersistenceManager>
{
    /** The filesystem based attachment content store. */
    @Named("file")
    @Inject
    private AttachmentContentStore contentStore;

    /** The DataNucleus based attachment metadata store. */
    @Named("datanucleus")
    @Inject
    private AttachmentStore<PersistenceManager> metaStore;

    /** A means of getting a transaction to run the attachment save operation in. */
    @Named("datanucleus")
    @Inject
    private TransactionProvider<PersistenceManager> provider;

    /**
     * Testing Constructor.
     *
     * @param contentStore the filesystem based store for the content.
     * @param metaStore the DataNucleus based store for the metadata.
     * @param provider the means of getting a transaction to run in.
     */
    public FilesystemDataNucleusAttachmentStoreAdapter(
        final AttachmentContentStore contentStore,
        final AttachmentStore<PersistenceManager> metaStore,
        final TransactionProvider<PersistenceManager> provider)
    {
        super(PersistenceManager.class);
        this.contentStore = contentStore;
        this.metaStore = metaStore;
        this.provider = provider;
    }

    /**
     * Component manager constructor.
     */
    public FilesystemDataNucleusAttachmentStoreAdapter()
    {
        super(PersistenceManager.class);
    }

    @Override
    protected AttachmentContentStore getContentStore()
    {
        return this.contentStore;
    }

    @Override
    protected AttachmentStore<PersistenceManager> getMetaStore()
    {
        return this.metaStore;
    }

    @Override
    protected StartableTransactionRunnable<PersistenceManager> getTransaction()
    {
        return this.provider.get();
    }
}
