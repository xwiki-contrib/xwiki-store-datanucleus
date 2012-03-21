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

import com.xpn.xwiki.XWikiContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.jdo.PersistenceManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.store.attachments.newstore.internal.DeletedAttachmentContentStore;
import org.xwiki.store.attachments.newstore.internal.DeletedAttachmentStore;
import org.xwiki.store.StartableTransactionRunnable;
import org.xwiki.store.TransactionProvider;

/**
 * Realization of {@link AttachmentRecycleBinStore} for filesystem storage.
 *
 * @version $Id: b60aa6a542203b6c857d48a62ed30a3d565f63a1 $
 * @since TODO
 */
@Component
@Named("file")
@Singleton
public class FilesystemDataNucleusAttachmentRecycleBinStoreAdapter
    extends AbstractAttachmentRecycleBinStoreAdapter<PersistenceManager>
{
    /** The means of getting DataNucleus transactions. */
    @Named("datanucleus")
    @Inject
    private TransactionProvider<PersistenceManager> transactionProvider;

    /** The metadata store which puts empty DeletedAttachment entries in the database. */
    @Named("datanucleus")
    @Inject
    private DeletedAttachmentStore<PersistenceManager> metaStore;

    /** The content store, this is filesystem based. */
    @Named("file")
    @Inject
    private DeletedAttachmentContentStore contentStore;

    /** Generic String reference resolver used for the name of the deleter. */
    @Inject
    private EntityReferenceResolver<String> resolver;

    /** A means of getting the XWikiContext to get the current wiki. */
    @Inject
    private Execution execution;

    @Override
    protected StartableTransactionRunnable<PersistenceManager> getTransaction()
    {
        return this.transactionProvider.get();
    }

    @Override
    protected DeletedAttachmentStore<PersistenceManager> getMetaStore()
    {
        return this.metaStore;
    }

    @Override
    protected DeletedAttachmentContentStore getContentStore()
    {
        return this.contentStore;
    }

    @Override
    protected EntityReferenceResolver<String> getDeleterNameResolver()
    {
        return this.resolver;
    }

    @Override
    protected AttachmentReference getAttachmentReferenceForId(final long id)
    {
        throw new RuntimeException("Not Implemented.");
    }

    /**
     * Get the current wiki reference.
     * This is required in order to get an attachment reference for an attachment ID.
     *
     * @return the current wiki reference.
     */
    private WikiReference getWikiRef()
    {
        final XWikiContext xc =
            (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
        return new WikiReference(xc.getDatabase());
    }
}
