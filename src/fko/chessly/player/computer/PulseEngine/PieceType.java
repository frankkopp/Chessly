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


final class PieceType {

	static final int MASK = 0x7;

	static final int PAWN = 0;
	static final int KNIGHT = 1;
	static final int BISHOP = 2;
	static final int ROOK = 3;
	static final int QUEEN = 4;
	static final int KING = 5;
	static final int NOPIECETYPE = 6;

	static final int[] values = { PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING };

	// Piece values as defined by Larry Kaufman
	static final int PAWN_VALUE = 100;
	static final int KNIGHT_VALUE = 325;
	static final int BISHOP_VALUE = 325;
	static final int ROOK_VALUE = 500;
	static final int QUEEN_VALUE = 975;
	static final int KING_VALUE = 20000;

	private PieceType() {
	}

	static boolean isValid(int pieceType) {
		switch (pieceType) {
		case PAWN:
		case KNIGHT:
		case BISHOP:
		case ROOK:
		case QUEEN:
		case KING:
			return true;
		case NOPIECETYPE:
		default:
			return false;
		}
	}

	static boolean isValidPromotion(int pieceType) {
		switch (pieceType) {
		case KNIGHT:
		case BISHOP:
		case ROOK:
		case QUEEN:
			return true;
		case PAWN:
		case KING:
		case NOPIECETYPE:
		default:
			return false;
		}
	}

	static boolean isSliding(int pieceType) {
		switch (pieceType) {
		case BISHOP:
		case ROOK:
		case QUEEN:
			return true;
		case PAWN:
		case KNIGHT:
		case KING:
			return false;
		case NOPIECETYPE:
		default:
			throw new IllegalArgumentException();
		}
	}

	static int getValue(int pieceType) {
		switch (pieceType) {
		case PAWN:
			return PAWN_VALUE;
		case KNIGHT:
			return KNIGHT_VALUE;
		case BISHOP:
			return BISHOP_VALUE;
		case ROOK:
			return ROOK_VALUE;
		case QUEEN:
			return QUEEN_VALUE;
		case KING:
			return KING_VALUE;
		case NOPIECETYPE:
		default:
			throw new IllegalArgumentException();
		}
	}

	static String toChar(int pieceType) {
		switch (pieceType) {
		case PAWN:
			return "P";
		case KNIGHT:
			return "N";
		case BISHOP:
			return "B";
		case ROOK:
			return "R";
		case QUEEN:
			return "Q";
		case KING:
			return "K";
		case NOPIECETYPE:
			return "";
		default:
			throw new IllegalArgumentException();
		}
	}

	static int fromChar(String s) {
		switch (s) {
		case "":
		case " ":
		case "P":
			return PAWN;
		case "N":
			return KNIGHT;
		case "B":
			return BISHOP;
		case "R":
			return ROOK;
		case "Q":
			return QUEEN;
		case "K":
			return KING;
		default:
			throw new IllegalArgumentException();
		}
	}

}
