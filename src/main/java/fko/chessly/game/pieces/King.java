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
import fko.chessly.game.GameCastling;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMoveImpl;
import fko.chessly.game.GameMoveList;
import fko.chessly.game.GamePiece;
import fko.chessly.game.GamePieceType;
import fko.chessly.game.GamePosition;

public class King extends PieceAbstractImpl implements GamePiece, Serializable {

    private static final long serialVersionUID = 7121830741135499310L;

    private static final GamePieceType _pieceType = GamePieceType.KING;

    private static final King white = new King(GameColor.WHITE);
    private static final King black = new King(GameColor.BLACK);

    private King(GameColor color) {
        super(_pieceType, color);
    }

    /**
     * Returns a King object of the given color.
     * @param color
     * @return A reference to a King object of the given color.
     */
    public static King create(GameColor color) {
        return color==GameColor.WHITE ? white : black;
    }

    @Override
    public GameMoveList getLegalMovesForPiece(GameBoard board,
            GamePosition fromPos, boolean capturingOnly) {
        return board.filterLegalMovesOnly(getPseudoLegalMovesForPiece(board, fromPos, capturingOnly));
    }

    @Override
    public GameMoveList getPseudoLegalMovesForPiece(GameBoard board,
            GamePosition fromPos, boolean capturingOnly) {

        GameMoveList allMoves = new GameMoveList();

        // -- check occupied by own piece or off board
        for (int i = 0; i < 8; i++) {
            GamePosition newPos = GamePosition.getGamePosition(fromPos.getFile()
                    + clockwiseLookup[i][0], fromPos.getRank() + clockwiseLookup[i][1]);
            if (board.canMoveTo(fromPos, newPos)) {
                final GamePiece toField = board.getPiece(newPos);
                if (!capturingOnly || (capturingOnly && toField != null)) {
                    allMoves.add(new GameMoveImpl(fromPos, newPos, this));
                }
            }
        }

        if (!capturingOnly) {
            // -- check for castling
            if (board.isCastlingKingSideAllowed(board.getPiece(fromPos)
                    .getColor())
                    && board.checkForFreePath(fromPos, GamePosition.getGamePosition(8, fromPos.getRank())) // no piece in between rking and rook
                    && !board.isFieldControlledBy(fromPos, this.getColor()
                            .getInverseColor()) // king not in check
                    && !board.isFieldControlledBy(GamePosition.getGamePosition(fromPos.getFile() + 1, fromPos.getRank()), this.getColor()
                            .getInverseColor()) // kings path not in check
                    && !board.isFieldControlledBy(
                            GamePosition.getGamePosition(7, fromPos.getRank()), this.getColor()
                            .getInverseColor()) // kings target not in
                    // check
                    ) {
                final GameMoveImpl m = new GameMoveImpl(fromPos, GamePosition.getGamePosition(7, fromPos.getRank()), this);
                if (this.isWhite()) {
                    m.setCastlingType(GameCastling.WHITE_KINGSIDE);
                } else {
                    m.setCastlingType(GameCastling.BLACK_KINGSIDE);
                }
                allMoves.add(m);
            }
            if (board.isCastlingQueenSideAllowed(board.getPiece(fromPos)
                    .getColor())
                    && board.checkForFreePath(fromPos, GamePosition.getGamePosition(1, fromPos.getRank()))
                    && !board.isFieldControlledBy(fromPos, this.getColor()
                            .getInverseColor())
                    && !board.isFieldControlledBy(GamePosition.getGamePosition(fromPos.getFile() - 1, fromPos.getRank()), this.getColor()
                            .getInverseColor())
                    && !board.isFieldControlledBy(
                            GamePosition.getGamePosition(3, fromPos.getRank()), this.getColor()
                            .getInverseColor())) {
                final GameMoveImpl m = new GameMoveImpl(fromPos, GamePosition.getGamePosition(3, fromPos.getRank()), this);
                if (this.isWhite()) {
                    m.setCastlingType(GameCastling.WHITE_QUEENSIDE);
                } else {
                    m.setCastlingType(GameCastling.BLACK_QUEENSIDE);
                }
                allMoves.add(m);
            }
        }
        return allMoves;
    }

}
