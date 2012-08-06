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

import javax.jdo.annotations.Index;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable(table = "BlobChunk")
class BlobChunk
{
    /**
     * The primary key.
     * This is constructed of the blob id followed by the chunk index, starting with zero.
     */
    @PrimaryKey
    private Object[] id;

    @Index
    private String blobId;

    @Index
    private long version;

    /** The content of the blob chunk. */
    private byte[] content;

    public BlobChunk(final String blobId,
                     final long version,
                     final int chunkIndex)
    {
        this.id = makeId(blobId, version, chunkIndex);
        this.blobId = blobId;
        this.version = version;
    }

    public byte[] getContent()
    {
        return this.content;
    }

    public void setContent(final byte[] content)
    {
        this.content = content;
    }

    public static Object[] makeId(final String blobId,
                                  final long version,
                                  final int chunkIndex)
    {
        return new Object[] { blobId, version, chunkIndex };
    }
}
