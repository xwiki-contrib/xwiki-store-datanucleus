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

import javax.inject.Named;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import org.apache.cassandra.thrift.CassandraDaemon;
import org.xwiki.component.annotation.Component;
import org.xwiki.store.TransactionProvider;
import org.xwiki.store.StartableTransactionRunnable;
import org.xwiki.store.datanucleus.DataNucleusClassLoader;

/**
 * A provider for acquiring transaction based on DataNucleus running over Cassandra.
 *
 * @version $Id$
 * @since TODO
 */
@Component
@Named("datanucleus-cassandra")
public class DataNucleusCassandraTransactionProvider implements TransactionProvider<PersistenceManager>
{
    private final CassandraDaemon cassi;

    private final PersistenceManagerFactory factory;

    private final DataNucleusClassLoader dnClassLoader;

    public DataNucleusCassandraTransactionProvider()
    {
        System.setProperty("log4j.configuration", "log4j.properties");
        System.setProperty("cassandra.config", "cassandra.yaml");
        System.setProperty("cassandra-foreground", "1");
        this.cassi = new CassandraDaemon();
        this.cassi.activate();

        this.factory = JDOHelper.getPersistenceManagerFactory("Test");
        this.dnClassLoader = new DataNucleusClassLoader(this.getClass().getClassLoader());
    }

    @Override
    public StartableTransactionRunnable<PersistenceManager> get()
    {
        return new DataNucleusCassandraTransaction(this.factory.getPersistenceManager(),
                                                   this.dnClassLoader);
    }
}
