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
package org.xwiki.store.legacy.internal.datanucleus;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.store.TransactionProvider;
import org.xwiki.store.StartableTransactionRunnable;

/**
 * A provider for acquiring transaction based on DataNucleus.
 * If we ever decided to support more than one underlying data store,
 * we could make this configurable but at the moment it makes no sense to do anything other
 * than forward to datanucleus-cassandra.
 *
 * @version $Id$
 * @since TODO
 */
@Component
@Named("datanucleus")
public class DataNucleusTransactionProvider implements TransactionProvider<PersistenceManager>
{
    /** The provider which we are wrapping. */
    @Inject
    @Named("datanucleus-cassandra")
    private TransactionProvider<PersistenceManager> wrapped;

    @Override
    public StartableTransactionRunnable<PersistenceManager> get()
    {
        return this.wrapped.get();
    }
}
