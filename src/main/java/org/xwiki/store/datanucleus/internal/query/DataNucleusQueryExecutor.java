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
package org.xwiki.store.datanucleus.internal.query;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryExecutor;
import org.xwiki.store.TransactionException;
import org.xwiki.store.TransactionRunnable;
import org.xwiki.store.StartableTransactionRunnable;
import org.xwiki.store.TransactionProvider;

/**
 * Default QueryExecutor for DataNucleus, uses JDOQL.
 * 
 * @version $Id$
 * @since 3.2M2
 */
@Component
@Named("datanucleus")
public class DataNucleusQueryExecutor implements QueryExecutor, Initializable
{
    private final Map<String, String> namedQuerySyntaxByName = new HashMap<String, String>();

    /** A TransactionProvider which is used to get transactions run queries in. */
    @Inject
    @Named("datanucleus")
    private TransactionProvider<PersistenceManager> provider;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            final GroovyClassLoader gcl = new GroovyClassLoader();
            final URL fileURL = this.getClass().getResource("/DataNucleusNamedQueries.groovy");
            final Class gc = gcl.parseClass(new GroovyCodeSource(fileURL));
            final Field[] fields = gc.getDeclaredFields();
            final Object go = gc.newInstance();
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                System.out.println("Storing: " + fields[i].getName() + "  " + fields[i].get(go));
                this.namedQuerySyntaxByName.put(fields[i].getName(), "" + fields[i].get(go));
            }
        } catch (Exception e) {
            throw new InitializationException("Failed to load named queries", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see QueryExecutor#execute(Query)
     */
    public <T> List<T> execute(final Query query) throws QueryException
    {
        final StartableTransactionRunnable<PersistenceManager> transaction = this.provider.get();
        final List<T> out = new ArrayList<T>();
        final String statement = (query.isNamed())
            ? this.namedQuerySyntaxByName.get(query.getStatement())
            : query.getStatement();

        (new TransactionRunnable<PersistenceManager>() {
            protected void onRun()
            {
                final javax.jdo.Query jdoQuery = this.getContext().newQuery(statement);

                if (query.getLimit() > 0 || query.getOffset() > 0) {
                    long rangeEnd = (query.getLimit() > 0) ?
                        query.getLimit() + query.getOffset() : Long.MAX_VALUE;
                    jdoQuery.setRange(query.getOffset(), rangeEnd);
                }

                // Add parameters, it makes no sense for there to be both positional and named parameters
                // and JDO doesn't allow it anyway so it's named unless positional is larger than 0.
                final Collection collection;
                if (query.getPositionalParameters().size() > 0) {
                    final Object[] params =
                        DataNucleusQueryExecutor.arrayForPositionalParameters(
                            query.getPositionalParameters());
                    collection = (Collection) jdoQuery.executeWithArray(params);
                } else {
                    collection = (Collection) jdoQuery.executeWithMap(query.getNamedParameters());
                }
                if (collection != null) {
                    out.addAll((Collection<T>) collection);
                }
            }
        }).runIn(transaction);

        try {
            transaction.start();
        } catch (TransactionException e) {
e.printStackTrace();
            throw new QueryException("Failed to run JDOQL query with statement: ["
                                     + statement + "]", query, e);
        }

        return out;
    }

    private static Object[] arrayForPositionalParameters(final Map<Integer, Object> parameters)
    {
        // TODO: What about "jpql style" parameters which are not 0 indexed?
        int highest = Collections.max(parameters.keySet());
        final List out = new ArrayList(highest + 1);
        for (int i = 0; i <= highest; i++) {
            final Object param = parameters.get(Integer.valueOf(i));
            if (param != null) {
                out.add(param);
            }
        }
        return out.toArray();
    }
}
