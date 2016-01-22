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
    public static King createKing(GameColor color) {
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
	    GamePosition newPos = GamePosition.getGamePosition(fromPos.x
		    + clockwiseLookup[i][0], fromPos.y + clockwiseLookup[i][1]);
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
		    && board.checkForFreePath(fromPos, GamePosition.getGamePosition(8, fromPos.y)) // no piece in between rking and rook
		    && !board.isFieldControlledBy(fromPos, this.getColor()
			    .getInverseColor()) // king not in check
		    && !board.isFieldControlledBy(GamePosition.getGamePosition(fromPos.x + 1, fromPos.y), this.getColor()
			    .getInverseColor()) // kings path not in check
		    && !board.isFieldControlledBy(
			    GamePosition.getGamePosition(7, fromPos.y), this.getColor()
				    .getInverseColor()) // kings target not in
							// check
	    ) {
		allMoves.add(new GameMoveImpl(fromPos, GamePosition.getGamePosition(7, fromPos.y), this));
	    }
	    if (board.isCastlingQueenSideAllowed(board.getPiece(fromPos)
		    .getColor())
		    && board.checkForFreePath(fromPos, GamePosition.getGamePosition(1, fromPos.y))
		    && !board.isFieldControlledBy(fromPos, this.getColor()
			    .getInverseColor())
		    && !board.isFieldControlledBy(GamePosition.getGamePosition(fromPos.x - 1, fromPos.y), this.getColor()
			    .getInverseColor())
		    && !board.isFieldControlledBy(
			    GamePosition.getGamePosition(3, fromPos.y), this.getColor()
				    .getInverseColor())) {
		allMoves.add(new GameMoveImpl(fromPos, GamePosition.getGamePosition(3, fromPos.y), this));
	    }
	}
	return allMoves;
    }

}
