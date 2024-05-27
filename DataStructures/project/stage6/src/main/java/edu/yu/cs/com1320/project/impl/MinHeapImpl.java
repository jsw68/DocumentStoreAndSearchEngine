package edu.yu.cs.com1320.project.impl;
import java.util.NoSuchElementException;

import edu.yu.cs.com1320.project.MinHeap;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E>{
    private E[] elements;
    private int startingArryLength = 10;
    public MinHeapImpl(){
        this.elements = (E[])new Comparable[startingArryLength];
    }

    @Override
    public void reHeapify(E element) {
        int index = getArrayIndex(element);
        if (index == -1){
            throw new IllegalArgumentException();
        }
        this.upHeap(index);
        this.downHeap(index);
    }

    @Override
    protected int getArrayIndex(E element) {
        for (int i = 1; i <= this.count; i++){
            if (this.elements[i].equals(element)){
                return i;
            }
        }
        // System.out.println("Element not found");
        // System.out.println(element);
        // System.out.println(this.elements);
        // System.out.println("Count: " + this.elements.length);
        return -1;
    }

    @Override
    protected void doubleArraySize() {
        E[] newElements = (E[]) new Comparable[this.elements.length*2];
        for (int i = 1; i < this.elements.length; i++){
            newElements[i] = this.elements[i];
        }
        this.elements = newElements;
    }

       /**
     * is elements[i] > elements[j]?
     */
    @Override
    protected boolean isGreater(int i, int j) {
        return this.elements[i].compareTo(this.elements[j]) > 0;
    }

    /**
     * swap the values stored at elements[i] and elements[j]
     */
    @Override
    protected void swap(int i, int j) {
        E temp = this.elements[i];
        this.elements[i] = this.elements[j];
        this.elements[j] = temp;
    }

    /**
     * while the key at index k is less than its
     * parent's key, swap its contents with its parent’s
     */
    @Override
    protected void upHeap(int k) {
        while (k > 1 && this.isGreater(k / 2, k)) {
            this.swap(k, k / 2);
            k = k / 2;
        }
    }

    /**
     * move an element down the heap until it is less than
     * both its children or is at the bottom of the heap
     */
    @Override
    protected void downHeap(int k) {
        while (2 * k <= this.count) {
            //identify which of the 2 children are smaller
            int j = 2 * k;
            if (j < this.count && this.isGreater(j, j + 1)) {
                j++;
            }
            //if the current value is < the smaller child, we're done
            if (!this.isGreater(k, j)) {
                break;
            }
            //if not, swap and continue testing
            this.swap(k, j);
            k = j;
        }
    }
    @Override
    public void insert(E x) {
        // double size of array if necessary
        if (this.count >= this.elements.length - 1) {
            this.doubleArraySize();
        }
        //add x to the bottom of the heap
        this.elements[++this.count] = x;
        //percolate it up to maintain heap order property
        this.upHeap(this.count);
    }
    @Override
    public E peek(){
        return this.elements[1];
    }
    @Override
    protected boolean isEmpty() {
        return this.count == 0;
    }
    @Override
    public E remove() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        E min = this.elements[1];
        //swap root with last, decrement count
        this.swap(1, this.count--);
        //move new root down as needed
        this.downHeap(1);
        this.elements[this.count + 1] = null; //null it to prepare for GC
        return min;
    }
    
}
