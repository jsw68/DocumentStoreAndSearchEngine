package edu.yu.cs.com1320.project.stage6;

import org.junit.jupiter.api.Test;  
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.lang.String;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
// import java.lang.reflect.Array;

import org.junit.jupiter.api.BeforeEach;  
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import edu.yu.cs.com1320.project.stage6.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.stage6.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage6.impl.DocumentStoreImpl;

public class DocumentStoreTest {
    DocumentStoreImpl store;
    byte[] binaryData1;
    byte[] binaryData2;
    private InputStream input;
    @BeforeEach
    public void Initialize() throws IOException{
        this.binaryData1 = "myString".getBytes();
        this.binaryData2 = "myString2".getBytes();
        this.store = new DocumentStoreImpl();
        this.input = new ByteArrayInputStream("tester".getBytes());
        InputStream is = new ByteArrayInputStream("myString".getBytes());
        this.store.put(is, URI.create("test"), DocumentStore.DocumentFormat.TXT);
        InputStream is2 = new ByteArrayInputStream( "myString2".getBytes() );
        this.store.put(is2, URI.create("test2"), DocumentStore.DocumentFormat.BINARY);
    }

    @Test
    public void getTest() throws IOException{
        assertEquals("myString", this.store.get(URI.create("test")).getDocumentTxt());
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), this.binaryData2));
        
    }
    @Test
    public void setMetadataTest() throws IOException{
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value"), null);
        assertEquals("value", this.store.getMetadata(URI.create("test"), "key"));
        assertEquals(this.store.setMetadata(URI.create("test"), "key2", "value2"), null);
        assertEquals("value2", this.store.getMetadata(URI.create("test"), "key2"));
        assertEquals(this.store.setMetadata(URI.create("test2"), "key", "value"), null);
        assertEquals("value", this.store.getMetadata(URI.create("test"), "key"));
        assertEquals(this.store.setMetadata(URI.create("test2"), "key2", "value2"), null);
        assertEquals("value2", this.store.setMetadata(URI.create("test"), "key2", "value3"));
        assertEquals("value", this.store.setMetadata(URI.create("test2"), "key", "value3"));
    }
    @Test
    public void deleteTest() throws IOException{

        assertEquals(true, this.store.delete(URI.create("test")));
        assertEquals(false, this.store.delete(URI.create("nothignm")));
        assertEquals(true, this.store.delete(URI.create("test2")));
        assertEquals(false, this.store.delete(URI.create("tenogbst2")));
    }

    @Test
    public void nullTest() throws IOException{
        // setMetadataNulls
        assertThrows(IllegalArgumentException.class, () -> this.store.setMetadata(null, "key", "value"));
        assertThrows(IllegalArgumentException.class, () -> this.store.setMetadata(URI.create(""), "key", "value"));
        // assertThrows(IllegalArgumentException.class, () -> this.store.setMetadata(URI.create("nothing"), "key", "value"));
        assertThrows(IllegalArgumentException.class, () -> this.store.setMetadata(URI.create("nothing"), "", "value"));
        assertThrows(IllegalArgumentException.class, () -> this.store.setMetadata(URI.create("nothing"), null, "value"));
        // getMetadataNulls
        assertThrows(IllegalArgumentException.class, () -> this.store.getMetadata(null, "key"));
        assertThrows(IllegalArgumentException.class, () -> this.store.getMetadata(URI.create(""), "key"));
        // assertThrows(IllegalArgumentException.class, () -> this.store.getMetadata(URI.create("nothing"), "key"));
        assertThrows(IllegalArgumentException.class, () -> this.store.getMetadata(URI.create("nothing"), ""));
        assertThrows(IllegalArgumentException.class, () -> this.store.getMetadata(URI.create("nothing"), null));
        // putNulls
        assertThrows(IllegalArgumentException.class, () -> this.store.put(this.input, null, DocumentStore.DocumentFormat.TXT));
        assertThrows(IllegalArgumentException.class, () -> this.store.put(this.input, URI.create(""), DocumentStore.DocumentFormat.TXT));
        assertThrows(IllegalArgumentException.class, () -> this.store.put(this.input, URI.create("nothing"), null));
    }

    @Test
    public void undoMetadataTest() throws IOException{
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value1"), null);
        assertEquals("value1", this.store.getMetadata(URI.create("test"), "key"));
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value2"), "value1");
        assertEquals("value2", this.store.getMetadata(URI.create("test"), "key"));
        this.store.undo();
        assertEquals("value1", this.store.getMetadata(URI.create("test"), "key"));
        this.store.undo();
        assertEquals(null, this.store.getMetadata(URI.create("test"), "key"));
    }
    @Test
    public void undoDeleteTest() throws IOException{
        assertEquals("myString", this.store.get(URI.create("test")).getDocumentTxt());
        assertTrue(this.store.delete(URI.create("test")));
        assertEquals(null, this.store.get(URI.create("test")));
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), this.binaryData2));
        assertTrue(this.store.delete(URI.create("test2")));
        assertEquals(null, this.store.get(URI.create("test2")));
        this.store.undo();
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), this.binaryData2));
        assertEquals(null, this.store.get(URI.create("test")));
        this.store.undo();
        assertEquals("myString", this.store.get(URI.create("test")).getDocumentTxt());
    }
    @Test
    public void undoPutTest() throws IOException{
        assertEquals("myString", this.store.get(URI.create("test")).getDocumentTxt());
        InputStream is = new ByteArrayInputStream("newDoc".getBytes());
        this.store.put(is, URI.create("test"), DocumentFormat.TXT);
        assertEquals("newDoc", this.store.get(URI.create("test")).getDocumentTxt());
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), this.binaryData2));
        InputStream is2 = new ByteArrayInputStream("newDoc2".getBytes());
        this.store.put(is2, URI.create("test2"), DocumentFormat.BINARY);
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), "newDoc2".getBytes()));
        this.store.undo();
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), this.binaryData2));
        assertEquals("newDoc", this.store.get(URI.create("test")).getDocumentTxt());
        this.store.undo();
        assertEquals("myString", this.store.get(URI.create("test")).getDocumentTxt());
    }
    @Test
    public void undoMixAndMatchTest() throws IOException{
        // replace doc at test
        assertEquals("myString", this.store.get(URI.create("test")).getDocumentTxt());
        InputStream is = new ByteArrayInputStream("newDoc".getBytes());
        this.store.put(is, URI.create("test"), DocumentFormat.TXT);
        assertEquals("newDoc", this.store.get(URI.create("test")).getDocumentTxt());
        // change metadata for doc at test1 first time
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value1"), null);
        assertEquals("value1", this.store.getMetadata(URI.create("test"), "key"));
        // change metadata for doc at test 1 again
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value2"), "value1");
        assertEquals("value2", this.store.getMetadata(URI.create("test"), "key"));
        // delete doc at test1
        assertEquals("newDoc", this.store.get(URI.create("test")).getDocumentTxt());
        assertTrue(this.store.delete(URI.create("test")));
        assertEquals(null, this.store.get(URI.create("test")));
        // replace doc at test2
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), this.binaryData2));
        InputStream is2 = new ByteArrayInputStream("newDoc2".getBytes());
        this.store.put(is2, URI.create("test2"), DocumentFormat.BINARY);
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), "newDoc2".getBytes()));
        // change metadata for doc at test2 first time
        assertEquals(this.store.setMetadata(URI.create("test2"), "key", "value1"), null);
        assertEquals("value1", this.store.getMetadata(URI.create("test2"), "key"));
        // change metadata for doc at test2 again
        assertEquals(this.store.setMetadata(URI.create("test2"), "key", "value2"), "value1");
        assertEquals("value2", this.store.getMetadata(URI.create("test2"), "key"));
        //delete doc at test 2
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), "newDoc2".getBytes()));
        assertTrue(this.store.delete(URI.create("test2")));
        assertEquals(null, this.store.get(URI.create("test2")));
    

        // un delete doc art test 2
        this.store.undo();
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), "newDoc2".getBytes()));
        assertEquals(null, this.store.get(URI.create("test")));
        // restore second change to metadata at test2
        this.store.undo();
        assertEquals("value1", this.store.getMetadata(URI.create("test2"), "key"));
        // restore first change to metadata at test2
        this.store.undo();
        assertEquals(null, this.store.getMetadata(URI.create("test2"), "key"));
        // restore doc at test 2 to prev
        this.store.undo();
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), this.binaryData2));
        //un delete doc at test 1
        this.store.undo();
        assertEquals("newDoc", this.store.get(URI.create("test")).getDocumentTxt());
        // restore second change to metadata at test1
        this.store.undo();
        assertEquals("value1", this.store.getMetadata(URI.create("test"), "key"));
        // restore first change to metadata at test1
        this.store.undo();
        assertEquals(null, this.store.getMetadata(URI.create("test"), "key"));
        // restore doc at test 1 to prev
        this.store.undo();
        assertEquals("myString", this.store.get(URI.create("test")).getDocumentTxt());
    }
    @Test
    public void undoAtURITest() throws IOException{
        // replace doc at test
        assertEquals("myString", this.store.get(URI.create("test")).getDocumentTxt());
        InputStream is = new ByteArrayInputStream("newDoc".getBytes());
        this.store.put(is, URI.create("test"), DocumentFormat.TXT);
        assertEquals("newDoc", this.store.get(URI.create("test")).getDocumentTxt());
        // change metadata for doc at test1 first time
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value1"), null);
        assertEquals("value1", this.store.getMetadata(URI.create("test"), "key"));
        // change metadata for doc at test 1 again
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value2"), "value1");
        assertEquals("value2", this.store.getMetadata(URI.create("test"), "key"));
        // delete doc at test1
        assertEquals("newDoc", this.store.get(URI.create("test")).getDocumentTxt());
        assertTrue(this.store.delete(URI.create("test")));
        assertEquals(null, this.store.get(URI.create("test")));
        // replace doc at test2
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), this.binaryData2));
        InputStream is2 = new ByteArrayInputStream("newDoc2".getBytes());
        this.store.put(is2, URI.create("test2"), DocumentFormat.BINARY);
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), "newDoc2".getBytes()));
        // change metadata for doc at test2 first time
        assertEquals(this.store.setMetadata(URI.create("test2"), "key", "value1"), null);
        assertEquals("value1", this.store.getMetadata(URI.create("test2"), "key"));
        // change metadata for doc at test2 again
        assertEquals(this.store.setMetadata(URI.create("test2"), "key", "value2"), "value1");
        assertEquals("value2", this.store.getMetadata(URI.create("test2"), "key"));
        //delete doc at test 2
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), "newDoc2".getBytes()));
        assertTrue(this.store.delete(URI.create("test2")));
        assertEquals(null, this.store.get(URI.create("test2")));
    

        // un delete doc art test 2
        this.store.undo();
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), "newDoc2".getBytes()));
        assertEquals(null, this.store.get(URI.create("test")));
        //un delete doc at test 1
        this.store.undo(URI.create("test"));
        assertEquals("newDoc", this.store.get(URI.create("test")).getDocumentTxt());
        // restore second change to metadata at test2
        this.store.undo();
        assertEquals("value1", this.store.getMetadata(URI.create("test2"), "key"));

        // restore second change to metadata at test1
        this.store.undo(URI.create("test"));
        assertEquals("value1", this.store.getMetadata(URI.create("test"), "key"));
        // restore first change to metadata at test2
        this.store.undo(URI.create("test2"));
        assertEquals(null, this.store.getMetadata(URI.create("test2"), "key"));
        // restore first change to metadata at test1
        this.store.undo(URI.create("test"));
        assertEquals(null, this.store.getMetadata(URI.create("test"), "key"));
        // restore doc at test 2 to prev
        this.store.undo();
        assertTrue(Arrays.equals(this.store.get(URI.create("test2")).getDocumentBinaryData(), this.binaryData2));
        // restore doc at test 1 to prev
        this.store.undo();
        assertEquals("myString", this.store.get(URI.create("test")).getDocumentTxt());


    }
    @Test
    public void searchTest() throws IOException{
        // List<Document> expectedDocs = new ArrayList<Document>();
        for (int i = 0; i < 10; i++){
            InputStream is = new ByteArrayInputStream(("myString"+i).getBytes());
            this.store.put(is, URI.create(String.valueOf(i)), DocumentStore.DocumentFormat.TXT);
            is = new ByteArrayInputStream(("myString"+i).getBytes());
            this.store.put(is, URI.create(String.valueOf(10+i)), DocumentStore.DocumentFormat.TXT);
        }
        for (int i = 0; i < 10; i++){
            List<Document> docs = new ArrayList<>(this.store.search("myString"+i));
            assertEquals(2, docs.size());
            // assertEquals(URI.create(String.valueOf(i)), (docs.get(0).getKey()));
        }
        InputStream is = new ByteArrayInputStream(("hello hello").getBytes());
        this.store.put(is, URI.create(String.valueOf("doc1")), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream(("hello hello hello").getBytes());
        this.store.put(is, URI.create(String.valueOf("doc2")), DocumentStore.DocumentFormat.TXT);
        List<Document> docs = new ArrayList<>(this.store.search("hello"));
        assertEquals(URI.create(String.valueOf("doc2")), (docs.get(0).getKey()));
        assertEquals(URI.create(String.valueOf("doc1")), (docs.get(1).getKey()));
    }

    @Test
    public void prefixSearchTest() throws IOException{
        // List<Document> expectedDocs = new ArrayList<Document>();
        String word = "h";
        for (int i = 0; i < 10; i++){
            InputStream is = new ByteArrayInputStream((word).getBytes());
            this.store.put(is, URI.create(String.valueOf(i)), DocumentStore.DocumentFormat.TXT);
            word+="i";
            // is = new ByteArrayInputStream(("myString"+i).getBytes());
            // this.store.put(is, URI.create(String.valueOf(10+i)), DocumentStore.DocumentFormat.TXT);
        }
        word = "h";
        for (int i = 10; i > 1; i--){
            List<Document> docs = new ArrayList<>(this.store.searchByPrefix(word));
            assertEquals(i, docs.size());
            word+="i";
            // assertEquals(URI.create(String.valueOf(i)), (docs.get(0).getKey()));
        }
        // check for sorting
        InputStream is = new ByteArrayInputStream(("hello heather").getBytes());
        this.store.put(is, URI.create(String.valueOf("doc1")), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream(("hi hello hix").getBytes());
        this.store.put(is, URI.create(String.valueOf("doc2")), DocumentStore.DocumentFormat.TXT);
        List<Document> docs = new ArrayList<>(this.store.searchByPrefix("h"));
        assertEquals(URI.create(String.valueOf("doc2")), (docs.get(0).getKey()));
        assertEquals(URI.create(String.valueOf("doc1")), (docs.get(1).getKey()));
        docs = new ArrayList<>(this.store.searchByPrefix("he"));
        assertEquals(URI.create(String.valueOf("doc1")), (docs.get(0).getKey()));
        assertEquals(URI.create(String.valueOf("doc2")), (docs.get(1).getKey()));
    }

    @Test
    public void deleteAllTest() throws IOException{
        InputStream is = new ByteArrayInputStream(("hello heather HI his").getBytes());
        this.store.put(is, URI.create("doc1"), DocumentStore.DocumentFormat.TXT);
        assertEquals(1, this.store.search("HI").size());
        is = new ByteArrayInputStream(("hello hi hix").getBytes());
        this.store.put(is, URI.create("doc2"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream(("hi hix").getBytes());
        this.store.put(is, URI.create("doc3"), DocumentStore.DocumentFormat.TXT);
        Set<URI> deletedUrIs = this.store.deleteAll("hi");
        assertTrue(deletedUrIs.contains(URI.create("doc2")));
        assertTrue(deletedUrIs.contains(URI.create("doc3")));
        assertEquals(0, this.store.search("hi").size());
        assertEquals(2, deletedUrIs.size());
        assertEquals(1, this.store.search("HI").size(), deletedUrIs.toString());
        assertEquals(1, this.store.search("his").size(), deletedUrIs.toString());
        assertEquals(1, this.store.searchByPrefix("H").size());

    }

    @Test
    public void deleteAllPrefixTest() throws IOException{
        InputStream is = new ByteArrayInputStream(("hello heather his").getBytes());
        this.store.put(is, URI.create("doc1"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream(("hello hi hix").getBytes());
        this.store.put(is, URI.create("doc2"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream(("hi hix").getBytes());
        this.store.put(is, URI.create("doc3"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream(("hem heather").getBytes());
        this.store.put(is, URI.create("doc4"), DocumentStore.DocumentFormat.TXT);
        Set<URI> deletedUrIs = this.store.deleteAllWithPrefix("hi");
        assertTrue(deletedUrIs.contains(URI.create("doc2")));
        assertTrue(deletedUrIs.contains(URI.create("doc3")));
        assertTrue(deletedUrIs.contains(URI.create("doc1")));
        assertEquals(3, deletedUrIs.size());
        assertEquals(0, this.store.searchByPrefix("hi").size());
        assertEquals(1, this.store.search("hem").size(), deletedUrIs.toString());
        assertEquals(1, this.store.searchByPrefix("h").size());
    }

    @Test
    public void searchByMetadataTest() throws IOException{
        InputStream is = new ByteArrayInputStream("myString".getBytes());
        this.store.put(is, URI.create("test3"), DocumentStore.DocumentFormat.TXT);
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "not", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "not1", "value2"), null);
        Map<String, String> query = new HashMap<>();
        query.put("key", "value");
        List<Document> docs = new ArrayList<>(this.store.searchByMetadata(query));
        assertEquals(2, docs.size());
        assertTrue(docs.contains(this.store.get(URI.create("test"))));
        assertTrue(docs.contains(this.store.get(URI.create("test2"))));
        query = new HashMap<>();
        query.put("not", "value");
        docs = new ArrayList<>(this.store.searchByMetadata(query));
        assertEquals(1, docs.size());
        assertTrue(docs.contains(this.store.get(URI.create("test3"))));

    }

    @Test
    public void searchByKeywordAndMetadataTest() throws IOException{
        InputStream is = new ByteArrayInputStream("hello".getBytes());
        this.store.put(is, URI.create("test"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream("hi".getBytes());
        this.store.put(is, URI.create("test2"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream("hello".getBytes());
        this.store.put(is, URI.create("test3"), DocumentStore.DocumentFormat.TXT);
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "key2", "value2"), null);
        Map<String, String> query = new HashMap<>();
        query.put("key", "value");
        List<Document> docs = new ArrayList<>(this.store.searchByKeywordAndMetadata("hello", query));
        assertEquals(2, docs.size());
        assertTrue(docs.contains(this.store.get(URI.create("test"))));
        assertTrue(docs.contains(this.store.get(URI.create("test3"))));
        docs = new ArrayList<>(this.store.searchByKeywordAndMetadata("hi", query));
        assertEquals(1, docs.size());
        assertTrue(docs.contains(this.store.get(URI.create("test2"))));
    }

    @Test
    public void searchByPrefixAndMetadataTest() throws IOException{
        InputStream is = new ByteArrayInputStream("hello heather".getBytes());
        this.store.put(is, URI.create("test"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream("hi".getBytes());
        this.store.put(is, URI.create("test2"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream("hello".getBytes());
        this.store.put(is, URI.create("test3"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream("hell heather hex".getBytes());
        this.store.put(is, URI.create("test4"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream("hello".getBytes());
        this.store.put(is, URI.create("test5"), DocumentStore.DocumentFormat.TXT);
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test4"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test4"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test5"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test5"), "not", "value2"), null);
        Map<String, String> query = new HashMap<>();
        query.put("key", "value");
        query.put("key2", "value2");
        List<Document> docs = new ArrayList<>(this.store.searchByPrefixAndMetadata("he", query));
        assertEquals(3, docs.size());
        assertTrue(docs.contains(this.store.get(URI.create("test"))));
        assertTrue(docs.contains(this.store.get(URI.create("test3"))));
        assertTrue(docs.contains(this.store.get(URI.create("test4"))));
        assertEquals(URI.create("test4"), docs.get(0).getKey());
        docs = new ArrayList<>(this.store.searchByPrefixAndMetadata("hi", query));
        assertEquals(1, docs.size());
        assertTrue(docs.contains(this.store.get(URI.create("test2"))));
        query.remove("key2");
        docs = new ArrayList<>(this.store.searchByPrefixAndMetadata("he", query));
        assertEquals(4, docs.size());
        assertTrue(docs.contains(this.store.get(URI.create("test"))));
        assertTrue(docs.contains(this.store.get(URI.create("test3"))));
        assertTrue(docs.contains(this.store.get(URI.create("test4"))));
        assertTrue(docs.contains(this.store.get(URI.create("test5"))));
    }

    @Test
    public void deleteAllWithMetadataTest() throws IOException{
        InputStream is = new ByteArrayInputStream("myString".getBytes());
        this.store.put(is, URI.create("test3"), DocumentStore.DocumentFormat.TXT);
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "not", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "not1", "value2"), null);
        Map<String, String> query = new HashMap<>();
        query.put("key", "value");
        Set<URI> docs = new HashSet<>(this.store.deleteAllWithMetadata(query));
        assertEquals(2, docs.size());
        assertTrue(docs.contains(URI.create("test")));
        assertTrue(docs.contains(URI.create("test2")));
        assertEquals(0, this.store.searchByMetadata(query).size());
        query = new HashMap<>();
        query.put("not", "value");
        docs = new HashSet<>(this.store.deleteAllWithMetadata(query));
        assertEquals(1, docs.size());
        assertTrue(docs.contains(URI.create("test3")));
        assertEquals(0, this.store.searchByMetadata(query).size());
    }
    @Test
    public void deleteAllWithKeywordAndMetadataTest() throws IOException{
        InputStream is = new ByteArrayInputStream("hello".getBytes());
        this.store.put(is, URI.create("test"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream("hi".getBytes());
        this.store.put(is, URI.create("test2"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream("hello".getBytes());
        this.store.put(is, URI.create("test3"), DocumentStore.DocumentFormat.TXT);
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "not", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "key2", "value2"), null);
        Map<String, String> query = new HashMap<>();
        query.put("key", "value");
        Set<URI> docs = new HashSet<>(this.store.deleteAllWithKeywordAndMetadata("hello", query));
        assertEquals(2, docs.size());
        assertTrue(docs.contains(URI.create("test")));
        assertTrue(docs.contains(URI.create("test3")));
        assertEquals(0, this.store.searchByKeywordAndMetadata("hello", query).size());
        query = new HashMap<>();
        query.put("not", "value");
        docs = new HashSet<>(this.store.deleteAllWithKeywordAndMetadata("hi", query));
        assertEquals(1, docs.size());
        assertTrue(docs.contains(URI.create("test2")));
        assertEquals(0, this.store.searchByKeywordAndMetadata("hello", query).size());
    }

    @Test
    public void deleteAllWithPrefixAndMetadataTest() throws IOException{
        InputStream is = new ByteArrayInputStream("hello heather".getBytes());
        this.store.put(is, URI.create("test"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream("hi".getBytes());
        this.store.put(is, URI.create("test2"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream("hello".getBytes());
        this.store.put(is, URI.create("test3"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream("hell heather hex".getBytes());
        this.store.put(is, URI.create("test4"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream("hello".getBytes());
        this.store.put(is, URI.create("test5"), DocumentStore.DocumentFormat.TXT);
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test4"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test4"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test5"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test5"), "not", "value2"), null);
        Map<String, String> query = new HashMap<>();
        query.put("key", "value");
        query.put("key2", "value2");
        Set<URI> docs = new HashSet<>(this.store.deleteAllWithPrefixAndMetadata("he", query));
        assertEquals(3, docs.size());
        assertTrue(docs.contains(URI.create("test")));
        assertTrue(docs.contains(URI.create("test3")));
        assertTrue(docs.contains(URI.create("test4")));
        assertEquals(0, this.store.searchByPrefixAndMetadata("he", query).size());
        docs = new HashSet<>(this.store.deleteAllWithPrefixAndMetadata("hi", query));
        assertEquals(1, docs.size());
        assertTrue(docs.contains(URI.create("test2")));
        assertEquals(0, this.store.searchByPrefixAndMetadata("hi", query).size());
        query.remove("key2");
        assertFalse(this.store.get(URI.create("test5"))==null);
        assertTrue(this.store.get(URI.create("test4"))==null);
        // docs = new HashSet<>(this.store.deleteAllWithPrefixAndMetadata("he", query));
        // List<Document> docs2 = new ArrayList<>(this.store.search("hello"));
        // assertEquals(1, docs.size(), String.valueOf(docs2.size()));
        // assertTrue(docs.contains(URI.create("test5")));
        // assertEquals(0, this.store.searchByKeywordAndMetadata("he", query).size());
    }

    @Test
    public void undoCommandSetTest() throws IOException{
        InputStream is = new ByteArrayInputStream("myString".getBytes());
        this.store.put(is, URI.create("test3"), DocumentStore.DocumentFormat.TXT);
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "not", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "not1", "value2"), null);
        Map<String, String> query = new HashMap<>();
        query.put("key", "value");
        is = new ByteArrayInputStream(("hello heather HI his").getBytes());
        this.store.put(is, URI.create("doc1"), DocumentStore.DocumentFormat.TXT);
        assertEquals(1, this.store.search("HI").size());
        is = new ByteArrayInputStream(("hello hi hix").getBytes());
        this.store.put(is, URI.create("doc2"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream(("hi hix").getBytes());
        this.store.put(is, URI.create("doc3"), DocumentStore.DocumentFormat.TXT);
        Set<URI> docs = new HashSet<>(this.store.deleteAllWithMetadata(query));
        assertEquals(2, docs.size());
        assertTrue(docs.contains(URI.create("test")));
        assertTrue(docs.contains(URI.create("test2")));
        assertEquals(0, this.store.searchByMetadata(query).size());
        Set<URI> deletedUrIs = this.store.deleteAll("hi");
        assertTrue(deletedUrIs.contains(URI.create("doc2")));
        assertTrue(deletedUrIs.contains(URI.create("doc3")));
        assertEquals(0, this.store.search("hi").size());
        assertEquals(2, deletedUrIs.size());
        this.store.undo();
        assertEquals(2, this.store.search("hi").size());
        assertEquals(2, this.store.search("hi").size());
        this.store.undo();
        assertEquals(2, this.store.searchByMetadata(query).size());
    
    }

    @Test
    public void undoSpecificCommandWithinSetTest() throws IOException{
        InputStream is = new ByteArrayInputStream("myString".getBytes());
        this.store.put(is, URI.create("test3"), DocumentStore.DocumentFormat.TXT);
        assertEquals(this.store.setMetadata(URI.create("test"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test2"), "key2", "value2"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "not", "value"), null);
        assertEquals(this.store.setMetadata(URI.create("test3"), "not1", "value2"), null);
        Map<String, String> query = new HashMap<>();
        query.put("key", "value");
        is = new ByteArrayInputStream(("hello heather HI his").getBytes());
        this.store.put(is, URI.create("doc1"), DocumentStore.DocumentFormat.TXT);
        assertEquals(1, this.store.search("HI").size());
        is = new ByteArrayInputStream(("hello hi hix").getBytes());
        this.store.put(is, URI.create("doc2"), DocumentStore.DocumentFormat.TXT);
        is = new ByteArrayInputStream(("hi hix").getBytes());
        this.store.put(is, URI.create("doc3"), DocumentStore.DocumentFormat.TXT);
        Set<URI> docs = new HashSet<>(this.store.deleteAllWithMetadata(query));
        assertEquals(2, docs.size());
        assertTrue(docs.contains(URI.create("test")));
        assertTrue(docs.contains(URI.create("test2")));
        assertEquals(0, this.store.searchByMetadata(query).size());
        Set<URI> deletedUrIs = this.store.deleteAll("hi");
        assertTrue(deletedUrIs.contains(URI.create("doc2")));
        assertTrue(deletedUrIs.contains(URI.create("doc3")));
        assertEquals(0, this.store.search("hi").size());
        assertEquals(2, deletedUrIs.size());
        this.store.undo(URI.create("test"));
        assertEquals(1, this.store.searchByMetadata(query).size());
        this.store.undo(URI.create("doc2"));
        assertEquals(1, this.store.search("hi").size());
        this.store.undo();
        assertEquals(2, this.store.search("hi").size());
        this.store.undo();
        assertEquals(2, this.store.searchByMetadata(query).size());
        this.store.undo();
        assertEquals(null, this.store.get(URI.create("doc3")));

    }

    @Test
    public void setMaxDocumentBytesTest() throws IOException{
        this.store.setMaxDocumentBytes(1);
        this.store.setMaxDocumentBytes(10000);
        InputStream is = new ByteArrayInputStream(("hello heather HI his").getBytes());
        // byte size: 20
        this.store.put(is, URI.create("doc1"), DocumentStore.DocumentFormat.TXT);
        assertEquals(1, this.store.search("HI").size());
        // byte size: 12
        is = new ByteArrayInputStream(("hello hi hix").getBytes());
        this.store.put(is, URI.create("doc2"), DocumentStore.DocumentFormat.TXT);
        // byte size: 6
        is = new ByteArrayInputStream(("hi hix").getBytes());
        this.store.put(is, URI.create("doc3"), DocumentStore.DocumentFormat.TXT);
        // assertEquals(2, this.store.search("hello").size());
        // System.out.println(this.store.search("hello").size());
        this.store.setMaxDocumentBytes(1);
        this.store.setMaxDocumentBytes(100);
        assertEquals(1, this.store.search("HI").size());
        assertEquals(2, this.store.search("hi").size());
        // List<Document> docs = this.store.search("hello"); 
        // for (Document doc : docs){
        //     System.out.println(doc.getKey());
        // }
        // this.store.setMaxDocumentBytes(10000);
        // is = new ByteArrayInputStream(("hello heather HI his").getBytes());
        // this.store.put(is, URI.create("doc1"), DocumentStore.DocumentFormat.TXT);
        // this.store.search("hello");
        // this.store.setMaxDocumentCount(2);
        // docs = this.store.search("hello"); 
        // for (Document doc : docs){
        //     System.out.println(doc.getKey());
        // }
        // assertEquals(2, this.store.search("hello").size());
        // assertEquals(1, this.store.search("hix").size());


    }

    @Test
    public void maxDocumentBytesEditWithReHeapifyTest() throws IOException{
            this.store.setMaxDocumentBytes(1);
            this.store.setMaxDocumentBytes(10000);
            // byte size: 17
            InputStream is = new ByteArrayInputStream(("document number 1").getBytes());
            this.store.put(is, URI.create("doc1"), DocumentStore.DocumentFormat.TXT);
            // byte size: 17
            is = new ByteArrayInputStream(("document number 2").getBytes());
            this.store.put(is, URI.create("doc2"), DocumentStore.DocumentFormat.TXT);
            // byte size: 17
            is = new ByteArrayInputStream(("document number 3").getBytes());
            this.store.put(is, URI.create("doc3"), DocumentStore.DocumentFormat.TXT);
            assertEquals(3, this.store.search("document").size());
            // System.out.println(this.store.currentDocumentBytes);
            // this.store.setMaxDocumentBytes(1);
            this.store.setMaxDocumentCount(1);
            
            // System.out.println(this.store.currentDocumentBytes);
            System.out.println(this.store.search("document").size());
            assertEquals(1, this.store.search("2").size());
            assertEquals(1, this.store.search("2").size());
            assertEquals(3, this.store.search("document").size());

    }
    @Test
    public void maxDocumentBytesEditAndUndoWithReHeapifyTest() throws IOException{
            this.store.setMaxDocumentBytes(1);
            this.store.setMaxDocumentBytes(10000);
            // byte size: 17
            InputStream is = new ByteArrayInputStream(("document number 1").getBytes());
            this.store.put(is, URI.create("doc1"), DocumentStore.DocumentFormat.TXT);
            // byte size: 17
            is = new ByteArrayInputStream(("document number 2").getBytes());
            this.store.put(is, URI.create("doc2"), DocumentStore.DocumentFormat.TXT);
            // byte size: 17
            is = new ByteArrayInputStream(("document number 3").getBytes());
            this.store.put(is, URI.create("doc3"), DocumentStore.DocumentFormat.TXT);
            assertEquals(3, this.store.search("document").size());
            
            this.store.setMetadata(URI.create("doc2"), "null", "null");
            this.store.setMetadata(URI.create("doc3"), "null", "null");
            this.store.undo();
            this.store.undo();
            this.store.setMaxDocumentBytes(25);
            
            // System.out.println(this.store.currentDocumentBytes);
            System.out.println(this.store.search("document").size());
            assertEquals(1, this.store.search("2").size());
            assertEquals(1, this.store.search("2").size());
            assertEquals(3, this.store.search("document").size());
    }
    @Test
    public void searchAndGetWithFakeURITest() {
        assertEquals(null, this.store.get(URI.create("fake")));
        assertEquals(0, this.store.search("fake").size());
        
    }
    // @Test
    public void maxDocumentBytesEditAndUndoSetWithReHeapifyTest() throws IOException{
            this.store.setMaxDocumentBytes(1);
            assertEquals(null, this.store.get(URI.create("test")));
            assertEquals(null, this.store.get(URI.create("test2")));
            this.store.setMaxDocumentBytes(10000);
            // byte size: 16
            InputStream is = new ByteArrayInputStream(("document hello 1").getBytes());
            this.store.put(is, URI.create("doc1"), DocumentStore.DocumentFormat.TXT);
            // byte size: 17
            is = new ByteArrayInputStream(("document number 2").getBytes());
            this.store.put(is, URI.create("doc2"), DocumentStore.DocumentFormat.TXT);
            // byte size: 16
            is = new ByteArrayInputStream(("document hello 3").getBytes());
            this.store.put(is, URI.create("doc3"), DocumentStore.DocumentFormat.TXT);
            assertEquals(3, this.store.search("document").size());
            List<Document> docs = this.store.search("document");
            for (Document doc : docs){
                System.out.println(doc.getKey());
                System.out.println(doc.getDocumentTxt().getBytes().length);
            }
            assertEquals(2, this.store.deleteAll("hello").size());
            this.store.undo();
            // System.out.println(this.store.currentDocumentBytes);
            docs = this.store.search("2");
            for (Document doc : docs){
                System.out.println(doc.getKey());
                System.out.println(doc.getDocumentTxt().getBytes().length);
            }
            // this.store.setMaxDocumentBytes(1);
            
            this.store.deleteAll("hello").size();
            this.store.setMaxDocumentBytes(19);
            this.store.undo();
            // System.out.println(this.store.currentDocumentBytes);
            System.out.println(this.store.search("document").size());
            assertEquals(1, this.store.search("2").size());
            docs = this.store.search("2");
            for (Document doc : docs){
                System.out.println(doc.getKey());
                System.out.println(doc.getDocumentTxt());
                System.out.println(doc.getDocumentTxt().getBytes().length);
            }
            assertEquals(1, this.store.search("2").size());
    }
}
