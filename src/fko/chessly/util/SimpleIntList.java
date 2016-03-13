/**
 * The MIT License (MIT)
 *
 * "Chessly by Frank Kopp"
 *
 * mail-to:frank@familie-kopp.de
 *
 * Copyright (c) 2016 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package fko.chessly.util;

import java.util.Arrays;

/**
 * Simple and fast list class for integers.
 * It has a fixed size and does not grow.
 *
 * @author Frank Kopp
 */
public class SimpleIntList {

    /**
     * Max entries of a MoveList
     */
    public static final int DEFAULT_MAX_ENTRIES = 256;

    int[] _list;
    int _head = 0;
    int _tail = 0;

    /**
     * Creates a list with a maximum of MAX_ENTRIES elements
     */
    public SimpleIntList() {
        this(DEFAULT_MAX_ENTRIES);
    }

    /**
     * Creates a list with a maximum of max_site elements
     * @param max_size
     */
    public SimpleIntList(int max_size) {
        _list = new int[max_size];
    }

    /**
     * Clear the list
     */
    public void clear() {
        _tail = _head = 0;
    }

    /**
     * Adds an element to the end of the list.
     * @param omegaIntegerList
     */
    public void add(int omegaIntegerList) {
        if (_tail>=_list.length)
            throw new ArrayIndexOutOfBoundsException("List is full");
        _list[_tail++] = omegaIntegerList;
    }

    /**
     * Adds an element to the end of the list.
     * @param newList
     */
    public void add(SimpleIntList newList) {
        if (_tail+newList.size()>_list.length)
            throw new ArrayIndexOutOfBoundsException("Not enough space to add new elements from newList");
        System.arraycopy(newList._list, newList._head, this._list, this._tail, newList.size());
        this._tail +=newList.size();
    }

    /**
     * Removes the last entry and returns the value.
     * If the list is empty it throws a
     * @return removed element
     */
    public int removeLast() {
        if (_tail<=_head)
            throw new ArrayIndexOutOfBoundsException("List is empty");
        return _list[--_tail];
    }

    /**
     * Removes the first entry and returns the value.
     * If the list is empty it throws a
     * @return removed element
     */
    public int removeFirst() {
        if (_tail<=_head)
            throw new ArrayIndexOutOfBoundsException("List is empty");
        return _list[_head++];
    }

    /**
     * Gets entry at a specific index
     * @param index
     * @return element at index
     */
    public int get(int index) {
        if (index < 0 || _tail<=_head)
            throw new ArrayIndexOutOfBoundsException("List is empty");
        if (_head+index > _tail)
            throw new ArrayIndexOutOfBoundsException("Index too high");
        return _list[_head+index];
    }

    /**
     * Gets entry at a last index
     * @return last element
     */
    public int getLast() {
        if (_tail<=_head)
            throw new ArrayIndexOutOfBoundsException("List is empty");
        return _list[_tail-1];
    }

    /**
     * Gets entry at a first index
     * @return first element
     */
    public int getFirst() {
        if (_tail<=_head)
            throw new ArrayIndexOutOfBoundsException("List is empty");
        return _list[_head];
    }

    /**
     * Returns the size of the list
     * @return number of elements
     */
    public int size() {
        return _tail-_head;
    }

    /**
     * Returns a number of how many elements can be added to this list before it is full.
     * @return number of available slots for elements to add
     */
    public int getAvailableCapacity() {
        return _list.length-size()-_head;
    }

    /**
     * Returns true is size==0
     * @return true if empty
     */
    public boolean empty() {
        return _tail-_head == 0;
    }

    /**
     * fast sort of list
     */
    public void sort() {
        quicksort(_head,_tail-1);
    }

    /**
     * Returns a copy of the list.
     * @return copy of list as int[]
     *
     */
    public int[] toArray() {
        return Arrays.copyOfRange(_list, _head, _tail);
    }

    /**
     * clones the list
     */
    @Override
    public SimpleIntList clone() {
        SimpleIntList n = new SimpleIntList();
        n.add(this);
        return n;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String s = "List size="+size()+" available capacity="+getAvailableCapacity()+" [";
        for (int i=_head; i<_tail; i++) {
            s += _list[i];
            if (i<_tail-1) s += ",";
        }
        s+="]";
        return s;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.toArray());
        return result;
    }

    /**
     * A MoveList is equal to another MoveList when they have the same
     * elements in the same order independent from internal implementation.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof SimpleIntList)) { return false; }
        SimpleIntList other = (SimpleIntList) obj;
        if (!Arrays.equals(this.toArray(), other.toArray())) { return false; }
        return true;
    }

    /**
     * Standard quicksort implementation to order the list according to the int value.
     * @param lo
     * @param hi
     */
    private void quicksort(int lo, int hi) {
        int low=lo, high=hi;
        int mid=_list[(lo+hi)/2];
        while (low<=high) {
            while (_list[low] < mid) low++;
            while (_list[high] > mid) high--;
            if (low <= high) {
                exchange(low,high);
                low++; high--;
            }
            if (lo<high) quicksort(lo, high);
            if (low<hi) quicksort(low, hi);
        }
    }

    /**
     * @param i
     * @param j
     */
    private void exchange(int i, int j) {
        int t=_list[i];
        _list[i]=_list[j];
        _list[j]=t;
    }


}
