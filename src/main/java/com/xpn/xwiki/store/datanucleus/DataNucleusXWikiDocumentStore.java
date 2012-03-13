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

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.XWikiDocumentStore;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.store.attachments.newstore.internal.AttachmentContentStore;
import org.xwiki.store.datanucleus.internal.DataNucleusPersistableObjectStore;
import org.xwiki.store.objects.PersistableObject;
import org.xwiki.store.StartableTransactionRunnable;
import org.xwiki.store.TransactionException;
import org.xwiki.store.TransactionProvider;
import org.xwiki.store.TransactionRunnable;
import org.xwiki.store.UnexpectedException;

@Component
@Named("datanucleus")
public class DataNucleusXWikiDocumentStore implements XWikiDocumentStore
{
    private final DataNucleusPersistableObjectStore objStore =
        new DataNucleusPersistableObjectStore();

    @Inject
    @Named("datanucleus")
    private TransactionProvider<PersistenceManager> provider;

    @Inject
    @Named("file")
    private AttachmentContentStore attachContentStore;

    public void saveXWikiDoc(final XWikiDocument doc, final XWikiContext context)
        throws XWikiException
    {
        final String key = PersistableXWikiDocument.keyGen(doc.getDocumentReference());

        final PersistableXWikiDocument pxd = new PersistableXWikiDocument();

        final TransactionRunnable<PersistenceManager> storeRunnable =
            this.objStore.getStoreTransactionRunnable(key, pxd);

        // Conversion from XWikiDocument to PersistableXWikiDocument must be done
        // after the thread context ClassLoader has been switched.
        (new TransactionRunnable<PersistenceManager>() {
            protected void onPreRun()
            {
                pxd.fromXWikiDocument(doc);
            }
        }).runIn(storeRunnable);

        final StartableTransactionRunnable<PersistenceManager> transaction = this.provider.get();
        storeRunnable.runIn(transaction);

        for (final XWikiAttachment attach : doc.getAttachmentList()) {
            if (attach.isContentDirty()) {
                this.attachContentStore
                    .getAttachmentContentSaveRunnable(attach.getAttachment_content())
                        .runIn(transaction);
            }
        }

        try {
            transaction.start();
        } catch (TransactionException e) {
            throw new UnexpectedException("Failed to store XWikiDocument [" + doc + "]", e);
        }
    }

    public void saveXWikiDoc(final XWikiDocument doc,
                             final XWikiContext context,
                             final boolean ignored)
        throws XWikiException
    {
        this.saveXWikiDoc(doc, context);
    }

    public XWikiDocument loadXWikiDoc(final XWikiDocument doc, final XWikiContext unused)
        throws XWikiException
    {
        final String key = PersistableXWikiDocument.keyGen(doc.getDocumentReference());
        final List<PersistableObject> out = new ArrayList<PersistableObject>(1);
        final StartableTransactionRunnable<PersistenceManager> transaction = this.provider.get();

        this.objStore.getLoadTransactionRunnable(new ArrayList<String>(1) { { add(key); } },
                                                 PersistableXWikiDocument.class.getName(),
                                                 out).runIn(transaction);

        try {
            transaction.start();
        } catch (TransactionException e) {
            throw new RuntimeException("Failed to load document " + Arrays.asList(key), e);
        }

        return (out.size() == 0) ? doc : ((PersistableXWikiDocument) out.get(0)).toXWikiDocument(doc);
    }

    public boolean exists(final XWikiDocument doc, final XWikiContext unused) throws XWikiException
    {
        return !this.loadXWikiDoc(doc, null).isNew();
    }

    public void deleteXWikiDoc(final XWikiDocument doc, final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }
}
