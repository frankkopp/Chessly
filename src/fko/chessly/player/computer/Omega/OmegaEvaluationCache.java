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

package fko.chessly.player.computer.Omega;

/**
 * A cache for board evaluation values to reduce evaluation calculation during
 * search. Implementation uses a simple array of an Entry class. The array indexes
 * are calculated by using the modulo of the max number of entries from the key.
 * <code>entries[key%maxNumberOfEntries]</code>. As long as key is randomly distributed
 * this works just fine.
 */
public class OmegaEvaluationCache {

    static private final int MB = 1024;

    private int _size;
    private int _max_entries;

    private int _numberOfEntries = 0;
    private long _numberOfCollisions = 0L;

    private final Entry[] entries;

    /**
     * Creates a hash table with a approximated number of entries calculated by
     * the size in MB divided by the entry size.</br>
     * The hash function is very simple using the modulo of number of entries on the key
     * @param size in MB (1024^2)
     */
    public OmegaEvaluationCache(int size) {
        _size = size*MB*MB;

        // check available mem - add some head room
        System.gc();
        long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long freeMemory = (Runtime.getRuntime().maxMemory()-usedMemory);
        int percentage = 5;
        if (freeMemory*percentage/100 < _size) {
            System.err.println(String.format("Not enough memory for a %,dMB evaluation cache - reducing to %,dMB", _size/(MB*MB), (freeMemory*percentage/100)/(MB*MB)));
            _size = (int) (freeMemory*percentage/100); // % of memory
        }

        // size in byte divided by entry size plus size for array bucket
        _max_entries = _size / (Entry.SIZE + Integer.BYTES);
        // create buckets for hash table
        entries = new Entry[_max_entries];
        // initialize
        for (int i=0; i<_max_entries; i++) {
            entries[i] = new Entry();
        }
    }

    /**
     * @param key
     * @param value
     */
    public void put(long key, int value) {
        final int hash = getHash(key);
        if (entries[hash].key == 0) { // new value
            _numberOfEntries++;
        } else { // collision
            _numberOfCollisions++;
        }
        entries[hash].key = key;
        entries[hash].value = value;
    }

    /**
     * @param key
     * @return value for key or <tt>Integer.MIN_VALUE</tt> if not found
     */
    public int get(long key) {
        final int hash = getHash(key);
        if (entries[hash].key == key) { // hash hit
            return entries[hash].value;
        }
        // cache miss or collision
        return Integer.MIN_VALUE;
    }

    private int getHash(long key) {
        return (int) (key%_max_entries);
    }

    /**
     * Clears all entry by resetting the to key=0 and
     * value=Integer-MIN_VALUE
     */
    public void clear() {
        // initialize
        for (int i=0; i<_max_entries; i++) {
            entries[i].key = 0;
            entries[i].value = Integer.MIN_VALUE;
        }
        _numberOfEntries = 0;
        _numberOfCollisions = 0;
    }

    /**
     * @return the numberOfEntries
     */
    public int getNumberOfEntries() {
        return this._numberOfEntries;
    }

    /**
     * @return the size in MB
     */
    public int getSize() {
        return this._size;
    }

    /**
     * @return the max_entries
     */
    public int getMaxEntries() {
        return this._max_entries;
    }

    /**
     * @return the numberOfCollisions
     */
    public long getNumberOfCollisions() {
        return _numberOfCollisions;
    }

    private static final class Entry {
        static final int SIZE = (Long.BYTES+Integer.BYTES) *2;
        long key   = 0L;
        int  value = Integer.MIN_VALUE;
    }



}
