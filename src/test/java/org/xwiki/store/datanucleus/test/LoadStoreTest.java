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
package org.xwiki.store.datanucleus.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiConfig;
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
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
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
    private XWikiStoreInterface store;

    @BeforeClass
    public static void init() throws Exception
    {
        final LoadStoreTest lst = new LoadStoreTest();
        final ClassLoader classLoader = lst.getClass().getClassLoader();

        new ComponentAnnotationLoader().initialize(lst.getComponentManager(), classLoader);
        Utils.setComponentManager(lst.getComponentManager());

        final XWiki xwiki = new XWiki();
        xwiki.setConfig(new XWikiConfig() {
            final Map<String, String> props = (new HashMap<String, String>() {{
                put("xwiki.store.main.hint", "datanucleus");
                put("xwiki.work.dir",        System.getProperty("java.io.tmpdir"));
            }});
            public String getProperty(String key, String defaultValue)
            {
                return (this.props.get(key) != null) ? this.props.get(key) : defaultValue;
            }
        });
        final XWikiContext xcontext = new XWikiContext();
        xcontext.setWiki(xwiki);
        final ExecutionContext context = new ExecutionContext();
        context.setProperty("xwikicontext", xcontext);
        lst.getComponentManager().lookup(Execution.class).setContext(context);
    }

    @Before
    public void setUp() throws Exception
    {
        this.store = Utils.getComponent(XWikiStoreInterface.class, "datanucleus");
    }

    @Test
    public void testLoadStoreXWikiDocument() throws Exception
    {
        final ClassLoader classLoader = this.getClass().getClassLoader();
        final XWikiDocument xwikiPrefs = new XWikiDocument(null);
        xwikiPrefs.fromXML(classLoader.getResourceAsStream("XWikiPreferences.xml"), false);

        final XWikiDocument globalRights = new XWikiDocument(null);
        globalRights.fromXML(classLoader.getResourceAsStream("XWikiGlobalRights.xml"), false);

        this.store.saveXWikiDoc(globalRights, null);
        final XWikiDocument globalRights2 = new XWikiDocument(globalRights.getDocumentReference());
        this.store.loadXWikiDoc(globalRights2, null);

        Assert.assertTrue(!globalRights2.isNew());

        this.store.saveXWikiDoc(xwikiPrefs, null);

        Assert.assertEquals(0, store.getTranslationList(xwikiPrefs, null).size());

        // Query



        /*Collection c = (Collection)
            this.manager.newQuery("javax.jdo.query.JPQL", "SELECT doc FROM "
                                  + PersistableXWikiDocument.class.getName() + " as doc WHERE "
                                  + "doc.fullName = 'XWiki.XWikiPreferences'").execute();*/


/*
        Assert.assertEquals(c.size(), 1);
        //System.out.println(c.iterator().next().toString());
        Assert.assertEquals("Me | Not indexed | Generated persistance capable class! | GroovyClass | "
                              +"MeMeMe | ni | I am a nested object, yay! | GroovyClass#2 | null",
                            c.iterator().next().toString());*/
    }

    @Test
    public void testSimpleQuery() throws Exception
    {
        final Collection c = this.store.getQueryManager().createQuery(
            "SELECT FROM com.xpn.xwiki.store.datanucleus.PersistableXWikiDocument WHERE "
          + "fullName == \"XWiki.XWikiPreferences\"", "jdoql").execute();
        Assert.assertTrue(c.size() == 1);
    }

    @Test
    public void testQueryOnObject() throws Exception
    {
        final Collection c = this.store.getQueryManager().createQuery(
            "SELECT FROM com.xpn.xwiki.store.datanucleus.PersistableXWikiDocument WHERE "
          + "objects.contains(obj) && "
          + "obj.colorTheme == \"ColorThemes.DefaultColorTheme\"", "jdoql").execute();
        Assert.assertTrue(c.size() == 1);
        final PersistableXWikiDocument xwikiPrefs = (PersistableXWikiDocument) c.toArray()[0];
        final XWikiDocument doc = xwikiPrefs.toXWikiDocument(null);
        Assert.assertEquals("XWiki.XWikiPreferences", doc.getFullName());
        Assert.assertEquals("{{include document=\"XWiki.AdminSheet\" /}}", doc.getContent());
        Assert.assertEquals("admin,edit,undelete",
                            doc.getObject("xwiki:XWiki.XWikiGlobalRights").getStringValue("levels"));
    }

    @Test
    public void testJpqlQuery() throws Exception
    {
        final Collection c = (Collection) this.store.getQueryManager().createQuery(
            "SELECT doc.content FROM com.xpn.xwiki.store.datanucleus.PersistableXWikiDocument "
          + " as doc WHERE doc.fullName = 'XWiki.XWikiPreferences'", "jpql").execute();
        Assert.assertEquals(1, c.size());
        Assert.assertEquals("{{include document=\"XWiki.AdminSheet\" /}}", c.toArray()[0]);
    }

    @Test
    @org.junit.Ignore("TODO: figure out how we can do jpql queries joining objects.")
    public void testJpqlQueryOnObject() throws Exception
    {
        final Collection c = (Collection) this.store.getQueryManager().createQuery(
            "SELECT doc FROM com.xpn.xwiki.store.datanucleus.PersistableXWikiDocument AS doc "
          + "INNER JOIN doc.objects AS obj WHERE obj.levels = 'admin,edit,undelete'",
        "jpql").execute();

        Assert.assertEquals(1, c.size());
        Assert.assertEquals("XWiki.XWikiPreferences", c.toArray()[0]);
    }

    @Test
    public void testJpqlQueryOnObjectAlone() throws Exception
    {
        final Collection c = (Collection) this.store.getQueryManager().createQuery(
            "SELECT obj.identity FROM xwiki.XWiki.XWikiGlobalRights AS obj WHERE "
          + "obj.levels = 'admin,edit,undelete'", "jpql").execute();

        Assert.assertEquals(1, c.size());
        Assert.assertEquals("xwiki:XWiki.XWikiPreferences.objects[0]", c.toArray()[0]);
    }
}
