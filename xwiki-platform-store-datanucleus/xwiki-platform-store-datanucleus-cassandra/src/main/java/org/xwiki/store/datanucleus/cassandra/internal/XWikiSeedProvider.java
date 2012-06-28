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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.cassandra.locator.SeedProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple seed provider which allows seeds to be added by a system property.
 * Specify -Dxwiki.store.XWikiSeedProvider.seeds=1.2.3.4,myserver.dom
 * as a comma deliniated list of seeds to use.
 */
public class XWikiSeedProvider implements SeedProvider
{
    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XWikiSeedProvider.class);

    /** The list of seeds to provide. */
    private List<InetAddress> seeds = new ArrayList<InetAddress>();

    /**
     * The Constructor.
     *
     * @param args a map of arguments which may contain an entry with the key "seeds"
     *             if this entry exists, it will be parsed as a comma deliniarted list of
     *             seed hostnames.
     */
    public XWikiSeedProvider(final Map<String, String> args)
    {
        String hosts = (String) System.getProperty("xwiki.store.XWikiSeedProvider.seeds");
        if (hosts == null) {
            hosts = args.get("seeds");
        } else {
            hosts = hosts + "," + args.get("seeds");
        }
        final String[] hostArray = args.get("seeds").split(",", -1);
        for (final String host : hostArray) {
            try {
                this.seeds.add(InetAddress.getByName(host.trim()));
            } catch (UnknownHostException ex) {
                LOGGER.warn("XWiki seed provider failed to lookup address of [" + host + "]");
            }
        }
    }

    @Override
    public List<InetAddress> getSeeds()
    {
        return Collections.unmodifiableList(seeds);
    }
}
