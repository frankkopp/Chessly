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

package fko.chessly.ui;

import java.awt.datatransfer.SystemFlavorMap;

import com.sun.org.apache.bcel.internal.generic.LoadClass;

import fko.chessly.Chessly;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;

/**
 * <p>A factory to create the user interface.<br>
 * It reads the UI class from the configuration in chessly.properties:</p>
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
            Class<?> loadClass = ClassLoader.getSystemClassLoader().loadClass(ui);
            return (UserInterface) loadClass.newInstance();
        } catch (InstantiationException e) {
            Chessly.fatalError("UI class " + ui + " could not be loaded");
        } catch (IllegalAccessException e) {
            Chessly.fatalError("UI class " + ui + " could not be loaded");
        } catch (ClassNotFoundException e) {
            Chessly.fatalError("UI class " + ui + " could not be loaded");
        }
        return null;
    }

}
