/*
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
 *
 */

package fko.chessly.ui;

import fko.chessly.Chessly;

/**
 * <p>A factory to create the user interface.<br>
 * It reads the UI class from the configuration in reversi.properties:</p>
 * <p>Example:
 * <code>ui.class = fko.chessly.ui.Swing.SwingUI</code>
 * </p>
 * <p>This class is a utility class and can not be instantiated. Use getUI() to get
 * the actual instance.</p>
 */
public class UserInterfaceFactory {

    private UserInterfaceFactory() {
    }

    /**
     * Returns a UserInterface based on the default class "fko.chessly.ui.ReversiGUI.ReversiGUI".<br/>
     * @return user interface instance
     */
    public static UserInterface getUI() {
       return createUI(null);
    }

    /**
     * Returns a UserInterface based on the given class.<br/>
     * @return user interface instance
     * @param class_name The class implementing the ui
     */
    public static UserInterface getUI(String class_name) {
       return createUI(class_name);
    }

    private static UserInterface createUI(String class_name) {
        String ui;

        if (class_name == null) {
            ui = Chessly.getProperties().getProperty("ui.class");
        } else {
            ui = class_name;
        }

        if (ui == null) {
            ui = "fko.chessly.ui.SwingGUI.SwingGUI";
            System.err.println("UI class property could not be found: using default: " + ui);
        }

        try {
            return (UserInterface) ClassLoader.getSystemClassLoader().loadClass(ui).newInstance();
        } catch (InstantiationException e) {
            Chessly.fatalError("Engine class " + ui + " could not be loaded");
        } catch (IllegalAccessException e) {
            Chessly.fatalError("Engine class " + ui + " could not be loaded");
        } catch (ClassNotFoundException e) {
            Chessly.fatalError("Engine class " + ui + " could not be loaded");
        }
        return null;
    }

}
