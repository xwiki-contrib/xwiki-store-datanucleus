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
import java.lang.ref.SoftReference;
import java.security.SecureClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The DataNucleusClassLoader is designed to load java classes from the data store.
 */
public abstract class AbstractPersistableClassLoader
    extends SecureClassLoader
    implements PersistableClassLoader
{
    private final Map<String, SoftReference<PersistableClass>> classByName =
        new ConcurrentHashMap<String, SoftReference<PersistableClass>>();

    private final Set<Callback> redefinitionCallbacks =
        Collections.newSetFromMap(new WeakHashMap<Callback, Boolean>());

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
        try {
            final Class out = super.loadClass(name);
            if (!(out.getClassLoader() instanceof PersistableClassLoader)) {
                return out;
            }
        } catch (ClassNotFoundException e) {
            // No class found, probably in the data store.
        }

        return this.loadPersistableClass(name).getNativeClass();
    }

    public synchronized PersistableClass<?> definePersistableClass(final String name,
                                                                   final byte[] byteCode)
    {
        final SoftReference<PersistableClass> pcRef = this.classByName.get(name);
        PersistableClass pc = (pcRef != null) ? pcRef.get() : null;
        if (pc != null && Arrays.equals(pc.getBytes(), byteCode)) {
            return pc;
        } else if (pc != null) {
            // It's being re-defined, call all of the callbacks.
            for (final Callback callbk : this.redefinitionCallbacks) {
                callbk.callback(new Object[] { name });
            }
        }
        pc = new PersistableClass(name, byteCode);
        new SinglePersistableClassLoader(this, pc);
        this.classByName.put(name, new SoftReference<PersistableClass>(pc));
        return pc;
    }

    public PersistableClass<?> loadPersistableClass(final String name) throws ClassNotFoundException
    {
        final SoftReference<PersistableClass> pcRef = this.classByName.get(name);
        PersistableClass pc = (pcRef != null) ? pcRef.get() : null;
        if (pc == null) {
            pc = this.getClassFromStorage(name);
            new SinglePersistableClassLoader(this, pc);
        }
        return pc;
    }

    public ClassLoader asNativeLoader()
    {
        return this;
    }

    public synchronized void onClassRedefinition(final Callback callback)
    {
        this.redefinitionCallbacks.add(callback);
    }

    protected abstract PersistableClass getClassFromStorage(final String name)
        throws ClassNotFoundException;

    /**
     * A PersistableClassLoader which loads one and only one class.
     * When the class is no longer used, it will be garbage collected along with the loader.
     */
    private static class SinglePersistableClassLoader
        extends SecureClassLoader
        implements PersistableClassLoader
    {
        private final Class singleClass;

        private final PersistableClassLoader parent;

        private SinglePersistableClassLoader(final PersistableClassLoader parent,
                                             final PersistableClass toLoad)
        {
            super(parent.asNativeLoader());
            this.parent = parent;
            final byte[] bytes = toLoad.getBytes();
            if (bytes == null) {
                throw new NullPointerException("The bytecode for class [" + toLoad.getName()
                                               + "] is null.");
            }
            final Class c = super.defineClass(toLoad.getName(), bytes, 0, bytes.length);
            this.resolveClass(c);
            toLoad.nativeClass = c;
            toLoad.persistableClassLoader = this;
            this.singleClass = c;
        }

        public PersistableClass<?> loadPersistableClass(final String name)
            throws ClassNotFoundException
        {
            return this.parent.loadPersistableClass(name);
        }

        public PersistableClass<?> definePersistableClass(final String name, final byte[] byteCode)
        {
            return this.parent.definePersistableClass(name, byteCode);
        }

        public ClassLoader asNativeLoader()
        {
            return this;
        }

        public void onClassRedefinition(final Callback callback)
        {
            this.parent.onClassRedefinition(callback);
        }
    }
}
