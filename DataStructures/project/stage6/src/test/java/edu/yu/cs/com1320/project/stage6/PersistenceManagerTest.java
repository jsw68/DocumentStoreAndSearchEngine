package edu.yu.cs.com1320.project.stage6;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;


import java.io.IOException;
import java.net.URI;

import edu.yu.cs.com1320.project.stage6.impl.DocumentImpl;
import org.junit.jupiter.api.BeforeEach;
import edu.yu.cs.com1320.project.stage6.impl.DocumentPersistenceManager;

public class PersistenceManagerTest {
    private DocumentPersistenceManager manager;
    @BeforeEach
    public void beforeEach() {
        this.manager = new DocumentPersistenceManager(null);
    }
    @Test
    public void testSerialize() throws IOException {
        Document doc = new DocumentImpl(URI.create("null"), "test", null);
        this.manager.serialize(URI.create("https://hello/one/two"), doc);
        this.manager.serialize(URI.create("hello/one/two/three"), doc);

    }

    @Test
    public void testDeserialize() throws IOException {
        Document doc = new DocumentImpl(URI.create("null"), "test", null);
        this.manager.serialize(URI.create("https://hello/one/two"), doc);
        this.manager.serialize(URI.create("hello/one/two/three"), doc);
        Document doc1 = this.manager.deserialize(URI.create("https://hello/one/two"));
        Document doc2 = this.manager.deserialize(URI.create("hello/one/two/three"));
        assertEquals(doc1.getDocumentTxt(), "test");
        assertEquals(doc2.getDocumentTxt(), "test");
    }

    @Test
    public void testDelete() throws IOException {
        Document doc = new DocumentImpl(URI.create("null"), "test", null);
        this.manager.serialize(URI.create("https://hello/one/two"), doc);
        this.manager.serialize(URI.create("hello/one/two/three"), doc);
        this.manager.delete(URI.create("https://hello/one/two"));
        this.manager.delete(URI.create("hello/one/two/three"));
        // assertThrows(IllegalArgumentException.class, () -> this.manager.deserialize(URI.create("https://hello/one/two")));
        // assertThrows(IllegalArgumentException.class, () -> this.manager.deserialize(URI.create("hello/one/two/three")));
    }

    @Test
    public void testOverwrite() throws IOException {
        // test when a non-existent document is deserialized
        Document doc2 = this.manager.deserialize(URI.create("hello/one/two/three"));

    }
}
