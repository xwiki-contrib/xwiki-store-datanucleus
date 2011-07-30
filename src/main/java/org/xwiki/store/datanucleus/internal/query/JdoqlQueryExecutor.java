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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import org.xwiki.component.annotation.Component;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryExecutor;

/**
 * QueryExecutor implementation for JDOQL run against DataNucleus.
 * 
 * @version $Id$
 * @since 3.2M2
 */
@Component("jdoql")
public class JdoqlQueryExecutor implements QueryExecutor
{
    /** The wrapped QueryExecutor. */
    @Inject
    @Named("datanucleus")
    private QueryExecutor executor;

    /**
     * {@inheritDoc}
     *
     * @see QueryExecutor#execute(Query)
     */
    public <T> List<T> execute(final Query query) throws QueryException
    {
        return this.executor.execute(query);
    }
}
