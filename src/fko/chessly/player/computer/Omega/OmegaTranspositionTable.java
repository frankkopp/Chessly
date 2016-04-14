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
public class OmegaTranspositionTable {

    static private final int MB = 1024;

    private int _size;
    private final int _max_entries;

    private int _numberOfEntries = 0;
    private long _numberOfCollisions = 0L;

    private final Entry[] entries;

    /**
     * Creates a hash table with a approximated number of entries calculated by
     * the size in MB divided by the entry size.</br>
     * The hash function is very simple using the modulo of number of entries on the key
     * @param size in MB (1024^2)
     */
    public OmegaTranspositionTable(int size) {
        _size = size;

        // check mem - add some head room
        System.gc();
        int freeMemory = (int) (Runtime.getRuntime().freeMemory() / (MB * MB));
        if (freeMemory < size*2) {
            System.err.println(String.format("Not enough memory for a %dMB evaluation cache - reducing to %dMB", size, freeMemory/4));
            _size = freeMemory/4;
        }

        // size in byte divided by entry size plus size for array bucket
        _max_entries = (_size * MB * MB) / (Entry.SIZE + Integer.BYTES);
        // create buckets for hash table
        entries = new Entry[_max_entries];
        // initialize
        for (int i=0; i<_max_entries; i++) {
            entries[i] = new Entry();
        }
    }

    /**
     * Stores the node value and the depth it has been calculated at.
     *
     * @param key
     * @param value
     * @param depth
     */
    public void put(long key, int value, int depth) {
        final int hash = getHash(key);
        if (entries[hash].key == 0) { // new value
            _numberOfEntries++;
        } else { // collision
            _numberOfCollisions++;
        }
        entries[hash].key = key;
        entries[hash].value = value;
        entries[hash].depth = depth;
    }

    /**
     * This retrieves the cached value of this node from cache if the
     * cached value has been calculated at a depth equal or deeper as the
     * depth value provided.
     *
     * @param key
     * @param depth after this node
     * @return value for key or <tt>Integer.MIN_VALUE</tt> if not found
     */
    public int get(long key, int depth) {
        final int hash = getHash(key);
        if (entries[hash].key == key && entries[hash].depth >= depth ) { // hash hit
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
            entries[i].key = 0L;
            entries[i].value = Integer.MIN_VALUE;
            entries[i].depth = 0;
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
        static final int SIZE = (Long.BYTES+Integer.BYTES+Integer.BYTES) *2;
        long key   = 0L;
        int  value = Integer.MIN_VALUE;
        int  depth = 0;
    }



}
