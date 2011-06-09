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
package com.xpn.xwiki.doc;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.NotPersistent;

public class PersistableXWikiDocument
{
    @NotPersistent
    private boolean isNew;

    /* XWikiDocument fields. */
    private String fullName;

    private String name;

    private String title;

    private String language;

    private String defaultLanguage;

    private int translation;

    private Date date;

    private Date contentUpdateDate;

    private Date creationDate;

    private String author;

    private String contentAuthor;

    private String creator;

    private String space;

    private String content;

    private String version;

    private String customClass;

    private String parent;

    private String xClassXML;

    private int elements;

    private String defaultTemplate;

    private String validationScript;

    private String comment;

    private boolean isMinorEdit;

    private String syntaxId;

    private boolean hidden;

    /* Objects. */
    


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

        //cloneXObjects(document);
        //cloneAttachments(document);
    }
}
