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
package fko.chessly.game.pieces;

import java.io.Serializable;

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMoveList;
import fko.chessly.game.GamePiece;
import fko.chessly.game.GamePieceType;
import fko.chessly.game.GamePosition;

public class Rook extends PieceAbstractImpl implements GamePiece, Serializable {

    private static final long serialVersionUID = 2775203237804283993L;

    private static GamePieceType _pieceType = GamePieceType.ROOK;

    private static final Rook white = new Rook(GameColor.WHITE);
    private static final Rook black = new Rook(GameColor.BLACK);

    /**
     * To support a clockwise lookup around a field This speeds up things a
     * little
     */
    private static final int[][] movevector = {
            { 0, 1 },
            { 1, 0 },
            { 0, -1 },
            { -1, 0 }, };

    /**
     * @param color
     * @return Rook object
     */
    public static Rook createRook(GameColor color) {
        return color==GameColor.WHITE ? white : black;
    }

    /**
     * @param color
     */
    private Rook(GameColor color) {
        super(_pieceType, color);
    }

    /*
     * (non-Javadoc)
     *
     * @see fko.chessly.game.Piece#getLegalMovesForPiece(fko.chessly.game.Field)
     */
    @Override
    public GameMoveList getLegalMovesForPiece(GameBoard board,
            GamePosition pos, boolean capturingOnly) {

        GameMoveList allMoves = getPseudoLegalMovesForPiece(board, pos,
                capturingOnly);
        return board.filterLegalMovesOnly(allMoves);
    }

    @Override
    public GameMoveList getPseudoLegalMovesForPiece(GameBoard board,
            GamePosition pos, boolean capturingOnly) {

        GameMoveList allMoves = new GameMoveList();
        getMovesForSlidingPiece(board, pos, allMoves, movevector, capturingOnly);
        return allMoves;
    }

}
