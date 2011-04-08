package org.xwiki.store.datanucleus.test;

import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import org.apache.cassandra.service.EmbeddedCassandraService;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;

public class AppTest
{
    private static PersistenceManagerFactory FACTORY;

    private PersistenceManager manager;

    @BeforeClass
    public static void init() throws Exception
    {
        System.setProperty("log4j.configuration", "log4j.properties");
        System.setProperty("cassandra.config", "cassandra.yaml");
        new EmbeddedCassandraService().start();
        Thread.sleep(2000);

        FACTORY = JDOHelper.getPersistenceManagerFactory("Test");

        final PersistenceManager pm = FACTORY.getPersistenceManager();

        // Store some documents
        pm.makePersistent(new Document("Title1", "Alice", "ContentA", "Hello"));
        pm.makePersistent(new Document("Title2", "Bob", "ContentA", "Hi"));
        pm.makePersistent(new Document("Title3", "Charlie", "ContentB", "Goodbye"));
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
    public void testSearchByTitle() throws Exception
    {
        searchDocuments("SELECT FROM org.xwiki.store.datanucleus.test.Document WHERE title == \"Title1\"");
    }

    @Test
    public void testSearchByAuthor() throws Exception
    {
        searchDocuments("SELECT FROM org.xwiki.store.datanucleus.test.Document WHERE author == \"Alice\"");
    }

    @Test
    public void testSearchByContent() throws Exception
    {
        searchDocuments("SELECT FROM org.xwiki.store.datanucleus.test.Document WHERE content == \"ContentA\"");
    }

    @Test
    public void testSearchByContentAndNonIndexed() throws Exception
    {
        searchDocuments("SELECT FROM org.xwiki.store.datanucleus.test.Document WHERE content == \"ContentA\" && notIndexed == \"Hello\"");
    }

    private void searchDocuments(final String jdoqlQuery)
    {
        System.out.println("Searching with: " + jdoqlQuery);
        for (Document doc : (Collection<Document>) manager.newQuery(jdoqlQuery).execute()) {
            System.out.println(doc.toString());
        }
        System.out.println();
    }
}
