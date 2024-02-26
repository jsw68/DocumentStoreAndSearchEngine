package edu.yu.cs.com1320.project.stage2.impl;

import java.net.URI;
import java.util.Arrays;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.HashTable;
import java.util.Set;
import java.util.HashSet;
import edu.yu.cs.com1320.project.stage2.Document;
import java.lang.Math;
public class DocumentImpl implements Document {
    private URI uri;
    private String txt;
    private byte[] binaryData;
    private HashTableImpl<String,String> meta;
    public DocumentImpl(URI uri, String txt){
        if (uri == null || txt == null || txt.isEmpty() || uri.toString().isEmpty()){
            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.txt = txt;
        this.meta = new HashTableImpl<>();


    }

    public DocumentImpl(URI uri, byte[] binaryData){
        if (uri == null || binaryData == null || binaryData.length == 0 || uri.toString().isEmpty()){
            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.binaryData = binaryData;
        this.meta = new HashTableImpl<>();
    }


    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (this.txt != null ? this.txt.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(this.binaryData);
        return Math.abs(result);
    }

    @Override
    public boolean equals(Object other){
        if (this == other){
            return true;
        }
        if (other == null || !(other instanceof Document)){
            return false;
        }
        other = (Document)other;
        if (other.hashCode() == this.hashCode()){
            return true;
        }
        return false;
    }

    @Override
    public String setMetadataValue(String key, String value){
        if (key == null || key.isEmpty()){
            throw new IllegalArgumentException();
        }
        return this.meta.put(key, value);
    }

    @Override
    public String getMetadataValue(String key){
        if (key == null || key.isEmpty()){
            throw new IllegalArgumentException();
        }
        return this.meta.get(key);
    }
    @Override
    public HashTable<String, String> getMetadata(){
        Set<String> metaKeys = this.meta.keySet();
        HashTable<String, String> meta = new HashTableImpl<>();
        for (String key : metaKeys){
            meta.put(key, this.meta.get(key));
        }
        return meta;
        
    }
    @Override
    public String getDocumentTxt(){
        return this.txt;
    }

    @Override
    public byte[] getDocumentBinaryData(){
        return this.binaryData;
    }

    @Override
    public URI getKey(){
        return this.uri;
    }

}
