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
import java.util.TreeMap;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.NotPersistent;
import org.xwiki.store.datanucleus.internal.JavaClassNameDocumentReferenceSerializer;
import org.xwiki.model.reference.DocumentReference;

@PersistenceCapable
public class PersistableXWikiDocument
{
    @NotPersistent
    private boolean isNew;

    /* XWikiDocument fields. */
    @Index
    private String fullName;

    @Index
    private String name;

    @Index
    private String title;

    @Index
    private String language;

    @Index
    private String defaultLanguage;

    @Index
    private int translation;

    @Index
    private Date date;

    @Index
    private Date contentUpdateDate;

    @Index
    private Date creationDate;

    @Index
    private String author;

    @Index
    private String contentAuthor;

    @Index
    private String creator;

    @Index
    private String space;

    @Index
    private String content;

    @Index
    private String version;

    @Index
    private String customClass;

    @Index
    private String parent;

    @Index
    private String xClassXML;

    @Index
    private int elements;

    @Index
    private String defaultTemplate;

    @Index
    private String validationScript;

    @Index
    private String comment;

    @Index
    private boolean isMinorEdit;

    @Index
    private String syntaxId;

    @Index
    private boolean hidden;

    /** The wiki where this document belongs. */
    private String wiki;

    /**
     * The class defined in this document.
     * This is needed so we can convert objects of this document's class.
     */
    private Class xwikiClass;

    /**
     * Objects.
     * The class of each object is determinable by object.getClass() and the object index
     * in the list corrisponding to it's class is determined by it's placement on the list.
     * All objects of the same class *should* be consecutive on this list but the code will recover from
     * an out of order situation.
     */
    private List<Object> objects;

    /**
     * XML representations of the classes of the above objects.
     * These are needed to convert the Objects back to XWiki objects.
     * The order of these classes determines the order in which the classes will be placed in the map.
     */
    private List<String> objectClassesXML;

    public PersistableXWikiDocument(final XWikiDocument toClone)
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
        this.xClassXML = toClone.getXClassXML();
        this.elements = toClone.getElements();
        this.defaultTemplate = toClone.getDefaultTemplate();
        this.validationScript = toClone.getValidationScript();
        this.comment = toClone.getComment();
        this.isMinorEdit = toClone.isMinorEdit();
        this.syntaxId = toClone.getSyntaxId();
        this.hidden = toClone.isHidden();

        this.wiki = toClone.getDatabase();

        // Get the XClass, if it doesn't exist then it's created so having no fields = nonexistance.
        final BaseClass baseClass = toClone.getXClass();
        if (baseClass.getPropertyList().size() > 0) {
            this.xwikiClass = XClassConverter.convertClass(toClone.getXClass());
        }

        this.objects = cloneXObjects(toClone);
        //cloneAttachments(document);
    }

    private static List<Object> cloneXObjects(final XWikiDocument toCloneXObjectsFrom)
    {
        final Map<DocumentReference, List<BaseObject>> xwikiObjects = toCloneXObjectsFrom.getXObjects();
        final List<Object> out = new ArrayList<Object>();
        for (List<BaseObject> list : xwikiObjects.values()) {
            for (BaseObject obj : list) {
                out.add(XObjectConverter.convertFromXObject(obj));
            }
        }
        return out;
    }

    public XWikiDocument toXWikiDocument()
    {
        final XWikiDocument out = new XWikiDocument(null);

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

        out.setXObjects(objectsToXObjects(this.objects, this.objectClassesXML, out.getDocumentReference()));

        return out;
    }

    private static Map<DocumentReference,
                       List<BaseObject>> objectsToXObjects(final List<Object> objects,
                                                           final List<String> objectClassesXML,
                                                           final DocumentReference docAttachedTo)
    {
        // Get the class for each object.
        final List<BaseClass> xwikiClasses = new ArrayList<BaseClass>();
        for (final String xml : objectClassesXML) {
            final BaseClass xwikiClass = new BaseClass();
            try {
                xwikiClass.fromXML(xml);
            } catch (XWikiException e) {
                throw new RuntimeException("Failed to convert xwiki class from xml", e);
            }
            xwikiClasses.add(xwikiClass);
        }

        // Convert the objects.
        final Map<String, List<Object>> unconverted = mapObjectsByClassName(objects);
        final Map<DocumentReference, List<BaseObject>> out =
            new TreeMap<DocumentReference, List<BaseObject>>();
        for (final BaseClass xwikiClass : xwikiClasses) {
            final DocumentReference docRef = xwikiClass.getDocumentReference();
            final String className = JavaClassNameDocumentReferenceSerializer.serializeRef(docRef, null);
            final List<BaseObject> outList = new ArrayList<BaseObject>();
            final List<Object> unconvertedList = unconverted.get(className);
            for (int i = 0; i < unconvertedList.size(); i++) {
                final BaseObject xwikiObj =
                    XObjectConverter.convertToXObject(unconvertedList.get(i), xwikiClass);
                xwikiObj.setDocumentReference(docAttachedTo);
                xwikiObj.setNumber(i);
                outList.add(xwikiObj);
            }
            out.put(docRef, outList);
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
    private static Map<String, List<Object>> mapObjectsByClassName(final List<Object> unmapped)
    {
        final Map<String, List<Object>> out = new TreeMap<String, List<Object>>();
        boolean outOfOrder = false;
        Class currentClass = null;
        List<Object> currentList = null;
        for (final Object obj : unmapped) {
            if (obj.getClass() != currentClass) {
                currentClass = obj.getClass();
                if (out.containsKey(currentClass.getName())) {
                    // This should not happen but if for some reason a stray object gets out of order,
                    // it would be better to take the time to correct it than fail catistrophicly because
                    // we are expecting them to be in order.
                    outOfOrder = true;
                }
                currentList = new ArrayList<Object>();
                out.put(currentClass.getName(), currentList);
            }
            currentList.add(obj);
        }
        if (outOfOrder) {
            // If this happens then we need to be careful and index over every entry to correct the order.
            for (final List<Object> list : out.values()) {
                list.clear();
            }
            for (final String className : out.keySet()) {
                for (final Object obj : unmapped) {
                    out.get(obj.getClass().getName()).add(obj);
                }
            }
        }
        return out;
    }
}
