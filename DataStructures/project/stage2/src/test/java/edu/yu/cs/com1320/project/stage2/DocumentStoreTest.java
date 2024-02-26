package edu.yu.cs.com1320.project.stage2;

import org.junit.jupiter.api.Test;  
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
// import java.lang.reflect.Array;

import org.junit.jupiter.api.BeforeEach;  
import java.net.URI;
import java.util.Arrays;

import edu.yu.cs.com1320.project.stage2.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage2.impl.DocumentStoreImpl;
import edu.yu.cs.com1320.project.stage2.DocumentStore;


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
}
