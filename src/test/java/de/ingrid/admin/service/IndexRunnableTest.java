package de.ingrid.admin.service;

import java.io.File;

import junit.framework.TestCase;

import org.apache.lucene.index.IndexReader;

import de.ingrid.admin.TestUtils;
import de.ingrid.admin.search.IndexRunnable;
import de.ingrid.admin.search.IngridIndexSearcher;
import de.ingrid.admin.search.QueryParsers;
import de.ingrid.utils.PlugDescription;

public class IndexRunnableTest extends TestCase {

    private IndexRunnable _indexRunnable;

    private PlugDescription _plugDescription;

    private File _file;

    @Override
    protected void setUp() {
        _file = new File(System.getProperty("java.io.tmpdir"), this.getClass().getName());
        TestUtils.delete(_file);
        assertTrue(_file.mkdirs());
        _plugDescription = new PlugDescription();
        _plugDescription.setWorkinDirectory(_file);

        IngridIndexSearcher searcher = new IngridIndexSearcher(new QueryParsers());
        _indexRunnable = new IndexRunnable(searcher);
        _indexRunnable.configure(_plugDescription);
        DummyProducer dummyProducer = new DummyProducer();
        dummyProducer.configure(_plugDescription);
        _indexRunnable.setDocumentProducer(dummyProducer);
        _indexRunnable.run();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            fail();
        }
    }

    @Override
    protected void tearDown() {
        TestUtils.delete(_file);
    }

    public void testIndexExists() {
        final File file = new File(_file, "index");
        assertTrue(file.exists());
        assertTrue(file.list().length > 0);
    }

    public void testReadIndex() throws Exception {
        final IndexReader reader = IndexReader.open(new File(_file, "index"));

        assertEquals(4, reader.maxDoc());

        assertEquals("Max", reader.document(0).get("first"));
        assertEquals("08.12.1988", reader.document(0).get("birthdate"));

        assertEquals("Marko", reader.document(1).get("first"));
        assertEquals("male", reader.document(1).get("gender"));

        assertEquals("Andreas", reader.document(2).get("first"));
        assertEquals("Kuester", reader.document(2).get("last"));

        assertEquals("Frank", reader.document(3).get("first"));
        assertNull(reader.document(3).get("nick"));
    }
}
