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

package fko.chessly;

import java.util.Properties;
import java.util.logging.Logger;

import fko.chessly.ui.UserInterface;
import fko.chessly.ui.UserInterfaceFactory;
import fko.chessly.util.ChesslyLogger;
import fko.chessly.util.ChesslyProperties;
import fko.chessly.util.CmdLineParser;

/**
 * <p>
 * The Chessly class is the main class handling the startup and processing the parameters. It also holds all the global variables. It can not be instantiated.
 * </p>
 *
 * <h2>Program Structure</h2>
 *
 * <ul>
 * <li><b>Chessly</b><br/>
 * The Chessly class is used to start up the application using a <i>Playroom</i> and a <i>UI</i> (user interface). It also processes command line arguments. It
 * gives access to the <i>ReversiProperties</i> which has configuration information about the application. The Chessly main logging class <i>ReversiLogger</i>
 * can also be accessed through this class.<br/>
 * <br/>
 * </li>
 * <ul>
 * <li><b>Playroom</b><br/>
 * The Playroom class is the main controlling part of the application. It represents and gives access to the model independently from the user interface (Model
 * View Controller).<br/>
 * The Playroom is started through the user interface (UI) and configured through properties and/or the user interface. It is responsible to build and start
 * players and games and also to give access to all model information which has to be queried and changed by the user interface.<br/>
 * The Playroom is started by the user interface and is running in a separate thread to actually play one or more games in a row. For doing this it creates a
 * game with the current settings and also creates two players to play in that game. When players and game are initialized it starts the game which is also
 * running in a separate thread.<br/>
 * <br/>
 * </li>
 * <ul>
 * <li><b>Game</b><br/>
 * Description<br/>
 * <br/>
 * </li>
 * <ul>
 * <li><b>Player</b><br/>
 * Description<br/>
 * <br/>
 * </li>
 * Description<br/>
 * <br/>
 * <li><b>Board</b><br/>
 * Description<br/>
 * <br/>
 * </li>
 * <li><b>Clock</b><br/>
 * Description<br/>
 * <br/>
 * </li>
 * </ul>
 * </ul> <li><b>UI</b><br/>
 * Description</li> <li><b>Properties</b><br/>
 * Description</li> <li><b>ReversiLogger</b><br/>
 * Description</li> </ul>
 * </ul>
 *
 * <b>Version Info:</b>
 * <ul>
 * <li>Version Info :)</li>
 * <li></li>
 * </ul>
 *
 * <b>Feature List:</b>
 * <ul>
 * <li>Graphical UI (Java Swing)</li>
 * <li>Chess board Implementation fully Rule compliant (incl. 50 moves rule, repetition)</li>
 * <li>Human and Engine Players (see more under player/computer/*)</li>
 * <li>Undo Move</li>
 * <li>Opening Book from PGN, SAN and simple notation</li>
 * <li>time based games</li>
 * </ul>
 * <p>
 * <b>Todo List:</b>
 * <ul>
 * <li>DONE: Drag&Drop move in UI
 * <li>DONE: Pondering</li>
 * <li>TODO: Show DRAW Reason</li>
 * <li>TODO: Force Move - break thinking<</li>
 * <li>TODO: Select Engine via menu</li>
 * <li>TODO: Refactor SwingGUI - See book Java Swing - use Actions, etc.<br/>
 * <li>TODO: Introduce resource bundles - multi language</li>
 * <li>TODO: Help text for usage and information</li>
 * </ul>
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public class Chessly {

    /**
     * This constant holds the current version of Chessly by Frank Kopp
     * v1.2 - added basic opening book
     * v1.3a - extended opening book
     * v1.4 - added pondering
     */
    public static final String VERSION = "v1.5";

    @SuppressWarnings("unused")
    private static Chessly _myChessly = null;

    private final Playroom _playroom;
    private final UserInterface _ui;

    // -- debug --
    private static boolean _debug = Boolean.valueOf(getProperties().getProperty("debug", "false"));

    /**
     * The main() method parses the command line arguments and processes them and finally creates an instance
     * of the Chessly class.<br/>
     * @param args command line options
     */
    public static void main(final String[] args) {

        CmdLineParser cp = new CmdLineParser();
        CmdLineParser.Option javafx   = cp.addBooleanOption('x', "javafx"  );
        CmdLineParser.Option debug    = cp.addBooleanOption('d', "debug"  );
        CmdLineParser.Option start    = cp.addBooleanOption('s', "start"  );
        CmdLineParser.Option cache    = cp.addBooleanOption('c', "cache"  );
        CmdLineParser.Option nocache  = cp.addBooleanOption('n', "nocache");
        CmdLineParser.Option usage    = cp.addBooleanOption('?', "help"   );

        // Parse cmd line args
        try { cp.parse(args); }
        catch ( CmdLineParser.OptionException e ) {
            System.err.println(e.getMessage());
            printUsage();
            exitReversi(2);
        }

        // Usage
        if ((Boolean) cp.getOptionValue(usage)) {
            printUsage();
            exitReversi(0);
        }

        // Set properties according to the command line options
        if ((Boolean) cp.getOptionValue(debug)) { changeProperty("debug", "true"); _debug = true;  }
        if ((Boolean) cp.getOptionValue(start)) { changeProperty("start", "true");}
        if ((Boolean) cp.getOptionValue(cache)) { changeProperty("engine.cacheEnabled", "true");  }
        if ((Boolean) cp.getOptionValue(nocache)) { changeProperty("engine.cacheEnabled", "false");  }

        if ((Boolean) cp.getOptionValue(javafx)) { changeProperty("ui.class", "fko.chessly.ui.JavaFX_GUI.JavaFX_GUI"); }

        // Now create our singleton instance of Chessly
        _myChessly = new Chessly();

    }

    /**
     * Singleton instance so this constructor is private.
     * This gets the Playroom instance, creates the gui and adds it to the Playroom instance as an Observer (MVC).
     */
    private Chessly() {
        // Create and get an instance of the singleton Playroom class
        _playroom = Playroom.getInstance();
        // Create and get an instance of an interface for Chessly.
        _ui = UserInterfaceFactory.getUI();
        // The user interface (View) is an Observer to the Playroom (Model)
        _playroom.addObserver(_ui);

        // Start game automatically?
        if (Boolean.valueOf(getProperties().getProperty("start"))) {
            _playroom.startPlayroom();
        }
    }

    /**
     * Returns ReversiProperties instance
     * @return ReversiProperties instance
     */
    public static Properties getProperties() {
        return ChesslyProperties.getInstance();
    }

    /**
     * Returns ReversiLogger instance
     * @return ReversiLogger instance
     */
    public static Logger getLogger() {
        return ChesslyLogger.getLogger();
    }

    /**
     * Returns Playroom instance
     * @return Playroom instance
     */
    public static Playroom getPlayroom() {
        return Playroom.getInstance();
    }

    /**
     * Called when there is an unexpected unrecoverable error.<br/>
     * Prints a stack trace together with a provided message.<br/>
     * Terminates with <tt>exit(1)</tt>.
     * @param message to be displayed with the exception message
     */
    public static void fatalError(String message) {
        Exception e = new Exception(message);
        e.printStackTrace();
        exitReversi(1);
    }

    /**
     * Called when there is an unexpected but recoverable error.<br/>
     * Prints a stack trace together with a provided message.<br/>
     * @param message to be displayed with the exception message
     */
    public static void criticalError(String message) {
        Exception e = new Exception(message);
        e.printStackTrace();
    }

    /**
     * Clean up and exit the application
     */
    public static void exitReversi() {
        exitReversi(0);
    }

    /**
     * Clean up and exit the application
     */
    private static void exitReversi(int returnCode) {
        // nothing to clean up yet
        System.exit(returnCode);
    }

    /**
     * Returns if the application is in debug mode. This can be set in the Chessly properties.
     * @return true when we are in debug mode
     */
    public static boolean isDebug() {
        return _debug;
    }

    /**
     * Changes a property due to a command line option.
     * @param name
     * @param value
     */
    private static void changeProperty(String name, String value) {
        getProperties().setProperty(name, value);
        System.out.println("Startup option: "+name + '=' +value);
    }

    /**
     * Usage message.
     */
    private static void printUsage() {
        System.out.println();
        System.out.println(
                "Usage: OptionTest [-d,--debug] [-s,--start] [-c,--cache] [--nocache]\n"
                        +"                  [-?, --help]"
                );
        System.out.println("Options:");
        System.out.println();
        System.out.println("-d debug mode");
        System.out.println("-s start game immediately with default settings");
        System.out.println("-c enables the cache for the engines (cache not implemented)");
        System.out.println("--nocache disables the cache for the engines (overrides -c)");
        System.out.println();
    }

}
