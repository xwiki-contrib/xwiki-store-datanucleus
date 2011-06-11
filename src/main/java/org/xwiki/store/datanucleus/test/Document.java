package org.xwiki.store.datanucleus.test;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Document
{
    @Index
    private String author;

    private String notIndexed;

    private String alsoNotIndexed;

    @Index
    private String content;

    @PrimaryKey
    @Index
    private String title;

    public Document(String title, String author, String content, final String notIndexed)
    {
        super();
        this.title = title;
        this.author = author;
        this.content = content;
        this.notIndexed = notIndexed;
        this.alsoNotIndexed = "Hi";
    }

    public String getAuthor()
    {
        return author;
    }

    public String getContent()
    {
        return content;
    }

    public String getTitle()
    {
        return title;
    }

    public String toString()
    {
        return this.title + "  " + this.author + "  " + this.content + "  " + this.notIndexed;
    }
}
