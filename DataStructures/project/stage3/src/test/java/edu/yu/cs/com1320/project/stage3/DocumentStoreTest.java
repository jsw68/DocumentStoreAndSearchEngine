package edu.yu.cs.com1320.project.stage3;

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

import edu.yu.cs.com1320.project.stage3.DocumentStore.DocumentFormat;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage3.impl.DocumentStoreImpl;

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
        assertThrows(IllegalArgumentException.class, () -> this.store.setMetadata(URI.create("nothing"), "key", "value"));
        assertThrows(IllegalArgumentException.class, () -> this.store.setMetadata(URI.create("nothing"), "", "value"));
        assertThrows(IllegalArgumentException.class, () -> this.store.setMetadata(URI.create("nothing"), null, "value"));
        // getMetadataNulls
        assertThrows(IllegalArgumentException.class, () -> this.store.getMetadata(null, "key"));
        assertThrows(IllegalArgumentException.class, () -> this.store.getMetadata(URI.create(""), "key"));
        assertThrows(IllegalArgumentException.class, () -> this.store.getMetadata(URI.create("nothing"), "key"));
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
}
