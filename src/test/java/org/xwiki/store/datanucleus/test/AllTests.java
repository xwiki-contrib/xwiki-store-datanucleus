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
package org.xwiki.store.datanucleus.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;
import org.apache.cassandra.thrift.CassandraDaemon;

/**
 * Runs all functional tests found in the classpath.
 *
 * @version $Id$
 * @since TODO
 */
@RunWith(ClasspathSuite.class)
public class AllTests
{
    private static CassandraDaemon DAEMON;

    // Moved to DataNucleusTransactionProvider.
    //@BeforeClass
    public static void init() throws Exception
    {
        System.setProperty("log4j.configuration", "log4j.properties");
        System.setProperty("cassandra.config", "cassandra.yaml");
        System.setProperty("cassandra-foreground", "1");
        DAEMON = new CassandraDaemon();
        DAEMON.activate();
    }

    // This takes forever so it's better to just let the jvm stop, we have no data to protect.
    //@AfterClass
    public static void shutdown() throws Exception
    {
        DAEMON.deactivate();
    }
}
