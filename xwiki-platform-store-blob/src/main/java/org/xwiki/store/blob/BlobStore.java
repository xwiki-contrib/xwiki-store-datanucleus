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
package org.xwiki.store.blob;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.store.TransactionRunnable;

/**
 * Generic storage interface for storing large binary objects.
 *
 * @version $Id$
 * @since TODO
 * @param <T> the type of transaction which this store's TransactionRunnables must run in.
 */
@Role
public interface BlobStore<T>
{
    /**
     * Get a TransactionRunnable for saving a blob.
     *
     * @param id the identifier for the blob.
     * @param readFrom the InputStream to read the blob content from.
     * @return a new TransactionRunnable.
     */
    TransactionRunnable<T> getSaveRunnable(final String id, final InputStream readFrom);

    /**
     * Get a TransactionRunnable for storing a blob to the store.
     *
     * @param id the identifier for the blob.
     * @param writeTo the OutputStream to write the blob content to.
     * @return a new TransactionRunnable.
     */
    TransactionRunnable<T> getLoadRunnable(final String id, final OutputStream writeTo);

    /**
     * Get a TransactionRunnable for removing a blob from the blob store.
     *
     * @param id the identifier for the blob.
     * @return a new TransactionRunnable.
     */
    TransactionRunnable<T> getDeleteRunnable(final String id);

    /**
     * Get a TransactionRunnable for listing all blobs in the blob store.
     *
     * @param populateList an empty list which will be populated with the ids of all entries.
     * @return a TransactionRunnable which will populate the list when run.
     */
    TransactionRunnable<T> getListAllRunnable(final List<String> populateList);
}
