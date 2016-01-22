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

package fko.chessly.ui.SwingGUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;

import fko.chessly.Chessly;

/**
 * This class represents the main window (JFrame) itself
 */
public class MainWindow extends JFrame implements KeyListener {

    private static final long serialVersionUID = 1L;

    // -- back reference to the ui object
    private SwingGUI _ui;

    // -- our menu --
    private MainMenu _reversiMenu;

    // -- components --
    private JPanel        _mainPanel;
    private JSplitPane    _workPane;
    private StatusPanel   _statusPanel;

    /**
     * Constructs a new frame that is initially invisible.
     */
    public MainWindow(SwingGUI ui) {

        super("Chessly by Frank Kopp (c) 2003-2016 (Version: " + Chessly.VERSION + ')');

        // -- backreference to the ui object
        this._ui = ui;

        // -- get last window position and size--
        int windowLocX = Integer.parseInt(
                SwingGUI.getWindowState().getProperty("windowLocationX") == null
                ? "100" : SwingGUI.getWindowState().getProperty("windowLocationX"));
        int windowLocY = Integer.parseInt(
                SwingGUI.getWindowState().getProperty("windowLocationY") == null
                ? "200" : SwingGUI.getWindowState().getProperty("windowLocationY"));
        int windowSizeX = Integer.parseInt(
                SwingGUI.getWindowState().getProperty("windowSizeX") == null
                ? "600" : SwingGUI.getWindowState().getProperty("windowSizeX"));
        int windowSizeY = Integer.parseInt(
                SwingGUI.getWindowState().getProperty("windowSizeY") == null
                ? "800" : SwingGUI.getWindowState().getProperty("windowSizeY"));

        // -- position and resize the window  --
        this.setLocation(windowLocX, windowLocY);
        this.setSize(new Dimension(windowSizeX, windowSizeY));

        // -- handle closing of window with the closing listener --
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                _ui.exitReversi();
            }
        });

        // -- key inputs --
        this.addKeyListener(this);

        // -- set layout of content pane
        getContentPane().setLayout(new BorderLayout());

        // ----------------------------------------------------
        // -- window components --

        // -- set menu --
        _reversiMenu = new MainMenu(_ui);
        this.setJMenuBar(_reversiMenu);

        // -- main panel --
        _mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(_mainPanel, BorderLayout.CENTER);

        // -- split panel
        _workPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        _workPane.setOneTouchExpandable(true);
        _workPane.setContinuousLayout(true);

        // -- set divider location to last known position --
        int dividerLocation =
                SwingGUI.getWindowState().getProperty("dividerLocation") == null ?
                        _workPane.getHeight()
                        - _workPane.getInsets().bottom
                        - _workPane.getInsets().top
                        - _workPane.getDividerSize()
                        : Integer.parseInt(SwingGUI.getWindowState().getProperty("dividerLocation"));
                        _workPane.setResizeWeight(0.5);
                        _workPane.setDividerLocation(dividerLocation);

                        _mainPanel.add(_workPane, BorderLayout.CENTER);

                        // -- status line --
                        _statusPanel = new StatusPanel();
                        getContentPane().add(_statusPanel, BorderLayout.SOUTH);

                        // -- keyboard events --
                        setFocusable(true);

    }

    /**
     * Is called to close the window. This also stores the current windows state to a file.
     */
    protected synchronized void closeWindowAction() {
        SwingGUI.getWindowState().setProperty("windowLocationX", String.valueOf(this.getLocation().x));
        SwingGUI.getWindowState().setProperty("windowLocationY", String.valueOf(this.getLocation().y));
        SwingGUI.getWindowState().setProperty("windowSizeX", String.valueOf(this.getSize().width));
        SwingGUI.getWindowState().setProperty("windowSizeY", String.valueOf(this.getSize().height));
        SwingGUI.getWindowState().setProperty("dividerLocation", String.valueOf(this._workPane.getDividerLocation()));
        this.dispose();
    }

    /**
     * user yes or no question
     *
     * @param question
     * @return int
     */
    protected int userConfirmation(String question) {
        return JOptionPane.showConfirmDialog(
                this,
                question,
                "Question",
                JOptionPane.YES_NO_OPTION);
    }

    /**
     * Creates a message box asking the user for input
     *
     * @param question
     * @param title
     * @return the string with the user input
     */
    protected String userInputDialog(String question, String title) {
        return JOptionPane.showInputDialog(
                this,
                question,
                title,
                JOptionPane.QUESTION_MESSAGE);
    }

    /**
     * Creates a message box asking the user for input
     *
     * @param question
     * @param title
     * @param defString
     * @return String
     */
    protected String userInputDialog(String question, String title, String defString) {
        return (String) JOptionPane.showInputDialog(
                this,
                question,
                title,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                defString);
    }

    /**
     * Invoked when a key has been typed.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key typed event.
     */
    @Override
    public void keyTyped(KeyEvent e) {
        //System.out.printf(Integer.toString(e.getKeyCode())+"\n");
    }

    /**
     * Invoked when a key has been pressed.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key pressed event.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        /*if (Chessly.is_debug()) {
            System.out.printf(Integer.toString(e.getKeyCode())+" : " + e.getKeyChar() + "\n");
        }*/
    }

    /**
     * Invoked when a key has been released.
     * See the class description for {@link java.awt.event.KeyEvent} for a definition of
     * a key released event.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        //System.out.printf(Integer.toString(e.getKeyCode())+"\n");
    }

    public MainMenu getMenu() {
        return _reversiMenu;
    }

    public JPanel getMainPanel() {
        return _mainPanel;
    }

    public JSplitPane getWorkPane() {
        return _workPane;
    }

    public StatusPanel getStatusPanel() {
        return _statusPanel;
    }
}
