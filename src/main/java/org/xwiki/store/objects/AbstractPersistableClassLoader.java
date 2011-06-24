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
package org.xwiki.store.objects;

import java.io.IOException;
import java.security.SecureClassLoader;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * The DataNucleusClassLoader is designed to load java classes from the data store.
 */
public abstract class AbstractPersistableClassLoader
    extends SecureClassLoader
    implements PersistableClassLoader
{
    private final Map<String, PersistableClass> classByName = new HashMap<String, PersistableClass>();

    /**
     * The Constructor.
     *
     * @param parent the ClassLoader which will be used to try loading before trying to load from storage.
     */
    public AbstractPersistableClassLoader(final ClassLoader parent)
    {
        super(parent);
    }

    public Class<?> loadClass(final String name) throws ClassNotFoundException
    {
        System.err.println("\n\n\n\n\n\n\n\n\n\n\n\n Attempting to load Class: " + name + "\n\n\n\n\n\n\n\n\n\n\n\n");
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException e) {
            return this.loadPersistableClass(name).getNativeClass();
        }
    }

    public PersistableClass<?> definePersistableClass(final String name, final byte[] byteCode)
    {
        PersistableClass pc = new PersistableClass(name, byteCode);
        final Class<?> c = this.defineClass(name, pc.bytes, 0, pc.bytes.length, (ProtectionDomain) null);
        this.resolveClass(c);
        pc = (PersistableClass<?>) pc;
        pc.nativeClass = c;
        this.classByName.put(name, pc);
        return pc;
    }

    public PersistableClass<?> loadPersistableClass(final String name) throws ClassNotFoundException
    {
        PersistableClass pc = this.classByName.get(name);
        if (pc != null) {
            return (PersistableClass<?>) pc;
        }

        pc = this.getClassFromStorage(name);
        final Class<?> c = this.defineClass(name, pc.bytes, 0, pc.bytes.length, (ProtectionDomain) null);
        pc = (PersistableClass<?>) pc;
        this.resolveClass(c);
        pc.nativeClass = c;

        pc.persistableClassLoader = this;
        this.classByName.put(name, pc);
        return pc;
    }

    public ClassLoader asNativeLoader()
    {
        return this;
    }

    protected abstract PersistableClass getClassFromStorage(final String name)
        throws ClassNotFoundException;
}
