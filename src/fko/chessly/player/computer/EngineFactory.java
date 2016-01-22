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
package fko.chessly.player.computer;

import fko.chessly.Chessly;
import fko.chessly.game.GameColor;
import fko.chessly.player.Player;

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
 * A factory for engines.<br>
 * It looks up the reversi.properties file for the engine.<br>
 * Example:<br>
 * <code>blackEngine.class = fko.chessly.player.computer.Adam.AdamEngine<br>
 * whiteEngine.class  = fko.chessly.player.computer.TreeSearch_v10.TreeSearchEngine_v10</code>
 */
public class EngineFactory {

    // -- Factories should not be instantiated --
    private EngineFactory() {}

    /**
     * @param player
     * @param color
     * @return Engine
     */
    public static Engine createEngine(Player player, GameColor color) {

        String engineClass;
        if (color.isBlack()) {
            engineClass = Chessly.getProperties().getProperty("blackEngine.class");
        } else if (color.isWhite()) {
            engineClass = Chessly.getProperties().getProperty("whiteEngine.class");
        } else {
            throw new IllegalArgumentException("Not a valid ChesslyColor for a player: "+color);
        }

        if (engineClass == null) {
            engineClass = "fko.chessly.player.computer.Adam.AdamEngine";
            System.err.println("Engine class property could not be found: using default: " + engineClass);
        }

        Engine engine = null;
        try {
            engine = (Engine) ClassLoader.getSystemClassLoader().loadClass(engineClass).newInstance();
            engine.init(player);
        } catch (InstantiationException e) {
            System.err.println("Engine class " + engine + " could not be loaded");
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch (IllegalAccessException e) {
            System.err.println("Engine class " + engine + " could not be loaded");
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        } catch (ClassNotFoundException e) {
            System.err.println("Engine class " + engine + " could not be loaded");
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }

        return engine;
    }

}
