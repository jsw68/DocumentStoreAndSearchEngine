package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.HashTable;
import java.util.HashSet;
import java.util.Set;
import java.lang.Math;

import javax.swing.RowFilter.Entry;

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class HashTableImpl<Key, Value> implements HashTable<Key, Value> {
    private class Element<Key, Value> {
        private Key key;
        private Value value;
        private Element<Key, Value> next;
        private Element(Key key, Value value, Element<Key, Value> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
        private Value editValue(Value newVal){
            Value previous = this.value;
            this.value = newVal;
            return previous;
        }
        
    }
    private class LinkedList<Key, Value> {
        private Element<Key, Value> first;
        private LinkedList() {
            this.first = null;
        }
        private void add(Key key, Value value) {
            this.first = new Element<Key, Value>(key, value, this.first);
        }
        private Element<Key, Value> getEl(Key key){
            Element<Key, Value> current = this.first;
            if (current == null){
                return null;
            }
            while (!current.key.equals(key)){
                current = current.next;
                if (current == null){
                    return null;
                }
            }
            return current;
        }
        private Value deleteEl (Key key){
            Element<Key, Value> current = this.first;
            if (current == null){
                return null;
            }
            Element<Key, Value> next = this.first.next;
            if (current.key.equals(key)){
                Value deletedValue = current.value;
                this.first = current.next;
                return deletedValue;
            }
            else if (next == null){
                return null;
            }
            while (!next.key.equals(key)){
                current = next;
                next = current.next;
                if (next == null){
                    return null;
                }
            }
            // if next is the target
            current.next = next.next;
            return next.value;
        }
    }
    private LinkedList<Key, Value>[] hashTable;
    public HashTableImpl() {
        int n = 5;
        this.hashTable = new LinkedList[n];
        for (int i =0; i<n; i++){
            this.hashTable[i] = new LinkedList<Key, Value>();
        }
    }
    @Override
    public Value get(Key k) {
        int index = Math.abs(k.hashCode()) % 5;
        LinkedList list = this.hashTable[index];
        if (list.getEl(k) == null){
            return null;
        }
        return (Value)list.getEl(k).value;

    }
    @Override
    public Value put(Key k, Value v) {
        int index = Math.abs(k.hashCode()) % 5;
        LinkedList list = this.hashTable[index];
        if (v == null){
            return (Value)list.deleteEl(k);
        }
        if(this.containsKey(k)) {
            Element<Key, Value> replaced = list.getEl(k);
            return replaced.editValue(v);
        }
        else{
            //add
            list.add(k, v);
            return null;
        }

    }
    @Override
    public boolean containsKey(Key key) {
        if (key == null){
            throw new NullPointerException();
        }
        int index = Math.abs(key.hashCode()) % 5;
        LinkedList list = this.hashTable[index];
        if (list.getEl(key) == null){
            return false;
        }
        return true;
    }
    @Override
    public Set<Key> keySet() {
        Set<Key> keys = new HashSet<Key>();
        for (LinkedList list : this.hashTable){
            Element<Key, Value> current = list.first;
            while (current != null){
                keys.add(current.key);
                current = current.next;
            }
        }
        Set<Key> noModkeys = Collections.unmodifiableSet(keys);
        return noModkeys;
    }
    @Override
    public Collection<Value> values() {
        ArrayList<Value> values = new ArrayList<Value>();
        for (LinkedList list : this.hashTable){
            Element<Key, Value> current = list.first;
            while (current != null){
                values.add(current.value);
                current = current.next;
            }
        }
        List<Value> noModvalues = Collections.unmodifiableList(values);
        return noModvalues;
    }
    @Override
    public int size() {
        int entries = 0;
        for (LinkedList list : this.hashTable){
            Element<Key, Value> current = list.first;
            while (current != null){
                entries++;
                current = current.next;
            }
        }
        return entries;

    }
}