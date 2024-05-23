package edu.yu.cs.com1320.project.stage6.impl;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.Writer;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.stage6.impl.DocumentImpl;
import edu.yu.cs.com1320.project.stage6.PersistenceManager;

public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {
    private String dir;

    private class DocumentSerializer implements JsonSerializer<Document> {
        @Override
        public JsonElement serialize(Document src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            if (src.getDocumentBinaryData() == null) {
                obj.addProperty("txt", src.getDocumentTxt());
                JsonObject words = new JsonObject();
                for (Entry<String, Integer> word : src.getWordMap().entrySet()) {
                    words.addProperty(word.getKey(), word.getValue());
                }
                obj.add("wordCount", words);
            } else {
                obj.addProperty("binaryData", Base64.getEncoder().encodeToString(src.getDocumentBinaryData()));
            }
            JsonObject meta = new JsonObject();
            for (Entry<String, String> metaPairs : src.getMetadata().entrySet()) {
                meta.addProperty(metaPairs.getKey(), metaPairs.getValue());
            }
            obj.add("meta", meta);
            obj.addProperty("uri", src.getKey().toString());
            return obj;
        }
    }

    private class DocumentDeserializer implements JsonDeserializer<Document> {
        @Override
        public Document deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            JsonObject obj = json.getAsJsonObject();
            String uri = obj.get("uri").getAsString();
            String txt = null;
            byte[] binaryData = null;
            Map<String, Integer> wordCount = new HashMap<>();
            if (obj.has("txt")) {
                txt = obj.get("txt").getAsString();
                JsonObject words = obj.get("wordCount").getAsJsonObject();
                for (Entry<String, JsonElement> word : words.entrySet()) {
                    wordCount.put(word.getKey(), word.getValue().getAsInt());
                }
            } else {
                binaryData = Base64.getDecoder().decode(obj.get("binaryData").getAsString());
            }
            JsonObject meta = obj.get("meta").getAsJsonObject();
            DocumentImpl doc;
            if (txt == null) {
                doc = new DocumentImpl(URI.create(uri), binaryData);
            } else {
                doc = new DocumentImpl(URI.create(uri), txt, wordCount);
            }
            HashMap<String, String> metaDataMap = new HashMap<>();
            for (Entry<String, JsonElement> metaPairs : meta.entrySet()) {
                metaDataMap.put(metaPairs.getKey(), metaPairs.getValue().getAsString());
            }
            doc.setMetadata(metaDataMap);
            return doc;
        }
    }

    public DocumentPersistenceManager(File baseDir) {
        if (baseDir == null) {
            this.dir = System.getProperty("user.dir");
            ;
        } else {
            this.dir = baseDir.getName();
        }
    }

    public void serialize(URI key, Document val) throws IOException {
        String fileName = getFileName(key);
        if (!(val instanceof Document)) {
            throw new IllegalArgumentException();
        }
        val = (DocumentImpl) val;
        try {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(DocumentImpl.class, new DocumentSerializer());
            Gson gson = builder.create();
            Path path = Paths.get(fileName);
            Files.createDirectories(path.getParent());
            File file = new File(fileName);
            // System.out.println(file.getAbsolutePath());
            file.createNewFile();
            Writer writer = new FileWriter(file);
            gson.toJson(val, writer);
            writer.close();
            // System.out.println(fileExists(fileName) + "exists in serialize");
            // System.out.println("aksfjb");
            // System.out.println(fileName);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public Document deserialize(URI key) throws IOException {
        String fileName = getFileName(key);
        if (!fileExists(fileName)) {
            // System.out.println("File does not exist");
            return null;
        }
        // System.out.println(fileName);
        // System.out.println("File does exist");
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(DocumentImpl.class, new DocumentDeserializer());
        Gson gson = builder.create();
        JsonReader reader = new JsonReader(new FileReader(fileName));
        Document val = gson.fromJson(reader, DocumentImpl.class);
        reader.close();
        delete(key);
        return val;
    }

    /**
     * delete the file stored on disk that corresponds to the given key
     * 
     * @param key
     * @return true or false to indicate if deletion occured or not
     * @throws IOException
     */
    public boolean delete(URI key) throws IOException {
        String fileName = getFileName(key);
        if (!fileExists(fileName)) {
            return false;
        }
        Path path = Paths.get(fileName);
        removeFileAndParentsIfEmpty(path);
        return true;
    }

    private String getFileName(URI key) {
        String uri = key.toString();
        if (uri.startsWith("https://")) {
            uri = uri.substring(8);
        }
        if (!this.dir.endsWith(File.separator)) {
            this.dir = this.dir + File.separator;
        }
        String fileName = this.dir + uri + ".json";
        return fileName;
    }

    private boolean fileExists(String fileName) {
        File file = new File(fileName);
        return file.isFile();
    }

    private void removeFileAndParentsIfEmpty(Path path) throws IOException {
        if (path == null || path.endsWith(this.dir))
            return;
        if (Files.isRegularFile(path)) {
            Files.deleteIfExists(path);
        } else if (Files.isDirectory(path) && Files.list(path).count() == 0){
            Files.deleteIfExists(path);
        }
        else if (Files.isDirectory(path) && Files.list(path).count() > 0){
            return;
        }
        removeFileAndParentsIfEmpty(path.getParent());
    }
}
