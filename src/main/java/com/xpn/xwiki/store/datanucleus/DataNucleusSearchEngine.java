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

import javax.inject.Inject;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.store.SearchEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.store.objects.PersistableClass;
import org.xwiki.store.TransactionException;
import org.xwiki.store.TransactionRunnable;
import org.xwiki.store.StartableTransactionRunnable;
import org.xwiki.store.TransactionProvider;

@Component
@Named("datanucleus")
public class DataNucleusSearchEngine implements SearchEngine
{
    /** The Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataNucleusSearchEngine.class);

    @Inject
    private QueryManager queryManager;

    /** An XWikiTransactionProvider which is used to load and store the locks and links. */
    @Inject
    @Named("datanucleus")
    private TransactionProvider<PersistenceManager> provider;

    public List<String> getTranslationList(final XWikiDocument doc) throws XWikiException
    {
        final List<String> languages = new ArrayList<String>();
        final org.xwiki.query.Query q;
        try {
            q = this.getQueryManager().getNamedQuery("getTranslationList");
        } catch (QueryException e) {
            throw new RuntimeException("Failed to get named query for getting translation list", e);
        }
        LOGGER.debug("Getting translation list for [{}:{}]", doc.getDatabase(), doc.getFullName());
        q.bindValue("wiki", doc.getDatabase()).bindValue("fullname", doc.getFullName());
        final List<PersistableXWikiDocument> translations;
        try {
            translations = (List<PersistableXWikiDocument>) (List) q.execute();
        } catch (QueryException e) {
            throw new RuntimeException("Failed to execute query for getting translation list", e);
        }
        for (final PersistableXWikiDocument translation : translations) {
            if (translation.language != null && !translation.language.equals("")) {
                languages.add(translation.language);
            }
        }

        /*final StartableTransactionRunnable<XWikiDataNucleusTransaction> transaction = this.provider.get();
        final List<String> languages = new ArrayList<String>();
        (new TransactionRunnable<XWikiDataNucleusTransaction>() {
            protected void onRun()
            {
                final PersistenceManager pm = this.getContext().getPersistenceManager();
                final Query query = pm.newQuery(PersistableXWikiDocument.class);
                query.setFilter("wiki == :wiki && fullName == :name");
                final Collection<PersistableXWikiDocument> translations =
                    (Collection<PersistableXWikiDocument>)
                        query.execute(doc.getDatabase(), doc.getFullName());

                for (final PersistableXWikiDocument translation : translations) {
                    if (translation.language != null && !translation.language.equals("")) {
                        languages.add(translation.language);
                    }
                }
            }
        }).runIn(transaction);

        try {
            transaction.start();
        } catch (TransactionException e) {
            throw new RuntimeException("Failed to get translations to document", e);
        }*/

        return languages;
    }

    public List<String> getClassList() throws XWikiException
    {
        final StartableTransactionRunnable<PersistenceManager> transaction = this.provider.get();
        final List<String> out = new ArrayList<String>();
        (new TransactionRunnable<PersistenceManager>() {
            protected void onRun()
            {
                final Query query = this.getContext().newQuery(PersistableClass.class);
                //query.setFilter("xClassXML != null");
                final Collection<PersistableClass> classes =
                    (Collection<PersistableClass>) query.execute();

                for (final PersistableClass klass : classes) {
                    if (klass.getName().startsWith("xwiki.")) {
                        out.add(klass.getName());
                    }
                }
            }
        }).runIn(transaction);

        try {
            transaction.start();
        } catch (TransactionException e) {
            throw new RuntimeException("Failed to get list of XWiki classes.", e);
        }

        return out;
    }

    public int countDocuments(final String wheresql) throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<DocumentReference> searchDocumentReferences(final String wheresql)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String wheresql)
        throws XWikiException
    {
        // Admin interface refuses to allow import if this throws an exception.
        return new ArrayList<String>(0);
    }

    public List<DocumentReference> searchDocumentReferences(final String wheresql,
                                                            final int nb,
                                                            final int start)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String wheresql,
                                             final int nb,
                                             final int start)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<DocumentReference> searchDocumentReferences(final String wheresql,
                                                            final int nb,
                                                            final int start,
                                                            final String selectColumns)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String wheresql,
                                             final int nb,
                                             final int start,
                                             final String selectColumns)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<DocumentReference> searchDocumentReferences(final String parametrizedSqlClause,
                                                            final int nb,
                                                            final int start,
                                                            final List<?> parameterValues)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String parametrizedSqlClause,
                                             final int nb,
                                             final int start,
                                             final List<?> parameterValues)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<DocumentReference> searchDocumentReferences(final String parametrizedSqlClause,
                                                            final List<?> parameterValues)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    @Deprecated
    public List<String> searchDocumentsNames(final String parametrizedSqlClause,
                                             final List<?> parameterValues)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public int countDocuments(final String parametrizedSqlClause,
                              final List<?> parameterValues)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final int nb,
                                               final int start)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final int nb,
                                               final int start)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final int nb,
                                               final int start,
                                               final List<?> parameterValues)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping,
                                               final int nb,
                                               final int start)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping,
                                               final boolean checkRight,
                                               final int nb,
                                               final int start)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final List<?> parameterValues)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final boolean distinctbylanguage,
                                               final boolean customMapping,
                                               final int nb,
                                               final int start,
                                               final List<?> parameterValues)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public List<XWikiDocument> searchDocuments(final String wheresql,
                                               final int nb,
                                               final int start,
                                               final List<?> parameterValues)
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
                                               final List<?> parameterValues)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public <T> List<T> search(final String sql,
                              final int nb,
                              final int start)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public <T> List<T> search(final String sql,
                              final int nb,
                              final int start,
                              final List<?> parameterValues)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public <T> List<T> search(final String sql,
                              final int nb,
                              final int start,
                              final Object[][] whereParams)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public <T> List<T> search(final String sql,
                              final int nb,
                              final int start,
                              final Object[][] whereParams,
                              final List<?> parameterValues)
        throws XWikiException
    {
        throw new RuntimeException("not implemented");
    }

    public QueryManager getQueryManager()
    {
        return this.queryManager;
    }
}
