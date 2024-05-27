package edu.yu.cs.com1320.project.stage6.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import edu.yu.cs.com1320.project.stage6.DocumentStore;
import edu.yu.cs.com1320.project.stage6.Document;
import edu.yu.cs.com1320.project.BTree;
import edu.yu.cs.com1320.project.impl.BTreeImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.impl.MinHeapImpl;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;
import edu.yu.cs.com1320.project.stage6.impl.DocumentPersistenceManager;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import java.util.function.Consumer;

public class DocumentStoreImpl implements DocumentStore {
    private BTree<URI, Document> docStorageTree;
    private StackImpl<Undoable> commandStack;
    private TrieImpl<DocumentPlaceholder> wordOccurenceTrie;
    private HashMap<Map.Entry<String, String>, Set<DocumentPlaceholder>> metadataToDocHashMap;
    private MinHeapImpl<DocumentPlaceholder> recentlyUsedDocumentsHeapImpl;
    private int maxDocumentCount;
    private int maxDocumentBytes;
    private int currentDocumentCount;
    private int currentDocumentBytes;
    private List<URI> docsOnDisk;
    private Map<URI, DocumentPlaceholder> docPlaceholdersMap;

    private class DocumentPlaceholder implements Comparable<DocumentPlaceholder> {
        private URI uri;
        private HashMap<String, Integer> wordMap;
        private String documentTxt;
        private byte[] binaryData;

        private DocumentPlaceholder(URI u) {
            this.uri = u;
            this.wordMap = null;
            this.documentTxt = null;
            this.binaryData = null;
        }

        private DocumentPlaceholder(URI u, String txt, HashMap<String, Integer> wordMap) {
            this.uri = u;
            this.documentTxt = txt;
            this.wordMap = wordMap;
            this.binaryData = null;
        }

        private DocumentPlaceholder(URI u, byte[] binaryData) {
            this.uri = u;
            this.binaryData = binaryData;
            this.documentTxt = null;
            this.wordMap = null;
        }



        private URI getURI() {
            return this.uri;
        }

        private String getDocumentTxt() {
            return this.documentTxt;
        }

        private byte[] getBinaryData() {
            return this.binaryData;
        }

        private HashMap<String, Integer> getWordMap() {
            return this.wordMap;
        }

        private int wordCount(String word) {
            // txt should be null if it's a binary document
            if (word == null || word.isEmpty() || this.getDocumentTxt() == null || this.getDocumentTxt().isEmpty()) {
                return 0;
            }
            Integer occurences = this.getWordMap().get(word);
            if (occurences == null) {
                return 0;
            }
            return occurences;
        }

        private int prefixCount(String prefix) {
            String[] wordList = this.getDocumentTxt().replaceAll("[^a-zA-Z0-9 ]", "").split(" ");
            int count = 0;
            for (String word : wordList) {
                if (word.startsWith(prefix)) {
                    count++;
                }
            }
            return count;
        }



        private Document getDoc() {
            if (docsOnDisk.contains(this.uri)) {
                Document doc = docStorageTree.get(this.uri);
                docsOnDisk.remove(this.uri);
                doc.setLastUseTime(System.nanoTime());
                recentlyUsedDocumentsHeapImpl.insert(this);
                // System.out.println("Inserted " + this);
                recentlyUsedDocumentsHeapImpl.reHeapify(this);
                currentDocumentCount++;
                Integer docSize;
                if (doc.getDocumentTxt() != null) {
                    docSize = doc.getDocumentTxt().length();
                } else {
                    docSize = doc.getDocumentBinaryData().length;
                }
                if (docSize > maxDocumentBytes) {
                    throw new IllegalArgumentException("Document is too large to fit in memory");
                }
                currentDocumentBytes += docSize;
                wipeDocumentsUntilSpaceAvailable();
                return doc;
            }
            else{
                Document doc = docStorageTree.get(this.uri);
                // System.out.println(this.uri);
                // System.out.println(doc);
                return doc;
            }
        }

        @Override
        public int compareTo(DocumentPlaceholder other) {
            Document thisDoc = this.getDoc();
            Document otherDoc = other.getDoc();
            // check for nulls
            if (thisDoc == null) {
                return -1;
            }
            if (otherDoc == null) {
                return 1;
            }
            return thisDoc.compareTo(otherDoc);
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (other instanceof DocumentPlaceholder) {
                DocumentPlaceholder otherDocPlaceholder = (DocumentPlaceholder) other;
                // System.out.println("GRW");
                // System.out.println(this.uri + " " + otherDocPlaceholder.getURI());
                return this.uri.equals(otherDocPlaceholder.getURI());
            }
            return false;
        }
    }

    public DocumentStoreImpl() {
        setup();
        this.docStorageTree.setPersistenceManager(new DocumentPersistenceManager(null));
    }

    public DocumentStoreImpl(File baseDir) {
        setup();
        this.docStorageTree.setPersistenceManager(new DocumentPersistenceManager(baseDir));
    }

    private void setup() {
        this.docStorageTree = new BTreeImpl<>();
        this.commandStack = new StackImpl<>();
        this.wordOccurenceTrie = new TrieImpl<>();
        this.metadataToDocHashMap = new HashMap<>();
        this.recentlyUsedDocumentsHeapImpl = new MinHeapImpl<>();
        this.maxDocumentCount = Integer.MAX_VALUE;
        this.maxDocumentBytes = Integer.MAX_VALUE;
        this.currentDocumentCount = 0;
        this.currentDocumentBytes = 0;
        this.docsOnDisk = new ArrayList<>();
        this.docPlaceholdersMap = new HashMap<>();
    }

    // close to 30 line limit. abstracted reading bytes out to reduce.
    @Override
    public int put(InputStream input, URI uri, DocumentFormat format) throws IOException {
        if (uri == null || format == null || uri.toString().isEmpty()) {
            throw new IllegalArgumentException();
        }
        Document previous;
        DocumentPlaceholder previousDocPlaceholder;
        if (input == null) {
            // delete doc at URI
            // DocumentPlaceholder previousDocPlaceholder = new DocumentPlaceholder(uri);
            Document old = this.docStorageTree.get(uri);
            previousDocPlaceholder = DocToPlaceholder(old);
            if (this.docsOnDisk.contains(uri)) {
                this.docsOnDisk.remove(uri);
            }
            old.setLastUseTime(System.nanoTime());
            this.recentlyUsedDocumentsHeapImpl.reHeapify(previousDocPlaceholder);
            previous = this.docStorageTree.put(uri, null);

        } else {
            Document doc = readBytesAndFormat(input, uri, format);
            previousDocPlaceholder = DocToPlaceholder(doc);
            previous = this.docStorageTree.put(uri, doc);
            this.recentlyUsedDocumentsHeapImpl.insert(previousDocPlaceholder);
            // System.out.println("Inserted " + previousDocPlaceholder);
            doc.setLastUseTime(System.nanoTime());
            this.recentlyUsedDocumentsHeapImpl.reHeapify(previousDocPlaceholder);
        }

        Consumer<URI> undo = (URI url) -> {
            this.docStorageTree.put(url, previous);
            if (previous != null) {
                removeDocFromTrie(previous);
                previous.setLastUseTime(System.nanoTime());
                this.recentlyUsedDocumentsHeapImpl.reHeapify(previousDocPlaceholder);
            }
            else{
                this.docsOnDisk.remove(url);
            }
        };
        GenericCommand<URI> command = new GenericCommand<URI>(uri, undo);
        this.commandStack.push(command);
        if (previous == null) {
            return 0;
        }
        return previous.hashCode();
    }

    private void addDocToTrie(Document doc) {
        if (doc.getDocumentTxt() != null) {
            Set<String> words = doc.getWords();
            DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
            for (String word : words) {
                this.wordOccurenceTrie.put(word, docPlaceholder);
            }
        }
    }

    private void removeDocFromTrie(Document doc) {
        if (doc.getDocumentTxt() != null) {
            Set<String> words = doc.getWords();
            DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
            for (String word : words) {
                this.wordOccurenceTrie.delete(word, docPlaceholder);
            }
        }
    }

    @Override
    public Document get(URI uri) throws IOException{
        Document doc = this.docStorageTree.get(uri);
        if (doc == null) {
            return null;
        }
        doc.setLastUseTime(System.nanoTime());
        DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
        this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
        return doc;
    }

    @Override
    public String setMetadata(URI uri, String key, String value) throws IOException {
        if (uri == null || uri.toString().isEmpty() || key == null || key.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Document doc = this.docStorageTree.get(uri);
        DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
        if (doc == null) {
            throw new IllegalArgumentException();
        }
        String oldValue = doc.setMetadataValue(key, value);
        addToMetaDocMap(doc, key, value);
        doc.setLastUseTime(System.nanoTime());
        this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
        Consumer<URI> undo = (URI url) -> {
            this.docStorageTree.get(url).setMetadataValue(key, oldValue);
            removeFromMetaDocMap(doc, key, value);
            doc.setLastUseTime(System.nanoTime());
            this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
        };
        GenericCommand<URI> command = new GenericCommand<URI>(uri, undo);
        this.commandStack.push(command);
        return oldValue;
    }

    private void addToMetaDocMap(Document doc, String key, String value) {
        Entry<String, String> keyValuePair = new AbstractMap.SimpleEntry<String, String>(key, value);
        if (this.metadataToDocHashMap.get(keyValuePair) == null) {
            Set<DocumentPlaceholder> newSet = new HashSet<>();
            newSet.add(DocToPlaceholder(doc));
            this.metadataToDocHashMap.put(keyValuePair, newSet);
        } else {
            this.metadataToDocHashMap.get(keyValuePair).add(DocToPlaceholder(doc));
        }
    }

    private void removeFromMetaDocMap(Document doc, String key, String value) {
        for (Map.Entry<String, String> entry : this.metadataToDocHashMap.keySet()) {
            if (entry.getKey().equals(key) && entry.getValue().equals(value)) {
                DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc); 
                this.metadataToDocHashMap.get(entry).remove(docPlaceholder);
            }
        }
    }

    // private Set<Document> getDocsFromMetaMap(String key, String value){
    // for (Map.Entry<String, String> entry : this.metadataToDocHashMap.keySet()){
    // if (entry.getKey().equals(key) && entry.getValue().equals(value)){
    // return this.metadataToDocHashMap.get(entry);
    // }
    // }
    // return new HashSet<Document>();
    // }

    @Override
    public String getMetadata(URI uri, String key) throws IOException{
        if (uri == null || uri.toString().isEmpty() || key == null || key.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Document doc = this.docStorageTree.get(uri);
        DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
        if (doc == null) {
            throw new IllegalArgumentException();
        }
        doc.setLastUseTime(System.nanoTime());
        this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
        return doc.getMetadataValue(key);
    }

    @Override
    public boolean delete(URI uri) {
        Document old = this.docStorageTree.get(uri);
        if (!this.docsOnDisk.contains(uri) && this.docStorageTree.get(uri) == null){
            return false;
        }
        DocumentPlaceholder deletedPlaceholder = DocToPlaceholder(old);
        totallyDeleteDocumentInMetaMap(deletedPlaceholder);
        totallyDeleteDocumentInTrie(deletedPlaceholder);
        old.setLastUseTime(System.nanoTime());
        this.recentlyUsedDocumentsHeapImpl.reHeapify(deletedPlaceholder);
        Document deleted = this.docStorageTree.put(uri, null);
        if (this.docsOnDisk.contains(uri)) {
            this.docsOnDisk.remove(uri);
        }
            Consumer<URI> undo = (URI url) -> {
                this.docStorageTree.put(url, deleted);
                addDocToTrie(deleted);
                for (String key : deleted.getMetadata().keySet()) {
                    addToMetaDocMap(deleted, key, deleted.getMetadata().get(key));
                }
                deleted.setLastUseTime(System.nanoTime());
                this.recentlyUsedDocumentsHeapImpl.reHeapify(deletedPlaceholder);
            };
            GenericCommand<URI> command = new GenericCommand<URI>(uri, undo);
            this.commandStack.push(command);
            return true;

    }

    private Document readBytesAndFormat(InputStream in, URI uri, DocumentFormat format) throws IOException {
        byte[] binaryData;
        try {
            binaryData = in.readAllBytes();
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            in.close();
        }
        if (binaryData.length > this.maxDocumentBytes) {
            throw new IllegalArgumentException();
        }
        Document doc;
        this.currentDocumentCount++;
        this.currentDocumentBytes += binaryData.length;
        checkMemory();
        switch (format) {
            case TXT:
                doc = new DocumentImpl(uri, new String(binaryData), null);
                addDocToTrie(doc);
                break;
            case BINARY:
                doc = new DocumentImpl(uri, binaryData);
                break;
            default:
                throw new IllegalArgumentException();
        }
        return doc;
    }

    private DocumentPlaceholder DocToPlaceholder(Document doc) {
        if (this.docPlaceholdersMap.containsKey(doc.getKey())) {
            return this.docPlaceholdersMap.get(doc.getKey());
        }
        DocumentPlaceholder docPlaceholder;
        if (doc.getDocumentTxt() != null) {
            docPlaceholder = new DocumentPlaceholder(doc.getKey(), doc.getDocumentTxt(), doc.getWordMap());
        } else {
            docPlaceholder =  new DocumentPlaceholder(doc.getKey(), doc.getDocumentBinaryData());
        }
        this.docPlaceholdersMap.put(doc.getKey(), docPlaceholder);
        return docPlaceholder;
    }

    @Override
    public void undo() throws IllegalStateException {
        Undoable command = this.commandStack.pop();
        if (command instanceof CommandSet) {
            CommandSet<URI> commandSet = (CommandSet<URI>) command;
            commandSet.undoAll();
        } else if (command instanceof GenericCommand) {
            GenericCommand<URI> genericCommand = (GenericCommand<URI>) command;
            genericCommand.undo();
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    // TODO shorten method to below 30
    public void undo(URI uri) throws IllegalStateException {
        StackImpl<Undoable> temp = new StackImpl<Undoable>();
        Undoable nextCommand = this.commandStack.pop();
        while (true) {
            if (nextCommand == null) {
                throw new IllegalStateException();
            } else if (nextCommand instanceof GenericCommand
                    && ((GenericCommand<URI>) nextCommand).getTarget().equals(uri)) {
                nextCommand = (GenericCommand<URI>) nextCommand;
                nextCommand.undo();
                break;
            } else if (nextCommand instanceof CommandSet && ((CommandSet<URI>) nextCommand).containsTarget(uri)) {
                CommandSet<URI> commandSet = (CommandSet<URI>) nextCommand;
                commandSet.undo(uri);
                // check if commandSet is empty
                if (commandSet.size() == 0) {
                    break;
                } else {
                    temp.push(commandSet);
                }
                break;
            } else {
                temp.push(nextCommand);
                nextCommand = this.commandStack.pop();
            }
        }
        Undoable tempCommands = temp.pop();
        while (tempCommands != null) {
            this.commandStack.push(tempCommands);
            tempCommands = temp.pop();
        }
    }

    // **********STAGE 4 ADDITIONS

    /**
     * Retrieve all documents whose text contains the given keyword.
     * Documents are returned in sorted, descending order, sorted by the number of
     * times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * 
     * @param keyword
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> search(String keyword) throws IOException{
        List<DocumentPlaceholder> docs = this.wordOccurenceTrie.getSorted(keyword,
                new Comparator<DocumentPlaceholder>() {
                    @Override
                    public int compare(DocumentPlaceholder doc1, DocumentPlaceholder doc2) {
                        return doc2.wordCount(keyword) - doc1.wordCount(keyword);
                    }
                });
        long currentNanoTime = System.nanoTime();
        if (docs.size() == 0) {
            return new ArrayList<>();
        }
        ArrayList<Document> docsReal = new ArrayList<>();
        for (DocumentPlaceholder doc : docs) {
            Document realDoc = doc.getDoc();
            // System.out.println("Found " + realDoc.getKey());
            // System.out.println("target" + doc.getURI());
            realDoc.setLastUseTime(currentNanoTime);
            this.recentlyUsedDocumentsHeapImpl.reHeapify(doc);
            docsReal.add(realDoc);
        }
        return docsReal;
    }

    private List<Document> searchPrivateNoFrills(String keyword) {
        List<DocumentPlaceholder> docs = this.wordOccurenceTrie.getSorted(keyword,
                new Comparator<DocumentPlaceholder>() {
                    @Override
                    public int compare(DocumentPlaceholder doc1, DocumentPlaceholder doc2) {
                        return doc2.wordCount(keyword) - doc1.wordCount(keyword);
                    }
                });
        ArrayList<Document> docsReal = new ArrayList<>();
        for (DocumentPlaceholder doc : docs) {
            docsReal.add(doc.getDoc());
        }
        return docsReal;
    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix
     * Documents are returned in sorted, descending order, sorted by the number of
     * times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     * 
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    @Override
    public List<Document> searchByPrefix(String keywordPrefix) throws IOException{
        List<Document> docs = this.searchByPrefixprivateNoFrills(keywordPrefix);
        long currentNanoTime = System.nanoTime();
        for (Document doc : docs) {
            DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
            doc.setLastUseTime(currentNanoTime);
            // System.out.println("PrefixFound" + docPlaceholder.getURI());
            // System.out.println(this.recentlyUsedDocumentsHeapImpl.getArrayIndex);
            this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
        }
        return docs;
    }

    private List<Document> searchByPrefixprivateNoFrills(String keywordPrefix) {
        List<DocumentPlaceholder> docs = this.wordOccurenceTrie.getAllWithPrefixSorted(keywordPrefix,
                new Comparator<DocumentPlaceholder>() {
                    @Override
                    public int compare(DocumentPlaceholder doc1, DocumentPlaceholder doc2) {
                        // return prefixCount(doc2.getDoc(), keywordPrefix) - prefixCount(doc1.getDoc(), keywordPrefix);
                        return doc2.prefixCount(keywordPrefix) - doc1.prefixCount(keywordPrefix);
                    }
                });
        ArrayList<Document> docsReal = new ArrayList<>();
        for (DocumentPlaceholder doc : docs) {
            docsReal.add(doc.getDoc());
        }
        return docsReal;
    }

    // private int prefixCount(Document doc, String prefix) {
    //     String[] wordList = doc.getDocumentTxt().replaceAll("[^a-zA-Z0-9 ]", "").split(" ");
    //     int count = 0;
    //     for (String word : wordList) {
    //         if (word.startsWith(prefix)) {
    //             count++;
    //         }
    //     }
    //     return count;
    // }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * Search is CASE SENSITIVE.
     * 
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAll(String keyword) {
        Set<DocumentPlaceholder> targetDocsPlaceholders = this.wordOccurenceTrie.get(keyword);
        ArrayList<Document> targetDocs = new ArrayList<>();
        for (DocumentPlaceholder doc : targetDocsPlaceholders) {
            targetDocs.add(doc.getDoc());
        }
        Set<URI> deletedURIs = new HashSet<URI>();
        for (Document doc : targetDocs) {
            deletedURIs.add(doc.getKey());
        }
        CommandSet<URI> commandSet = new CommandSet<URI>();
        for (Document doc : targetDocs) {
            Consumer<URI> undo = (URI url) -> {
                this.docStorageTree.put(url, doc);
                addDocToTrie(doc);
                for (String key : doc.getMetadata().keySet()) {
                    addToMetaDocMap(doc, key, doc.getMetadata().get(key));
                }
                doc.setLastUseTime(System.nanoTime());
                DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
                this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
            };
            GenericCommand<URI> command = new GenericCommand<URI>(doc.getKey(), undo);
            commandSet.addCommand(command);
        }
        deleteAllHelper(deletedURIs);
        this.commandStack.push(commandSet);
        return deletedURIs;
    }
    private void deleteAllHelper(Set<URI> URIs) {
        long currentNanoTime = System.nanoTime();
        for (URI uri : URIs) {
            Document doc = this.docStorageTree.get(uri);
            if (doc != null) {
                if (this.docsOnDisk.contains(uri)) {
                    this.docsOnDisk.remove(uri);
                }
                // System.out.println("Deleting: " + uri);
                DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
                totallyDeleteDocumentInTrie(docPlaceholder);
                totallyDeleteDocumentInMetaMap(docPlaceholder);
                doc.setLastUseTime(currentNanoTime);
                this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
                this.docStorageTree.put(uri, null);
            }
        }
    }

    /**
     * Completely remove any trace of any document which contains a word that has
     * the given prefix
     * Search is CASE SENSITIVE.
     * 
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        // UNDO functionality not yet implemented
        List<DocumentPlaceholder> targetDocsPlaceholders = this.wordOccurenceTrie.getAllWithPrefixSorted(keywordPrefix,
                null);
        ArrayList<Document> targetDocs = new ArrayList<>();
        for (DocumentPlaceholder doc : targetDocsPlaceholders) {
            targetDocs.add(doc.getDoc());
        }
        Set<URI> deletedURIs = new HashSet<URI>();
        CommandSet<URI> commandSet = new CommandSet<URI>();
        for (Document doc : targetDocs) {
            Consumer<URI> undo = (URI url) -> {
                this.docStorageTree.put(url, doc);
                addDocToTrie(doc);
                for (String key : doc.getMetadata().keySet()) {
                    addToMetaDocMap(doc, key, doc.getMetadata().get(key));
                }
                doc.setLastUseTime(System.nanoTime());
                DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc); 
                this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
            };
            GenericCommand<URI> command = new GenericCommand<URI>(doc.getKey(), undo);
            commandSet.addCommand(command);
        }
        long currentNanoTime = System.nanoTime();
        for (Document doc : targetDocs) {
            deletedURIs.add(doc.getKey());
            if (this.docsOnDisk.contains(doc.getKey())) {
                this.docsOnDisk.remove(doc.getKey());
            }
            DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
            totallyDeleteDocumentInTrie(docPlaceholder);
            totallyDeleteDocumentInMetaMap(docPlaceholder);
            doc.setLastUseTime(currentNanoTime);
            this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
            this.docStorageTree.put(doc.getKey(), null);
        }

        this.commandStack.push(commandSet);
        return deletedURIs;
    }

    /**
     * @param keysValues metadata key-value pairs to search for
     * @return a List of all documents whose metadata contains ALL OF the given
     *         values for the given keys. If no documents contain all the given
     *         key-value pairs, return an empty list.
     */
    @Override
    public List<Document> searchByMetadata(Map<String, String> keysValues) throws IOException{
        List<Document> docs = this.searchByMetadataPrivateNoFrills(keysValues);
        long currentNanoTime = System.nanoTime();
        for (Document doc : docs) {
            doc.setLastUseTime(currentNanoTime);
            DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
            this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
        }
        return new ArrayList<>(docs);
    }
    private List<Document> searchByMetadataPrivateNoFrills(Map<String, String> keysValues) {
        List<DocumentPlaceholder> docs = new ArrayList<>();
        boolean first = true;
        for (Map.Entry<String, String> entry : keysValues.entrySet()) {
            Set<DocumentPlaceholder> tempSet = this.metadataToDocHashMap.get(entry);
            // System.out.println("meta initial" + tempSet);
            // System.out.println(this.metadataToDocHashMap);
            if (tempSet == null) {
                return new ArrayList<>();
            }
            if (first) {
                docs.addAll(tempSet);
                first = false;
            } else {
                docs.retainAll(tempSet);
            }
        }
        ArrayList<Document> docsReal = new ArrayList<>();
        for (DocumentPlaceholder doc : docs) {
            docsReal.add(doc.getDoc());
        }
        return docsReal;
    }

    /**
     * Retrieve all documents whose text contains the given keyword AND which has
     * the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of
     * times the keyword appears in the document.
     * Search is CASE SENSITIVE.
     * 
     * @param keyword
     * @param keysValues
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    // should be a union of two prev methods
    @Override
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException{
        List<Document> keywordMatches = searchPrivateNoFrills(keyword);
        List<Document> metadataMatches = searchByMetadataPrivateNoFrills(keysValues);
        keywordMatches.retainAll(metadataMatches);
        long currentNanoTime = System.nanoTime();
        for (Document doc : keywordMatches) {
            doc.setLastUseTime(currentNanoTime);
            DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
            this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
        }
        return keywordMatches;
    }

    /**
     * Retrieve all documents that contain text which starts with the given prefix
     * AND which has the given key-value pairs in its metadata
     * Documents are returned in sorted, descending order, sorted by the number of
     * times the prefix appears in the document.
     * Search is CASE SENSITIVE.
     * 
     * @param keywordPrefix
     * @return a List of the matches. If there are no matches, return an empty list.
     */
    // should be a union of two prev methods
    @Override
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException{
        List<Document> prefixMatches = searchByPrefixprivateNoFrills(keywordPrefix);
        List<Document> metadataMatches = searchByMetadataPrivateNoFrills(keysValues);
        prefixMatches.retainAll(metadataMatches);
        // TODO check that order is maintained
        long currentNanoTime = System.nanoTime();
        for (Document doc : prefixMatches) {
            doc.setLastUseTime(currentNanoTime);
            DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
            this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
        }
        return prefixMatches;
    }

    /**
     * Completely remove any trace of any document which has the given key-value
     * pairs in its metadata
     * Search is CASE SENSITIVE.
     * 
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) throws IOException{
        List<Document> targetDocs = searchByMetadata(keysValues);
        Set<URI> deletedURIs = new HashSet<URI>();
        CommandSet<URI> commandSet = new CommandSet<URI>();
        for (Document doc : targetDocs) {
            Consumer<URI> undo = (URI url) -> {
                this.docStorageTree.put(url, doc);
                addDocToTrie(doc);
                for (String key : doc.getMetadata().keySet()) {
                    addToMetaDocMap(doc, key, doc.getMetadata().get(key));
                }
                doc.setLastUseTime(System.nanoTime());
                DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
                this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
            };
            GenericCommand<URI> command = new GenericCommand<URI>(doc.getKey(), undo);
            commandSet.addCommand(command);

        }
        long currentNanoTime = System.nanoTime();
        for (Document doc : targetDocs) {
            deletedURIs.add(doc.getKey());
            if (this.docsOnDisk.contains(doc.getKey())) {
                this.docsOnDisk.remove(doc.getKey());
            }
            DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
            totallyDeleteDocumentInTrie(docPlaceholder);
            totallyDeleteDocumentInMetaMap(docPlaceholder);
            doc.setLastUseTime(currentNanoTime);
            this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
            this.docStorageTree.put(doc.getKey(), null);
        }

        this.commandStack.push(commandSet);
        return deletedURIs;
    }

    private void totallyDeleteDocumentInTrie(DocumentPlaceholder doc) {
        if (doc.getDocumentTxt() != null) {
            Set<String> words = doc.getWordMap().keySet();
            for (String word : words) {
                this.wordOccurenceTrie.delete(word, doc);
            }
        }
    }

    private void totallyDeleteDocumentInMetaMap(DocumentPlaceholder doc) {
        for (Map.Entry<String, String> entry : this.metadataToDocHashMap.keySet()) {
            if (this.metadataToDocHashMap.get(entry).contains(doc)) {
                this.metadataToDocHashMap.get(entry).remove(doc);
            }
        }
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * 
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) throws IOException{
        // UNDO functionality not yet implemented
        List<Document> targetDocs = searchByKeywordAndMetadata(keyword, keysValues);
        Set<URI> deletedURIs = new HashSet<URI>();
        CommandSet<URI> commandSet = new CommandSet<URI>();
        for (Document doc : targetDocs) {
            Consumer<URI> undo = (URI url) -> {
                this.docStorageTree.put(url, doc);
                addDocToTrie(doc);
                for (String key : doc.getMetadata().keySet()) {
                    addToMetaDocMap(doc, key, doc.getMetadata().get(key));
                }
                doc.setLastUseTime(System.nanoTime());
                DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
                this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
            };

            GenericCommand<URI> command = new GenericCommand<URI>(doc.getKey(), undo);
            commandSet.addCommand(command);
        }
        long currentNanoTime = System.nanoTime();
        for (Document doc : targetDocs) {
            deletedURIs.add(doc.getKey());
            if (this.docsOnDisk.contains(doc.getKey())) {
                this.docsOnDisk.remove(doc.getKey());
            }
            DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
            totallyDeleteDocumentInTrie(docPlaceholder);
            totallyDeleteDocumentInMetaMap(docPlaceholder);
            doc.setLastUseTime(currentNanoTime);
            this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
            this.docStorageTree.put(doc.getKey(), null);
        }

        this.commandStack.push(commandSet);
        return deletedURIs;
    }

    /**
     * Completely remove any trace of any document which contains a word that has
     * the given prefix AND which has the given key-value pairs in its metadata
     * Search is CASE SENSITIVE.
     * 
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    @Override
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) throws IOException{
        // UNDO functionality not yet implemented
        List<Document> targetDocs = searchByPrefixAndMetadata(keywordPrefix, keysValues);
        Set<URI> deletedURIs = new HashSet<URI>();
        CommandSet<URI> commandSet = new CommandSet<URI>();
        for (Document doc : targetDocs) {
            Consumer<URI> undo = (URI url) -> {
                this.docStorageTree.put(url, doc);
                addDocToTrie(doc);
                for (String key : doc.getMetadata().keySet()) {
                    addToMetaDocMap(doc, key, doc.getMetadata().get(key));
                }
                doc.setLastUseTime(System.nanoTime());
                DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
                this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
            };
            GenericCommand<URI> command = new GenericCommand<URI>(doc.getKey(), undo);
            commandSet.addCommand(command);
        }
        long currentNanoTime = System.nanoTime();
        for (Document doc : targetDocs) {
            deletedURIs.add(doc.getKey());
            if (this.docsOnDisk.contains(doc.getKey())) {
                this.docsOnDisk.remove(doc.getKey());
            }
            DocumentPlaceholder docPlaceholder = DocToPlaceholder(doc);
            totallyDeleteDocumentInTrie(docPlaceholder);
            totallyDeleteDocumentInMetaMap(docPlaceholder);
            doc.setLastUseTime(currentNanoTime);
            this.recentlyUsedDocumentsHeapImpl.reHeapify(docPlaceholder);
            this.docStorageTree.put(doc.getKey(), null);
        }
        this.commandStack.push(commandSet);
        return deletedURIs;
    }

    // **********STAGE 5 ADDITIONS

    /**
     * set maximum number of documents that may be stored
     * 
     * @param limit
     * @throws IllegalArgumentException if limit < 1
     */
    @Override
    public void setMaxDocumentCount(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException();
        }
        this.maxDocumentCount = limit;
        checkMemory();
    }

    /**
     * set maximum number of bytes of memory that may be used by all the documents
     * in memory combined
     * 
     * @param limit
     * @throws IllegalArgumentException if limit < 1
     */
    @Override
    public void setMaxDocumentBytes(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException();
        }
        this.maxDocumentBytes = limit;
        checkMemory();
    }

    private void checkMemory() {
        if (this.maxDocumentCount != -1 && this.currentDocumentCount > this.maxDocumentCount) {
            wipeDocumentsUntilSpaceAvailable();
        }
        if (this.maxDocumentBytes != -1 && this.currentDocumentBytes > this.maxDocumentBytes) {
            wipeDocumentsUntilSpaceAvailable();
        }
    }


    private void wipeDocumentsUntilSpaceAvailable() {
        while (this.currentDocumentCount > this.maxDocumentCount || this.currentDocumentBytes > this.maxDocumentBytes) {
            Document doc = this.recentlyUsedDocumentsHeapImpl.remove().getDoc();
            // if (doc == null) {
            //     System.out.println("Doc has been deleted");
            //     // continue;
            // }
            // if (this.docsOnDisk.contains(doc.getKey())) {
            //     this.recentlyUsedDocumentsHeapImpl.insert(DocToPlaceholder(doc));
            //     continue;
            // }
            if (doc.getDocumentBinaryData() != null) {
                this.currentDocumentBytes -= doc.getDocumentBinaryData().length;
            } else {
                this.currentDocumentBytes -= doc.getDocumentTxt().getBytes().length;
            }
            this.currentDocumentCount--;
            if (doc != null) {
                try {
                    this.docStorageTree.moveToDisk(doc.getKey());
                    this.docsOnDisk.add(doc.getKey());
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }
    }

    // private void totallyWipeDocument(Document doc) {
    //     this.docStorageTree.put(doc.getKey(), null);
    //     if (this.docsOnDisk.contains(doc.getKey())) {
    //         this.docsOnDisk.remove(doc.getKey());
    //     }
    //     totallyDeleteDocumentInTrie(doc);
    //     totallyDeleteDocumentInMetaMap(doc);
    //     totallyDeleteDocumentInCommandStack(doc);
    // }

    private void totallyDeleteDocumentInCommandStack(Document doc) {
        StackImpl<Undoable> temp = new StackImpl<Undoable>();
        Undoable nextCommand = this.commandStack.pop();
        URI targetURI = doc.getKey();
        for (int i = 0; i < this.commandStack.size(); i++) {
            if (nextCommand == null) {
                continue;
            } else if (nextCommand instanceof CommandSet && ((CommandSet<URI>) nextCommand).containsTarget(targetURI)) {
                CommandSet<URI> commandSet = (CommandSet<URI>) nextCommand;
                for (GenericCommand<URI> command : commandSet) {
                    if (command.getTarget().equals(targetURI)) {
                        command = null;
                    }
                }
                // check if commandSet is empty
                if (commandSet.size() == 0) {
                    continue;
                } else {
                    temp.push(commandSet);
                }
            } else {
                temp.push(nextCommand);
                nextCommand = this.commandStack.pop();
            }
        }
        Undoable tempCommand = temp.pop();
        while (tempCommand != null) {
            this.commandStack.push(tempCommand);
            tempCommand = temp.pop();
        }
    }

}
