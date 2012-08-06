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
package org.xwiki.store.blob.datanucleus.internal;

import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable(table = "Blob")
class Blob
{
    @PrimaryKey
    private String id;

    /**
     * The ID of the active version of the blob.
     * Multiple versions are kept in the data store at once in order to allow for 2-stage commits.
     */
    private long activeVersion;

    public Blob(final String id, final long activeVersion)
    {
        this.id = id;
        this.activeVersion = activeVersion;
    }

    /**
     * Get the active version.
     * Although versions are not supported, there is a version field which is used to resolve cases
     * where two threads save the same blob at the same time, potentially on two different machines
     * if the database is distributed. For each new update, the version number is incremented by
     * a random number between 0 and 2^31-1, if two threads are saving at the same time, they will
     * almost certainly choose different numbers and the higher of the two numbers will become the
     * active version.
     *
     * @return the active version.
     */
    public long getActiveVersion()
    {
        return this.activeVersion;
    }

    /** @return the identifier for the blob. */
    public String getId()
    {
        return id;
    }
}
