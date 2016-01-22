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

final class Color {

	static final int WHITE = 0;
	static final int BLACK = 1;
	static final int NOCOLOR = 2;

	static final int[] values = { WHITE, BLACK };

	private Color() {
	}

	static boolean isValid(int color) {
		switch (color) {
		case WHITE:
		case BLACK:
			return true;
		case NOCOLOR:
		default:
			return false;
		}
	}

	static int opposite(int color) {
		switch (color) {
		case WHITE:
			return BLACK;
		case BLACK:
			return WHITE;
		case NOCOLOR:
		default:
			throw new IllegalArgumentException();
		}
	}

	static char toChar(int color) {
		switch (color) {
		case WHITE:
			return 'w';
		case BLACK:
			return 'b';
		case NOCOLOR:
		default:
			throw new IllegalArgumentException();
		}
	}

}
