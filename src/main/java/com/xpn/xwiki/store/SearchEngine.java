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
package com.xpn.xwiki.store;

import java.util.List;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import org.xwiki.component.annotation.ComponentRole;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.query.QueryManager;

@ComponentRole
public interface SearchEngine
{
    List<String> getTranslationList(final XWikiDocument doc) throws XWikiException;

    List<String> getClassList() throws XWikiException;

    int countDocuments(final String wheresql) throws XWikiException;

    List<DocumentReference> searchDocumentReferences(final String wheresql) throws XWikiException;

    @Deprecated
    List<String> searchDocumentsNames(final String wheresql) throws XWikiException;

    List<DocumentReference> searchDocumentReferences(final String wheresql,
                                                     final int nb,
                                                     final int start) throws XWikiException;
    @Deprecated
    List<String> searchDocumentsNames(final String wheresql,
                                      final int nb,
                                      final int start) throws XWikiException;

    List<DocumentReference> searchDocumentReferences(final String wheresql,
                                                     final int nb,
                                                     final int start,
                                                     final String selectColumns)
        throws XWikiException;

    @Deprecated
    List<String> searchDocumentsNames(final String wheresql,
                                      final int nb,
                                      final int start,
                                      final String selectColumns) throws XWikiException;

    List<DocumentReference> searchDocumentReferences(final String parametrizedSqlClause,
                                                     final int nb,
                                                     final int start,
                                                     final List<?> parameterValues)
        throws XWikiException;

    @Deprecated
    List<String> searchDocumentsNames(final String parametrizedSqlClause,
                                      final int nb,
                                      final int start,
                                      final List<?> parameterValues) throws XWikiException;

    List<DocumentReference> searchDocumentReferences(final String parametrizedSqlClause,
                                                     final List<?> parameterValues)
        throws XWikiException;

    @Deprecated
    List<String> searchDocumentsNames(final String parametrizedSqlClause,
                                      final List<?> parameterValues) throws XWikiException;

    int countDocuments(final String parametrizedSqlClause,
                       final List<?> parameterValues) throws XWikiException;

    List<XWikiDocument> searchDocuments(final String wheresql,
                                        final boolean distinctbylanguage) throws XWikiException;

    List<XWikiDocument> searchDocuments(final String wheresql,
                                        final int nb,
                                        final int start) throws XWikiException;

    List<XWikiDocument> searchDocuments(final String wheresql,
                                        final boolean distinctbylanguage,
                                        final boolean customMapping) throws XWikiException;

    List<XWikiDocument> searchDocuments(final String wheresql,
                                        final boolean distinctbylanguage,
                                        final int nb,
                                        final int start) throws XWikiException;

    List<XWikiDocument> searchDocuments(final String wheresql,
                                        final boolean distinctbylanguage,
                                        final int nb,
                                        final int start,
                                        final List<?> parameterValues) throws XWikiException;

    List<XWikiDocument> searchDocuments(final String wheresql,
                                        final boolean distinctbylanguage,
                                        final boolean customMapping,
                                        final int nb,
                                        final int start) throws XWikiException;

    List<XWikiDocument> searchDocuments(final String wheresql) throws XWikiException;

    List<XWikiDocument> searchDocuments(final String wheresql,
                                        final boolean distinctbylanguage,
                                        final boolean customMapping,
                                        final boolean checkRight,
                                        final int nb,
                                        final int start) throws XWikiException;

    List<XWikiDocument> searchDocuments(final String wheresql,
                                        final List<?> parameterValues) throws XWikiException;

    List<XWikiDocument> searchDocuments(final String wheresql,
                                        final boolean distinctbylanguage,
                                        final boolean customMapping,
                                        final int nb,
                                        final int start,
                                        final List<?> parameterValues) throws XWikiException;

    List<XWikiDocument> searchDocuments(final String wheresql,
                                        final int nb,
                                        final int start,
                                        final List<?> parameterValues) throws XWikiException;

    List<XWikiDocument> searchDocuments(final String wheresql,
                                        final boolean distinctbylanguage,
                                        final boolean customMapping,
                                        final boolean checkRight,
                                        final int nb,
                                        final int start,
                                        final List<?> parameterValues) throws XWikiException;

    <T> List<T> search(final String sql,
                       final int nb,
                       final int start) throws XWikiException;

    <T> List<T> search(final String sql,
                       final int nb,
                       final int start,
                       final List<?> parameterValues) throws XWikiException;

    <T> List<T> search(final String sql,
                       final int nb,
                       final int start,
                       final Object[][] whereParams) throws XWikiException;

    <T> List<T> search(final String sql,
                      final int nb,
                      final int start,
                      final Object[][] whereParams,
                      final List<?> parameterValues) throws XWikiException;

    QueryManager getQueryManager();
}
