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
package org.xwiki.store.legacy.internal.datanucleus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Locale;

import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.PropertyClass;
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


@PersistenceCapable(detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
class PersistableXWikiDocument extends PersistableObject
{
    /* XWikiDocument fields. */
    @Index
    private String fullName;

    @Index
    private String name;

    private String title;

    @Index
    private String language;

    private String defaultLanguage;

    private int translation;

    private Date date;

    private Date contentUpdateDate;

    private Date creationDate;

    private String author;

    private String contentAuthor;

    private String creator;

    @Index
    private String space;

    private String content;

    private String version;

    private String customClass;

    @Index
    private String parent;

    @Index
    private String xClassXML;

    private int elements;

    private String defaultTemplate;

    private String validationScript;

    private String comment;

    private boolean isMinorEdit;

    private String syntaxId;

    @Index
    private boolean hidden;

    /** The wiki where this document belongs. */
    @Persistent
    private String wiki;

    /**
     * Objects.
     * The class of each object is determinable by object.getClass() and the object index
     * in the list corrisponding to it's class is determined by it's placement on the list.
     * All objects of the same class *should* be consecutive on this list but the code will
     * recover from an out of order situation.
     */
    @Persistent(defaultFetchGroup="true")
    @Element(dependent="true")
    @Index
    private List<AbstractXObject> objects;

    @Persistent(defaultFetchGroup="true")
    @Element(dependent="true")
    private Map<String, PersistableXWikiAttachment> attachments;

    /**
     * The PersistableClass defined in this document.
     * This is set if the class changes
     * and it will be persisted manually by DataNucleusXWikiDocumentStore.
     */
    @NotPersistent
    private PersistableClass persistableClass;

    @NotPersistent
    private XWikiDocument original;

    private PersistableXWikiDocument()
    {
        // do nothing.
    }

    PersistableXWikiDocument(final XWikiDocument toClone)
    {
        this.fullName = toClone.getFullName();
        this.name = toClone.getName();
        this.title = toClone.getTitle();
        this.language = toClone.getLanguage();
        this.defaultLanguage = toClone.getDefaultLanguage();
        this.translation = toClone.getTranslation();
        this.creationDate = toClone.getCreationDate();
        this.author = toClone.getAuthor();

        // This is a special case which is handled in XWikiHibernateStore#saveXWikiDoc()
        final Date now = new Date();
        this.date = now;
        if (toClone.isContentDirty()) {
            this.contentUpdateDate = now;
            this.contentAuthor = toClone.getAuthor();
        } else {
            this.contentUpdateDate = toClone.getContentUpdateDate();
            this.contentAuthor = toClone.getContentAuthor();
        }

        this.creator = toClone.getCreator();
        this.space = toClone.getSpace();
        this.content = toClone.getContent();
        this.version = toClone.getVersion();
        this.customClass = toClone.getCustomClass();
        this.parent = toClone.getParent();
        this.elements = toClone.getElements();
        this.defaultTemplate = toClone.getDefaultTemplate();
        this.validationScript = toClone.getValidationScript();
        this.comment = toClone.getComment();
        this.isMinorEdit = toClone.isMinorEdit();
        this.syntaxId = toClone.getSyntaxId();
        this.hidden = toClone.isHidden();

        this.wiki = toClone.getDatabase();

        this.original = toClone;
    }

    /**
     * This is needed because reentrence is not allowed in the store and some of the
     * functions in XWikiDocument use the store, eg: getSyntaxId().
     * After beginning a transaction, call this function to prep the persistable
     * document for storage.
     */
    void convertObjects()
    {
        final PersistableClassLoader pcl =
            (PersistableClassLoader) Thread.currentThread().getContextClassLoader();

        // Check if the class has been altered.
        if (this.original.getXClass().getFieldList().size() > 0) {
            this.xClassXML = this.original.getXClass().toXMLString();
            if (!this.xClassXML.equals(this.original.getXClassXML())) {
                // make a new PersistableClass
                this.persistableClass = convertXClass(this.original.getXClass(), pcl);
            }
        }

        this.objects = (List) xObjectsToObjects(this.original.getXObjects(), pcl);

        this.attachments = xAttachmentsToPersistableAttachments(this.original.getAttachmentList());
    }

    XWikiDocument toXWikiDocument(final XWikiDocument prototype)
    {
        final XWikiDocument out = (prototype == null) ? new XWikiDocument(null) : prototype;

        // A new document should not have a JDO key but a loaded document will.
        out.setNew(this.getId() == null);

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
            // TODO This cast is dirty, need to figure out a better way
            out.setXObjects(objectsToXObjects(((List) this.objects), out.getDocumentReference()));
        }

        if (this.attachments != null) {
            out.setAttachmentList(persistableAttachmentsToXWikiAttachments(this.attachments, out));
        }

        return out;
    }

    /** Get the PersistableClass defined in this XDoc if any. */
    PersistableClass getDefinedPersistableClass()
    {
        return this.persistableClass;
    }


    //------------------- Static Functions ---------------------//

    private static PersistableClass<XObject> convertXClass(final BaseClass xclass,
                                                           final PersistableClassLoader loader)
    {
        final String className =
            JavaClassNameDocumentReferenceSerializer.serializeRef(xclass.getDocumentReference(),
                                                                  null);
        Class<XObject> storedClass = null;
        try {
            // If there's a stored class, we merge fields in the stored class with fields in the new class.
            storedClass = (Class<XObject>) loader.asNativeLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            // No class.
        }

        // Get the properties from the stored class.
        final Map<String, Class> storedPropertyMap;
        if (storedClass != null) {
            final XObject xo;
            try {
                xo = storedClass.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(
                    "Failed to create new instance of [" + storedClass.getName() + "]", e);
            }
            storedPropertyMap = xo.getMetaData();
        } else {
            storedPropertyMap = new HashMap<String, Class>();
        }

        // Get the properties from the new class.
        final Map<String, Class> propertyMap = new HashMap<String, Class>();
        for (final String fieldName : xclass.getPropertyNames()) {
            if (fieldName != null) {
                final PropertyClass field = (PropertyClass) xclass.getField(fieldName);
                final BaseProperty prop = field.newProperty();
                propertyMap.put(fieldName, prop.getClass());
            }
        }

        // TODO: For any property which is not in the new class version but is in the old class version,
        //       search for PersistableObjects containing this property, if there are none, remove the property.
        //       This is needed to fully support removing properties from a class. Right now the classes will
        //       grow forever even though it will not affect the XClass presented to the user.

        // TODO: If there is a field with the same name in both the new and old fields and it has the same type,
        //       all objects must be scanned and those with that field must have it removed as it will otherwise
        //       confuse the serializer.

        storedPropertyMap.putAll(propertyMap);
        return new XClassConverter(loader).convert(xclass.getDocumentReference(), storedPropertyMap);
    }


    private static List<XObject> xObjectsToObjects(
        final Map<DocumentReference, List<BaseObject>> xObjects,
        final PersistableClassLoader loader)
    {
        final List<XObject> out = new ArrayList<XObject>();
        final XClassConverter converter = new XClassConverter(loader);

        for (final DocumentReference ref : xObjects.keySet()) {
            final List<BaseObject> list = xObjects.get(ref);

            Class<XObject> cls = null;
            for (final BaseObject obj : list) {
                if (obj != null) {
                    if (cls == null) {
                        cls = converter.convert(obj).getNativeClass();
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
        } else if (reference.getType() != EntityType.DOCUMENT) {
            sb.append(".");
        }
    }

    /**
     * Generate a key for a given document.
     * This implementation attempts to be forward compatable with nested spaces.
     */
    public static String keyGen(final XWikiDocument doc)
    {
        final DocumentReference reference = doc.getDocumentReference();
        final StringBuilder sb = new StringBuilder();
        keyGenStep(reference, sb);
        final String lang = doc.getLanguage();
        if (!"".equals(lang)) {
            sb.append(":").append(lang);
        }
        return sb.toString();
    }

    private static Map<String, PersistableXWikiAttachment>
        xAttachmentsToPersistableAttachments(final List<XWikiAttachment> attachments)
    {
        final Map<String, PersistableXWikiAttachment> out =
            new HashMap<String, PersistableXWikiAttachment>((int)(attachments.size() / 0.75));
        for (final XWikiAttachment attach : attachments) {
            out.put(attach.getFilename(), new PersistableXWikiAttachment(attach));
        }
        return out;
    }

    private static List<XWikiAttachment> persistableAttachmentsToXWikiAttachments(
        final Map<String, PersistableXWikiAttachment> attachments,
        final XWikiDocument attachedTo)
    {
        final List<XWikiAttachment> out = new ArrayList<XWikiAttachment>(attachments.size());
        for (final PersistableXWikiAttachment attach : attachments.values()) {
            out.add(attach.toXWikiAttachment(attachedTo));
        }
        return out;
    }

    private static Map<DocumentReference,
                       List<BaseObject>> objectsToXObjects(final List<XObject> objects,
                                                           final DocumentReference docAttachedTo)
    {
        // Convert the objects.
        final Map<String, List<XObject>> unconverted = mapObjectsByClassName(objects);
        final Map<DocumentReference, List<BaseObject>> out =
            new TreeMap<DocumentReference, List<BaseObject>>();

        for (final Map.Entry<String, List<XObject>> e : unconverted.entrySet()) {
            final DocumentReference classRef =
                JavaClassNameDocumentReferenceSerializer.resolveRef(e.getKey(), null);

            final List<XObject> unconvertedList = e.getValue();
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
     * This implementation will run with a number of map lookups and inserts equal
     * to the number of classes and ArrayList.add's equal to the number of objects
     * if all objects of each class are in sequence in the list, if they are not then
     * it will perform badly but will not fail.
     *
     * @param unmapped a list of Objects.
     * @return a Map of Object lists by class name, ordered the same as the input list.
     */
    private static Map<String, List<XObject>> mapObjectsByClassName(final List<XObject> unmapped)
    {
        final Map<String, List<XObject>> out =
            new TreeMap<String, List<XObject>>();
        boolean outOfOrder = false;
        Class currentClass = null;
        List<XObject> currentList = null;
        for (final XObject obj : unmapped) {
            if (obj.getClass() != currentClass) {
                currentClass = obj.getClass();
                if (out.containsKey(currentClass.getName())) {
                    // This should not happen but if for some reason a stray object gets out of
                    // order, it would be better to take the time to correct it than fail
                    // catistrophicly because we are expecting them to be in order.
                    outOfOrder = true;
                }
                currentList = new ArrayList<XObject>();
                out.put(currentClass.getName(), currentList);
            }
            currentList.add(obj);
        }
        if (outOfOrder) {
            // If this happens then we need to be careful and index over every entry to correct
            // the order.
            for (final List<XObject> list : out.values()) {
                list.clear();
            }
            for (final String className : out.keySet()) {
                for (final XObject obj : unmapped) {
                    out.get(obj.getClass().getName()).add(obj);
                }
            }
        }
        return out;
    }
}
