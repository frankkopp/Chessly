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

final class Rank {

	static final int r1 = 0;
	static final int r2 = 1;
	static final int r3 = 2;
	static final int r4 = 3;
	static final int r5 = 4;
	static final int r6 = 5;
	static final int r7 = 6;
	static final int r8 = 7;
	static final int NORANK = 8;

	static final int[] values = { r1, r2, r3, r4, r5, r6, r7, r8 };

	private Rank() {
	}

	static boolean isValid(int rank) {
		switch (rank) {
		case r1:
		case r2:
		case r3:
		case r4:
		case r5:
		case r6:
		case r7:
		case r8:
			return true;
		case NORANK:
		default:
			return false;
		}
	}

}
