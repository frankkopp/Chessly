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

public abstract class PieceAbstractImpl implements GamePiece, Serializable {

    private static final long serialVersionUID = -297626039605707827L;

    protected final GameColor _color;
    protected final GamePieceType _pieceType;

    protected PieceAbstractImpl(GamePieceType type, GameColor color) {
        this._color = color;
        this._pieceType = type;
    }

    @Override
    public abstract GameMoveList getLegalMovesForPiece(GameBoard board,
            GamePosition pos, boolean capturingOnly);

    @Override
    public abstract GameMoveList getPseudoLegalMovesForPiece(GameBoard board,
            GamePosition pos, boolean capturingOnly);

    @Override
    public GameColor getColor() {
        return _color;
    }

    @Override
    public boolean isWhite() {
        return _color.equals(GameColor.WHITE);
    }

    @Override
    public boolean isBlack() {
        return _color.equals(GameColor.BLACK);
    }

    @Override
    public GamePieceType getType() {
        return _pieceType;
    }

    @Override
    public String toNotationString() {
        return _pieceType.toChar();
    }

    @Override
    public String toString() {
        if (_color.isWhite())
            return _pieceType.toChar().toUpperCase();
        return _pieceType.toChar().toLowerCase();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_color == null) ? 0 : _color.hashCode());
        result = prime * result
                + ((_pieceType == null) ? 0 : _pieceType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PieceAbstractImpl)) {
            return false;
        }
        PieceAbstractImpl other = (PieceAbstractImpl) obj;
        if (_color != other._color) {
            return false;
        }
        if (_pieceType != other._pieceType) {
            return false;
        }
        return true;
    }

    @Override
    public Object clone() {
        return this; // immutable
    }

    protected void getMovesForSlidingPiece(GameBoard board, GamePosition pos,
            GameMoveList legalMoves, int[][] deltas, boolean capturingOnly) {

        for (int i = 0; i < deltas.length; i++) {
            int col_inc = deltas[i][0];
            int row_inc = deltas[i][1];
            int new_col = pos.getFile() + col_inc;
            int new_row = pos.getRank() + row_inc;
            GamePosition newPos = GamePosition.getGamePosition(new_col, new_row);

            // traverse and find legal moves
            while (board.isWithinBoard(newPos)) {
                final GamePiece toField = board.getPiece(newPos);
                if (toField == null) {
                    // non capturing move
                    if (!capturingOnly) {
                        legalMoves.add(new GameMoveImpl(pos, newPos, this));
                    }
                } else if (toField.getColor().equals(
                        this.getColor().getInverseColor())) {
                    // capturing move
                    GameMove m = new GameMoveImpl(pos, newPos, this);
                    m.setCapturedPiece(board.getPiece(newPos));
                    legalMoves.add(m);
                    break;
                } else {
                    // own piece - non valid move
                    break;
                }
                new_col = new_col + col_inc;
                new_row = new_row + row_inc;
                newPos = GamePosition.getGamePosition(new_col, new_row);
            }
        }
    }

}
