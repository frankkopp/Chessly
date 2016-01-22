/*
 * Copyright (C) 2007-2014 Phokham Nonava
 *
 * This file is part of Flux Chess.
 *
 * Flux Chess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flux Chess is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
 * =============================================================================
 * Chessly
 *
 * <p>GPL Dislaimer</p>
 * <p>
 * "Chessly by Frank Kopp"
 * Copyright (c) 2003-2015 Frank Kopp
 * mail-to:frank@familie-kopp.de
 *
 * This file is part of "Chessly by Frank Kopp".
 *
 * "Chessly by Frank Kopp" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * "Chessly by Frank Kopp" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "Chessly by Frank Kopp"; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * </p>
 */
package fko.chessly.player.computer.PulseEngine;

/**
 * @author Phokham Nonava
 */
final class TranspositionTable {

    // Size of one transposition entry
    static final int ENTRYSIZE = 40;

    private final int size;
    
    private volatile int numberOfEntries;
    
    // Entry
    private final TranspositionTableEntry[] entry;

    /**
     * Creates a new TranspositionTable.
     *
     * @param newSize
     *            the size.
     */
    TranspositionTable(int newSize) {
	assert newSize >= 1;

	this.size = newSize;
	
	this.numberOfEntries = 0;

	// Initialize entry
	this.entry = new TranspositionTableEntry[newSize];
	for (int i = 0; i < this.entry.length; i++) {
	    this.entry[i] = new TranspositionTableEntry();
	}

    }

    /**
     * Clears the Transposition Table.
     */
    void clear() {
	for (TranspositionTableEntry anEntry : this.entry) {
	    synchronized (anEntry) {
		anEntry.clear();
	    }
	}
	numberOfEntries = 0;
    }

    /**
     * Puts the values into the TranspositionTable.
     *
     * @param zobristCode
     *            the zobrist code.
     * @param depth
     *            the depth.
     * @param value
     *            the value.
     * @param type
     *            the value type.
     * @param move
     *            the move.
     */
    void put(long zobristCode, int depth, int value, int type, int move, int ply) {
	
	assert depth >= 0;
	assert type != Bound.NOBOUND;
	assert ply >= 0;

	int position = (int) (zobristCode % this.size);
	TranspositionTableEntry currentEntry = this.entry[position];
	
	synchronized (currentEntry) {
	    // ## BEGIN "always replace" Scheme
	    if (currentEntry.zobristCode == 0) {

		// This is a new entry
		numberOfEntries++;
		currentEntry.zobristCode = zobristCode;
		currentEntry.depth = depth;
		currentEntry.setValue(value, ply);
		currentEntry.type = type;
		currentEntry.move = move;
		
	    } else if (currentEntry.zobristCode == zobristCode) {

		// The same zobrist key already exists - update
		if (depth >= currentEntry.depth && move != Move.NOMOVE) {
		    currentEntry.depth = depth;
		    currentEntry.setValue(value, ply);
		    currentEntry.type = type;
		    currentEntry.move = move;
		}
		
	    } else {
		
		// We have a collision. Overwrite existing entry
		currentEntry.zobristCode = zobristCode;
		currentEntry.depth = depth;
		currentEntry.setValue(value, ply);
		currentEntry.type = type;
		currentEntry.move = move;
		
	    }
	    // ## ENDOF "always replace" Scheme	   
	}
	
    }

    /**
     * Returns the transposition table entry given the zobrist code.
     *
     * @param zobristCode
     *            the zobrist code.
     * @return the transposition table entry or null if there exists no entry.
     */
    TranspositionTableEntry get(long zobristCode) {
	int position = (int) (zobristCode % this.size);
	TranspositionTableEntry currentEntry = this.entry[position];

	synchronized (currentEntry) {
	    if (currentEntry.zobristCode == zobristCode) {
		try {
		    // needs to be clone so that after we release the monitor lock 
		    // the enrty cannot be changed outside in an uncontrolled matter
		    return (TranspositionTableEntry) currentEntry.clone();
		} catch (CloneNotSupportedException e) {
		    e.printStackTrace();
		}
	    } 
	}
	return null;
    }

    public int getSize() {
        return size;
    }
    
    public int getNumberOfEntries() {
	return numberOfEntries;
    }

    public static int getEntrysize() {
        return ENTRYSIZE;
    }

    /**
     * Cache Entry 
     * @author Frank
     */
    static final class TranspositionTableEntry implements Cloneable {
        long zobristCode = 0;
        int  depth 	 = -1;
        int  value 	 = -Value.INFINITE;
        int  type 	 = Bound.NOBOUND;
        int  move 	 = Move.NOMOVE;
    
        TranspositionTableEntry() {
        }
        
        @Override
	protected Object clone() throws CloneNotSupportedException {
	    return super.clone();
	}

	void clear() {
            this.zobristCode = 0;
            this.depth = -1;
            this.value = -Value.INFINITE;
            this.type = Bound.NOBOUND;
            this.move = Move.NOMOVE;
        }
    
        int getValue(int ply) {
            int value = this.value;
            if (value < -Value.CHECKMATE_THRESHOLD) {
        	value += ply;
            } else if (value > Value.CHECKMATE_THRESHOLD) {
        	value -= ply;
            }
    
            return value;
        }
    
        void setValue(int value, int ply) {
            // Normalize mate values
            if (value < -Value.CHECKMATE_THRESHOLD) {
        	value -= ply;
            } else if (value > Value.CHECKMATE_THRESHOLD) {
        	value += ply;
            }
            assert value <= Value.CHECKMATE || value >= -Value.CHECKMATE;
    
            this.value = value;
        }
    }

}
