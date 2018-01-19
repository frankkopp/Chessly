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

public class Bishop extends PieceAbstractImpl implements GamePiece, Serializable {

    private static final long serialVersionUID = 6891648393454073071L;

    private static GamePieceType _pieceType = GamePieceType.BISHOP;

    private static final Bishop white = new Bishop(GameColor.WHITE);
    private static final Bishop black = new Bishop(GameColor.BLACK);

    /**
     * To support a clockwise lookup around a field This speeds up things a
     * little
     */
    private static final int[][] movevector = {
            { 1, 1 }, // 1 --> top right  // corner (-1,1)
            { 1, -1 }, // 3 --> bottom right corner (1,1)
            { -1, -1 }, // 5 --> buttom left corner (1,-1)
            { -1, 1 } // 7 --> top left corner (-1,-1)
    };

    /**
     * @param color
     * @return Bishop object
     */
    public static Bishop create(GameColor color) {
        return color==GameColor.WHITE ? white : black;
    }

    /**
     * @param color
     */
    private Bishop(GameColor color) {
        super(_pieceType, color);
    }

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
