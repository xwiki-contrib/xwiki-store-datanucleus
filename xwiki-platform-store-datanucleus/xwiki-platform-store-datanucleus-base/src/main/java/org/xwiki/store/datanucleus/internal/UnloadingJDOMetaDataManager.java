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

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.OMFContext;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.jdo.metadata.JDOMetaDataManager;
import org.xwiki.store.objects.PersistableClassLoader;
import org.xwiki.store.objects.Callback;


/**
 * A JDO Metadata manager which unloads MetaData if the class is redefined.
 */
public class UnloadingJDOMetaDataManager extends JDOMetaDataManager implements Callback
{
    public UnloadingJDOMetaDataManager(final OMFContext ctxt)
    {
        super(ctxt);
    }

    public AbstractClassMetaData getMetaDataForClassInternal(final Class c,
                                                             final ClassLoaderResolver clr)
    {
        if (c != null && c.getClassLoader() instanceof PersistableClassLoader) {
            ((PersistableClassLoader) c.getClassLoader()).onClassRedefinition(this);
        }
        return super.getMetaDataForClassInternal(c, clr);
    }

    public void callback(final Object[] args)
    {
        final String className = (String) args[0];
        final AbstractClassMetaData acmd = this.classMetaDataByClass.get(className);
        if (acmd != null) {
            // Remove the acmd from all maps which might contain it, handling duplicate values.
            while (this.classMetaDataByClass.values().remove(acmd));
            while (this.classMetaDataByEntityName.values().remove(acmd));
            while (this.classMetaDataByDiscriminatorName.values().remove(acmd));
            while (this.ormClassMetaDataByClass.values().remove(acmd));
            while (this.classMetaDataByInterface.values().remove(acmd));
        }
    }
}
