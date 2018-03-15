package ru.track.list;

import java.util.NoSuchElementException;

/**
 * Должен наследовать List
 * Односвязный список
 */
public class MyLinkedList extends List {

    /**
     * private - используется для сокрытия этого класса от других.
     * Класс доступен только изнутри того, где он объявлен
     * <p>
     * static - позволяет использовать Node без создания экземпляра внешнего класса
     */
    Node root = new Node(null, null, -1);

    private static class Node {
        Node prev;
        Node next;
        int val;

        Node(Node prev, Node next, int val) {
            this.prev = prev;
            this.next = next;
            this.val = val;
        }
    }

    @Override
    void add(int item) {
        Node current = root;
        while(current.next != null){
            current = current.next;
        }
        Node result = new Node(current, null, item);
        current.next = result;
        size++;
    }

    @Override
    int remove(int idx) throws NoSuchElementException {
        Node current = root;
        if (idx > size - 1 || idx < 0){
            throw new NoSuchElementException();
        }
        for(int i = 0; i < idx + 1; i++){
            current = current.next;
        }
        int result = current.val;
        size--;
        if(current.prev != null && current.next != null) {
            current.next.prev = current.prev;
            current.prev.next = current.next;
        }
        else if(current.next == null){
            current.prev.next = null;
        }
        return result;
    }

    @Override
    int get(int idx) throws NoSuchElementException {
        Node current = root;
        if (idx > size - 1 || idx < 0){
            throw new NoSuchElementException();
        }
        for(int i = 0; i < idx + 1; i++){
            current = current.next;
        }
        return current.val;
    }

/*    @Override
    int size() {
        return size;
    }*/
}
