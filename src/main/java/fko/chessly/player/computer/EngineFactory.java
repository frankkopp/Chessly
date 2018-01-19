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
 * A factory for engines.<br>
 * It looks up the Chessly.properties file for the engine.<br>
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

			final Class<?> loadedClass = Chessly.class.getClassLoader().loadClass(engineClass);
			engine = (Engine) loadedClass.getConstructor().newInstance();
			engine.init(player);

		} catch (Exception e) {
			Chessly.criticalError("Engine class " + engine + " could not be loaded");
		}

		return engine;
	}

}
