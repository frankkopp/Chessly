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

    public GameMoveList getPseudoLegalMovesForPiece(GameBoard board,
	    GamePosition pos, boolean capturingOnly) {

	GameMoveList allMoves = new GameMoveList();
	getMovesForSlidingPiece(board, pos, allMoves, movevector, capturingOnly);
	return allMoves;
    }

}
