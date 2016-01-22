/**
 * The MIT License (MIT)
 *
 * "Chessly by Frank Kopp"
 *
 * mail-to:frank@familie-kopp.de
 *
 * Copyright (c) 2016 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
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
    @Override
    String toString();

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    int hashCode();

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    boolean equals(Object obj);

    /**
     * Return a deep clone of this piece
     *
     * @return deep clone
     */
    Object clone();


}
