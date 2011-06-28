package com.xpn.xwiki.store.datanucleus;

import java.util.Arrays;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import org.xwiki.model.reference.DocumentReference;
import com.xpn.xwiki.store.XWikiAttachmentStoreInterface;
import org.xwiki.store.datanucleus.internal.DataNucleusPersistableObjectStore;
import org.xwiki.store.objects.PersistableClassLoader;
import org.xwiki.store.EntityProvider;


public class DataNucleusXWikiDocumentStore
{
    final PersistableClassLoader loader;

    final DataNucleusPersistableObjectStore objStore;

    private final EntityProvider<XWikiDocument, DocumentReference> provider;

    public DataNucleusXWikiDocumentStore(final PersistableClassLoader loader,
                                         final DataNucleusPersistableObjectStore objStore)
    {
        this.loader = loader;
        this.objStore = objStore;
        this.provider = new DataNucleusXWikiDocumentProvider(loader, objStore);
    }

    public void saveXWikiDoc(final XWikiDocument doc, final XWikiContext context) throws XWikiException
    {
        final XWikiAttachmentStoreInterface attachmentStore = context.getWiki().getAttachmentStore();

        final String[] key =
            PersistableXWikiDocument.keyGen(doc.getDocumentReference(), doc.getLanguage());
        System.err.println(">>>>>STORING! " + Arrays.asList(key));

        final PersistableXWikiDocument pxd = new PersistableXWikiDocument(doc, this.provider, this.loader);
        this.objStore.put(pxd);

        // TODO: Transaction safety.
        for (final XWikiAttachment attach : doc.getAttachmentList()) {
            if (attach.isContentDirty()) {
                context.getWiki().getAttachmentStore().saveAttachmentContent(attach, false, context, false);
            }
        }
    }

    public void saveXWikiDoc(final XWikiDocument doc, final XWikiContext unused, final boolean ignored)
        throws XWikiException
    {
        this.saveXWikiDoc(doc, null);
    }

    public XWikiDocument loadXWikiDoc(final XWikiDocument doc, final XWikiContext unused)
        throws XWikiException
    {
        final String[] key = PersistableXWikiDocument.keyGen(doc.getDocumentReference(), doc.getLanguage());
        System.err.println(">>>>>LOADING! " + Arrays.asList(key));

        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.loader.asNativeLoader());
            final PersistableXWikiDocument pxd =
                (PersistableXWikiDocument) this.objStore.get(key, PersistableXWikiDocument.class.getName());
            return (pxd == null) ? doc : pxd.toXWikiDocument();
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
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
