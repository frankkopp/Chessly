/*
 * <p>GPL Disclaimer</p>
 * <p>
 * "Chessly by Frank Kopp"
 * Copyright (c) 2003-2016 Frank Kopp
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
 *
 */
package fko.chessly.openingbook;

import fko.chessly.game.GameMove;

/**
 * Interface for opening books gor Chessly. 
 * @author fkopp
 *
 */
public interface OpeningBook {
    
    /**
     * Initializes the book with default settings
     */
    void initialize();
    
    /**
     * Returns move from opening book as string based on a position
     */
    GameMove getBookMove(String fen); 

}
