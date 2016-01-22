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

    public GamePieceType getType() {
	return _pieceType;
    }

    public String toNotationString() {
	return _pieceType.toChar();
    }

    public String toString() {
	return _color.toChar() + _pieceType.toChar();
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
	    int new_col = pos.x + col_inc;
	    int new_row = pos.y + row_inc;
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
