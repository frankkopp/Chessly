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
import fko.chessly.game.GameMove;
import fko.chessly.game.GameMoveImpl;
import fko.chessly.game.GameMoveList;
import fko.chessly.game.GamePiece;
import fko.chessly.game.GamePieceType;
import fko.chessly.game.GamePosition;

public class Knight extends PieceAbstractImpl implements GamePiece, Serializable {

    private static final long serialVersionUID = 1158621116752030134L;

    private static GamePieceType _pieceType = GamePieceType.KNIGHT;

    private static final Knight white = new Knight(GameColor.WHITE);
    private static final Knight black = new Knight(GameColor.BLACK);

    /**
     * @param color
     * @return Knight object
     */
    public static Knight create(GameColor color) {
        return color==GameColor.WHITE ? white : black;
    }

    private Knight(GameColor color) {
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

        // generate Knight moves
        for (int i = 0; i < GamePiece.knightAttackVectors.length; i++) {
            int col_inc = GamePiece.knightAttackVectors[i][0];
            int row_inc = GamePiece.knightAttackVectors[i][1];
            int new_col = pos.getFile() + col_inc;
            int new_row = pos.getRank() + row_inc;
            GamePosition newPos = GamePosition.getGamePosition(new_col, new_row);

            if (board.canMoveTo(pos, newPos)) {
                final GamePiece toField = board.getPiece(newPos);
                if (!capturingOnly || (capturingOnly && toField != null)) {
                    GameMove m = new GameMoveImpl(pos, newPos, this);
                    allMoves.add(m);
                }
            }
        }
        return allMoves;
    }

}
