/*
 * =============================================================================
 * Pulse
 *
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 *
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
 * This class stores our moves for a specific position. For the root node we
 * will populate pv for every root move.
 */
final class MoveHistory {

	private static final int MAX_MOVES = 512;

	private final Entry[] entries = new Entry[MAX_MOVES];

	private int currentEntry = 0;

	// stores everything we need to create a chass game notation
	// and should be sufficient to to use when replaying from
	// initial board
	static final class Entry {
		int move = Move.NOMOVE;
		int value = Value.NOVALUE;
	}

	MoveHistory() {
		for (int i = 0; i < MAX_MOVES; ++i) {
			entries[i] = new Entry();
		}
	}

	void addMove(int move) {
		entries[currentEntry++].move = move;
	}

	int removeMove() {
		int move = entries[--currentEntry].move;
		entries[currentEntry].move = Move.NOMOVE;
		entries[currentEntry].value = Value.NOVALUE;
		return move;

	}

	int lastMove() {
		if (currentEntry == 0)
			return Move.NOMOVE;
		return entries[currentEntry - 1].move;
	}

	protected MoveHistory clone() {
		MoveHistory clone = new MoveHistory();
		clone.currentEntry = currentEntry;
		for (int i = 0; i <= currentEntry; ++i) {
			clone.entries[i].move = entries[i].move;
			clone.entries[i].value = entries[i].value;
		}
		return clone;
	}
	
	public String toString() {
		String s = "";
		for (int i = 0; i < currentEntry; ++i) {
			s += i + ". " + entries[i] + " ";
		}
		return s;
	}

}
