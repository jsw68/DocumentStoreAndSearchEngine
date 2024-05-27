package edu.yu.cs.com1320.project.stage6;

import org.junit.jupiter.api.Test;
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
    private File baseDir;
    @BeforeEach
    public void beforeEach() {
        // this.baseDir = new File("/Users/jwain/YU Schoolwork/2023/Spring/Data Structures/Project/Code/Wainberg_Jake_800759404/DataStructures/project/stage6/JSON");
        this.baseDir = new File(System.getProperty("user.dir"));
        System.out.println(this.baseDir.getAbsolutePath() + "is the baseDir");
        this.manager = new DocumentPersistenceManager(this.baseDir);
    }
    @Test
    public void testSerialize() throws IOException {
        Document doc = new DocumentImpl(URI.create("lasn"), "test", null);
        this.manager.serialize(URI.create("http://hello/one/two"), doc);
        this.manager.serialize(URI.create("hello/one/two/three"), doc);
        String fileName1 = getFileName(URI.create("http://hello/one/two"), this.baseDir.getAbsolutePath());
        String fileName2 = getFileName(URI.create("hello/one/two/three"), this.baseDir.getAbsolutePath());
        System.out.println(fileName1 + "is fileName1");
        System.out.println(fileName2 + "is fileName2");
        assertTrue(fileExists(fileName1));
        assertTrue(fileExists(fileName2));

    }

    @Test
public void testSerialize2() throws IOException {
    Document doc = new DocumentImpl(URI.create("null"), "test", null);

    // Serialize the documents
    this.manager.serialize(URI.create("http://hello/one/two"), doc);
    this.manager.serialize(URI.create("hello/one/two/three"), doc);

    // Determine file names
    String fileName1 = getFileName(URI.create("http://hello/one/two"), this.baseDir.getAbsolutePath());
    String fileName2 = getFileName(URI.create("hello/one/two/three"), this.baseDir.getAbsolutePath());

    // Print paths for debugging
    System.out.println("Expected file path 1: " + fileName1);
    System.out.println("Expected file path 2: " + fileName2);

    // Check file existence
    boolean file1Exists = fileExists(fileName1);
    boolean file2Exists = fileExists(fileName2);

    // Print existence for debugging
    System.out.println("File 1 exists: " + file1Exists);
    System.out.println("File 2 exists: " + file2Exists);

    // Assertions
    assertTrue(file1Exists);
    assertTrue(file2Exists);
}

    @Test
    public void testDeserialize() throws IOException {
        Document doc = new DocumentImpl(URI.create("null"), "test", null);
        this.manager.serialize(URI.create("https://hello/one/two"), doc);
        this.manager.serialize(URI.create("hello/one/two/three"), doc);
        String fileName1 = getFileName(URI.create("https://hello/one/two"), this.baseDir.getAbsolutePath());
        String fileName2 = getFileName(URI.create("hello/one/two/three"), this.baseDir.getAbsolutePath());
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
        String fileName1 = getFileName(URI.create("https://hello/one/two"), this.baseDir.getAbsolutePath());
        String fileName2 = getFileName(URI.create("hello/one/two/three"), this.baseDir.getAbsolutePath());
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


    private String getFileName(URI key, String dir) {
        String uri = key.toString();
        if (uri.startsWith("http://")) {
            uri = uri.substring(7);
        }
        if (!dir.endsWith(File.separator)) {
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
