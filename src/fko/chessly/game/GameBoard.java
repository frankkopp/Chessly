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
 *
 *
 */

package fko.chessly.game;


/**
 * Interface for a Chessly board.
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public interface GameBoard {

    /**
     * Generates all legal moves for given color
     * @return returns an unordered ArrayList of possible moves
     */
    GameMoveList generateMoves();

    /**
     * Checks for legal move
     * @param move
     * @return returns true if the given move is legal on this board
     */
    boolean isLegalMove(GameMove move);

    /**
     * Checks if next player has a legal move - if not it is checkmate or stalemate
     * @return returns true when the next player has a legal move
     */
    boolean isGameOver();

    /**
     * Makes move on board according to rules
     * @param move Move to make on the board
     * @return Piece captured or null if non captured
     * @throws IllegalMoveException Thrown when a illegal move has been passed as parameter
     */
    GamePiece makeMove(GameMove move) throws IllegalMoveException;

    /**
     * Undo the last move
     * @return Move undone
     */
    public GameMove undoMove();

    /**
     * Creates a string representation of the board.
     *
     * @return returns a string representing the current board
     */
    String toString();

    /**
     * Creates a FEN based String representation of the board.
     * Can be used to save or exchange a position.
     * @return String representing the board's position as defined by FEN
     */
    String toFEN();

    /**
     * Return reference to a piece on a field on col, row
     * @param col 1..8
     * @param row 1..8
     * @return returns Piece on this Field or null if empty
     */
    GamePiece getPiece(int col, int row);

    /**
     * Return reference to a piece on a field on col, row
     * @param GamePosition
     * @return returns Piece on this Field or null if empty
     */
    GamePiece getPiece(GamePosition pos);

    /**
     * @return the field the white King is currently on
     */
    GamePosition getKingField(GameColor color);

    /**
     * Return color of next player or ChesslyColor.NONE if there are no more moves.
     * @return color of player for next move or none when there are no more moves.
     */
    GameColor getNextPlayerColor();

    /**
     * Return color of last player
     * @return color of player from last move (-1,0,1 -- BLACK, NONE, WHITE)
     */
    GameColor getLastPlayerColor();

    /**
     * Getter for lastMove
     * @return returns the last move mode on this board
     */
    GameMove getLastMove();

    /**
     * @return A deep copy to the list of played moves - do not modify!
     */
    GameMoveList getMoveHistory();

    /**
     * Returns the number of move made so far
     * @return returns the number of moves made so far
     */
    int getLastHalfMoveNumber();

    /**
     * Returns the number of the next move
     * @return returns the number of the next move
     */
    int getNextHalfMoveNumber();

    /**
     * getter for hashKey
     *
     * @return returns a unique hash key for this board
     */
    String getHashKey();

    /**
     * Checking if the position of this board is identical to the position of the other board.
     * This does not regard the MoveHistory (incl. no of moves) of the game.
     * Is does regard:
     *  - next player
     *  - castling rights
     *  - en passant rights (and file)
     *  
     * FIDE rules:
     * Positions are considered the same if and only if the same player has the move, pieces of 
     * the same kind and colour occupy the same squares and the possible moves of all the 
     * pieces of both players are the same. Thus positions are not the same if:
     * 	- at the start of the sequence a pawn could have been captured en passant.
     *  - a king or rook had castling rights, but forfeited these after moving. The castling 
     *    rights are lost only after the king or rook is moved.
     *   
     * @param the board to check 
     * @return true when the position is identical according to FIDE rules
     */
    boolean hasSamePosition(GameBoard b);

    /**
     * @return true if last move put the next player into check
     */
    boolean hasCheck();

    /**
     * @return true if last move put the next player into checkmate
     */
    public boolean hasStaleMate();

    /**
     * @return true if last move put the next player into stalemate
     */
    public boolean hasCheckMate();

    /**
     * Check if the move will leave the own king in check
     * @param move to be tested
     * @return true if own king would be in check after this move - false otherwise
     */
    public boolean leavesKingInCheck(GameMove move);

    /**
     * Check if the field is attacked
     * @param a_col 1..8
     * @param a_row 1..8
     * @param attackingColor 
     * @return true when any other piece can capture on this field in the next move
     */
    boolean isFieldControlledBy(final GamePosition pos, final GameColor attackingColor);

    /**
     * @param color
     * @return true if queen side castling still allowed
     */
    boolean isCastlingQueenSideAllowed(GameColor color);


    /**
     * @param color
     * @return true if king side castling still allowed
     */
    boolean isCastlingKingSideAllowed(GameColor color);


    /**
     * Checks if there is another piece between the from field 
     * and the to field along the alowed move path.
     * @param fromCol 1..8
     * @param fromRow 1..8
     * @param toCol 1..8
     * @param toRow 1..8
     * @return true when path is free
     */
    boolean checkForFreePath(GamePosition from, GamePosition to);

    /**
     * Checks if there is another piece between the from field 
     * and the to field along the alowed move path.
     * @param move
     * @return true if no other piece blocking the way - false otherwise
     */
    boolean checkForFreePath(GameMove move);

    /**
     * Checks if a move to a field is possible - is free or occupied by opponent. 
     * Does NOT check if pawn can capture forward!
     * Does NOT check if the way to the field is blocked!
     * Does NOT check if this is otherwise a legal move (e.g. King moving into Check)
     * @param fromCol 1..8
     * @param fromRow 1..8
     * @param toCol 1..8
     * @param toRow 1..8
     * @return true if to field is empty or occupied by opponent - false otherwise
     */
    boolean canMoveTo(GamePosition from, GamePosition to);

    /**
     * @param col 1..8
     * @param row 1..8
     * @return true if still on valid board fields
     */
    boolean isWithinBoard(GamePosition p);

    public boolean hasEnPassantCapturable();
    public GamePosition getEnPassantCapturable();

    public abstract int getHalfmoveClock();

    public abstract boolean hasInsufficientMaterial();

    public abstract boolean tooManyMovesWithoutCapture();

    public abstract GameMoveList filterLegalMovesOnly(GameMoveList moves);


}
