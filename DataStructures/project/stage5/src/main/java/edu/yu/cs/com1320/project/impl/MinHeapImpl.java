package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.MinHeap;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E>{
    public MinHeapImpl(){
        this.elements = (E[]) new Comparable<E>[10];
    }

    @Override
    public void reHeapify(E element) {
        int index = getArrayIndex(element);
        if (index == -1){
            throw new IllegalArgumentException();
        }
        if (this.isGreater(index, index/2)){
            this.upHeap(index);
        }
        else if (index*2 <= this.count && this.isGreater(index*2, index)){
            this.downHeap(index);
        }
    }

    @Override
    protected int getArrayIndex(E element) {
        for (int i = 1; i <= this.count; i++){
            if (this.elements[i].equals(element)){
                return i;
            }
        }
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
    
}
