package ru.track.list;

import java.util.NoSuchElementException;

/**
 * Должен наследовать List
 *
 * Должен иметь 2 конструктора
 * - без аргументов - создает внутренний массив дефолтного размера на ваш выбор
 * - с аргументом - начальный размер массива
 */
public class MyArrayList extends List {

    int[] array;
    /*int size = 0;*/

    public MyArrayList() {
        this.array = new int[1024];
    }

    public MyArrayList(int capacity) {
        this.array = new int[capacity];

    }

    @Override
    void add(int item) {
        if (size == array.length){
            this.array = new int[array.length + 1024];
        }
        array[size++] = item;
    }

    @Override
    int remove(int idx) throws NoSuchElementException {
        int result;
        if (idx > size - 1 || idx < 0){
            throw new NoSuchElementException();
        }
        result = array[idx];
        for(int i = idx + 1; i < size; i++){
            array[i - 1] = array[i];
        }
        size--;
        return result;
    }

    @Override
    int get(int idx) throws NoSuchElementException {
        int result;
        if (idx > size - 1 || idx < 0){
            throw new NoSuchElementException();
        }
        result = array[idx];
        return result;
    }

/*    @Override
    int size() {
        return size;
    }*/
}
