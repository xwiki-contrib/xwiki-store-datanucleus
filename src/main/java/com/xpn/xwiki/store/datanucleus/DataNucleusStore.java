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

import java.util.List;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.doc.XWikiLink;
import com.xpn.xwiki.doc.XWikiLock;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.store.LinkAndLockStore;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.store.SearchEngine;
import javax.inject.Named;
import javax.inject.Inject;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.QueryManager;
import org.xwiki.store.XWikiTransactionProvider;
import org.xwiki.store.datanucleus.internal.XWikiDataNucleusTransactionProvider;


@Component("datanucleus")
public class DataNucleusStore implements XWikiStoreInterface, Initializable
{
    @Inject
    @Named("datanucleus")
    private XWikiTransactionProvider provider;

    @Inject
    @Named("datanucleus")
    private LinkAndLockStore linksAndLocks;

    @Inject
    @Named("datanucleus")
    private SearchEngine search;

    private DataNucleusXWikiDocumentStore docStore;

    public void initialize()
    {
        this.docStore =
            new DataNucleusXWikiDocumentStore((XWikiDataNucleusTransactionProvider) this.provider);
    }

    public void cleanUp(final XWikiContext context)
    {
        // This is a hook for when the system shuts down, nothing is required to be done.
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
        // All objects are essentially custom mapped in this schema but there is no need for custom mappings
        // to be injected on a per load basis so we can pretend that nothing is ever custom mapped.
        return false;
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

    /* ------------------ Load & Store documents (DataNucleusXWikiDocumentStore) ------------------ */

    public void saveXWikiDoc(final XWikiDocument doc, final XWikiContext context) throws XWikiException
    {
        this.docStore.saveXWikiDoc(doc, context);
    }

    public void saveXWikiDoc(final XWikiDocument doc, final XWikiContext context, final boolean ignored)
        throws XWikiException
    {
        this.docStore.saveXWikiDoc(doc, context, false);
    }

    public XWikiDocument loadXWikiDoc(final XWikiDocument doc, final XWikiContext unused)
        throws XWikiException
    {
        return this.docStore.loadXWikiDoc(doc, null);
    }

    public boolean exists(final XWikiDocument doc, final XWikiContext unused) throws XWikiException
    {
        return this.docStore.exists(doc, null);
    }

    public void deleteXWikiDoc(final XWikiDocument doc, final XWikiContext context)
        throws XWikiException
    {
        this.docStore.deleteXWikiDoc(doc, null);
    }

    /* ------------------ Search (DataNucleusSearchEngine) ------------------ */

    public List<String> getTranslationList(final XWikiDocument doc, final XWikiContext unused)
        throws XWikiException
    {
        return this.search.getTranslationList(doc);
    }

    public List<String> getClassList(final XWikiContext unused) throws XWikiException
    {
        return this.search.getClassList();
    }

    public int countDocuments(final String wheresql, final XWikiContext unused) throws XWikiException
    {
        return this.search.countDocuments(wheresql);
    }

    public List<DocumentReference> searchDocumentReferences(final String wheresql,
                                                            final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocumentReferences(wheresql);
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String wheresql, final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocumentsNames(wheresql);
    }

    public List<DocumentReference> searchDocumentReferences(final String wheresql,
                                                            final int nb,
                                                            final int start,
                                                            final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocumentReferences(wheresql, nb, start);
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String wheresql,
                                             final int nb,
                                             final int start,
                                             final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocumentsNames(wheresql, nb, start);
    }

    public List<DocumentReference> searchDocumentReferences(final String wheresql,
                                                            final int nb,
                                                            final int start,
                                                            final String selectColumns,
                                                            final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocumentReferences(wheresql, nb, start, selectColumns);
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String wheresql,
                                             final int nb,
                                             final int start,
                                             final String selectColumns,
                                             final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocumentsNames(wheresql, nb, start, selectColumns);
    }

    public List<DocumentReference> searchDocumentReferences(final String parametrizedSqlClause,
                                                            final int nb,
                                                            final int start,
                                                            final List<?> parameterValues,
                                                            final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocumentReferences(parametrizedSqlClause, nb, start, parameterValues);
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String parametrizedSqlClause,
                                             final int nb,
                                             final int start,
                                             final List<?> parameterValues,
                                             final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocumentsNames(parametrizedSqlClause, nb, start, parameterValues);
    }

    public List<DocumentReference> searchDocumentReferences(final String parametrizedSqlClause,
                                                            final List<?> parameterValues,
                                                            final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocumentReferences(parametrizedSqlClause, parameterValues);
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String parametrizedSqlClause,
                                             final List<?> parameterValues,
                                             final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocumentsNames(parametrizedSqlClause, parameterValues);
    }

    public int countDocuments(final String parametrizedSqlClause,
                              final List<?> parameterValues,
                              final XWikiContext unused)
        throws XWikiException
    {
        return this.search.countDocuments(parametrizedSqlClause, parameterValues);
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocuments(wheresql, distinctbylanguage);
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final int nb,
                                               final int start,
                                               final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocuments(wheresql, nb, start);
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping,
                                               final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocuments(wheresql, distinctbylanguage, customMapping);
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final int nb,
                                               final int start,
                                               final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocuments(wheresql, distinctbylanguage, nb, start);
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final int nb,
                                               final int start,
                                               final List<?> parameterValues,
                                               final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocuments(wheresql, distinctbylanguage, nb, start, parameterValues);
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping,
                                               final int nb,
                                               final int start,
                                               final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocuments(wheresql, distinctbylanguage, customMapping, nb, start);
    }

    public List<XWikiDocument> searchDocuments(final String wheresql, final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocuments(wheresql);
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping,
                                               final boolean checkRight,
                                               final int nb,
                                               final int start,
                                               final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocuments(wheresql, distinctbylanguage, customMapping, checkRight, nb,
                                           start);
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final List<?> parameterValues,
                                               final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocuments(wheresql, parameterValues);
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping,
                                               final int nb,
                                               final int start,
                                               final List<?> parameterValues,
                                               final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocuments(wheresql, distinctbylanguage, customMapping, nb, start,
                                           parameterValues);
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final int nb,
                                               final int start,
                                               final List<?> parameterValues,
                                               final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocuments(wheresql, nb, start, parameterValues);
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping,
                                               final boolean checkRight,
                                               final int nb,
                                               final int start,
                                               final List<?> parameterValues,
                                               final XWikiContext unused)
        throws XWikiException
    {
        return this.search.searchDocuments(wheresql, distinctbylanguage, customMapping, checkRight, nb,
                                           start, parameterValues);
    }

    public <T> List<T> search(final String sql,
                              final int nb,
                              final int start,
                              final XWikiContext unused)
        throws XWikiException
    {
        return this.search.search(sql, nb, start);
    }

    public <T> List<T> search(final String sql,
                              final int nb,
                              final int start,
                              final List<?> parameterValues,
                              final XWikiContext unused)
        throws XWikiException
    {
        return this.search.search(sql, nb, start, parameterValues);
    }

    public <T> List<T> search(final String sql,
                              final int nb,
                              final int start,
                              final Object[][] whereParams,
                              final XWikiContext unused)
        throws XWikiException
    {
        return this.search.search(sql, nb, start, whereParams);
    }

    public <T> List<T> search(final String sql,
                              final int nb,
                              final int start,
                              final Object[][] whereParams,
                              final List<?> parameterValues,
                              final XWikiContext unused)
        throws XWikiException
    {
        return this.search.search(sql, nb, start, whereParams, parameterValues);
    }

    public QueryManager getQueryManager()
    {
        return this.search.getQueryManager();
    }

    /*------------------- Links & Locks -------------------*/

    public XWikiLock loadLock(final long docId, final XWikiContext unused, final boolean ignored)
        throws XWikiException
    {
        return this.linksAndLocks.loadLock(docId);
    }

    public void saveLock(final XWikiLock lock, final XWikiContext unused, final boolean ignored)
        throws XWikiException
    {
        this.linksAndLocks.saveLock(lock);
    }

    public void deleteLock(final XWikiLock lock, final XWikiContext unused, final boolean ignored)
        throws XWikiException
    {
        this.linksAndLocks.deleteLock(lock);
    }

    public List<XWikiLink> loadLinks(final long docId, final XWikiContext unused, final boolean ignored)
        throws XWikiException
    {
        return this.linksAndLocks.loadLinks(docId);
    }

    public List<DocumentReference> loadBacklinks(final DocumentReference documentReference,
                                                 final boolean ignored,
                                                 final XWikiContext unused)
        throws XWikiException
    {
        return this.linksAndLocks.loadBacklinks(documentReference);
    }

    @Deprecated
    public List<String> loadBacklinks(final String fullName,
                                      final XWikiContext unused,
                                      final boolean ignored)
        throws XWikiException
    {
        return this.linksAndLocks.loadBacklinks(fullName);
    }

    public void saveLinks(final XWikiDocument doc, final XWikiContext unused, final boolean ignored)
        throws XWikiException
    {
        this.linksAndLocks.saveLinks(doc);
    }

    public void deleteLinks(final long docId, final XWikiContext unused, final boolean ignored)
        throws XWikiException
    {
        this.linksAndLocks.deleteLinks(docId);
    }
}
