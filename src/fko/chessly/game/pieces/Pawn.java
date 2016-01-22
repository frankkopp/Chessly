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
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMoveImpl;
import fko.chessly.game.GameMoveList;
import fko.chessly.game.GamePiece;
import fko.chessly.game.GamePieceType;
import fko.chessly.game.GamePosition;

/**
 * @author Frank
 *
 */
public class Pawn extends PieceAbstractImpl implements GamePiece, Serializable {

    private static final long serialVersionUID = 6979893115895070545L;

    private static final GamePieceType _pieceType = GamePieceType.PAWN;

    private static final int[][] blackPawnMoveVectors = { { -1, -1 }, { 1, -1 } };
    private static final int[][] whitePawnMoveVectors = { { -1,  1 }, { 1, 1 } };
    
    private static final Pawn whitePawn = new Pawn(GameColor.WHITE);
    private static final Pawn blackPawn = new Pawn(GameColor.BLACK);

    /**
     * @param color
     */
    private Pawn(GameColor color) {
	super(_pieceType, color);
    }
    
    /**
     * Returns a pawn object of the given color. 
     * @param color
     * @return A reference to a Pawn object of the given color.
     */
    public static Pawn createPawn(GameColor color) {
        return color==GameColor.WHITE ? whitePawn : blackPawn;
    }
    
    /**
     * Generates Pawn moves. Attention!! Promotion generates several moves.
     * 
     */
    @Override
    public GameMoveList getLegalMovesForPiece(GameBoard board,
	    GamePosition fromPos, boolean capturingOnly) {

	GameMoveList allMoves = getPseudoLegalMovesForPiece(board, fromPos,
		capturingOnly);
	return board.filterLegalMovesOnly(allMoves);
    }

    public GameMoveList getPseudoLegalMovesForPiece(GameBoard board,
	    GamePosition fromPos, boolean capturingOnly) {

	GameMoveList allMoves = new GameMoveList();

	GameColor pawnColor = board.getNextPlayerColor();
	switch (pawnColor) {
	case WHITE:
	    whitePawnMoves(board, fromPos, allMoves, capturingOnly);
	    break;
	case BLACK:
	    blackPawnMoves(board, fromPos, allMoves, capturingOnly);
	    break;
	default:
	    throw new IllegalArgumentException("No valid pawnColor");
	}
	return allMoves;
    }

    private void whitePawnMoves(GameBoard board, GamePosition fromPos,
	    GameMoveList legalMoves, boolean capturingOnly) {

	GameColor pawnColor = GameColor.WHITE;

	// forward for white
	int direction = GameBoardImpl.WHITE_DIRECTION;
	int pawnBase = GameBoardImpl.WHITE_BASE_ROW + direction;
	int promotionRow = GameBoardImpl.BLACK_BASE_ROW;

	if (!capturingOnly) {
	    // allow 1 field forward
	    forwardOneField(board, fromPos, legalMoves, pawnColor, direction,
		    promotionRow);

	    // if on base line allow 2 fields
	    forwardTwoFields(board, fromPos, legalMoves, direction, pawnBase);
	}

	// attack 1 diagonal
	int[][] deltas = whitePawnMoveVectors;
	attackMoves(board, fromPos, legalMoves, pawnColor, promotionRow, deltas);
    }

    private void blackPawnMoves(GameBoard board, GamePosition fromPos,
	    GameMoveList legalMoves, boolean capturingOnly) {

	GameColor pawnColor = GameColor.BLACK;

	// forward for black
	int direction = GameBoardImpl.BLACK_DIRECTION;
	int pawnBase = GameBoardImpl.BLACK_BASE_ROW + direction;
	int promotionRow = GameBoardImpl.WHITE_BASE_ROW;

	if (!capturingOnly) {
	    // allow 1 field forward
	    forwardOneField(board, fromPos, legalMoves, pawnColor, direction,
		    promotionRow);

	    // if on base line allow 2 fields
	    forwardTwoFields(board, fromPos, legalMoves, direction, pawnBase);
	}

	// attack 1 diagonal
	int[][] deltas = blackPawnMoveVectors;
	attackMoves(board, fromPos, legalMoves, pawnColor, promotionRow, deltas);
    }

    private void forwardOneField(GameBoard board, GamePosition fromPos,
	    GameMoveList legalMoves, GameColor pawnColor, int direction,
	    int promotionRow) {

	int new_row = fromPos.y + direction;
	if (board.getPiece(fromPos.x, new_row) == null) {
	    final GamePosition newPos = GamePosition.getGamePosition(fromPos.x, new_row);
	    GameMoveImpl m = new GameMoveImpl(fromPos, newPos, this);
	    if (new_row == promotionRow) {
		m.setPromotedTo(Queen.createQueen(pawnColor));
		legalMoves.add(m);
		m = new GameMoveImpl(fromPos, newPos, this);
		m.setPromotedTo(Rook.createRook(pawnColor));
		legalMoves.add(m);
		m = new GameMoveImpl(fromPos, newPos, this);
		m.setPromotedTo(Bishop.createBishop(pawnColor));
		legalMoves.add(m);
		m = new GameMoveImpl(fromPos, newPos, this);
		m.setPromotedTo(Knight.createKnight(pawnColor));
		legalMoves.add(m);
	    } else {
		legalMoves.add(m);
	    }

	}
    }

    private void forwardTwoFields(GameBoard board, GamePosition fromPos,
	    GameMoveList legalMoves, int direction, int pawnBase) {

	if (fromPos.y == pawnBase) {
	    int new_row = fromPos.y + (2 * direction);
	    if (board.getPiece(fromPos.x, pawnBase + direction) == null
		    && board.getPiece(fromPos.x, new_row) == null) {
		final GameMoveImpl m = new GameMoveImpl(fromPos,
			GamePosition.getGamePosition(fromPos.x, new_row), this);
		m.setEnPassantNextMovePossible(true);
		// TODO: we could set en passant position here but this will be done later
		// when move is actually done as it would require creating
		// a new position object every time here. 
		legalMoves.add(m);
	    }
	}
    }

    private void attackMoves(GameBoard board, GamePosition fromPos,
	    GameMoveList legalMoves, GameColor pawnColor, int promotionRow,
	    int[][] deltas) {

	for (int i = 0; i < deltas.length; i++) {
	    int col_inc = deltas[i][0];
	    int row_inc = deltas[i][1];
	    int new_col = fromPos.x + col_inc;
	    int new_row = fromPos.y + row_inc;
	    GamePosition newPos = GamePosition.getGamePosition(new_col, new_row);

	    if (board.isWithinBoard(newPos)) {
		if (board.getPiece(new_col, new_row) != null) {
		    if (board.getPiece(new_col, new_row).getColor()
			    .equals(this._color.getInverseColor())) {
			GameMoveImpl m = new GameMoveImpl(fromPos, newPos, this);
			if (newPos.y == promotionRow) {
			    m.setPromotedTo(Queen.createQueen(pawnColor));
			    m.setCapturedPiece(board.getPiece(new_col, new_row));
			    legalMoves.add(m);
			    m = new GameMoveImpl(fromPos, newPos, this);
			    m.setPromotedTo(Rook.createRook(pawnColor));
			    m.setCapturedPiece(board.getPiece(new_col, new_row));
			    legalMoves.add(m);
			    m = new GameMoveImpl(fromPos, newPos, this);
			    m.setPromotedTo(Bishop.createBishop(pawnColor));
			    m.setCapturedPiece(board.getPiece(new_col, new_row));
			    legalMoves.add(m);
			    m = new GameMoveImpl(fromPos, newPos, this);
			    m.setPromotedTo(Knight.createKnight(pawnColor));
			    m.setCapturedPiece(board.getPiece(new_col, new_row));
			    legalMoves.add(m);
			} else {
			    m.setCapturedPiece(board.getPiece(new_col, new_row));
			    legalMoves.add(m);
			}
		    }
		} else { // en passant
		    if (board.hasEnPassantCapturable()
			    && board.getEnPassantCapturable().x == new_col
			    && board.getEnPassantCapturable().y == fromPos.y) {
			final GameMoveImpl m = new GameMoveImpl(fromPos, newPos, this);
			m.setCapturedPiece(board.getPiece(new_col, fromPos.y));
			m.setWasEnPassantCapture(true);
			legalMoves.add(m);
		    }
		}
	    }
	}
    }

}

