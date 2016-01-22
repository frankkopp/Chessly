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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.Format;
import java.util.Enumeration;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import fko.chessly.Chessly;
import fko.chessly.game.GameColor;

/**
 * This class implements the main menu bar
 */
public class MainMenu extends JMenuBar {

    private static final long serialVersionUID = -2382754596327948308L;

    private SwingGUI _ui;
    private MVController _MVController;

    private ButtonGroup blackLevelGroup;
    private JMenuItem blackLevel_other;

    private ButtonGroup whiteLevelGroup;
    private JMenuItem whiteLevel_other;

    private JCheckBoxMenuItem showMoveList;
    private JCheckBoxMenuItem showEngineInfoBlack;
    private JCheckBoxMenuItem showEngineInfoWhite;
    private JCheckBoxMenuItem showPossibleMoves;

    private ButtonGroup numberOfGamesGroup;
    private JMenuItem numberOfGamesExtras_other;

    private static final Format digitFormat = new java.text.DecimalFormat("00");

    // -- Actions --
    private CommandAction newGameAction;
    private CommandAction stopGameAction;
    private CommandAction pauseGameAction;
    private CommandAction resumeGameAction;
    private CommandAction exitAction;

    private CommandAction undoMoveAction;

    private CommandAction timedGameAction;
    private CommandAction timeBlackAction;
    private CommandAction timeWhiteAction;
    private CommandAction blackLevelAction;
    private CommandAction whiteLevelAction;

    private CommandAction showPossibleMovesAction;
    private CommandAction showMovelistAction;
    private CommandAction numberOfGamesAction;
    private CommandAction showEngineInfoBlackAction;
    private CommandAction showEngineInfoWhiteAction;
    private CommandAction numberOfThreadsAction;


    public MainMenu(SwingGUI backReference) {

        super();

        // -- back reference to the ui object and controller --
        this._ui = backReference;
        this._MVController = _ui.getController();

        try { // -- because we use Command.parse() --

            /* **********************************************************************
            // -- menu Game --
             ************************************************************************/
            JMenu menuGame = new JMenu("Game");
            add(menuGame);

            // -- NEW GAME --
            newGameAction = new CommandAction(
                    Command.parse(_MVController, "newGameDialog"),
                    "New Game ...", null, "New Game ...",
                    KeyStroke.getKeyStroke(78, java.awt.event.InputEvent.CTRL_DOWN_MASK),
                    0,
                    true
                    );
            menuGame.add(newGameAction);

            // -- STOP GAME --
            stopGameAction = new CommandAction(
                    Command.parse(_MVController, "stopCurrentGame"),
                    "Stop Game", null, "Stop Game",
                    KeyStroke.getKeyStroke(83, java.awt.event.InputEvent.CTRL_DOWN_MASK),
                    0,
                    false
                    );
            menuGame.add(stopGameAction);

            // -- PAUSE GAME --
            pauseGameAction = new CommandAction(
                    Command.parse(_MVController, "pauseOrResumeCurrentGame"),
                    "Pause Game", null, "Pause Game",
                    KeyStroke.getKeyStroke(80, java.awt.event.InputEvent.CTRL_DOWN_MASK),
                    0,
                    false
                    );
            menuGame.add(pauseGameAction);

            // -- RESUME GAME --
            resumeGameAction = new CommandAction(
                    Command.parse(_MVController, "pauseOrResumeCurrentGame"),
                    "Resume Game", null, "Resume Game",
                    KeyStroke.getKeyStroke(80, java.awt.event.InputEvent.CTRL_DOWN_MASK),
                    0,
                    false
                    );
            menuGame.add(resumeGameAction);

            menuGame.addSeparator();

            // -- EXIT --
            exitAction = new CommandAction(
                    Command.parse(_ui, "exitReversi"),
                    "Exit programm.", null, "Exit programm.",
                    KeyStroke.getKeyStroke(81, java.awt.event.InputEvent.CTRL_DOWN_MASK),
                    0,
                    true
                    );
            menuGame.add(exitAction);

            /* **********************************************************************
            // -- menu Moves --
             ************************************************************************/
            JMenu menuMoves = new JMenu("Moves");
            menuMoves.setEnabled(true);
            add(menuMoves);

            // -- UNDO MOVE --
            undoMoveAction = new CommandAction(
                    Command.parse(_MVController, "undoMove"),
                    "Undo My Last Move", null, "Undo My Last Move",
                    KeyStroke.getKeyStroke(90, java.awt.event.InputEvent.CTRL_DOWN_MASK),
                    0,
                    false
                    );
            menuMoves.add(undoMoveAction);

            /* **********************************************************************
            // -- menu Level --
             ************************************************************************/
            JMenu menuLevel = new JMenu("Level");
            menuLevel.setEnabled(true);
            add(menuLevel);

            // -- timed game toggle --
            timedGameAction = new CommandAction(
                    Command.parse(_MVController, "toggleTimedGame"),
                    "Timed Game.", null, "Timed Game.",
                    null, 0,
                    true
                    );
            JCheckBoxMenuItem timedGame = new JCheckBoxMenuItem(timedGameAction);
            timedGame.setState(Chessly.getPlayroom().isTimedGame());
            menuLevel.add(timedGame);

            // -- black time --
            timeBlackAction = new CommandAction(
                    Command.parse(
                            new timeBlackActionListener()
                            , "dialog"
                            ),
                    "Time Black...", null, "Time Black...",
                    null, 0,
                    true

                    );
            menuLevel.add(timeBlackAction);

            // -- white time --
            timeWhiteAction = new CommandAction(
                    Command.parse(
                            new timeWhiteActionListener()
                            , "dialog"
                            ),
                    "Time White...", null, "Time White...",
                    null, 0,
                    true
                    );
            menuLevel.add(timeWhiteAction);

            menuLevel.addSeparator();

            // -- black level --
            blackLevelAction = new CommandAction(
                    null,
                    "Black Level", null, "Black Level",
                    null, 0,
                    true
                    );
            JMenu blackLevel = new JMenu(blackLevelAction);
            // -- will be set to true when player black is an engine
            menuLevel.add(blackLevel);

            // -- radio grouped --
            blackLevelGroup = new ButtonGroup();

            // Add levels to the black level group
            addBlackLevel(2, blackLevel);
            addBlackLevel(4, blackLevel);
            addBlackLevel(6, blackLevel);
            addBlackLevel(8, blackLevel);
            addBlackLevel(10, blackLevel);
            addBlackLevel(20, blackLevel);
            addBlackLevelOther(blackLevel);

            // -- select the initial level --
            String lb = Integer.toString(Chessly.getPlayroom().getCurrentEngineLevelBlack());
            Enumeration group = blackLevelGroup.getElements();
            boolean selected = false;
            selected = selectItem(group, lb, selected);
            if (!selected) {
                blackLevel_other.setSelected(true);
            }

            // -- white level --
            whiteLevelAction = new CommandAction(
                    null,
                    "White Level", null, "White Level",
                    null, 0,
                    true
                    );
            JMenu whiteLevel = new JMenu(whiteLevelAction);
            // -- will be set to true when player black is an engine
            menuLevel.add(whiteLevel);

            // -- radio grouped --
            whiteLevelGroup = new ButtonGroup();

            addWhiteLevel(2, whiteLevel);
            addWhiteLevel(4, whiteLevel);
            addWhiteLevel(6, whiteLevel);
            addWhiteLevel(8, whiteLevel);
            addWhiteLevel(10, whiteLevel);
            addWhiteLevel(20, whiteLevel);
            addWhiteLevelOther(whiteLevel);

            // -- select the initial level --
            String lw = Integer.toString(Chessly.getPlayroom().getCurrentEngineLevelWhite());
            group = whiteLevelGroup.getElements();
            selected = false;
            selected = selectItem(group, lw, selected);
            if (!selected) {
                whiteLevel_other.setSelected(true);
            }

            /* **********************************************************************
            // -- menu Extras --
             ************************************************************************/
            JMenu menuExtras = new JMenu("Extras");
            add(menuExtras);

            // -- show move list --
            showMovelistAction = new CommandAction(
                    Command.parse(_MVController, "toggleShowMoveListAction"),
                    "Show move list", null, "Show move list",
                    null, 0,
                    true
                    );
            showMoveList = new JCheckBoxMenuItem(showMovelistAction);
            showMoveList.setState(_ui.is_showPossibleMoves());
            menuExtras.add(showMoveList);

            // -- show engine info black --
            showEngineInfoBlackAction = new CommandAction(
                    Command.parse(_MVController, "toggleShowEngineInfoBlackAction"),
                    "Show black engine info", null, "Show engine info for black player",
                    null, 0,
                    true
                    );
            showEngineInfoBlack = new JCheckBoxMenuItem(showEngineInfoBlackAction);
            showEngineInfoBlack.setState(_ui.is_showEngineInfoWindowBlack());
            menuExtras.add(showEngineInfoBlack);

            // -- show engine info black --
            showEngineInfoWhiteAction = new CommandAction(
                    Command.parse(_MVController, "toggleShowEngineInfoWhiteAction"),
                    "Show white engine info", null, "Show engine info for white player",
                    null, 0,
                    true
                    );
            showEngineInfoWhite = new JCheckBoxMenuItem(showEngineInfoWhiteAction);
            showEngineInfoWhite.setState(_ui.is_showEngineInfoWindowWhite());
            menuExtras.add(showEngineInfoWhite);

            // -- show possible move toggle --
            showPossibleMovesAction = new CommandAction(
                    Command.parse(_MVController, "toggleShowPossibleMovesAction"),
                    "Show possible moves", null, "Show possible moves",
                    null, 0,
                    true
                    );
            showPossibleMoves = new JCheckBoxMenuItem(showPossibleMovesAction);
            showPossibleMoves.setState(_ui.is_showPossibleMoves());
            menuExtras.add(showPossibleMoves);

            menuExtras.addSeparator();

            // -- number of games --
            numberOfGamesAction = new CommandAction(
                    null,
                    "Number of games", null, "Sets the number of games to play in a row (good for computer vs. computer)",
                    null, 0,
                    true
                    );
            JMenu numberOfGamesExtras = new JMenu(numberOfGamesAction);
            menuExtras.add(numberOfGamesExtras);

            // -- radio grouped --
            numberOfGamesGroup = new ButtonGroup();

            addNumberOfGamesMenuItem(1, numberOfGamesExtras);
            addNumberOfGamesMenuItem(5, numberOfGamesExtras);
            addNumberOfGamesMenuItem(10, numberOfGamesExtras);
            addNumberOfGamesMenuItem(20, numberOfGamesExtras);
            addNumberOfGamesMenuItem(50, numberOfGamesExtras);
            addNumberOfGamesMenuItem(100, numberOfGamesExtras);
            addNumberOfGamesOtherMenuItem(numberOfGamesExtras);

            // -- select the initial number --
            String nog = Integer.toString(Chessly.getPlayroom().getNumberOfGames());
            group = numberOfGamesGroup.getElements();
            selected = false;
            selected = selectItem(group, nog, selected);
            if (!selected) {
                numberOfGamesExtras_other.setSelected(true);
            }

            menuExtras.addSeparator();

            // -- number of threads --
            numberOfThreadsAction = new CommandAction(
                    Command.parse(_MVController, "numberOfTreadsDialog"),
                    "Number of Threads...", null, "Number of Threads...",
                    KeyStroke.getKeyStroke(84, java.awt.event.InputEvent.CTRL_DOWN_MASK),
                    0,
                    true
                    );
            menuExtras.add(numberOfThreadsAction);

            menuExtras.addSeparator();

            menuExtras.add(initLookAndFeelMenu());

            /* **********************************************************************
            // -- menu Help --
             ************************************************************************/
            JMenu menuHelp = new JMenu("?");
            add(menuHelp);

            JMenuItem helpHelp = new JMenuItem("Help");
            helpHelp.setToolTipText("Not yet available!");
            helpHelp.setEnabled(false);
            menuHelp.add(helpHelp);

            menuHelp.addSeparator();

            JMenuItem helpAbout = new JMenuItem("About");
            helpAbout.setToolTipText("About this program");
            helpAbout.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AboutDialog dialog = new AboutDialog();
                    dialog.pack();
                    dialog.setResizable(false);
                    AbstractDialog.centerComponent(dialog);
                    dialog.setVisible(true);
                }
            });
            helpAbout.setEnabled(true);
            menuHelp.add(helpAbout);

        } catch (IOException e) { // because we use Command.parse()
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private void addBlackLevel(final int level, JMenu blackLevel) {
        JMenuItem myBlackLevel = new JRadioButtonMenuItem(String.valueOf(level));
        myBlackLevel.setEnabled(true);
        myBlackLevel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _MVController.setLevelAction(GameColor.BLACK, level);
            }
        });
        blackLevel.add(myBlackLevel);
        blackLevelGroup.add(myBlackLevel);
    }

    private void addBlackLevelOther(JMenu blackLevel) {
        blackLevel_other = new JRadioButtonMenuItem("other");
        blackLevel_other.setEnabled(true);
        blackLevel_other.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // -- default question --
                String question = "Please enter a level for Black";
                String userInput;
                int level = 0;
                do { // -- loop until there is a valid input or cancel --
                    // -- get the input --
                    userInput = _ui.getMainWindow().userInputDialog(question, "Black Level");
                    try {
                        level = Integer.parseInt(userInput);
                    } catch (NumberFormatException nfe) {
                        // -- there was no integer in the input --
                        question = "The level for Black must be a valid postive number";
                    }
                    if (level < 1) { // -- level must be > 0 --
                        question = "The level for Black must be a valid postive number";
                        level = 0;
                    }
                } while (userInput != null && level == 0);
                // -- set level if not cancel has been pressed --
                if (userInput != null) {
                    _MVController.setLevelAction(GameColor.BLACK, level);
                }
                // -- select the current level --
                String lb = Integer.toString(Chessly.getPlayroom().getCurrentEngineLevelBlack());
                Enumeration group = blackLevelGroup.getElements();
                boolean selected = false;
                selected = selectItem(group, lb, selected);
                if (!selected) {
                    blackLevel_other.setSelected(true);
                }
            }
        });
        blackLevel.add(blackLevel_other);
        blackLevelGroup.add(blackLevel_other);
    }

    private void addWhiteLevel(final int level, JMenu whiteLevel) {
        JMenuItem myWhiteLevel = new JRadioButtonMenuItem(String.valueOf(level));
        myWhiteLevel.setEnabled(true);
        myWhiteLevel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _MVController.setLevelAction(GameColor.WHITE, level);
            }
        });
        whiteLevel.add(myWhiteLevel);
        whiteLevelGroup.add(myWhiteLevel);
    }

    private void addWhiteLevelOther(JMenu whiteLevel) {
        whiteLevel_other = new JRadioButtonMenuItem("other");
        whiteLevel_other.setEnabled(true);
        whiteLevel_other.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // -- default question --
                String question = "Please enter a level for White";
                String userInput;
                int level = 0;
                do { // -- loop until there is a valid input or cancel --
                    // -- get the input --
                    userInput = _ui.getMainWindow().userInputDialog(question, "White Level");
                    try {
                        level = Integer.parseInt(userInput);
                    } catch (NumberFormatException nfe) {
                        // -- there was no integer in the input --
                        question = "The level for White must be a valid postive number";
                    }
                    if (level < 1) { // -- level must be > 0 --
                        question = "The level for White must be a valid postive number";
                        level = 0;
                    }
                } while (userInput != null && level == 0);
                // -- set level if not cancel has been pressed --
                if (userInput != null) {
                    _MVController.setLevelAction(GameColor.WHITE, level);
                }
                // -- select the current level --
                String lb = Integer.toString(Chessly.getPlayroom().getCurrentEngineLevelWhite());
                Enumeration group = whiteLevelGroup.getElements();
                boolean selected = false;
                selected = selectItem(group, lb, selected);
                if (!selected) {
                    whiteLevel_other.setSelected(true);
                }
            }
        });
        whiteLevel.add(whiteLevel_other);
        whiteLevelGroup.add(whiteLevel_other);
    }

    private void addNumberOfGamesMenuItem(final int t, JMenu numberOfGamesExtras) {
        JMenuItem numberOfGamesExtras_1 = new JRadioButtonMenuItem(String.valueOf(t));
        numberOfGamesExtras_1.setEnabled(true);
        numberOfGamesExtras_1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _MVController.setNumberOfGamesAction(t);
            }
        });
        numberOfGamesExtras.add(numberOfGamesExtras_1);
        numberOfGamesGroup.add(numberOfGamesExtras_1);
    }

    private void addNumberOfGamesOtherMenuItem(JMenu numberOfGamesExtras) {
        numberOfGamesExtras_other = new JRadioButtonMenuItem("other");
        numberOfGamesExtras_other.setEnabled(true);
        numberOfGamesExtras_other.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // -- default question --
                String question = "Please enter the number of sequential games";
                String userInput;
                int number = 0;
                do { // -- loop until there is a valid input or cancel --
                    // -- get the input --
                    userInput = _ui.getMainWindow().userInputDialog(question, "Number of sequential games");
                    try {
                        number = Integer.parseInt(userInput);
                    } catch (NumberFormatException nfe) {
                        // -- there was no integer in the input --
                        question = "The number of games must be a valid postive number";
                    }
                    if (number < 1) { // -- level must be > 3 --
                        question = "The number of games must at least 1";
                        number = 0;
                    }
                } while (userInput != null && number == 0);
                // -- set number if not cancel has been pressed --
                if (userInput != null) {
                    _MVController.setNumberOfGamesAction(number);
                }
                // -- select the current level --
                String lb = Integer.toString(8);
                Enumeration group = numberOfGamesGroup.getElements();
                boolean selected = false;
                selected = selectItem(group, lb, selected);
                if (!selected) {
                    numberOfGamesExtras_other.setSelected(true);
                }
            }
        });
        numberOfGamesExtras.add(numberOfGamesExtras_other);
        numberOfGamesGroup.add(numberOfGamesExtras_other);
    }

    private static boolean selectItem(Enumeration group, String lb, boolean selected) {
        while (group.hasMoreElements()) {
            JRadioButtonMenuItem curItem = (JRadioButtonMenuItem) group.nextElement();
            if (curItem.getText().matches(lb)) {
                curItem.setSelected(true);
                selected = true;
            }
        }
        return selected;
    }

    private JMenu initLookAndFeelMenu() {
        // -- Look & Feel --
        JMenu lookAndFeel = new JMenu("Look & Feel");
        lookAndFeel.setToolTipText("Change the look & feel");
        lookAndFeel.setEnabled(true);

        // -- radio grouped --
        ButtonGroup lookAndFeelGroup = new ButtonGroup();

        // -- check for look & feels --
        UIManager.LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();

        // -- create menueitem for all foound look&feels --
        for (UIManager.LookAndFeelInfo plaf : plafs) {
            String plafsName = plaf.getName();
            final String plafClassName = plaf.getClassName();
            //noinspection ObjectAllocationInLoop
            JMenuItem menuitem = lookAndFeel.add(new JRadioButtonMenuItem(plafsName+" ("+plafClassName+")"));
            // -- action listener --
            //noinspection ObjectAllocationInLoop
            menuitem.addActionListener(new lookAndFeelActionListener(plafClassName));
            lookAndFeelGroup.add(menuitem);
        }
        return lookAndFeel;
    }

    public JCheckBoxMenuItem getShowMoveList() {
        return showMoveList;
    }

    public JCheckBoxMenuItem getShowEngineInfoBlack() {
        return showEngineInfoBlack;
    }

    public JCheckBoxMenuItem getShowEngineInfoWhite() {
        return showEngineInfoWhite;
    }

    public JCheckBoxMenuItem getShowPossibleMoves() {
        return showPossibleMoves;
    }

    public CommandAction getNewGameAction() {
        return newGameAction;
    }

    public CommandAction getStopGameAction() {
        return stopGameAction;
    }

    public CommandAction getPauseGameAction() {
        return pauseGameAction;
    }

    public CommandAction getResumeGameAction() {
        return resumeGameAction;
    }

    public CommandAction getExitAction() {
        return exitAction;
    }

    public CommandAction getUndoMoveAction() {
        return undoMoveAction;
    }

    public CommandAction getTimedGameAction() {
        return timedGameAction;
    }

    public CommandAction getTimeBlackAction() {
        return timeBlackAction;
    }

    public CommandAction getTimeWhiteAction() {
        return timeWhiteAction;
    }

    public CommandAction getBlackLevelAction() {
        return blackLevelAction;
    }

    public CommandAction getWhiteLevelAction() {
        return whiteLevelAction;
    }

    public CommandAction getShowPossibleMovesAction() {
        return showPossibleMovesAction;
    }

    public CommandAction getShowMovelistAction() {
        return showMovelistAction;
    }

    public CommandAction getNumberOfGamesAction() {
        return numberOfGamesAction;
    }

    public CommandAction getNumberOfThreadsAction() {
        return numberOfGamesAction;
    }

    public CommandAction getShowEngineInfoBlackAction() {
        return showEngineInfoBlackAction;
    }

    public CommandAction getShowEngineInfoWhiteAction() {
        return showEngineInfoWhiteAction;
    }

    private class lookAndFeelActionListener implements ActionListener {
        private final String plafClassName;

        private lookAndFeelActionListener(String aPlafClassName) {
            this.plafClassName = aPlafClassName;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                e.getSource();
                UIManager.setLookAndFeel(plafClassName);
                SwingUtilities.updateComponentTreeUI(_ui.getMainWindow());
                SwingUtilities.updateComponentTreeUI(_ui.getMoveListWindow());
                SwingUtilities.updateComponentTreeUI(_ui.getEngineInfoWindowBlack());
                SwingUtilities.updateComponentTreeUI(_ui.getEngineInfoWindowWhite());
            } catch (Exception ex) {
                //noinspection UseOfSystemOutOrSystemErr
                System.err.println(ex);
            }
        }
    }

    private class timeWhiteActionListener implements ActionListener {
        public void dialog() {
            actionPerformed(null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // -- default question --
            String origQuestion = "Please enter the time available for white (min:sec)";
            String question = origQuestion;
            String userInput;
            int timeInMilliSec = 0;
            do { // -- loop until there is a valid input or cancel --
                // -- get the input --
                long timeWhite = Chessly.getPlayroom().getTimeWhite() / 1000;
                String defaultString = (timeWhite / 60) + ":" + digitFormat.format((timeWhite % 60));
                userInput = _ui.getMainWindow().userInputDialog(question, "Time White", defaultString);
                // -- cancel --
                if (userInput == null) {
                    return;
                }
                // -- input must be of format min:sec --
                // -- ^((\d*:[0-5])?|(:[0-5])?|(\d*)?)\d$ --
                if (!(userInput.matches("^\\d$")
                        || userInput.matches("^\\d*\\d$")
                        || userInput.matches("^:[0-5]\\d$")
                        || userInput.matches("^\\d*:[0-5]\\d$")
                        )
                        ) {
                    question = "Wrong Format\n" + origQuestion;
                    continue;
                } else {
                    question = origQuestion;
                    // -- is there a colon? --
                    int indexOfColon;
                    if ((indexOfColon = userInput.indexOf(':')) >= 0) {
                        // -- is it at the first place? --
                        if (userInput.startsWith(":")) {
                            // -- if there is a colon we only allow numbers <60
                            timeInMilliSec = Integer.parseInt(userInput.substring(1));
                        } else { // -- not starting with a colon --
                            int min = Integer.parseInt(userInput.substring(0, indexOfColon));
                            int sec = Integer.parseInt(userInput.substring(indexOfColon + 1));
                            timeInMilliSec = min * 60 + sec;
                        }
                    } else { // -- no colon --
                        try {
                            timeInMilliSec = Integer.parseInt(userInput);
                        } catch (NumberFormatException ex) {
                            question = "Not a valid time in sec. Too big?\n" + origQuestion;
                        }
                    }
                }
            } while (timeInMilliSec == 0);
            // -- set level if not cancel has been pressed--
            _MVController.setTimeWhiteAction(timeInMilliSec * 1000);
        }
    }

    private class timeBlackActionListener implements ActionListener {
        public void dialog() {
            actionPerformed(null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // -- default question --
            String origQuestion = "Please enter the time available for black (min:sec)";
            String question = origQuestion;
            String userInput;
            int timeInMilliSec = 0;
            do { // -- loop until there is a valid input or cancel --
                // -- get the input --
                long timeBlack = Chessly.getPlayroom().getTimeBlack() / 1000;
                String defaultString = (timeBlack / 60) + ":" + digitFormat.format((timeBlack % 60));
                userInput = _ui.getMainWindow().userInputDialog(question, "Time Black", defaultString);
                // -- cancel --
                if (userInput == null) {
                    return;
                }
                // -- input must be of format min:sec --
                // -- ^((\d*:[0-5])?|(:[0-5])?|(\d*)?)\d$ --
                if (!(userInput.matches("^\\d$")
                        || userInput.matches("^\\d*\\d$")
                        || userInput.matches("^:[0-5]\\d$")
                        || userInput.matches("^\\d*:[0-5]\\d$")
                        )
                        ) {
                    question = "Wrong Format\n" + origQuestion;
                    continue;
                } else {
                    question = origQuestion;
                    // -- is there a colon? --
                    int indexOfColon;
                    if ((indexOfColon = userInput.indexOf(':')) >= 0) {
                        // -- is it at the first place? --
                        if (userInput.startsWith(":")) {
                            // -- if there is a colon we only allow numbers <60
                            timeInMilliSec = Integer.parseInt(userInput.substring(1));
                        } else { // -- not starting with a colon --
                            int min = Integer.parseInt(userInput.substring(0, indexOfColon));
                            int sec = Integer.parseInt(userInput.substring(indexOfColon + 1));
                            timeInMilliSec = min * 60 + sec;
                        }
                    } else { // -- no colon --
                        try {
                            timeInMilliSec = Integer.parseInt(userInput);
                        } catch (NumberFormatException ex) {
                            question = "Not a valid time in sec. Too big?\n" + origQuestion;
                        }
                    }
                }
            } while (timeInMilliSec == 0);
            // -- set level if not cancel has been pressed--
            _MVController.setTimeBlackAction(timeInMilliSec * 1000);
        }
    }

}
