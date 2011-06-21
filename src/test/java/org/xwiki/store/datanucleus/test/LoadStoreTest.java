package org.xwiki.store.datanucleus.test;

import java.util.ArrayList;
import java.util.List;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.XWikiContext;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.test.AbstractComponentTestCase;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.store.EntityProvider;
import com.xpn.xwiki.store.datanucleus.DataNucleusStore;
import com.xpn.xwiki.store.datanucleus.PersistableXWikiDocument;
import com.xpn.xwiki.store.XWikiStoreInterface;

/*import org.xwiki.context.Execution;

import java.util.Enumeration;
import java.net.URL;
import org.apache.commons.io.IOUtils;*/

public class LoadStoreTest extends AbstractComponentTestCase
{
    private static PersistenceManagerFactory FACTORY;

    private static XWikiStoreInterface STORE;

    private PersistenceManager manager;

    @BeforeClass
    public static void init() throws Exception
    {
        STORE = new DataNucleusStore();
        FACTORY = JDOHelper.getPersistenceManagerFactory("Test");
    }

    @Before
    public void setUp() throws Exception
    {
        this.manager = FACTORY.getPersistenceManager();
    }

    @After
    public void tearDown() throws Exception
    {
        this.manager.close();
    }

    @Test
    public void testLoadStoreXWikiDocument() throws Exception
    {
        final ClassLoader classLoader = this.getClass().getClassLoader();

        new ComponentAnnotationLoader().initialize(this.getComponentManager(), classLoader);
        Utils.setComponentManager(this.getComponentManager());

        final XWikiDocument xwikiPrefs = new XWikiDocument(null);
        xwikiPrefs.fromXML(classLoader.getResourceAsStream("XWikiPreferences.xml"), false);

        final XWikiDocument globalRights = new XWikiDocument(null);
        globalRights.fromXML(classLoader.getResourceAsStream("XWikiGlobalRights.xml"), false);

        final EntityProvider<XWikiDocument, DocumentReference> docProvider =
            new StubDocumentProvider(new ArrayList<XWikiDocument>(){{
                add(xwikiPrefs);
                add(globalRights);
            }});

        STORE.saveXWikiDoc(globalRights, null);
        STORE.saveXWikiDoc(xwikiPrefs, null);

        Assert.assertEquals(0, STORE.getTranslationList(xwikiPrefs, null).size());

        // Query

        // This doesn't work because JPQL is not supported (yet...)
        /*Collection c = (Collection)
            this.manager.newQuery("javax.jdo.query.JPQL", "SELECT doc FROM " + gclass.getName()
                                + " as doc WHERE doc.title = 'GroovyClass'").execute();*/

        // This doesn't work because querying against nested objects is not supported. (yet)
        /*Collection c = (Collection)
            this.manager.newQuery("SELECT FROM " + gclass.getName()
                                  + " WHERE innerDocument.title == 'GroovyClass#2'").execute();*/
/*
        Collection c = (Collection)
            this.manager.newQuery("SELECT FROM " + gclass.getName()
                                  + " WHERE title == 'GroovyClass'").execute();

        Assert.assertEquals(c.size(), 1);
        //System.out.println(c.iterator().next().toString());
        Assert.assertEquals("Me | Not indexed | Generated persistance capable class! | GroovyClass | "
                              +"MeMeMe | ni | I am a nested object, yay! | GroovyClass#2 | null",
                            c.iterator().next().toString());*/
    }

    private static class StubDocumentProvider implements EntityProvider<XWikiDocument, DocumentReference>
    {
        private final List<XWikiDocument> toProvide;

        public StubDocumentProvider(final List<XWikiDocument> toProvide)
        {
            this.toProvide = toProvide;
        }

        public XWikiDocument get(final DocumentReference docRef)
        {
            for (XWikiDocument doc : this.toProvide) {
                if (doc.getDocumentReference().getName().equals(docRef.getName())) {
                    return doc;
                }
            }
            return null;
        }

        public List<XWikiDocument> get(final List<DocumentReference> refs)
        {
            throw new RuntimeException("Not implemented");
        }
    }
}
