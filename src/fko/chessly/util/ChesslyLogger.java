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

package fko.chessly.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import fko.chessly.Chessly;

/**
 * A logger for Chessly. It uses a Logger for actually handling the logging.
 * Get the Logger through getLogger() and use as documented in there.
 * @see java.util.logging.Logger
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public class ChesslyLogger {

    private final static Logger _log = Logger.getLogger("fko.reversi");

    private final static ChesslyLogger _instance = new ChesslyLogger();

    /**
     * ReversiLogger is a Singleton so use getInstance()
     * @return ReversiLogger instance
     */
    public static ChesslyLogger getInstance() {
        return _instance;
    }

    private ChesslyLogger() {
        // get global LOG filename from properties
        String logfileFileName = Chessly.getProperties().getProperty("log.global");

        // -- logging to file found in properties --
        if (logfileFileName != null) {
            logfileFileName = System.getProperty("user.dir") + logfileFileName;
            _log.info("Start Logging to " + logfileFileName);
            // create FileOutputHandler
            try {
                FileHandler fileHandle = new FileHandler(logfileFileName);
                _log.addHandler(fileHandle);
                _log.getHandlers()[0].setFormatter(new SimpleFormatter());
                _log.setUseParentHandlers(false);
            } catch (IOException e) {
                _log.warning("Couldn't open " + logfileFileName + " for logging!");
                _log.warning("Using default (stdout)");
            }
            // -- no LOG file configured in properties, using default --
        } else {
            _log.getParent().getHandlers()[0].setFormatter(new SimpleFormatter());
            _log.info("Start logging to console");
        }

        _log.info("Chessly started");
    }

    /**
     * Returns the Logger object used by this class to acually do the logging.
     * @see java.util.logging.Logger
     * @return The Logger object.
     */
    public static Logger getLogger() {
        return _log;
    }

}
