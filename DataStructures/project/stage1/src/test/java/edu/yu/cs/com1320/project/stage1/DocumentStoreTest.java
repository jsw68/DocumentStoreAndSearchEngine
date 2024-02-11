package edu.yu.cs.com1320.project.stage1;

import org.junit.jupiter.api.Test;  
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;  
import java.net.URI;


public class DocumentStoreTest {
    DocumentStoreImpl store;
    @BeforeEach
    public void Initialize() throws IOException{
        this.store = new DocumentStoreImpl();
        InputStream is = new ByteArrayInputStream( "myString".getBytes() );
        this.store.put(is, URI.create("test"), DocumentStore.DocumentFormat.TXT);
    }

    @Test
    public void getTest() throws IOException{
        Document doc = this.store.get(URI.create("test"));
    }
}
