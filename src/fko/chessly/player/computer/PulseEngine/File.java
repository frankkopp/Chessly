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

final class File {

	static final int a = 0;
	static final int b = 1;
	static final int c = 2;
	static final int d = 3;
	static final int e = 4;
	static final int f = 5;
	static final int g = 6;
	static final int h = 7;
	static final int NOFILE = 8;

	static final int[] values = { a, b, c, d, e, f, g, h };

	static final String[] chars = { "a", "b", "c", "d", "e", "f", "g", "h" };

	private File() {
	}

	static boolean isValid(int file) {
		switch (file) {
		case a:
		case b:
		case c:
		case d:
		case e:
		case f:
		case g:
		case h:
			return true;
		case NOFILE:
		default:
			return false;
		}
	}

	public static String toChar(int f) {
		if (!isValid(f))
			throw new IllegalArgumentException();
		return chars[f];
	}

}
