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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;

import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.objects.classes.StringClass;
import com.xpn.xwiki.objects.classes.NumberClass;
import com.xpn.xwiki.store.XWikiAttachmentStoreInterface;
import com.xpn.xwiki.store.XWikiStoreInterface;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiConfig;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.servlet.ServletContext;
import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.environment.Environment;
import org.xwiki.environment.internal.ServletEnvironment;
import org.xwiki.test.AbstractComponentTestCase;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument;
import org.xwiki.store.attachments.adapter.internal.FilesystemDataNucleusAttachmentStoreAdapter;


public class LoadStoreTest
{
    private static XWikiContext XCONTEXT;

    private XWikiStoreInterface store;

    private XWikiContext xcontext;

    @BeforeClass
    public static void init() throws Exception
    {
        final AbstractComponentTestCase actc = new AbstractComponentTestCase(){};
        final ClassLoader classLoader = actc.getClass().getClassLoader();

        new ComponentAnnotationLoader().initialize(actc.getComponentManager(), classLoader);
        Utils.setComponentManager(actc.getComponentManager());

        final ServletEnvironment sev = (ServletEnvironment)
            actc.getComponentManager().getInstance(Environment.class);
        final ServletContext sc = actc.getMockery().mock(ServletContext.class);
        sev.setServletContext(sc);
        actc.getMockery().checking(new Expectations() {{
            allowing(sc).getAttribute("javax.servlet.context.tempdir");
                will(returnValue(new File(System.getProperty("java.io.tmpdir"))));
            allowing(sc).getResource("/WEB-INF/xwiki.properties");
                will(returnValue(null));
        }});

        final XWiki xwiki = new XWiki();
        xwiki.setStore(Utils.getComponent(XWikiStoreInterface.class, "datanucleus"));
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

        XCONTEXT = new XWikiContext();
        XCONTEXT.setWiki(xwiki);
        final ExecutionContext context = new ExecutionContext();
        context.setProperty("xwikicontext", XCONTEXT);
        final Execution exec = actc.getComponentManager().getInstance(Execution.class);
        exec.setContext(context);
        xwiki.setAttachmentStore((XWikiAttachmentStoreInterface)
            actc.getComponentManager().getInstance(XWikiAttachmentStoreInterface.class, "file"));
    }

    @Before
    public void setUp() throws Exception
    {
        this.store = Utils.getComponent(XWikiStoreInterface.class, "datanucleus");
        this.xcontext = (XWikiContext) Utils.getComponent(Execution.class).getContext().getProperty("xwikicontext");
        this.xcontext.getWiki().setStore(this.store);
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
    }

    @Test
    public void testXWikiDocumentClass() throws Exception
    {
        final XWikiDocument xdoc = new XWikiDocument(new DocumentReference("xwiki", "Test", "TestClass"));
        this.store.saveXWikiDoc(xdoc, null);

        Assert.assertFalse(this.store.getClassList(null).contains("xwiki:Test.TestClass"));

        final BaseClass xclass = xdoc.getXClass();
        xclass.addField("string", new StringClass());
        this.store.saveXWikiDoc(xdoc, null);
        Assert.assertTrue(this.store.getClassList(null).contains("xwiki:Test.TestClass"));

        xclass.addField("number", new NumberClass());
        this.store.saveXWikiDoc(xdoc, null);

        Collection c;

        c = (Collection) this.store.getQueryManager().createQuery(
            "SELECT cls.bytes FROM "
          + "org.xwiki.store.objects.PersistableClass AS cls "
          + "WHERE cls.name = 'xwiki.Test.TestClass'", "jpql").execute();

        Assert.assertEquals(1, c.size());
        final byte[] byteCode = (byte[]) c.toArray()[0];
        Class cls = (new ClassLoader() {{
            this.defineClass("xwiki.Test.TestClass", byteCode, 0, byteCode.length);
        }}).loadClass("xwiki.Test.TestClass");
        cls.getDeclaredField("string");
        cls.getDeclaredField("number");

        final BaseObject obj =
            xdoc.newXObject(new DocumentReference("xwiki", "Test", "TestClass"), this.xcontext);
        obj.set("string", "Hello World", this.xcontext);
        obj.set("number", 123, this.xcontext);
        this.store.saveXWikiDoc(xdoc, null);


        c = (Collection) this.store.getQueryManager().createQuery(
            "SELECT obj FROM "
          + "xwiki.Test.TestClass AS obj "
          + "WHERE obj.id = 'xwiki:Test.TestClass.objects[0]'", "jpql").execute();
        Assert.assertEquals(1, c.size());
        cls = c.toArray()[0].getClass();
        cls.getDeclaredField("string");
        cls.getDeclaredField("number");
    }

    @Test
    public void testSimpleQuery() throws Exception
    {
        final Collection c = this.store.getQueryManager().createQuery(
            "SELECT FROM org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument WHERE "
          + "fullName == \"XWiki.XWikiPreferences\"", "jdoql").execute();
        Assert.assertTrue(c.size() == 1);
    }

    @Test
    public void testQueryOnObject() throws Exception
    {
        final Collection c = this.store.getQueryManager().createQuery(
            "SELECT FROM org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument WHERE "
          + "objects.contains(obj) && "
          + "obj.colorTheme == \"ColorThemes.DefaultColorTheme\"", "jdoql").execute();
        Assert.assertEquals(1, c.size());
        final PersistableXWikiDocument xwikiPrefs = (PersistableXWikiDocument) c.toArray()[0];
        final XWikiDocument doc = xwikiPrefs.toXWikiDocument(null);
        Assert.assertEquals("XWiki.XWikiPreferences", doc.getFullName());
        Assert.assertEquals("{{include document=\"XWiki.AdminSheet\" /}}", doc.getContent());
        Assert.assertEquals("admin,edit,undelete",
                            doc.getObject("xwiki:XWiki.XWikiGlobalRights").getStringValue("levels"));
    }

    @Test
    public void testQueryForObjectOfClass() throws Exception
    {
        final Collection c = this.store.getQueryManager().createQuery(
            "SELECT FROM org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument WHERE "
          + "objects.contains(obj) "
          + "&& obj.className == \"xwiki.XWiki.XWikiGlobalRights\" "
          + "&& obj.levels == \"admin,edit,undelete\"", "jdoql").execute();
        Assert.assertEquals(1, c.size());
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
            "SELECT doc.content FROM "
          + "org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument AS doc "
          + "WHERE doc.fullName = 'XWiki.XWikiPreferences'", "jpql").execute();
        Assert.assertEquals(1, c.size());
        Assert.assertEquals("{{include document=\"XWiki.AdminSheet\" /}}", c.toArray()[0]);
    }

    // TODO make this work.
    //@Test
    public void testJpqlQueryOnObject() throws Exception
    {
        final Collection c = (Collection) this.store.getQueryManager().createQuery(
            "SELECT doc.fullName "
          + "FROM org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument AS doc, "
          + "IN(doc.objects) AS obj "
          + "WHERE obj.className = 'xwiki.XWiki.XWikiGlobalRights' "
          + "AND obj.levels = 'admin,edit,undelete' "
          + "VARIABLES ", "jpql").execute();

        Assert.assertEquals(1, c.size());
        Assert.assertEquals("XWiki.XWikiPreferences", c.toArray()[0]);
    }

    //@Test
    public void testJpqlQueryOnObject2() throws Exception
    {
        final Collection c = (Collection) this.store.getQueryManager().createQuery(
            "SELECT doc.fullName "
          + "FROM org.xwiki.store.legacy.internal.datanucleus.PersistableXWikiDocument AS doc, "
          + "xwiki.XWiki.XWikiGlobalRights AS obj "
          + "WHERE obj MEMBER OF doc.objects "
          + "AND obj.levels = 'admin,edit,undelete' ", "jpql").execute();

        Assert.assertEquals(1, c.size());
        Assert.assertEquals("XWiki.XWikiPreferences", c.toArray()[0]);
    }

    @Test
    public void testJpqlQueryOnObjectAlone() throws Exception
    {
        final Collection c = (Collection) this.store.getQueryManager().createQuery(
            "SELECT obj.className "
          + "FROM xwiki.XWiki.XWikiGlobalRights AS obj "
          + "WHERE obj.levels = 'admin,edit,undelete'", "jpql").execute();
        Assert.assertEquals(1, c.size());
        Assert.assertEquals("xwiki.XWiki.XWikiGlobalRights", c.toArray()[0]);
    }

    @Test
    public void testAttachment() throws Exception
    {
        final String testContent = "This is a test";
        final XWikiContext xc = (XWikiContext)
            Utils.getComponent(Execution.class).getContext().getProperty("xwikicontext");
        final DocumentReference ref = new DocumentReference("xwiki", "XWiki", "XWikiDocument");
        final XWikiDocument testDoc = new XWikiDocument(ref);
        testDoc.addAttachment("file.txt", testContent.getBytes("UTF-8"), xc);

        this.store.saveXWikiDoc(testDoc, null);

        final XWikiDocument testDoc2 = new XWikiDocument(ref);
        this.store.loadXWikiDoc(testDoc2, null);

        final XWikiAttachment attach = testDoc2.getAttachment("file.txt");
        byte[] content = attach.getContent(xc);

        Assert.assertTrue(new String(content, "UTF-8").equals(testContent));
    }

    @Test
    public void testModifyClass() throws Exception
    {
        // Create class with 1 field
        final DocumentReference ref = new DocumentReference("xwiki", "Main", "MyClass");
        XWikiDocument testDoc = new XWikiDocument(ref);
        testDoc.getXClass().addField("str1", new StringClass());
        this.store.saveXWikiDoc(testDoc, null);

        // Create object of this class.
        testDoc = new XWikiDocument(ref);
        this.store.loadXWikiDoc(testDoc, null);
        BaseObject obj = testDoc.newXObject(ref, XCONTEXT);
        obj.setStringValue("str1", "test");
        this.store.saveXWikiDoc(testDoc, null);

        // Get the object and make sure it's correct.
        Collection queryResult = (Collection) this.store.getQueryManager().createQuery(
            "SELECT obj "
          + "FROM xwiki.Main.MyClass AS obj "
          + "WHERE obj.str1 = 'test'", "jpql").execute();
        Assert.assertEquals(1, queryResult.size());
        final Class class1 = queryResult.toArray()[0].getClass();
        Assert.assertNotNull(class1.getDeclaredField("str1"));

        // Add another field.
        testDoc = new XWikiDocument(ref);
        this.store.loadXWikiDoc(testDoc, null);
        testDoc.getXClass().addField("str2", new StringClass());
        this.store.saveXWikiDoc(testDoc, null);

        // Create another object.
        testDoc = new XWikiDocument(ref);
        this.store.loadXWikiDoc(testDoc, null);
        obj = testDoc.newXObject(ref, XCONTEXT);
        obj.setStringValue("str1", "hello");
        obj.setStringValue("str2", "world");
        this.store.saveXWikiDoc(testDoc, null);

        // Query for the object.
        queryResult = (Collection) this.store.getQueryManager().createQuery(
            "SELECT obj "
          + "FROM xwiki.Main.MyClass AS obj "
          + "WHERE obj.str1 = 'hello'", "jpql").execute();
        Assert.assertEquals(1, queryResult.size());
        final XObject nativeObj = (XObject) queryResult.toArray()[0];
        final Class class2 = nativeObj.getClass();
        Assert.assertTrue(class1 != class2);
        Assert.assertNotNull(class2.getDeclaredField("str1"));
        Assert.assertNotNull(class2.getDeclaredField("str2"));
        Assert.assertEquals("hello", nativeObj.getFields().get("str1"));
        Assert.assertEquals("world", nativeObj.getFields().get("str2"));

        // Load the object.
        testDoc = new XWikiDocument(ref);
        this.store.loadXWikiDoc(testDoc, null);

        obj = testDoc.getXObject(ref, 0);
        Assert.assertEquals("test", obj.getStringValue("str1"));
        Assert.assertEquals("", obj.getStringValue("str2"));
        Assert.assertEquals("", obj.getStringValue("nosuchfield"));

        obj = testDoc.getXObject(ref, 1);
        Assert.assertEquals("hello", obj.getStringValue("str1"));
        Assert.assertEquals("world", obj.getStringValue("str2"));
    }
}
