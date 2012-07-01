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
package org.xwiki.store.datanucleus.cassandra.internal;

import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;
import org.xwiki.store.datanucleus.internal.DataNucleusClassLoader;
import org.xwiki.store.StartableTransactionRunnable;

/**
 * A means of supplying the PersistanceManager to code running inside of
 * TransactionRunnables based on DataNucleus transactions.
 *
 * @version $Id$
 * @since 3.2M1
 */
final class DataNucleusCassandraTransaction extends StartableTransactionRunnable<PersistenceManager>
{
    private final PersistenceManager manager;
    private final Transaction transaction;
    private final DataNucleusClassLoader dnClassLoader;
    private ClassLoader oldClassLoader;

    public DataNucleusCassandraTransaction(final PersistenceManager manager,
                                           final DataNucleusClassLoader dnClassLoader)
    {
        this.manager = manager;
        this.dnClassLoader = dnClassLoader;
        this.transaction = manager.currentTransaction();
    }

    @Override
    protected PersistenceManager getProvidedContext()
    {
        return this.manager;
    }

    @Override
    protected void onPreRun()
    {
        this.transaction.begin();

        // Save the old classloader so we can put it back after.
        this.oldClassLoader = Thread.currentThread().getContextClassLoader();

        // Setup the classloader and set it to be used.
        this.dnClassLoader.setPersistenceManager(this.manager);
        Thread.currentThread().setContextClassLoader(dnClassLoader);
    }

    @Override
    protected void onCommit()
    {
        if (this.transaction.getRollbackOnly()) {
            this.transaction.rollback();
        } else {
            this.transaction.commit();
        }
    }

    @Override
    protected void onComplete()
    {
        // Remove the PersistenceManager since it should be garbage collected.
        this.dnClassLoader.removePersistenceManager();

        // Set the classloader back.
        Thread.currentThread().setContextClassLoader(this.oldClassLoader);

        if (this.transaction.isActive()) {
            this.transaction.rollback();
        }
        this.manager.close();
    }
}
