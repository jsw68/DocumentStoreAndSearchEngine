package edu.yu.cs.com1320.project.stage3;


import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;


import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import edu.yu.cs.com1320.project.stage3.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage3.Document;
import org.junit.jupiter.api.BeforeEach;  

public class DocumentTest {
    Document StringDoc;
    Document ByteDoc;
    byte[] binaryData;
    Map<String, String> stringmeta;
    Map<String, String> bytemeta;
    @BeforeEach
    public void beforeEach(){
        this.binaryData = "bytedoc".getBytes();
        this.StringDoc = new DocumentImpl(URI.create("string"), "Stringdoc") ;
        this.ByteDoc = new DocumentImpl(URI.create("byte"), this.binaryData);
        this.StringDoc.setMetadataValue("string1 meta key", "string1 meta value");
        this.ByteDoc.setMetadataValue("byte1 meta key", "byte1 meta value");
        this.StringDoc.setMetadataValue("string2 meta key", "string2 meta value");
        this.ByteDoc.setMetadataValue("byte2 meta key", "byte2 meta value");
        this.StringDoc.setMetadataValue("string3 meta key", "string3 meta value");
        this.ByteDoc.setMetadataValue("byte3 meta key", "byte3 meta value");
        this.stringmeta = new HashMap<>();
        this.bytemeta = new HashMap<>();
        this.stringmeta.put("string1 meta key", "string1 meta value");
        this.stringmeta.put("string2 meta key", "string2 meta value");
        this.stringmeta.put("string3 meta key", "string3 meta value");
        this.bytemeta.put("byte1 meta key", "byte1 meta value");
        this.bytemeta.put("byte2 meta key", "byte2 meta value");
        this.bytemeta.put("byte3 meta key", "byte3 meta value");

    }

    @Test
    public void getKeyTest(){
        assertTrue(this.StringDoc.getKey().equals( URI.create("string")));
        assertTrue(this.ByteDoc.getKey().equals(URI.create("byte")));
    }

    @Test
    public void getDocumentBinaryDataTest(){
        assertTrue(this.StringDoc.getDocumentBinaryData() == null);
        assertTrue(this.ByteDoc.getDocumentBinaryData().equals(this.binaryData));
    }

    @Test
    public void getDocumentTxtTest(){
        assertTrue(this.StringDoc.getDocumentTxt().equals("Stringdoc"));
        assertEquals(this.StringDoc.getDocumentTxt(),  "Stringdoc");
        assertTrue(this.ByteDoc.getDocumentTxt() == null);
    }

    @Test
    public void getMetadataValueTest(){
        assertTrue(this.StringDoc.getMetadataValue("string1 meta key").equals("string1 meta value"));
        assertTrue(this.ByteDoc.getMetadataValue("byte1 meta key").equals("byte1 meta value"));
        assertTrue(this.StringDoc.getMetadataValue("string2 meta key").equals("string2 meta value"));
        assertTrue(this.ByteDoc.getMetadataValue("byte2 meta key").equals("byte2 meta value"));
        assertTrue(this.StringDoc.getMetadataValue("string3 meta key").equals("string3 meta value"));
        assertTrue(this.ByteDoc.getMetadataValue("byte3 meta key").equals("byte3 meta value"));
    }

    // @Test
    // public void getMetadataTest(){
    //     // TODO adjust test to work with new Storage, create storage that should be equla to input
    //     assertTrue(this.ByteDoc.getMetadata().equals(this.bytemeta));
    //     assertTrue(this.StringDoc.getMetadata().equals(this.stringmeta));
    // }

    @Test
    public void nullTest(){
        // constructor string tests
        assertThrows(IllegalArgumentException.class, () -> new DocumentImpl(null, "txt"));
        assertThrows(IllegalArgumentException.class, () -> new DocumentImpl(URI.create("string"), "".getBytes()));
        assertThrows(IllegalArgumentException.class, () -> new DocumentImpl(URI.create("string"), ""));
        assertThrows(IllegalArgumentException.class, () -> new DocumentImpl(URI.create(""), "txt"));

        //setMetadataValue null tests
        assertThrows(IllegalArgumentException.class, () -> this.StringDoc.setMetadataValue(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> this.StringDoc.setMetadataValue("", "value"));
        //getMetadata null tests
        assertThrows(IllegalArgumentException.class, () -> this.StringDoc.getMetadataValue(null));
        assertThrows(IllegalArgumentException.class, () -> this.StringDoc.getMetadataValue(""));
        //getMetadata null tests
        assertThrows(IllegalArgumentException.class, () -> this.StringDoc.getMetadataValue(null));
        assertThrows(IllegalArgumentException.class, () -> this.StringDoc.getMetadataValue(""));

    }



    
}
