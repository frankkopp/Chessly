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
package fko.chessly.ui.SwingGUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**e
 * The SatelliteWindow class extends a JDialog mainly with the ability to store its window position
 * with the help of ReversiGUI.getWindowState().
 */
public class SatelliteWindow extends JFrame {

    private static final long serialVersionUID = 9190106299247327100L;

    // -- my label to store and restore the windows --
    private String _myLabel;

    // -- dispose or setViable=false --
    private boolean _dispose = false;

    /**
     * Creates a new, initially invisible <code>Frame</code> with the
     * specified title.
     * <p/>
     * This constructor sets the component's locale property to the value
     * returned by <code>JComponent.getDefaultLocale</code>.
     * @param title the title for the frame
     * @param label a label for the frame to store and restore the last window size and position
     */
    public SatelliteWindow(String title, String label) {
        super(title);

        // -- only keep word characters [a-zA-Z_0-9] --
        this._myLabel = label.trim().replaceAll("\\W+","");

        // -- get last window position and size--
        int windowLocX = Integer.parseInt(
                SwingGUI.getWindowState().getProperty("windowLocationX_"+_myLabel) == null
                ? "100" : SwingGUI.getWindowState().getProperty("windowLocationX_"+_myLabel));
        int windowLocY = Integer.parseInt(
                SwingGUI.getWindowState().getProperty("windowLocationY_"+_myLabel) == null
                ? "200" : SwingGUI.getWindowState().getProperty("windowLocationY_"+_myLabel));
        int windowSizeX = Integer.parseInt(
                SwingGUI.getWindowState().getProperty("windowSizeX_"+_myLabel) == null
                ? "600" : SwingGUI.getWindowState().getProperty("windowSizeX_"+_myLabel));
        int windowSizeY = Integer.parseInt(
                SwingGUI.getWindowState().getProperty("windowSizeY_"+_myLabel) == null
                ? "800" : SwingGUI.getWindowState().getProperty("windowSizeY_"+_myLabel));

        // -- position and resize the window  --
        this.setLocation(windowLocX, windowLocY);
        this.setSize(new Dimension(windowSizeX, windowSizeY));

        // -- handle closing of window with the closing listener --
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        // -- close frame handler --
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWindowAction();
            }
        });

        // -- set layout of content pane
        getContentPane().setLayout(new BorderLayout());

    }

    /**
     * returns the setting of the dispose mode
     * @return true if window will be disposed on close, false when window will be set invisible
     */
    public boolean is_dispose() {
        return _dispose;
    }

    /**
     * set mode for window close
     * @param newDisposeMode true for dispose on close, false for set invisible on close
     */
    public void set_dispose(boolean newDisposeMode) {
        this._dispose = newDisposeMode;
    }

    /**
     * Is called to close the window. This also stores the current windows state to a file.
     * <p/>
     * mainWindow.setLocation(windowLocX, windowLocY);
     * mainWindow.setSize(new Dimension(windowSizeX, windowSizeY));
     */
    protected synchronized void closeWindowAction() {
        SwingGUI.getWindowState().setProperty("windowLocationX_"+_myLabel, String.valueOf(this.getLocation().x));
        SwingGUI.getWindowState().setProperty("windowLocationY_"+_myLabel, String.valueOf(this.getLocation().y));
        SwingGUI.getWindowState().setProperty("windowSizeX_"+_myLabel, String.valueOf(this.getSize().width));
        SwingGUI.getWindowState().setProperty("windowSizeY_"+_myLabel, String.valueOf(this.getSize().height));
        if (_dispose) {
            this.dispose();
        } else {
            this.setVisible(false);
        }
    }


}
