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
package org.xwiki.store.datanucleus.internal;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import org.xwiki.component.annotation.Component;
import org.xwiki.store.Pointer;
import org.xwiki.store.XWikiTransactionProvider;
import org.xwiki.store.StartableTransactionRunnable;
import org.xwiki.store.datanucleus.XWikiDataNucleusTransaction;

/**
 * A provider for acquiring transaction based on XWikiHibernateStore.
 * This is the default provider because XWikiHibernateStore is the default storage component.
 *
 * @version $Id$
 * @since 3.2M1
 */
@Component("datanucleus")
public class XWikiDataNucleusTransactionProvider implements XWikiTransactionProvider
{
    private final PersistenceManagerFactory factory;

    private final DataNucleusClassLoader dnClassLoader;

    public XWikiDataNucleusTransactionProvider()
    {
        this.factory = JDOHelper.getPersistenceManagerFactory("Test");
        this.dnClassLoader = new DataNucleusClassLoader(this.getClass().getClassLoader());
    }

    /**
     * {@inheritDoc}
     *
     * @see XWikiTransactionProvider#get()
     */
    public StartableTransactionRunnable<XWikiDataNucleusTransaction> get()
    {
        final PersistenceManager pm = this.factory.getPersistenceManager();
        final XWikiDataNucleusTransaction xtx = new XWikiDataNucleusTransaction(pm);
        final Transaction tx = pm.currentTransaction();
        final Pointer<ClassLoader> currentLoaderPtr = new Pointer<ClassLoader>();

        return (new StartableTransactionRunnable<XWikiDataNucleusTransaction>() {
            public XWikiDataNucleusTransaction getProvidedContext()
            {
                return xtx;
            }

            protected void onPreRun()
            {
                tx.begin();

                // Save the old classloader so we can put it back after.
                currentLoaderPtr.target = Thread.currentThread().getContextClassLoader();

                // Setup the classloader and set it to be used.
                dnClassLoader.setPersistenceManager(pm);
                Thread.currentThread().setContextClassLoader(dnClassLoader);
            }

            protected void onCommit()
            {
                if (tx.getRollbackOnly()) {
                    tx.rollback();
                } else {
                    tx.commit();
                }
            }

            protected void onComplete()
            {
                // Remove the PersistenceManager since it should be garbage collected.
                dnClassLoader.removePersistenceManager();

                // Set the classloader back.
                Thread.currentThread().setContextClassLoader(currentLoaderPtr.target);

                if (tx.isActive()) {
                    tx.rollback();
                }
                pm.close();
            }
        });
    }
}
