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

import javax.jdo.annotations.NotPersistent;

/**
 * An Object which has a reference to the bytecode of it's own class.
 */
public abstract class PersistableObject
{
    /** The PersistableClass for this object */
    private transient PersistableClass persistableClass;

    public final PersistableClass getPersistableClass()
    {
        if (this.persistableClass == null) {
            if (!(this.getClass().getClassLoader() instanceof PersistableClassLoader)) {
                throw new RuntimeException("PersistableObjects classes can only be loaded using a "
                                           + "PersistableClassLoader.");
            }
            final PersistableClassLoader pcl = (PersistableClassLoader) this.getClass().getClassLoader();
            try {
                this.persistableClass = pcl.loadPersistableClass(this.getClass().getName());
            } catch (Exception e) {
                throw new RuntimeException("This object's class seems to have been loaded with a "
                                           + "PersistableClassLoader which is now unable to reload the "
                                           + "PersistableClass, is it consolation to say "
                                           + "``this can't happen''?");
            }
        }

        return this.persistableClass;
    }
}
