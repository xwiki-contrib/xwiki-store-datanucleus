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

import java.util.Arrays;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.JDOObjectNotFoundException;

/**
 * An Object which has a reference to the bytecode of it's own class.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public final class PersistableClass<T>
{
    /** The name of the class. */
    @Index
    @PrimaryKey
    private String name;

    /**
     * The bytecode representation of this class.
     * Package private so that the classloader can access it directly.
     */
    byte[] bytes;

    /** The java class which this PersistableClass represents. */
    @NotPersistent
    transient Class<T> nativeClass;

    transient PersistableClassLoader persistableClassLoader;

    @NotPersistent
    private transient boolean dirty;

    PersistableClass(final String name, final byte[] bytes)
    {
        this.name = name;
        this.bytes = Arrays.copyOf(bytes, bytes.length);
        this.dirty = true;
    }

    public String getName()
    {
        return this.name;
    }

    public byte[] getBytes()
    {
        return Arrays.copyOf(this.bytes, this.bytes.length);
    }

    public Class<T> getNativeClass()
    {
        return this.nativeClass;
    }

    public boolean isDirty()
    {
        return this.dirty;
    }

    public PersistableClassLoader getClassLoader()
    {
        return this.persistableClassLoader;
    }
}
