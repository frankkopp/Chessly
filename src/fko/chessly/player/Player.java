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
