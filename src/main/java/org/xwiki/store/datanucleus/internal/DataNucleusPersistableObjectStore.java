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
import javax.jdo.JDOObjectNotFoundException;
import org.xwiki.store.Pointer;
import org.xwiki.store.TransactionException;
import org.xwiki.store.TransactionRunnable;
import org.xwiki.store.datanucleus.XWikiDataNucleusTransaction;
import org.xwiki.store.objects.PersistableObject;
import org.xwiki.store.objects.PersistableClass;
import org.xwiki.store.objects.PersistableClassLoader;

/**
 * The DataNucleusClassLoader is designed to load java classes from the data store.
 */
public class DataNucleusPersistableObjectStore
{
    public TransactionRunnable<XWikiDataNucleusTransaction> getStoreTransactionRunnable(
        final PersistableObject value)
    {
        final Set<PersistableClass> classes = new HashSet<PersistableClass>();

        return (new TransactionRunnable<XWikiDataNucleusTransaction>() {
            protected void onRun()
            {
				try {
				    getClasses(value, classes, new Stack());
				} catch (IllegalAccessException e) {
				    throw new RuntimeException("Could not reflect nested objects, "
				                               + "is a security manager preventing it?");
				}
                final PersistenceManager manager = this.getContext().getPersistenceManager();
                for (final PersistableClass pc : classes) {
                    if (pc.isDirty()) {
                        try {
System.out.println("STORING CLASS!!! " + pc.getNativeClass().getName() + "    " + pc.getBytes().length);
                        manager.makePersistent(pc);
                        } catch (Exception e) { }
                    }
                }
                manager.makePersistent(value);
            }
        });
    }

    public TransactionRunnable<XWikiDataNucleusTransaction> getLoadTransactionRunnable(
        final Object key,
        final String className,
        final Pointer<PersistableObject> outPointer)
    {
        return (new TransactionRunnable<XWikiDataNucleusTransaction>() {
            protected void onRun() throws ClassNotFoundException, ClassCastException
            {
                final Class<? extends PersistableObject> cls =
                    (Class<? extends PersistableObject>) Class.forName(className);
                final PersistenceManager pm = this.getContext().getPersistenceManager();
                try {
                    outPointer.target = pm.getObjectById(cls, key);
                    pm.makeTransient(outPointer.target);
                } catch (JDOObjectNotFoundException e) {
                    // Not found, outPointer is already null so we leave it.
                }
            }
        });
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
