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

import java.io.InputStream;
import java.io.IOException;
import java.util.Random;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import org.apache.commons.io.IOUtils;
import org.xwiki.store.TransactionRunnable;


class BlobSaveTransactionRunnable extends TransactionRunnable<PersistenceManager>
{
    /** The number of bytes in each chunk of the blob, 1<<20 == 1 megabyte. */
    private static final int CHUNK_SIZE = 1<<20;

    /** The key to save under. */
    private final String key;

    /** The source of data to copy from. */
    private final InputStream copyFrom;

    /** A java.util.Random used to increment the version. */
    private final Random random;

    /** The blob metadata which will be saved after all blob chunks are saved. */
    private Blob blob;

    public BlobSaveTransactionRunnable(final String key,
                                       final InputStream copyFrom,
                                       final Random random)
    {
        this.key = key;
        this.copyFrom = copyFrom;
        this.random = random;
    }

    @Override
    protected void onPreRun()
    {
        final PersistenceManager pm = this.getContext();
        final long versionNumber = this.getNextVersionNumber();
        this.deleteOldEntries(versionNumber);
        this.blob = new Blob(this.key, versionNumber);
    }

    @Override
    protected void onRun() throws IOException
    {
        final long version = this.blob.getActiveVersion();
        final PersistenceManager pm = this.getContext();
        final byte[] chunkBuff = new byte[CHUNK_SIZE];
        long totalLength = 0;
        for (int i = 0;; i++) {
            int length = IOUtils.read(this.copyFrom, chunkBuff);
            final BlobChunk bc = new BlobChunk(this.key, version, i);
            if (length > 0) {
                totalLength += length;
                if (length < CHUNK_SIZE) {
                    // The last (incomplete) chunk needs to have a shorter array.
                    byte[] smallBuff = new byte[length];
                    System.arraycopy(chunkBuff, 0, smallBuff, 0, length);
                    bc.setContent(smallBuff);
                } else {
                    bc.setContent(chunkBuff);
                }
                pm.makePersistent(bc);
                pm.flush();
            }
            if (length < CHUNK_SIZE) {
                break;
            }
        }
        pm.makePersistent(this.blob);
        this.deleteOldEntries(version);
    }

    /**
     * Delete all entries with versions lower than the given version.
     *
     * @param deleteBeforeVersion delete versions of the attachment lower than this.
     */
    private void deleteOldEntries(final long deleteBeforeVersion)
    {
        final Query q = this.getContext().newQuery(BlobChunk.class, "WHERE blobId == ? && version < ?");
        q.deletePersistentAll(new Object[] { this.key, deleteBeforeVersion });
    }

    /**
     * Increment the version number.
     * If two threads (potentially on two different VMs) are saving the same attachment at the same time,
     * the blob chunks must not interleave from the two versions.
     *
     * @return the next version number in the sequence.
     */
    private long getNextVersionNumber()
    {
        final PersistenceManager pm = this.getContext();
        long versionNumber = 0L;
        try {
            final Blob b = pm.getObjectById(Blob.class, this.key);
            versionNumber = b.getActiveVersion();
        } catch (JDOObjectNotFoundException e) {
            // No older version, just start at 0.
        }
        // draw straws, highest number wins.
        return versionNumber + this.random.nextInt(Integer.MAX_VALUE);
    }
}
