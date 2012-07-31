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

import java.security.SecureClassLoader;

/**
 * A ClassLoader which refuses to load a given class,
 * useful for forcing a child classloader to use a defined class.
 */
public class BlockingClassLoader extends SecureClassLoader
{
    private final String nameOfClassToBlock;

    /**
     * The Constructor.
     *
     * @param nameOfClassToBlock the name of the class to refuse to load.
     */
    public BlockingClassLoader(final ClassLoader parent, final String nameOfClassToBlock)
    {
        super(parent);
        this.nameOfClassToBlock = nameOfClassToBlock;
    }

    public Class<?> loadClass(final String name) throws ClassNotFoundException
    {
        if (this.nameOfClassToBlock.equals(name)) {
            throw new ClassNotFoundException("Loading of class [" + name + "] is prevented.");
        }
        return super.loadClass(name);
    }
}
