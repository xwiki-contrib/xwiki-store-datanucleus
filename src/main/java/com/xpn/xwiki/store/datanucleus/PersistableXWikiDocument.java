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
package com.xpn.xwiki.store.datanucleus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Locale;

import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import org.xwiki.model.EntityType;
import org.xwiki.store.datanucleus.internal.JavaClassNameDocumentReferenceSerializer;
import org.xwiki.store.objects.PersistableObject;
import org.xwiki.store.objects.PersistableClass;
import org.xwiki.store.objects.PersistableClassLoader;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.store.objects.legacy.internal.AbstractXObject;

@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class PersistableXWikiDocument extends PersistableObject
{
    /* XWikiDocument fields. */
    @Index
    public String fullName;

    @Index
    public String name;

    @Persistent
    public String title;

    @Index
    public String language;

    @Persistent
    public String defaultLanguage;

    @Persistent
    public int translation;

    @Persistent
    public Date date;

    @Persistent
    public Date contentUpdateDate;

    @Persistent
    public Date creationDate;

    @Persistent
    public String author;

    @Persistent
    public String contentAuthor;

    @Persistent
    public String creator;

    @Index
    public String space;

    @Persistent
    public String content;

    @Persistent
    public String version;

    @Persistent
    public String customClass;

    @Index
    public String parent;

    @Persistent
    public String xClassXML;

    @Persistent
    public int elements;

    @Persistent
    public String defaultTemplate;

    @Persistent
    public String validationScript;

    @Persistent
    public String comment;

    @Persistent
    public boolean isMinorEdit;

    @Persistent
    public String syntaxId;

    @Index
    public boolean hidden;

    /** The wiki where this document belongs. */
    @Persistent
    public String wiki;

    /**
     * Objects.
     * The class of each object is determinable by object.getClass() and the object index
     * in the list corrisponding to it's class is determined by it's placement on the list.
     * All objects of the same class *should* be consecutive on this list but the code will recover from
     * an out of order situation.
     */
    @Persistent(defaultFetchGroup="true")
    @Element(dependent="true")
    public List<AbstractXObject> objects;

    @Persistent(defaultFetchGroup="true")
    @Element(dependent="true")
    public List<PersistableXWikiAttachment> attachments;

    public PersistableXWikiDocument()
    {
        // do nothing.
    }

    public void fromXWikiDocument(final XWikiDocument toClone)
    {
        this.fullName = toClone.getFullName();
        this.name = toClone.getName();
        this.title = toClone.getTitle();
        this.language = toClone.getLanguage();
        this.defaultLanguage = toClone.getDefaultLanguage();
        this.translation = toClone.getTranslation();
        this.date = toClone.getDate();
        this.contentUpdateDate = toClone.getContentUpdateDate();
        this.creationDate = toClone.getCreationDate();
        this.author = toClone.getAuthor();
        this.contentAuthor = toClone.getContentAuthor();
        this.creator = toClone.getCreator();
        this.space = toClone.getSpace();
        this.content = toClone.getContent();
        this.version = toClone.getVersion();
        this.customClass = toClone.getCustomClass();
        this.parent = toClone.getParent();

        // This one is special since XWikiDocument.getXClassXML() does not reflect the current state
        // Of the XClass but is rather a holding place for the XML.
        // The storage engine is expected to update it.
        this.xClassXML = toClone.getXClass().toXMLString();

        this.elements = toClone.getElements();
        this.defaultTemplate = toClone.getDefaultTemplate();
        this.validationScript = toClone.getValidationScript();
        this.comment = toClone.getComment();
        this.isMinorEdit = toClone.isMinorEdit();
        this.syntaxId = toClone.getSyntaxId();
        this.hidden = toClone.isHidden();

        this.wiki = toClone.getDatabase();

        final PersistableClassLoader pcl =
            (PersistableClassLoader) Thread.currentThread().getContextClassLoader();
        this.objects = xObjectsToObjects(toClone.getXObjects(), pcl);

        this.attachments = xAttachmentsToPersistableAttachments(toClone.getAttachmentList());
    }

    private static List<AbstractXObject> xObjectsToObjects(
        final Map<DocumentReference, List<BaseObject>> xObjects,
        final PersistableClassLoader loader)
    {
        final List<AbstractXObject> out = new ArrayList<AbstractXObject>();
        final XClassConverter converter = new XClassConverter(loader);

        for (final DocumentReference ref : xObjects.keySet()) {
            final List<BaseObject> list = xObjects.get(ref);

            Class<AbstractXObject> cls = null;
            for (final BaseObject obj : list) {
                if (obj == null) {
                    System.err.println("\n\nA baseobject was null!\n\n");
                } else {
                    if (cls == null) {
                        cls = converter.convert(obj);
                    }
                    out.add(XObjectConverter.convertFromXObject(obj, cls));
                }
            }
        }
        return out;
    }

    private static void keyGenStep(final EntityReference reference, final StringBuilder sb)
    {
        if (reference == null) {
            return;
        }
        keyGenStep(reference.getParent(), sb);
        sb.append(reference.getName());
        if (reference.getType() == EntityType.WIKI) {
            sb.append(":");
        } else if (reference.getType() == EntityType.DOCUMENT) {
            final Locale locale = ((DocumentReference) reference).getLocale();
            if (locale != null) {
                sb.append(":").append(locale.toString());
            }
        } else {
            sb.append(".");
        }
    }

    /**
     * Generate a key for a given document.
     * This implementation attempts to be forward compatable with nested spaces.
     */
    public static String keyGen(final DocumentReference reference)
    {
        final StringBuilder sb = new StringBuilder();
        keyGenStep(reference, sb);
        return sb.toString();
    }

    private static List<PersistableXWikiAttachment>
        xAttachmentsToPersistableAttachments(final List<XWikiAttachment> attachments)
    {
        final List<PersistableXWikiAttachment> out =
            new ArrayList<PersistableXWikiAttachment>(attachments.size());
        for (final XWikiAttachment attach : attachments) {
            out.add(new PersistableXWikiAttachment(attach));
        }
        return out;
    }

    public XWikiDocument toXWikiDocument(final XWikiDocument prototype)
    {
        final XWikiDocument out = (prototype == null) ? new XWikiDocument(null) : prototype;

        // A new document should not have a JDO key but a loaded document will.
        out.setNew(this.getPersistableObjectId() == null);

        out.setFullName(this.fullName);
        out.setName(this.name);
        out.setTitle(this.title);
        out.setLanguage(this.language);
        out.setDefaultLanguage(this.defaultLanguage);
        out.setTranslation(this.translation);
        out.setDate(this.date);
        out.setContentUpdateDate(this.contentUpdateDate);
        out.setCreationDate(this.creationDate);
        out.setAuthor(this.author);
        out.setContentAuthor(this.contentAuthor);
        out.setCreator(this.creator);
        out.setSpace(this.space);
        out.setContent(this.content);
        out.setVersion(this.version);
        out.setCustomClass(this.customClass);
        out.setParent(this.parent);
        out.setXClassXML(this.xClassXML);
        out.setElements(this.elements);
        out.setDefaultTemplate(this.defaultTemplate);
        out.setValidationScript(this.validationScript);
        out.setComment(this.comment);
        out.setMinorEdit(this.isMinorEdit);
        out.setSyntaxId(this.syntaxId);
        out.setHidden(this.hidden);

        out.setDatabase(this.wiki);

        final String cxml = this.xClassXML;
        if (cxml != null) {
            try {
                out.getXClass().fromXML(cxml);
            } catch (XWikiException e) {
                throw new RuntimeException("Failed to deserialize xml class", e);
            }
        }

        if (this.objects != null) {
            out.setXObjects(objectsToXObjects(this.objects, out.getDocumentReference()));
        }

        if (this.attachments != null) {
            out.setAttachmentList(persistableAttachmentsToXWikiAttachments(this.attachments, out));
        } else {
            System.err.println("\n\n\n\n\n\n\n" + this.fullName + " has no attachments right?\n\n\n\n\n");
        }

        return out;
    }

    private static List<XWikiAttachment> persistableAttachmentsToXWikiAttachments(
        final List<PersistableXWikiAttachment> attachments, final XWikiDocument attachedTo)
    {
        final List<XWikiAttachment> out = new ArrayList<XWikiAttachment>(attachments.size());
        for (final PersistableXWikiAttachment attach : attachments) {
            out.add(attach.toXWikiAttachment(attachedTo));
        }
        return out;
    }

    private static Map<DocumentReference,
                       List<BaseObject>> objectsToXObjects(final List<AbstractXObject> objects,
                                                           final DocumentReference docAttachedTo)
    {
        // Convert the objects.
        final Map<String, List<AbstractXObject>> unconverted = mapObjectsByClassName(objects);
        final Map<DocumentReference, List<BaseObject>> out =
            new TreeMap<DocumentReference, List<BaseObject>>();

        for (final Map.Entry<String, List<AbstractXObject>> e : unconverted.entrySet()) {
            final DocumentReference classRef =
                JavaClassNameDocumentReferenceSerializer.resolveRef(e.getKey(), null);

            final List<AbstractXObject> unconvertedList = e.getValue();
            final List<BaseObject> outList = new ArrayList<BaseObject>(unconvertedList.size());
            for (int i = 0; i < unconvertedList.size(); i++) {
                final BaseObject xwikiObj =
                    XObjectConverter.convertToXObject(unconvertedList.get(i));
                xwikiObj.setDocumentReference(docAttachedTo);
                xwikiObj.setNumber(i);
                outList.add(xwikiObj);
            }
            out.put(classRef, outList);
        }

        return out;
    }

    /**
     * Convert a List of objects into a TreeMap of Lists of Objects by class name.
     * The TreeMap will be ordered the same as the order of objects in the list.
     * This implementation will run with a number of map lookups and inserts equal to the number of classes
     * and ArrayList.add's equal to the number of objects if all objects of each class are in sequence
     * in the list, if they are not then it will perform badly but will not fail.
     *
     * @param unmapped a list of Objects.
     * @return a Map of Object lists by class name, ordered the same as the input list.
     */
    private static Map<String, List<AbstractXObject>> mapObjectsByClassName(
        final List<AbstractXObject> unmapped)
    {
        final Map<String, List<AbstractXObject>> out =
            new TreeMap<String, List<AbstractXObject>>();
        boolean outOfOrder = false;
        Class currentClass = null;
        List<AbstractXObject> currentList = null;
        for (final AbstractXObject obj : unmapped) {
            if (obj.getClass() != currentClass) {
                currentClass = obj.getClass();
                if (out.containsKey(currentClass.getName())) {
                    // This should not happen but if for some reason a stray object gets out of
                    // order, it would be better to take the time to correct it than fail
                    // catistrophicly because we are expecting them to be in order.
                    outOfOrder = true;
                }
                currentList = new ArrayList<AbstractXObject>();
                out.put(currentClass.getName(), currentList);
            }
            currentList.add(obj);
        }
        if (outOfOrder) {
            // If this happens then we need to be careful and index over every entry to correct
            // the order.
            for (final List<AbstractXObject> list : out.values()) {
                list.clear();
            }
            for (final String className : out.keySet()) {
                for (final AbstractXObject obj : unmapped) {
                    out.get(obj.getClass().getName()).add(obj);
                }
            }
        }
        return out;
    }
}
