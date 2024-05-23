package edu.yu.cs.com1320.project.stage6;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import edu.yu.cs.com1320.project.stage6.impl.DocumentImpl;
import org.junit.jupiter.api.BeforeEach;
import edu.yu.cs.com1320.project.stage6.impl.DocumentPersistenceManager;

public class PersistenceManagerTest {
    private DocumentPersistenceManager manager;
    @BeforeEach
    public void beforeEach() {
        File file = new File("/Users/jwain/YU Schoolwork/2023/Spring/Data Structures/Project/JSON");
        this.manager = new DocumentPersistenceManager(file);
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
        String fileName1 = getFileName(URI.create("https://hello/one/two"), "/Users/jwain/YU Schoolwork/2023/Spring/Data Structures/Project/JSON");
        String fileName2 = getFileName(URI.create("hello/one/two/three"), "/Users/jwain/YU Schoolwork/2023/Spring/Data Structures/Project/JSON");
        // System.out.println(fileExists(fileName1) + "exists in testDeserialize" + fileName1);
        // System.out.println(fileExists(fileName2) + "exists in testDeserialize" + fileName2);
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
        String fileName1 = getFileName(URI.create("https://hello/one/two"), "/Users/jwain/YU Schoolwork/2023/Spring/Data Structures/Project/JSON");
        String fileName2 = getFileName(URI.create("hello/one/two/three"), "/Users/jwain/YU Schoolwork/2023/Spring/Data Structures/Project/JSON");
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


    
    @AfterAll
    public static void afterAll() throws IOException{
        File file = new File("/Users/jwain/YU Schoolwork/2023/Spring/Data Structures/Project/JSON/Test");
        file.createNewFile();
    }


    private String getFileName(URI key, String dir) {
        String uri = key.toString();
        if (uri.startsWith("https://")) {
            uri = uri.substring(8);
        }
        if (dir.endsWith(File.separator)) {
            dir = dir + File.separator;
        }
        String fileName = dir + uri + ".json";
        return fileName;
    }

    private boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.isFile();
    }
}
