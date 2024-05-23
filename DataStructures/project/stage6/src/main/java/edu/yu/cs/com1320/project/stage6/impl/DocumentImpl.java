package edu.yu.cs.com1320.project.stage6.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import edu.yu.cs.com1320.project.impl.TrieImpl;
// import edu.yu.cs.com1320.project.HashTable;
import java.util.Set;
// import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import edu.yu.cs.com1320.project.stage6.Document;
import java.lang.Math;

public class DocumentImpl implements Document {
    private URI uri;
    private String txt;
    private byte[] binaryData;
    private HashMap<String, String> meta;
    private HashMap<String, Integer> wordAppearanceCount;
    private TrieImpl<Integer> wordCountTrie;
    private long lastUseTime;
    private int wordCount;

    public DocumentImpl(URI uri, String txt, Map<String, Integer> wordCountMap) {
        if (uri == null || txt == null || txt.isEmpty() || uri.toString().isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.txt = txt;
        this.meta = new HashMap<>();
        this.lastUseTime = System.nanoTime();
        // word count and number of times a word appears
        this.wordAppearanceCount = new HashMap<>();
        this.wordCountTrie = new TrieImpl<>();
        List<String> wordList = new ArrayList<>(
                Arrays.asList(this.txt.replaceAll("[^a-zA-Z0-9 ]", "").trim().split(" ")));
        wordList.removeIf(item -> item == null || "".equals(item) || item.isEmpty());
        this.wordCount = wordList.size();
        if (wordCountMap == null) {
            for (String word : wordList) {
                Integer occurences = this.wordAppearanceCount.get(word);
                if (occurences != null) {
                    this.wordAppearanceCount.put(word, occurences + 1);
                    this.wordCountTrie.put(word, occurences + 1);
                } else {
                    this.wordAppearanceCount.put(word, 1);
                    this.wordCountTrie.put(word, 1);
                }
            }
        }

    }

    public DocumentImpl(URI uri, byte[] binaryData) {
        if (uri == null || binaryData == null || binaryData.length == 0 || uri.toString().isEmpty()) {
            throw new IllegalArgumentException();
        }

        this.uri = uri;
        this.binaryData = binaryData;
        this.meta = new HashMap<>();
        this.wordAppearanceCount = new HashMap<>();
        this.lastUseTime = System.nanoTime();

    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + (this.txt != null ? this.txt.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(this.binaryData);
        return Math.abs(result);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !(other instanceof Document)) {
            return false;
        }
        other = (Document) other;
        if (other.hashCode() == this.hashCode()) {
            return true;
        }
        return false;
    }

    @Override
    public String setMetadataValue(String key, String value) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return this.meta.put(key, value);
    }

    @Override
    public String getMetadataValue(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return this.meta.get(key);
    }

    @Override
    public HashMap<String, String> getMetadata() {
        Set<String> metaKeys = this.meta.keySet();
        HashMap<String, String> meta = new HashMap<>();
        for (String key : metaKeys) {
            meta.put(key, this.meta.get(key));
        }
        return meta;

    }

    @Override
    public String getDocumentTxt() {
        return this.txt;
    }

    @Override
    public byte[] getDocumentBinaryData() {
        return this.binaryData;
    }

    @Override
    public URI getKey() {
        return this.uri;
    }

    /**
     * how many times does the given word appear in the document?
     * 
     * @param word
     * @return the number of times the given words appears in the document. If it's
     *         a binary document, return 0.
     */
    @Override
    public int wordCount(String word) {
        // txt should be null if it's a binary document
        if (word == null || word.isEmpty() || this.txt == null || this.txt.isEmpty()) {
            return 0;
        }
        Integer occurences = this.wordAppearanceCount.get(word);
        if (occurences == null) {
            return 0;
        }
        return occurences;
    }

    /**
     * @return all the words that appear in the document
     */
    @Override
    public Set<String> getWords() {
        if (this.txt == null || this.txt.isEmpty()) {
            return null;
        }
        return this.wordAppearanceCount.keySet();
    }

    /** START STAGE 5 ADDITIONS */
    // * return the last time this document was used, via put/get or via a search
    // result
    // * (for stage 4 of project)
    // */
    @Override
    public long getLastUseTime() {
        return this.lastUseTime;
    }

    @Override
    public void setLastUseTime(long timeInNanoseconds) {
        this.lastUseTime = timeInNanoseconds;
    }

    @Override
    public int compareTo(Document doc) {
        if (doc == null) {
            return 1;
        }
        if (this.lastUseTime > doc.getLastUseTime()) {
            return 1;
        } else if (this.lastUseTime < doc.getLastUseTime()) {
            return -1;
        }
        return 0;
    }
    /**
     * @return a copy of the word to count map so it can be serialized
     */
    @Override
    public HashMap<String, Integer> getWordMap(){
        return this.wordAppearanceCount;
    
    }

    /**
     * This must set the word to count map durlng deserialization
     *
     * @param wordMap
     */
    @Override
    public void setWordMap(HashMap<String, Integer> wordMap){
        this.wordAppearanceCount = wordMap;
    }

    @Override
    public void setMetadata(HashMap<String, String> metadata){
        this.meta = metadata;
    }
}
