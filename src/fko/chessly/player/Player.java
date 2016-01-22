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

package fko.chessly.player;

import fko.chessly.game.Game;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;

/**
 * <p>This interface defines a Player able to be used together with the
 * GAME class.</p>
 *
 * <p>
 * A player has a NAME, a certain COLOR.
 * A player can have one of the following states:
 * <b>WAITING | THINKING | HAS_MOVE | STOPPED</b>
 * </p>
 *
 * <p>Typically a player will be either a human, a computer.</p>
 */
public interface Player extends Runnable {

    // -- possible states of a player --
    int WAITING = 0;
    int THINKING = 1;
    int HAS_MOVE = 2;
    int STOPPED = 3;

    /**
     * Start a player thread
     * @param game 
     */
    void startPlayer(Game game);

    /**
     * Stop a player thread
     */
    void stopPlayer();

    /**
     * join()
     */
    void joinPlayerThread();

    /**
     * return next move
     * @param board
     * @return Move
     */
    GameMove getNextMove(GameBoard board);

    /**
     * returns the player's color
     * @return int
     */
    GameColor getColor();

    /**
     * Sets the color for this player.
     * @param color
     */
    void setColor(GameColor color);

    /**
     * returns name of player
     * @return String
     */
    String getName();

    /**
     * Returns the current game the player is in
     * @return Game
     */
    Game getCurrentGame();

    /**
     * Sets the name for this player
     * @param name 
     */
    void setName(String name);

    /**
     * Implementation of getMove() for to determine the next move
     * @return Move
     */
    GameMove getMove();

    /**
     * @return if is waitung
     */
    boolean isWaiting();

    /**
     * 
     */
    void waitUntilWaiting();
    
    /**
     * @return if is thinking
     */
    boolean isThinking();

    /**
     * @return true if has move
     */
    boolean hasMove();

    /**
     * @return true if stopped
     */
    boolean isStopped();

    /**
     * Returns the int value of the PlayerType for a given player
     * @return PlayerType
     */
    PlayerType getPlayerType();

    
}
