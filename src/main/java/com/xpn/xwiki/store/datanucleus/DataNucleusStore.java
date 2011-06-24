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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.doc.XWikiLink;
import com.xpn.xwiki.doc.XWikiLock;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.store.XWikiStoreInterface;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.QueryManager;
import org.apache.cassandra.thrift.CassandraDaemon;

import org.xwiki.store.datanucleus.internal.DataNucleusPersistableObjectStore;
import org.xwiki.store.datanucleus.internal.DataNucleusClassLoader;
import org.xwiki.store.objects.PersistableClassLoader;
import org.xwiki.store.EntityProvider;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.Query;

import org.apache.commons.io.IOUtils;


@Component("datanucleus")
public class DataNucleusStore implements XWikiStoreInterface
{
    private final PersistenceManagerFactory factory;

    private final DataNucleusPersistableObjectStore objStore;

    private final EntityProvider<XWikiDocument, DocumentReference> provider;

    private final PersistableClassLoader loader;

    public DataNucleusStore()
    {
        /*System.setProperty("log4j.configuration", "log4j.properties");
        System.setProperty("cassandra.config", "cassandra.yaml");
        System.setProperty("cassandra-foreground", "1");
        final CassandraDaemon daemon = new CassandraDaemon();
        try {
            daemon.init(null);
        } catch (IOException e) {
            throw new RuntimeException("failed to start cassandra", e);
        }*/
        /*new Thread(new Runnable()
        {
            public void run()
            {
                System.out.println("Starting Cassandra...");
                daemon.start();
                System.out.println("Started Cassandra...");
            }
        });*/
        //daemon.start();
        //try{Thread.sleep(10000);}catch(Exception e){}

        this.factory = JDOHelper.getPersistenceManagerFactory("Test");
        this.provider = new DataNucleusXWikiDocumentProvider(this.factory);
        this.objStore = new DataNucleusPersistableObjectStore(this.factory);
        this.loader = new DataNucleusClassLoader(this.factory, this.getClass().getClassLoader());
    }

    public void cleanUp(final XWikiContext context)
    {
        // This is a hook for when the system shuts down, nothing is required to be done.
    }

    /* ------------------ Load & Store ------------------ */

    public void saveXWikiDoc(final XWikiDocument doc, final XWikiContext unused) throws XWikiException
    {
        final String[] key =
            PersistableXWikiDocument.keyGen(doc.getDocumentReference(), doc.getLanguage());
        System.err.println(">>>>>STORING! " + Arrays.asList(key));

        final PersistableXWikiDocument pxd = new PersistableXWikiDocument(doc, this.provider, this.loader);
        this.objStore.put(pxd);

/*        
        final PersistenceManager manager = this.factory.getPersistenceManager();
        final Transaction txn = manager.currentTransaction();
        txn.begin();
        manager.makePersistent(pxd);
        //manager.putUserObject(key, pxd);
        txn.commit();
        manager.close();
*/
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

        //final PersistenceManager manager = this.factory.getPersistenceManager();
        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.loader.asNativeLoader());
            //final PersistableXWikiDocument pxd =
              //  (PersistableXWikiDocument) manager.getObjectById(PersistableXWikiDocument.class, key);
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

    public List<String> getTranslationList(final XWikiDocument doc, final XWikiContext unused)
        throws XWikiException
    {
        final PersistenceManager manager = this.factory.getPersistenceManager();
        final Query query = manager.newQuery(PersistableXWikiDocument.class);
        query.setFilter("wiki == :wiki && fullName == :name");
        final Collection<PersistableXWikiDocument> translations =
            (Collection<PersistableXWikiDocument>) query.execute(doc.getDatabase(), doc.getFullName());
        final List<String> out = new ArrayList<String>(translations.size());
        for (final PersistableXWikiDocument translation : translations) {
            if (translation.language != null && !translation.language.equals("")) {
                out.add(translation.language);
            }
        }
        return out;
    }

    public void deleteXWikiDoc(final XWikiDocument doc, final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<String> getClassList(final XWikiContext context) throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    /*------------------- Links & Locks -------------------*/

    public XWikiLock loadLock(final long docId, final XWikiContext context, final boolean bTransaction)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public void saveLock(final XWikiLock lock, final XWikiContext context, final boolean bTransaction)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public void deleteLock(final XWikiLock lock, final XWikiContext context, final boolean bTransaction)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiLink> loadLinks(final long docId,
                                     final XWikiContext context,
                                     final boolean bTransaction)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<DocumentReference> loadBacklinks(final DocumentReference documentReference,
                                                 final boolean bTransaction,
                                                 final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    @Deprecated
    public List<String> loadBacklinks(final String fullName,
                                      final XWikiContext context,
                                      final boolean bTransaction)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public void saveLinks(final XWikiDocument doc, final XWikiContext context, final boolean bTransaction)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public void deleteLinks(final long docId, final XWikiContext context, final boolean bTransaction)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    /*------------------- Search -------------------*/

    public int countDocuments(final String wheresql, final XWikiContext context) throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<DocumentReference> searchDocumentReferences(final String wheresql,
                                                            final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String wheresql, final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<DocumentReference> searchDocumentReferences(final String wheresql,
                                                            final int nb,
                                                            final int start,
                                                            final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String wheresql,
                                             final int nb,
                                             final int start,
                                             final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<DocumentReference> searchDocumentReferences(final String wheresql,
                                                            final int nb,
                                                            final int start,
                                                            final String selectColumns,
                                                            final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String wheresql,
                                             final int nb,
                                             final int start,
                                             final String selectColumns,
                                             final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<DocumentReference> searchDocumentReferences(final String parametrizedSqlClause,
                                                            final int nb,
                                                            final int start,
                                                            final List<?> parameterValues,
                                                            final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String parametrizedSqlClause,
                                             final int nb,
                                             final int start,
                                             final List<?> parameterValues,
                                             final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<DocumentReference> searchDocumentReferences(final String parametrizedSqlClause,
                                                            final List<?> parameterValues,
                                                            final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String parametrizedSqlClause,
                                             final List<?> parameterValues,
                                             final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public int countDocuments(final String parametrizedSqlClause,
                              final List<?> parameterValues,
                              final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final int nb,
                                               final int start,
                                               final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping,
                                               final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final int nb,
                                               final int start,
                                               final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final int nb,
                                               final int start,
                                               final List<?> parameterValues,
                                               final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping,
                                               final int nb,
                                               final int start,
                                               final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql, final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping,
                                               final boolean checkRight,
                                               final int nb,
                                               final int start,
                                               final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final List<?> parameterValues,
                                               final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping,
                                               final int nb,
                                               final int start,
                                               final List<?> parameterValues,
                                               final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final int nb,
                                               final int start,
                                               final List<?> parameterValues,
                                               final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping,
                                               final boolean checkRight,
                                               final int nb,
                                               final int start,
                                               final List<?> parameterValues,
                                               final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public <T> List<T> search(final String sql,
                              final int nb,
                              final int start,
                              final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public <T> List<T> search(final String sql,
                              final int nb,
                              final int start,
                              final List<?> parameterValues,
                              final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public <T> List<T> search(final String sql,
                              final int nb,
                              final int start,
                              final Object[][] whereParams,
                              final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public <T> List<T> search(final String sql,
                              final int nb,
                              final int start,
                              final Object[][] whereParams,
                              final List<?> parameterValues,
                              final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public QueryManager getQueryManager()
    {
        throw new RuntimeException("not implemented");
    }

    /*------------------- Multi-wiki -------------------*/

    public boolean isWikiNameAvailable(final String wikiName, final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public void createWiki(final String wikiName, final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public void deleteWiki(final String wikiName, final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    /*------------------- Custom Mapping -------------------*/

    public boolean isCustomMappingValid(final BaseClass bclass,
                                        final String custommapping1,
                                        final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public boolean injectCustomMapping(final BaseClass doc1class, final XWikiContext xWikiContext)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public boolean injectCustomMappings(final XWikiDocument doc, final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<String> getCustomMappingPropertyList(final BaseClass bclass)
    {
        throw new RuntimeException("not implemented");
    }

    public void injectCustomMappings(final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public void injectUpdatedCustomMappings(final XWikiContext context)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }
}
