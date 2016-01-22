/* 
 * Copyright (C) 2013-2014 Phokham Nonava
 *
 * Use of this source code is governed by the MIT license that can be
 * found in the LICENSE file.
 * ================================================================================
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
 * Bitboard stores squares as bits in a 64-bit long. We provide methods to
 * convert bit squares to 0x88 squares and vice versa.
 */
final class Bitboard implements Cloneable {

	long squares = 0;

	public Bitboard() {
	}

	public Bitboard(Bitboard oldBitboard) {
		this.squares = oldBitboard.squares;
	}

	static int next(long squares) {
		return toX88Square(Long.numberOfTrailingZeros(squares));
	}

	private static int toX88Square(int square) {
		assert square >= 0 && square < Long.SIZE;

		return ((square & ~7) << 1) | (square & 7);
	}

	private static int toBitSquare(int square) {
		assert Square.isValid(square);

		return ((square & ~7) >>> 1) | (square & 7);
	}

	int size() {
		return Long.bitCount(squares);
	}

	void add(int square) {
		assert Square.isValid(square);
		assert (squares & (1L << toBitSquare(square))) == 0;

		squares |= 1L << toBitSquare(square);
	}

	void remove(int square) {
		assert Square.isValid(square);
		assert (squares & (1L << toBitSquare(square))) != 0;

		squares &= ~(1L << toBitSquare(square));
	}

	public String toString() {
		String s = "";
		for (int r = 7; r >= 0; r--) {
			for (int f = 0; f < 8; f++) {
				int sq = Square.values[r * 8 + f];
				if (Square.isValid(sq)) {
					long b = 1L << Bitboard.toBitSquare(sq);
					if ((b & squares) == b) {
						s += "X ";
					} else {
						s += "O ";
					}
				}
			}
			s += "\n";
		}
		return s;
	}

	@Override
	protected Object clone() {
		return new Bitboard(this);
	}

}
