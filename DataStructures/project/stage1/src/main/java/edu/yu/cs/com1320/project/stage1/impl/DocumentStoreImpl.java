package edu.yu.cs.com1320.project.stage1;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public class DocumentStoreImpl implements DocumentStore {
    private Map<URI, Document> store;

    public DocumentStoreImpl() {
        this.store = new HashMap<>();
    }

    @Override
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException {
        if (uri == null || format == null || uri.toString().isEmpty()) {
            throw new IllegalArgumentException();
        }
        Document previous;
        if (input == null) {
            // delete doc at URI
            previous = this.store.remove(uri);

        } else {
            byte[] binaryData = input.readAllBytes();
            Document doc;

            switch (format) {
                case TXT:
                    doc = new DocumentImpl(uri, binaryData.toString());
                    break;
                case BINARY:
                    doc = new DocumentImpl(uri, binaryData);
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            previous = this.store.put(uri, doc);
        }
        if (previous == null) {
            return 0;
        }
        return previous.hashCode();
    }

    @Override
    public Document get(URI uri){
        return this.store.get(uri);
    }
    @Override
    public String setMetadata(URI uri, String key, String value){
        if (uri==null || uri.toString().isEmpty() || key==null || key.isEmpty()){
            throw new IllegalArgumentException();
        }
        Document doc = this.store.get(key);
        if (doc==null){
            throw new IllegalArgumentException();
        }
        return doc.setMetadataValue(key, value);
    }

    @Override
    public String getMetadata(URI uri, String key){
        if (uri==null || uri.toString().isEmpty() || key==null || key.isEmpty()){
            throw new IllegalArgumentException();
        }
        Document doc = this.store.get(key);
        if (doc==null){
            throw new IllegalArgumentException();
        }
        return doc.getMetadataValue(key);
    }
    @Override
    public boolean delete(URI uri){
        Document deleted = this.store.remove(uri);
        if (deleted==null){
            return false;
        }
        return true;
    }
}
