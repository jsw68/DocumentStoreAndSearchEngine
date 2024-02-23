package edu.yu.cs.com1320.project.stage2.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import edu.yu.cs.com1320.project.stage2.DocumentStore;
import edu.yu.cs.com1320.project.stage2.Document;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;

public class DocumentStoreImpl implements DocumentStore {
    private HashTable<URI, Document> store;

    public DocumentStoreImpl() {
        this.store = new HashTableImpl<>();
    }
    // close to 30 line limit. abstracted reading bytes out to reduce. 
    @Override
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException {
        if (uri == null || format == null || uri.toString().isEmpty()) {
            throw new IllegalArgumentException();
        }
        Document previous;
        if (input == null) {
            // delete doc at URI
            previous = this.store.put(uri, null);
        } else {
            byte[] binaryData = readBytes(input);
            Document doc;
            switch (format) {
                case TXT:
                doc = new DocumentImpl(uri, new String(binaryData));
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
        Document doc = this.store.get(uri);
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
        Document doc = this.store.get(uri);
        if (doc==null){
            throw new IllegalArgumentException();
        }
        return doc.getMetadataValue(key);
    }
    @Override
    public boolean delete(URI uri){
        Document deleted = this.store.put(uri, null);
        if (deleted==null){
            return false;
        }
        return true;
    }

    private byte[] readBytes(InputStream in) throws IOException{
        byte[] binaryData;
        try{
            binaryData = in.readAllBytes();
        }
        catch (IOException e){
            throw new IOException(e);
        }
        finally{
            in.close();
        }
        return binaryData;
    }
}
