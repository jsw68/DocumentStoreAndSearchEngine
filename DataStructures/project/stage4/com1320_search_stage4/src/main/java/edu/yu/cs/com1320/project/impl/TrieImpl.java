package edu.yu.cs.com1320.project.impl;

// lot of generics warnings, maybe fix using ?

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

import edu.yu.cs.com1320.project.Trie;

public class TrieImpl<Value> implements Trie<Value> {

    private static final int alphabetSize = 256; // extended ASCII
    private Node<Value> root; // root of trie

    private class Node<Value> {
        private Set<Value> vals;
        private Node<Value>[] links;

        public Node() {
            this.vals = new HashSet<Value>();
            this.links = new Node[TrieImpl.alphabetSize];
        }
    }

    public TrieImpl() {
        this.root = new Node<Value>();
    }

    /**
     * add the given value at the given key
     * 
     * @param key
     * @param val
     */
    public void put(String key, Value val) {
        if (val == null) {
            this.deleteAll(key);
        } else {
            this.root = put(this.root, key, val, 0);
        }
    }

    private Node<Value> put(Node<Value> x, String key, Value val, int d) {
        if (x == null) {
            x = new Node<Value>();
        }
        // we've reached the last node in the key,
        // set the value for the key and return the node
        if (d == key.length()) {
            x.vals.add(val);
            return x;
        }
        // proceed to the next node in the chain of nodes that
        // forms the desired key
        char c = key.charAt(d);
        x.links[c] = this.put(x.links[c], key, val, d + 1);
        return x;
    }

    /**
     * Get all exact matches for the given key, sorted in descending order, where
     * "descending" is defined by the comparator.
     * NOTE FOR COM1320 PROJECT: FOR PURPOSES OF A *KEYWORD* SEARCH, THE COMPARATOR
     * SHOULD DEFINE ORDER AS HOW MANY TIMES THE KEYWORD APPEARS IN THE DOCUMENT.
     * Search is CASE SENSITIVE.
     * 
     * @param key
     * @param comparator used to sort values
     * @return a List of matching Values. Empty List if no matches.
     */
    public List<Value> getSorted(String key, Comparator<Value> comparator) {
        Set<Value> vals = this.get(key);
        if (vals == null) {
            return null;
        }
        List<Value> valList = new ArrayList<Value>(vals);
        Collections.sort(valList, comparator);
        return valList;
    }

    /**
     * get all exact matches for the given key.
     * Search is CASE SENSITIVE.
     * 
     * @param key
     * @return a Set of matching Values. Empty set if no matches.
     */
    public Set<Value> get(String key) {
        Node<Value> vals = this.get(this.root, key, 0);
        if (vals == null) {
            return new HashSet<Value>();
        }
        return vals.vals;
    }

    private Node<Value> get(Node<Value> x, String key, int d) {
        // link was null - return null, indicating a miss
        if (x == null) {
            return null;
        }
        // we've reached the last node in the key,
        // return the node
        if (d == key.length()) {
            return x;
        }
        // proceed to the next node in the chain of nodes that
        // forms the desired key
        char c = key.charAt(d);
        return this.get(x.links[c], key, d + 1);
    }

    /**
     * get all matches which contain a String with the given prefix, sorted in
     * descending order, where "descending" is defined by the comparator.
     * NOTE FOR COM1320 PROJECT: FOR PURPOSES OF A *KEYWORD* SEARCH, THE COMPARATOR
     * SHOULD DEFINE ORDER AS HOW MANY TIMES THE KEYWORD APPEARS IN THE DOCUMENT.
     * For example, if the key is "Too", you would return any value that contains
     * "Tool", "Too", "Tooth", "Toodle", etc.
     * Search is CASE SENSITIVE.
     * 
     * @param prefix
     * @param comparator used to sort values
     * @return a List of all matching Values containing the given prefix, in
     *         descending order. Empty List if no matches.
     */
    public List<Value> getAllWithPrefixSorted(String prefix, Comparator<Value> comparator){
        List<Value> results = new ArrayList<Value>();
        Node<Value> x = this.get(this.root, prefix, 0);
        if (x == null) {
            return results;
        }
        this.collect(x, results);
        // remove duplicates
        Set<Value> set = new HashSet<Value>(results);
        results = new ArrayList<Value>(set);
        if (comparator == null) {
            return results;
        }
        Collections.sort(results, comparator);
        return results;
    }

    /**
     * Delete the subtree rooted at the last character of the prefix.
     * Search is CASE SENSITIVE.
     * 
     * @param prefix
     * @return a Set of all Values that were deleted.
     */
    public Set<Value> deleteAllWithPrefix(String prefix){
        List<Value> results = getAllWithPrefixSorted(prefix, null);
        Set<Value> vals = new HashSet<Value>(results);
        Node<Value> prefixNode = this.get(this.root, prefix, 0);
        prefixNode.links = new Node[TrieImpl.alphabetSize];
        return vals;
    }

    private void collect(Node<Value> x, List<Value> results){
        //if this node has a value, add its key to the queue
        if (!x.vals.isEmpty()) {
            //add a string made up of the chars from
            //root to this node to the result set
            results.addAll(x.vals);
        }
        //visit each non-null child/link
        for (char c = 0; c < TrieImpl.alphabetSize; c++) {
            if(x.links[c]!=null){
                this.collect(x.links[c], results);
            }
        }
    }

    /**
     * Delete all values from the node of the given key (do not remove the values
     * from other nodes in the Trie)
     * 
     * @param key
     * @return a Set of all Values that were deleted.
     */
    public Set<Value> deleteAll(String key) {
        Set<Value> vals = this.get(key);
        this.root = this.deleteAll(this.root, key, 0, null);
        return vals;
    }

    /**
     * Remove the given value from the node of the given key (do not remove the
     * value from other nodes in the Trie)
     * 
     * @param key
     * @param val
     * @return the value which was deleted. If the key did not contain the given
     *         value, return null.
     */
    public Value delete(String key, Value val) {
        Set<Value> vals = this.get(key);
        if (!vals.contains(val)) {
            return null;
        }
        this.root = this.deleteAll(this.root, key, 0, val);
        return val;
    }

    private Node<Value> deleteAll(Node<Value> x, String key, int d, Value val) {
        if (x == null) {
            return null;
        }
        // we're at the node to del - set the val to null
        if (d == key.length()) {
            if (val == null) {
                x.vals = new HashSet<Value>();
            } else {
                x.vals.remove(val);
            }
        }
        // continue down the trie to the target node
        else {
            char c = key.charAt(d);
            x.links[c] = this.deleteAll(x.links[c], key, d + 1, val);
        }
        // this node has a val – do nothing, return the node
        if (x.vals.isEmpty()) {
            return x;
        }
        // remove subtrie rooted at x if it is completely empty
        for (int c = 0; c < TrieImpl.alphabetSize; c++) {
            if (x.links[c] != null) {
                return x; // not empty
            }
        }
        // empty - set this link to null in the parent
        return null;
    }
}
