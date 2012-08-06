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
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

import javax.inject.Named;
import javax.jdo.PersistenceManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.TransactionRunnable;

@Component
@Named("datanucleus")
public class DataNucleusBlobStore implements BlobStore<PersistenceManager>
{
    private final Random random = new Random();

    @Override
    public TransactionRunnable<PersistenceManager> getSaveRunnable(final String id, final InputStream readFrom)
    {
        return new BlobSaveTransactionRunnable(id, readFrom, this.random);
    }

    @Override
    public TransactionRunnable<PersistenceManager> getLoadRunnable(final String id, final OutputStream writeTo)
    {
        return new BlobLoadTransactionRunnable(id, writeTo);
    }

    @Override
    public TransactionRunnable<PersistenceManager> getDeleteRunnable(final String id)
    {
        return new BlobDeleteTransactionRunnable(id);
    }

    @Override
    public TransactionRunnable<PersistenceManager> getListAllRunnable(final List<String> outputList)
    {
        return new BlobListAllTransactionRunnable(outputList);
    }
}
