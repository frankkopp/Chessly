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
    public static Knight createKnight(GameColor color) {
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

    public GameMoveList getPseudoLegalMovesForPiece(GameBoard board,
	    GamePosition pos, boolean capturingOnly) {

	GameMoveList allMoves = new GameMoveList();

	// generate Knight moves
	for (int i = 0; i < GamePiece.knightAttackVectors.length; i++) {
	    int col_inc = GamePiece.knightAttackVectors[i][0];
	    int row_inc = GamePiece.knightAttackVectors[i][1];
	    int new_col = pos.x + col_inc;
	    int new_row = pos.y + row_inc;
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
