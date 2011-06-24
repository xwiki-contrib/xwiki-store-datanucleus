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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import org.xwiki.store.objects.PersistableObject;
import org.xwiki.store.objects.PersistableClass;
import org.xwiki.store.objects.PersistableClassLoader;

/**
 * The DataNucleusClassLoader is designed to load java classes from the data store.
 */
public class DataNucleusPersistableObjectStore
{
    private final PersistenceManagerFactory factory;

    private final ClassLoader classLoader;

    /**
     * The Constructor.
     *
     * @param factory the means of persisting and loading classes.
     */
    public DataNucleusPersistableObjectStore(final PersistenceManagerFactory factory)
    {
        this.factory = factory;
        this.classLoader = new DataNucleusClassLoader(factory, this.getClass().getClassLoader());
    }

    public void put(final PersistableObject value)
    {
        final Set<PersistableClass> classes = new HashSet<PersistableClass>();
        try {
            getClasses(value, classes, new Stack());
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not reflect nested objects, "
                                       + "is a security manager preventing it?");
        }
        final PersistenceManager manager = this.factory.getPersistenceManager();
        final Transaction txn = manager.currentTransaction();
        txn.begin();
        for (final PersistableClass pc : classes) {
            manager.makePersistent(pc);
        }
        manager.makePersistent(value);
        txn.commit();
    }

    public PersistableObject get(final Object key, final String className)
    {
        final PersistenceManager manager = this.factory.getPersistenceManager();
        final ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.classLoader);
            final Class cls;
            try {
                cls = (Class<? extends PersistableObject>) Class.forName(className);
            } catch (ClassNotFoundException ee) {
                throw new RuntimeException("Could not find the class " + className);
            } catch (ClassCastException ee) {
                throw new RuntimeException("The class " + className + " apparently does not "
                                           + "implement PersistableObject so it cannot be loaded.");
            }
            return (PersistableObject) manager.getObjectById(cls, key);
        } catch (JDOObjectNotFoundException e) {
            return null;
        } finally {
            Thread.currentThread().setContextClassLoader(currentLoader);
        }
    }

    private static void getClasses(final Object value,
                                   final Set<PersistableClass> out,
                                   final Stack objectsExamined)
        throws IllegalAccessException
    {
        if (value == null || objectsExamined.contains(value)) {
            return;
        }
        objectsExamined.push(value);

        // We want to store the classes which are not going to be available
        // without a PersistableClassLoader.
        if (value.getClass().getClassLoader() instanceof PersistableClassLoader) {
System.out.println("        ADDING!!! " + value.getClass().getName());
            out.add(((PersistableObject) value).getPersistableClass());
        } else if (value instanceof Collection) {
            // Check the collection for more persistables.
            for (final Object item : ((Collection) value)) {
                getClasses(item, out, objectsExamined);
            }
        } else {
            // If it's an array then go through that too.
            final Class vclass = value.getClass();

            if (vclass.isArray() && !vclass.getComponentType().isPrimitive()) {
                for (int i = 0; i < Array.getLength(value); i++) {
                    getClasses(Array.get(value, i), out, objectsExamined);
                }
            } else {
                // Index over the fields looking for more persistables
                final Field[] fields = value.getClass().getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    if (!fields[i].getType().isPrimitive()) {
                        fields[i].setAccessible(true);
                        getClasses(fields[i].get(value), out, objectsExamined);
                    }
                }
            }
        }
        objectsExamined.pop();
    }
}
