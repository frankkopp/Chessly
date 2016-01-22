/*
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
package fko.chessly.game;


public interface GamePiece {
	
    /**
     * To support a clockwise lookup around a field This speeds up things a
     * little
     */
    public static final int[][] clockwiseLookup = {
        { 0, 1}, // 0
        { 1, 1}, // 1 --> top right corner (-1,1)
        { 1, 0}, // 2
        { 1,-1}, // 3 --> bottom right corner (1,1)
        { 0,-1}, // 4
        {-1,-1}, // 5 --> buttom left corner (1,-1)
        {-1, 0}, // 6
        {-1, 1}  // 7 --> top left corner (-1,-1)
    };
    
    public static final int[][] blackPawnAttackVectors = {
        { 1, 1}, 
        {-1, 1} 
    };
	
    public static final int[][] whitePawnAttackVectors = {
        { 1, -1},
        {-1, -1} 
    };
	
    public static final int[][] knightAttackVectors = {
        { 1, 2}, 
        { 2, 1},
        { 2,-1},
        { 1,-2},
        {-1,-2},
        {-2,-1},
        {-2, 1},
        {-1, 2}
    };

    /**
     * Returns the color of this piece
     * 
     * @return {@link GameColor} color
     */
    GameColor getColor();

    /**
     * @return Returns true is color is white
     */
    boolean isWhite();

    /**
     * @return Returns true is color is black
     */
    boolean isBlack();

    /**
     * Returns the type of the piece as the enum instance of
     * {@link GamePieceType}e
     * 
     * @return {@link GamePieceType}
     */
    GamePieceType getType();

    /**
     * Generate all legal moves for this piece. Attention: does not check which
     * player (color) has next move!
     * 
     * @param board
     * @param pos
     * @param capturingOnly
     * @return
     */
    GameMoveList getLegalMovesForPiece(GameBoard board, GamePosition pos,
	    boolean capturingOnly);

    /**
     * Generate all moves for this piece but do not check is king is left in
     * check. Attention: does not check which player (color) has next move!
     * 
     * @param board
     * @param pos
     * @param capturingOnly
     * @return
     */
    GameMoveList getPseudoLegalMovesForPiece(GameBoard board, GamePosition pos,
	    boolean capturingOnly);

    /**
     * Generates a String in form of a valid notation for Chess
     * 
     * @return chess notation for this piece
     */
    String toNotationString();

    /**
     * String representation of this piece
     * 
     * @return String
     */
    String toString();

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    int hashCode();

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    boolean equals(Object obj);

    /**
     * Return a deep clone of this piece
     * 
     * @return deep clone
     */
    Object clone();

		
}
