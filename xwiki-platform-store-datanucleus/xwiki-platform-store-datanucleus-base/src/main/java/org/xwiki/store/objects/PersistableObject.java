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

import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.store.UnexpectedException;

/**
 * An Object which has a reference to the bytecode of it's own class.
 */
@PersistenceCapable(
    table = "PersistableObject",
    identityType = IdentityType.APPLICATION,
    detachable="true"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public class PersistableObject
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistableObject.class);

    @Index
    @PrimaryKey
    private String id;

    /**
     * The PersistableClass for this object.
     * This should not be persisted the normal way because then it would be reloaded
     * with the object rather than using a class in memory which is potentially newer
     * and definitely already in memory.
     */
    @NotPersistent
    private PersistableClass persistableClass;

    public final PersistableClass getPersistableClass()
    {
        if (this.persistableClass == null) {
            final ClassLoader loader = this.getClass().getClassLoader();
            if (loader instanceof PersistableClassLoader) {
                final PersistableClassLoader pcl = (PersistableClassLoader) loader;
                try {
                    this.persistableClass = pcl.loadPersistableClass(this.getClass().getName());
                } catch (Exception e) {
                    throw new UnexpectedException("This object's class seems to have been loaded "
                                                  + "with a PersistableClassLoader which is now "
                                                  + "unable to reload the PersistableClass, is it "
                                                  + "consolation to say ``this can't happen''?", e);
                }
            } else {
                LOGGER.debug("The PersistableClass [{}] was defined in the codebase.",
                             this.getClass());

                this.persistableClass =
                    new PersistableClass(this.getClass().getName(), new byte[0]);
            }
        }

        return this.persistableClass;
    }

    public final void setId(final String identity)
    {
        this.id = identity;
    }

    public final String getId()
    {
        return this.id;
    }
}
