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
package org.xwiki.store.datancleus.internal;


import org.datanucleus.JDOClassLoaderResolver;
import org.xwiki.store.objects.PersistableClassLoader;

/**
 * A ClassLoaderResolver which checks if classes are Persistable and if so,
 * makes sure the latest version is used.
 */
public class UpdatingJDOClassLoaderResolver extends JDOClassLoaderResolver
{
    public UpdatingJDOClassLoaderResolver(ClassLoader pmLoader)
    {
        super(pmLoader);
    }

    public UpdatingJDOClassLoaderResolver()
    {
        this(null);
    }

    public Class classForName(String name, ClassLoader primary)
    {
        return getLatestVersion(super.classForName(name, primary));
    }

    public Class classForName(String name, ClassLoader primary, boolean initialize)
    {
        return getLatestVersion(super.classForName(name, primary, initialize));
    }

    /**
     * If the class is a PersistableClass, get the latest version of the class from it's classloader.
     */
    private static Class getLatestVersion(final Class c)
    {
        if (c.getClassLoader() instanceof PersistableClassLoader) {
            final PersistableClassLoader pcl = (PersistableClassLoader) c.getClassLoader();
            try {
                return pcl.loadPersistableClass(c.getName()).getNativeClass();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Tried to use class [" + c.getName()
                                           + "] which is no longer in the store.");
            }
        }
        return c;
    }
}
