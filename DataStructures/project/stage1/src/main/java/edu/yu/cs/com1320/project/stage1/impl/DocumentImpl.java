package edu.yu.cs.com1320.project.stage1.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import edu.yu.cs.com1320.project.stage1.Document;

public class DocumentImpl implements Document {
    private URI uri;
    private String txt;
    private byte[] binaryData;
    private Map<String,String> meta;
    public DocumentImpl(URI uri, String txt){
        if (uri == null || txt == null || txt.isEmpty() || uri.toString().isEmpty()){
            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.txt = txt;
        this.meta = new HashMap<>();


    }

    public DocumentImpl(URI uri, byte[] binaryData){
        if (uri == null || binaryData == null || binaryData.length == 0 || uri.toString().isEmpty()){
            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.binaryData = binaryData;
        this.meta = new HashMap<>();
    }


    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (this.txt != null ? this.txt.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(this.binaryData);
        return result;
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
    public HashMap<String, String> getMetadata(){
        return new HashMap<String,String>(this.meta);
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
