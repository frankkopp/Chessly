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
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.player.computer.Engine;
import fko.chessly.player.computer.EngineFactory;

/**
 * <p>GPL Disclaimer</p>
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
 * <hr/>
 *
 * A computer player for Chessly.
 */
public class ComputerPlayer extends AbstractPlayer {

    private Engine _engine;

    /**
     * This constructor is protected to indicate to use the PlayerFactory to create
     * a new player of this kind
     * @param game - a back reference to the game the player plays in
     * @param name - the name of the player
     * @param color - the color the player has in the current game
     */
    protected ComputerPlayer(Game game, String name, GameColor color) {
        super(game, name, color);
        this._engine = EngineFactory.createEngine(this, color);
    }

    /**
     * This constructor is protected to indicate to use the PlayerFactory to create
     * a new player of this kind
     * @param name - the name of the player
     * @param color - the color the player has in the current game
     */
    protected ComputerPlayer(String name, GameColor color) {
        super(name, color);
        this._engine = EngineFactory.createEngine(this, color);
    }

    /**
     * Implementation of getMove() for to determine the next move
     * @return return computed move
     */
    @Override
    public GameMove getMove() {
        // <ENGINE>
        return _engine.getNextMove(getCurBoard());
        // </ENGINE>
    }

    /**
     * return the player engine
     */
    public Engine getEngine() {
        return _engine;
    }

    /**
     * This method may be overwritten to to some extra stuff when startPlayer() is called and
     * before the actual Thread is started.
     */
    @Override
    protected void startPlayerPrepare() {
        super.startPlayerPrepare();
        _engine.setGame(getCurrentGame());
    }

    /**
     * Returns the int value of the PlayerType for a given player
     */
    @Override
    public PlayerType getPlayerType() {
        return PlayerType.COMPUTER;
    }

    /**
     * @return
     */
    public PlayerStatusController getPlayerStatus() {
        return _playerStatus;
    }


}
