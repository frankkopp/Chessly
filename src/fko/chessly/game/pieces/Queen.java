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

public class Queen extends PieceAbstractImpl implements GamePiece, Serializable {

    private static final long serialVersionUID = 386626721737667667L;

    private static final GamePieceType _pieceType = GamePieceType.QUEEN;
    
    private static final Queen white = new Queen(GameColor.WHITE);
    private static final Queen black = new Queen(GameColor.BLACK);

    /**
     * @param color
     * @return Queen object
     */
    public static Queen createQueen(GameColor color) {
        return color==GameColor.WHITE ? white : black;
    }

    /**
     * @param color
     */
    private Queen(GameColor color) {
	super(_pieceType, color);
    }

    @Override
    public GameMoveList getLegalMovesForPiece(GameBoard board, GamePosition pos, boolean capturingOnly) {

	GameMoveList allMoves = getPseudoLegalMovesForPiece(board, pos,	capturingOnly);
	return board.filterLegalMovesOnly(allMoves);
    }

    public GameMoveList getPseudoLegalMovesForPiece(GameBoard board, GamePosition pos, boolean capturingOnly) {

	GameMoveList allMoves = new GameMoveList();
	getMovesForSlidingPiece(board, pos, allMoves, clockwiseLookup, capturingOnly);
	return allMoves;
    }

}
