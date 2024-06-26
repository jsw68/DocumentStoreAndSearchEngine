package edu.yu.cs.com1320.project.stage4.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import edu.yu.cs.com1320.project.stage4.DocumentStore;
import edu.yu.cs.com1320.project.stage4.Document;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.impl.TrieImpl;
import edu.yu.cs.com1320.project.undo.CommandSet;
import edu.yu.cs.com1320.project.undo.GenericCommand;
import edu.yu.cs.com1320.project.undo.Undoable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import java.util.function.Consumer;

public class DocumentStoreImpl implements DocumentStore {
    private HashTable<URI, Document> docStoreHashTable;
    private StackImpl<Undoable> commandStack;
    private TrieImpl<Document> wordOccurenceTrie;
    private HashMap<Map.Entry<String, String>, Set<Document>> metadataToDocHashMap;

    public DocumentStoreImpl() {
        this.docStoreHashTable = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
        this.wordOccurenceTrie = new TrieImpl<>();
        this.metadataToDocHashMap = new HashMap<>();
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
            previous = this.docStoreHashTable.put(uri, null);

        } else {
            Document doc = readBytesAndFormat(input, uri, format);
            previous = this.docStoreHashTable.put(uri, doc);
        }

        Consumer<URI> undo = (URI url) -> {
            this.docStoreHashTable.put(url, previous);
            if (previous != null) {
                removeDocFromTrie(previous);
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
            for (String word : words) {
                this.wordOccurenceTrie.put(word, doc);
            }
        }
    }

    private void removeDocFromTrie(Document doc) {
        if (doc.getDocumentTxt() != null) {
            Set<String> words = doc.getWords();
            for (String word : words) {
                this.wordOccurenceTrie.delete(word, doc);
            }
        }
    }

    @Override
    public Document get(URI uri) {
        return this.docStoreHashTable.get(uri);
    }

    @Override
    public String setMetadata(URI uri, String key, String value) {
        if (uri == null || uri.toString().isEmpty() || key == null || key.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Document doc = this.docStoreHashTable.get(uri);
        if (doc == null) {
            throw new IllegalArgumentException();
        }
        String oldValue = doc.setMetadataValue(key, value);
        addToMetaDocMap(doc, key, value);
        Consumer<URI> undo = (URI url) -> {
            this.docStoreHashTable.get(url).setMetadataValue(key, oldValue);
            removeFromMetaDocMap(doc, key, value);
        };
        GenericCommand<URI> command = new GenericCommand<URI>(uri, undo);
        this.commandStack.push(command);
        return oldValue;
    }

    private void addToMetaDocMap(Document doc, String key, String value) {
        for (Map.Entry<String, String> entry : this.metadataToDocHashMap.keySet()) {
            if (entry.getKey().equals(key) && entry.getValue().equals(value)) {
                this.metadataToDocHashMap.get(entry).add(doc);
            } else {
                Set<Document> newSet = new HashSet<Document>();
                newSet.add(doc);
                this.metadataToDocHashMap.put(entry, newSet);
            }
        }
    }

    private void removeFromMetaDocMap(Document doc, String key, String value) {
        for (Map.Entry<String, String> entry : this.metadataToDocHashMap.keySet()) {
            if (entry.getKey().equals(key) && entry.getValue().equals(value)) {
                this.metadataToDocHashMap.get(entry).remove(doc);
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
    public String getMetadata(URI uri, String key) {
        if (uri == null || uri.toString().isEmpty() || key == null || key.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Document doc = this.docStoreHashTable.get(uri);
        if (doc == null) {
            throw new IllegalArgumentException();
        }
        return doc.getMetadataValue(key);
    }

    @Override
    public boolean delete(URI uri) {
        Document deleted = this.docStoreHashTable.put(uri, null);

        if (deleted == null) {
            return false;
        } else {
            totallyDeleteDocumentInMetaMap(deleted);
            totallyDeleteDocumentInTrie(deleted);
            Consumer<URI> undo = (URI url) -> {
                this.docStoreHashTable.put(url, deleted);
                addDocToTrie(deleted);
                for (String key : deleted.getMetadata().keySet()) {
                    addToMetaDocMap(deleted, key, deleted.getMetadata().get(key));
                }
            };
            GenericCommand<URI> command = new GenericCommand<URI>(uri, undo);
            this.commandStack.push(command);
            return true;
        }

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
        Document doc;
        switch (format) {
            case TXT:
                doc = new DocumentImpl(uri, new String(binaryData));
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

    @Override
    public void undo() throws IllegalStateException {
        Undoable command = this.commandStack.pop();
        if (command instanceof CommandSet){
            CommandSet<URI> commandSet = (CommandSet<URI>)command;
            commandSet.undoAll();
        }
        else if (command instanceof GenericCommand){
            GenericCommand<URI> genericCommand = (GenericCommand<URI>)command;
            genericCommand.undo();
        }
        else{
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
    public List<Document> search(String keyword) {
        List<Document> docs = this.wordOccurenceTrie.getSorted(keyword, new Comparator<Document>() {
            @Override
            public int compare(Document doc1, Document doc2) {
                return doc2.wordCount(keyword) - doc1.wordCount(keyword);
            }
        });
        return docs;
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
    public List<Document> searchByPrefix(String keywordPrefix) {
        List<Document> docs = this.wordOccurenceTrie.getAllWithPrefixSorted(keywordPrefix, new Comparator<Document>() {
            @Override
            public int compare(Document doc1, Document doc2) {
                return prefixCount(doc2, keywordPrefix) - prefixCount(doc1, keywordPrefix);
            }
        });
        return docs;
    }

    private int prefixCount(Document doc, String prefix) {
        String[] wordList = doc.getDocumentTxt().replaceAll("[^a-zA-Z0-9 ]", "").split(" ");
        int count = 0;
        for (String word : wordList) {
            if (word.startsWith(prefix)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Completely remove any trace of any document which contains the given keyword
     * Search is CASE SENSITIVE.
     * 
     * @param keyword
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAll(String keyword) {
        Set<Document> targetDocs = this.wordOccurenceTrie.get(keyword);
        Set<URI> deletedURIs = new HashSet<URI>();
        for (Document doc : targetDocs) {
            deletedURIs.add(doc.getKey());
        }
        CommandSet<URI> commandSet = new CommandSet<URI>();
        for (Document doc : targetDocs) {
            Consumer<URI> undo = (URI url) -> {
                this.docStoreHashTable.put(url, doc);
                addDocToTrie(doc);
                for (String key : doc.getMetadata().keySet()) {
                    addToMetaDocMap(doc, key, doc.getMetadata().get(key));
                }
            };
            GenericCommand<URI> command = new GenericCommand<URI>(doc.getKey(), undo);
            commandSet.addCommand(command);
        }
        for (URI uri : deletedURIs) {
            Document doc = this.docStoreHashTable.get(uri);
            if (doc != null) {
                this.docStoreHashTable.put(uri, null);
                totallyDeleteDocumentInTrie(doc);
                totallyDeleteDocumentInMetaMap(doc);
            }
        }
        this.commandStack.push(commandSet);
        return deletedURIs;
    }

    /**
     * Completely remove any trace of any document which contains a word that has
     * the given prefix
     * Search is CASE SENSITIVE.
     * 
     * @param keywordPrefix
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithPrefix(String keywordPrefix) {
        // UNDO functionality not yet implemented
        List<Document> targetDocs = this.wordOccurenceTrie.getAllWithPrefixSorted(keywordPrefix, null);
        Set<URI> deletedURIs = new HashSet<URI>();
        CommandSet<URI> commandSet = new CommandSet<URI>();
        for (Document doc : targetDocs) {
            Consumer<URI> undo = (URI url) -> {
                this.docStoreHashTable.put(url, doc);
                addDocToTrie(doc);
                for (String key : doc.getMetadata().keySet()) {
                    addToMetaDocMap(doc, key, doc.getMetadata().get(key));
                }
            };
            GenericCommand<URI> command = new GenericCommand<URI>(doc.getKey(), undo);
            commandSet.addCommand(command);
        }
        for (Document doc : targetDocs) {
            deletedURIs.add(doc.getKey());
            this.docStoreHashTable.put(doc.getKey(), null);
            totallyDeleteDocumentInTrie(doc);
            totallyDeleteDocumentInMetaMap(doc);
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
    public List<Document> searchByMetadata(Map<String, String> keysValues) {
        Set<Document> docs = new HashSet<>(this.docStoreHashTable.values());
        for (Map.Entry<String, String> entry : keysValues.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Set<Document> tempSet = new HashSet<>();
            for (Document doc : docs) {
                if (doc.getMetadata().containsKey(key) && doc.getMetadata().get(key).equals(value)) {
                    tempSet.add(doc);
                }
            }
            docs.retainAll(tempSet); // Retain documents that match the current key-value pair
        }
        return new ArrayList<>(docs);
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
    public List<Document> searchByKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        List<Document> keywordMatches = search(keyword);
        List<Document> metadataMatches = searchByMetadata(keysValues);
        keywordMatches.retainAll(metadataMatches);
        // TODO check that order is maintained
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
    public List<Document> searchByPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        List<Document> prefixMatches = searchByPrefix(keywordPrefix);
        List<Document> metadataMatches = searchByMetadata(keysValues);
        prefixMatches.retainAll(metadataMatches);
        // TODO check that order is maintained
        return prefixMatches;
    }

    /**
     * Completely remove any trace of any document which has the given key-value
     * pairs in its metadata
     * Search is CASE SENSITIVE.
     * 
     * @return a Set of URIs of the documents that were deleted.
     */
    public Set<URI> deleteAllWithMetadata(Map<String, String> keysValues) {
        List<Document> targetDocs = searchByMetadata(keysValues);
        Set<URI> deletedURIs = new HashSet<URI>();
        CommandSet<URI> commandSet = new CommandSet<URI>();
        for (Document doc : targetDocs) {
            Consumer<URI> undo = (URI url) -> {
                this.docStoreHashTable.put(url, doc);
                addDocToTrie(doc);
                for (String key : doc.getMetadata().keySet()) {
                    addToMetaDocMap(doc, key, doc.getMetadata().get(key));
                }
            };
            GenericCommand<URI> command = new GenericCommand<URI>(doc.getKey(), undo);
            commandSet.addCommand(command);
        }
        for (Document doc : targetDocs) {
            deletedURIs.add(doc.getKey());
            this.docStoreHashTable.put(doc.getKey(), null);
            totallyDeleteDocumentInTrie(doc);
            totallyDeleteDocumentInMetaMap(doc);
        }
        
        this.commandStack.push(commandSet);
        return deletedURIs;
    }

    private void totallyDeleteDocumentInTrie(Document doc) {
        if (doc.getDocumentTxt() != null) {
            Set<String> words = doc.getWords();
            for (String word : words) {
                this.wordOccurenceTrie.delete(word, doc);
            }
        }
    }

    private void totallyDeleteDocumentInMetaMap(Document doc) {
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
    public Set<URI> deleteAllWithKeywordAndMetadata(String keyword, Map<String, String> keysValues) {
        // UNDO functionality not yet implemented
        List<Document> targetDocs = searchByKeywordAndMetadata(keyword, keysValues);
        Set<URI> deletedURIs = new HashSet<URI>();
        CommandSet<URI> commandSet = new CommandSet<URI>();
        for (Document doc : targetDocs) {
            Consumer<URI> undo = (URI url) -> {
                this.docStoreHashTable.put(url, doc);
                addDocToTrie(doc);
                for (String key : doc.getMetadata().keySet()) {
                    addToMetaDocMap(doc, key, doc.getMetadata().get(key));
                }
            };
            GenericCommand<URI> command = new GenericCommand<URI>(doc.getKey(), undo);
            commandSet.addCommand(command);
        }
        for (Document doc : targetDocs) {
            deletedURIs.add(doc.getKey());
            this.docStoreHashTable.put(doc.getKey(), null);
            totallyDeleteDocumentInTrie(doc);
            totallyDeleteDocumentInMetaMap(doc);
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
    public Set<URI> deleteAllWithPrefixAndMetadata(String keywordPrefix, Map<String, String> keysValues) {
        // UNDO functionality not yet implemented
        List<Document> targetDocs = searchByPrefixAndMetadata(keywordPrefix, keysValues);
        Set<URI> deletedURIs = new HashSet<URI>();
        CommandSet<URI> commandSet = new CommandSet<URI>();
        for (Document doc : targetDocs) {
            Consumer<URI> undo = (URI url) -> {
                this.docStoreHashTable.put(url, doc);
                addDocToTrie(doc);
                for (String key : doc.getMetadata().keySet()) {
                    addToMetaDocMap(doc, key, doc.getMetadata().get(key));
                }
            };
            GenericCommand<URI> command = new GenericCommand<URI>(doc.getKey(), undo);
            commandSet.addCommand(command);
        }
        for (Document doc : targetDocs) {
            deletedURIs.add(doc.getKey());
            this.docStoreHashTable.put(doc.getKey(), null);
            totallyDeleteDocumentInTrie(doc);
            totallyDeleteDocumentInMetaMap(doc);
        }
        
        this.commandStack.push(commandSet);
        return deletedURIs;
    }

}
