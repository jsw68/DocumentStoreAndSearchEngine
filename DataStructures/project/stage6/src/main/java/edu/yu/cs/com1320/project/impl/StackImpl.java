package edu.yu.cs.com1320.project.impl;
import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T>{
    /**
     * @param element object to add to the Stack
     */
    private class Element<E> {
        private E value;
        private Element<E> next;
        private Element(E value, Element<E> next) {
            this.value = value;
            this.next = next;
        }
    }
    private Element<T> first;
    public StackImpl(){
        this.first = null;
    }
    @Override
    public void push(T element){
        this.first = new Element<T>(element, this.first);
    }

    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    @Override
    public T pop(){
        if (this.first == null){
            return null;
        }
        T value = this.first.value;
        this.first = this.first.next;
        return value;
    }

    /**
     *
     * @return the element at the top of the stack without removing it
     */
    @Override
    public T peek(){
        if (this.first == null){
            return null;
        }
        return this.first.value;
    }

    /**
     *
     * @return how many elements are currently in the stack
     */
    @Override
    public int size(){
        int count = 0;
        Element<T> current = this.first;
        while (current != null){
            count++;
            current = current.next;
        }
        return count;
    }
}
