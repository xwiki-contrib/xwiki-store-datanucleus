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
 *
 */

package com.xpn.xwiki.store.datanucleus;

import java.util.Arrays;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.FilesystemAttachmentStore;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.store.datanucleus.internal.DataNucleusPersistableObjectStore;
import org.xwiki.store.datanucleus.internal.XWikiDataNucleusTransactionProvider;
import org.xwiki.store.datanucleus.XWikiDataNucleusTransaction;
import org.xwiki.store.EntityProvider;
import org.xwiki.store.objects.PersistableObject;
import org.xwiki.store.Pointer;
import org.xwiki.store.StartableTransactionRunnable;
import org.xwiki.store.TransactionException;
import org.xwiki.store.TransactionRunnable;


public class DataNucleusXWikiDocumentStore
{
    private final DataNucleusPersistableObjectStore objStore;

    private final XWikiDataNucleusTransactionProvider provider;

    public DataNucleusXWikiDocumentStore(final XWikiDataNucleusTransactionProvider provider)
    {
        this.objStore = new DataNucleusPersistableObjectStore();
        this.provider = provider;
    }

    public void saveXWikiDoc(final XWikiDocument doc, final XWikiContext context) throws XWikiException
    {
        final String[] key = PersistableXWikiDocument.keyGen(doc.getDocumentReference(), doc.getLanguage());
        System.err.println(">>>>>STORING! " + Arrays.asList(key));

        final PersistableXWikiDocument pxd = new PersistableXWikiDocument();
        final TransactionRunnable<XWikiDataNucleusTransaction> storeRunnable =
            this.objStore.getStoreTransactionRunnable(pxd);

        // Conversion from XWikiDocument to PersistableXWikiDocument must be done
        // after the thread context ClassLoader has been switched.
        (new TransactionRunnable<XWikiDataNucleusTransaction>() {
            protected void onPreRun()
            {
                final EntityProvider<XWikiDocument, DocumentReference> provider =
                    new DataNucleusXWikiDocumentProvider(this.getContext().getPersistenceManager());
                pxd.fromXWikiDocument(doc, provider);
            }
        }).runIn(storeRunnable);

        final StartableTransactionRunnable<XWikiDataNucleusTransaction> transaction = this.provider.get();
        storeRunnable.runIn(transaction);

        try {
            transaction.start();
        } catch (TransactionException e) {
            throw new RuntimeException("Failed to store XWikiDocument " + doc, e);
        }

        // TODO: Transaction Safety!
        for (final XWikiAttachment attach : doc.getAttachmentList()) {
            if (attach.isContentDirty()) {
                final FilesystemAttachmentStore fas =
                    ((FilesystemAttachmentStore) context.getWiki().getAttachmentStore());
                fas.saveAttachmentContent(attach, false, context, false);
            }
        }
    }

    public void saveXWikiDoc(final XWikiDocument doc, final XWikiContext context, final boolean ignored)
        throws XWikiException
    {
        this.saveXWikiDoc(doc, context);
    }

    public XWikiDocument loadXWikiDoc(final XWikiDocument doc, final XWikiContext unused)
        throws XWikiException
    {
        final String[] key = PersistableXWikiDocument.keyGen(doc.getDocumentReference(), doc.getLanguage());
        System.err.println(">>>>>LOADING! " + Arrays.asList(key));

        final Pointer<PersistableObject> docPtr = new Pointer<PersistableObject>();
        final TransactionRunnable<XWikiDataNucleusTransaction> loadRunnable =
            this.objStore.getLoadTransactionRunnable(key, PersistableXWikiDocument.class.getName(), docPtr);
        final StartableTransactionRunnable<XWikiDataNucleusTransaction> transaction = this.provider.get();
        loadRunnable.runIn(transaction);

        try {
            transaction.start();
        } catch (TransactionException e) {
            throw new RuntimeException("Failed to load document " + Arrays.asList(key), e);
        }

        return (docPtr.target == null) ?
            doc : ((PersistableXWikiDocument) docPtr.target).toXWikiDocument();
    }

    public boolean exists(final XWikiDocument doc, final XWikiContext unused) throws XWikiException
    {
        return !this.loadXWikiDoc(doc, null).isNew();
    }

    public void deleteXWikiDoc(final XWikiDocument doc, final XWikiContext context) throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }
}
