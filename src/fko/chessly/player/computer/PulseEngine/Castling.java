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

final class Castling {

	static final int WHITE_KINGSIDE = 0;
	static final int WHITE_QUEENSIDE = 1;
	static final int BLACK_KINGSIDE = 2;
	static final int BLACK_QUEENSIDE = 3;
	static final int NOCASTLING = 4;

	static final int[] values = { WHITE_KINGSIDE, WHITE_QUEENSIDE,
			BLACK_KINGSIDE, BLACK_QUEENSIDE };

	private Castling() {
	}

	static boolean isValid(int castling) {
		switch (castling) {
		case WHITE_KINGSIDE:
		case WHITE_QUEENSIDE:
		case BLACK_KINGSIDE:
		case BLACK_QUEENSIDE:
			return true;
		case NOCASTLING:
		default:
			return false;
		}
	}

	static int valueOf(int color, int castlingType) {
		switch (color) {
		case Color.WHITE:
			switch (castlingType) {
			case CastlingType.KINGSIDE:
				return WHITE_KINGSIDE;
			case CastlingType.QUEENSIDE:
				return WHITE_QUEENSIDE;
			case CastlingType.NOCASTLINGTYPE:
			default:
				throw new IllegalArgumentException();
			}
		case Color.BLACK:
			switch (castlingType) {
			case CastlingType.KINGSIDE:
				return BLACK_KINGSIDE;
			case CastlingType.QUEENSIDE:
				return BLACK_QUEENSIDE;
			case CastlingType.NOCASTLINGTYPE:
			default:
				throw new IllegalArgumentException();
			}
		case Color.NOCOLOR:
		default:
			throw new IllegalArgumentException();
		}
	}

	static public String toChar(int castling) {
		switch (castling) {
		case WHITE_KINGSIDE:
			return "K";
		case WHITE_QUEENSIDE:
			return "Q";
		case BLACK_KINGSIDE:
			return "k";
		case BLACK_QUEENSIDE:
			return "q";
		case NOCASTLING:
		default:
			return "-";
		}
	}

}
