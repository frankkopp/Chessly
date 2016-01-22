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


final class EvaluationTable {

    // Size of one evaluation entry
    static final int ENTRYSIZE = 24;

    private final int size;
    
    private volatile int numberOfEntries;

    private final EvaluationTableEntry[] entry;

    static final class EvaluationTableEntry {
	long zobristCode = 0;
	int evaluation = -Value.INFINITE;

	EvaluationTableEntry() {
	}
	
	void clear() {
	    zobristCode=0;
	}

    }

    EvaluationTable(int newSize) {
	assert newSize >= 1;

	this.size = newSize;
	
	this.numberOfEntries = 0;

	// Initialize entry
	this.entry = new EvaluationTableEntry[newSize];
	for (int i = 0; i < this.entry.length; i++) {
	    this.entry[i] = new EvaluationTableEntry();
	}
    }
    
    /**
     * Clears the Evaluation Table.
     */
    void clear() {
	for (EvaluationTableEntry anEntry : this.entry) {
	    synchronized (anEntry) {
		anEntry.clear();
	    }
	}
	numberOfEntries = 0;
    }

    /**
     * Puts a zobrist code and evaluation value into the table.
     *
     * @param newZobristCode
     *            the zobrist code.
     * @param newEvaluation
     *            the evaluation value.
     */
    void put(long newZobristCode, int newEvaluation) {
	int position = (int) (newZobristCode % this.size);
	EvaluationTableEntry currentEntry = this.entry[position];

	synchronized (currentEntry) {
	    if (currentEntry.zobristCode == 0) {
		// new entry
		numberOfEntries++;
	    }
	    currentEntry.zobristCode = newZobristCode;
	    currentEntry.evaluation = newEvaluation;
	}
    }

    /**
     * Returns the evaluation table entry given the zobrist code.
     *
     * @param newZobristCode
     *            the zobrist code.
     * @return the evaluation table entry or null if there exists no entry.
     */
    EvaluationTableEntry get(long newZobristCode) {
	int position = (int) (newZobristCode % this.size);
	EvaluationTableEntry currentEntry = this.entry[position];
	
	synchronized (currentEntry) {
	    if (currentEntry.zobristCode == newZobristCode) {
		return currentEntry;
	    } else {
		return null;
	    }
	}
    }

    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    public int getSize() {
        return size;
    }

}
