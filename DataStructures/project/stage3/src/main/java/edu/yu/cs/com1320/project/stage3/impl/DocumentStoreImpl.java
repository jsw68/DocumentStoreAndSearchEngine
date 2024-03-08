package edu.yu.cs.com1320.project.stage3.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import edu.yu.cs.com1320.project.stage3.DocumentStore;
import edu.yu.cs.com1320.project.stage3.Document;
import edu.yu.cs.com1320.project.HashTable;
import edu.yu.cs.com1320.project.impl.HashTableImpl;
import edu.yu.cs.com1320.project.impl.StackImpl;
import edu.yu.cs.com1320.project.undo.Command;
import java.util.function.Consumer;


public class DocumentStoreImpl implements DocumentStore {
    private HashTable<URI, Document> store;
    private StackImpl<Command> commandStack;
    public DocumentStoreImpl() {
        this.store = new HashTableImpl<>();
        this.commandStack = new StackImpl<>();
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
            Document doc = readBytesAndFormat(input, uri, format);
            previous = this.store.put(uri, doc);
        }
        Consumer<URI> undo = (URI url) -> {
            this.store.put(url, previous);
        };
        Command command = new Command(uri, undo);
        this.commandStack.push(command);
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
        String oldValue = doc.setMetadataValue(key, value);
        Consumer<URI> undo = (URI url) -> {
            this.store.get(url).setMetadataValue(key, oldValue);
        };
        Command command = new Command(uri, undo);
        this.commandStack.push(command);
        return oldValue;
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
        else{
            Consumer<URI> undo = (URI url) -> {
                this.store.put(url, deleted);
            };
            Command command = new Command(uri, undo);
            this.commandStack.push(command);
            return true;
        }

    }

    private Document readBytesAndFormat(InputStream in, URI uri, DocumentFormat format) throws IOException{
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
        return doc;
    }
    @Override
    public void undo() throws IllegalStateException{
        Command command = this.commandStack.pop();
        if (command==null){
            throw new IllegalStateException();
        }
        command.undo();
    }
    @Override
    public void undo(URI uri) throws IllegalStateException{
        StackImpl<Command> temp = new StackImpl<Command>();
        Command nextCommand = this.commandStack.pop();
        while (true){
            if (nextCommand==null){
                throw new IllegalStateException();
            }
            else if (nextCommand.getUri().equals(uri)){
                nextCommand.undo();
                break;
            }
            else{
                temp.push(nextCommand);
                nextCommand = this.commandStack.pop();
            }
        }
        Command tempCommands = temp.pop();
        while (tempCommands!=null){
            this.commandStack.push(tempCommands);
            tempCommands = temp.pop();
        }
    }
}
