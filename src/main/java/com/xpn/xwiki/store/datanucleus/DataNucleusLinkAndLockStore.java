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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.JDOObjectNotFoundException;
import javax.inject.Inject;
import javax.inject.Named;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.doc.XWikiLink;
import com.xpn.xwiki.doc.XWikiLock;
import org.xwiki.rendering.syntax.Syntax;
import com.xpn.xwiki.store.LinkAndLockStore;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.store.objects.PersistableObject;
import org.xwiki.store.TransactionException;
import org.xwiki.store.TransactionRunnable;
import org.xwiki.store.StartableTransactionRunnable;
import org.xwiki.store.datanucleus.DataNucleusTransaction;
import org.xwiki.store.TransactionProvider;
import org.xwiki.store.UnexpectedException;

@Component("datanucleus")
public class DataNucleusLinkAndLockStore implements LinkAndLockStore
{
    /**
     * Used to resolve a string into a proper Document Reference using the current document's
     * reference to fill the blanks, except for the page name for which the default page name
     * is used instead and for the wiki name for which the current wiki is used instead of the
     * current document reference's wiki.
     */
    @Inject
    @Named("currentmixed")
    private DocumentReferenceResolver<String> currentMixedDocumentReferenceResolver;

    /** Used to convert a proper Document Reference to a string but without the wiki name. */
    @Inject
    @Named("local")
    private EntityReferenceSerializer<String> localEntityReferenceSerializer;

    /** Needed to get the XWikiContext since we don't require a context to be passed to us. */
    @Inject
    private Execution execution;

    /** A TransactionProvider which is used to load and store the locks and links. */
    @Inject
    @Named("datanucleus")
    private TransactionProvider<PersistenceManager> provider;

    /* -------------------------- Locks -------------------------- */

    public XWikiLock loadLock(final long docId)
    {
        final PersistableXWikiLock[] out = new PersistableXWikiLock[1];
        final StartableTransactionRunnable<PersistenceManager> transaction = this.provider.get();
        (new TransactionRunnable<PersistenceManager>() {
            protected void onRun()
            {
                final PersistenceManager pm = this.getContext();
                try {
                    out[0] = pm.getObjectById(PersistableXWikiLock.class, "" + docId);
                    pm.makeTransient(out[0]);
                } catch (JDOObjectNotFoundException e) {
                    // Not found, out[0] is already null so we leave it.
                }
            }
        }).runIn(transaction);

        try {
            transaction.start();
        } catch (TransactionException e) {
            throw new UnexpectedException("Failed to get lock for document", e);
        }

        return out[0] == null ? null : out[0].toXWikiLock();
    }

    public void saveLock(final XWikiLock lock)
    {
        final StartableTransactionRunnable<PersistenceManager> transaction = this.provider.get();
        (new TransactionRunnable<PersistenceManager>() {
            protected void onRun()
            {
                this.getContext().makePersistent(new PersistableXWikiLock(lock));
            }
        }).runIn(transaction);

        try {
            transaction.start();
        } catch (TransactionException e) {
            throw new UnexpectedException("Failed to store lock for document", e);
        }
    }

    public void deleteLock(final XWikiLock lock)
    {
        final StartableTransactionRunnable<PersistenceManager> transaction = this.provider.get();
        (new TransactionRunnable<PersistenceManager>() {
            protected void onRun()
            {
                final PersistenceManager pm = this.getContext();
                try {
                    final PersistableXWikiLock plock =
                        pm.getObjectById(PersistableXWikiLock.class, "" + lock.getDocId());
                    pm.deletePersistent(plock);
                } catch (JDOObjectNotFoundException e) {
                    // Can't delete what isn't there.
                }
            }
        }).runIn(transaction);

        try {
            transaction.start();
        } catch (TransactionException e) {
            throw new UnexpectedException("Failed to delete lock for document", e);
        }
    }

    /* -------------------------- Links -------------------------- */

    public List<XWikiLink> loadLinks(final long docId)
    {
        final Collection<PersistableXWikiLink> links = new ArrayList<PersistableXWikiLink>();
        final StartableTransactionRunnable<PersistenceManager> transaction = this.provider.get();
        this.getLoadPersistableLinksRunnable(docId, links).runIn(transaction);

        try {
            transaction.start();
        } catch (TransactionException e) {
            throw new UnexpectedException("Failed to get forward-links", e);
        }

        final List<XWikiLink> out = new ArrayList<XWikiLink>(links.size());
        for (final PersistableXWikiLink link : links) {
            out.add(link.toXWikiLink());
        }

        return out;
    }

    public List<DocumentReference> loadBacklinks(final DocumentReference documentReference)
    {
        final String docName = this.localEntityReferenceSerializer.serialize(documentReference);
        final Collection<PersistableXWikiLink> links = new ArrayList<PersistableXWikiLink>();
        final StartableTransactionRunnable<PersistenceManager> transaction = this.provider.get();
        (new TransactionRunnable<PersistenceManager>() {
            protected void onRun()
            {
                final Query query = this.getContext().newQuery(PersistableXWikiLink.class);
                query.setFilter("link == :link");
                links.addAll((Collection<PersistableXWikiLink>) query.execute(docName));
            }
        }).runIn(transaction);

        try {
            transaction.start();
        } catch (TransactionException e) {
            throw new UnexpectedException("Failed to get backlinks", e);
        }

        final List<DocumentReference> out = new ArrayList<DocumentReference>(links.size());
        for (final PersistableXWikiLink link : links) {
            out.add(this.currentMixedDocumentReferenceResolver.resolve(link.fullName));
        }

        return out;
    }

    /**
     * @deprecated since 2.2M2 use {@link #loadBacklinks(DocumentReference)}
     */
    @Deprecated
    public List<String> loadBacklinks(final String fullName)
    {
        final List<String> backlinkNames = new ArrayList<String>();
        final List<DocumentReference> backlinkReferences =
            this.loadBacklinks(this.currentMixedDocumentReferenceResolver.resolve(fullName));
        for (final DocumentReference backlinkReference : backlinkReferences) {
            backlinkNames.add(this.localEntityReferenceSerializer.serialize(backlinkReference));
        }
        return backlinkNames;
    }

    public void saveLinks(final XWikiDocument doc)
    {
        if (doc.getSyntax().equals(Syntax.XWIKI_1_0)) {
            // TODO: Support syntax 1.0...  Wait NO, TODO: STOP supporting syntax 1.0!
            return;
        }

        final XWikiContext context =
            (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
        final Collection<XWikiLink> links;
        try {
            links = doc.getUniqueWikiLinkedPages(context);
        } catch (XWikiException e) {
            throw new UnexpectedException("Failed to get links from document", e);
        }
        final List<PersistableXWikiLink> toStore = new ArrayList<PersistableXWikiLink>(links.size());
        for (final XWikiLink link : links) {
            toStore.add(new PersistableXWikiLink(link));
        }

        final StartableTransactionRunnable<PersistenceManager> transaction = this.provider.get();
        (new TransactionRunnable<PersistenceManager>() {
            protected void onRun()
            {
                this.getContext().makePersistentAll(toStore);
            }
        }).runIn(transaction);

        try {
            transaction.start();
        } catch (TransactionException e) {
            throw new UnexpectedException("Failed to store backlinks", e);
        }
    }

    public void deleteLinks(final long docId)
    {
        final Collection<PersistableXWikiLink> links = new ArrayList<PersistableXWikiLink>();
        final StartableTransactionRunnable<PersistenceManager> transaction = this.provider.get();

        final TransactionRunnable<PersistenceManager> getLinksRunnable =
            this.getLoadPersistableLinksRunnable(docId, links);

        final TransactionRunnable<PersistenceManager> deleteLinksRunnable =
            new TransactionRunnable<PersistenceManager>() {
                protected void onRun()
                {
                    this.getContext().deletePersistentAll(links);
                }
            };

        deleteLinksRunnable.runIn(this.getLoadPersistableLinksRunnable(docId, links));
        getLinksRunnable.runIn(transaction);

        try {
            transaction.start();
        } catch (TransactionException e) {
            throw new UnexpectedException("Failed to delete backlinks", e);
        }
    }

    private TransactionRunnable<PersistenceManager> getLoadPersistableLinksRunnable(
        final long docId,
        final Collection<PersistableXWikiLink> links)
    {
        return new TransactionRunnable<PersistenceManager>() {
            protected void onRun()
            {
                final Query query = this.getContext().newQuery(PersistableXWikiLink.class);
                query.setFilter("docId == :docId");
                links.addAll((Collection<PersistableXWikiLink>) query.execute(docId));
            }
        };
    }
}
