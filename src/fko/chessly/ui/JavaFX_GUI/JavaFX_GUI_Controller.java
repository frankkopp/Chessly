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
package fko.chessly.ui.JavaFX_GUI;

import java.net.URL;
import java.text.Format;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;

import fko.chessly.Chessly;
import fko.chessly.Playroom;
import fko.chessly.game.Game;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.mvc.ModelObservable;
import fko.chessly.mvc.ModelEvents.ModelEvent;
import fko.chessly.player.ComputerPlayer;
import fko.chessly.player.HumanPlayer;
import fko.chessly.player.PlayerType;
import fko.chessly.ui.JavaFX_GUI.MoveListModel.FullMove;
import fko.chessly.util.HelperTools;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Main Controller for the Chessly JavaFX user interface.
 * It actually builds the complete UI when <code>initialize</code> is
 * called. It received all fields from the FXML file which were injected with
 * values by the FXML loader. It takes care of all user input by defining
 * actions which are called by the UI and which were declared in the FXML file.
 * This controller is also defined as an observer to the model and receives
 * all updates the model shares via <code>notifyObservers(...)</code>.
 * The <code>update</code> and <code>updateFrom</code> methods will then update the ui
 * based on the changes in the model. It is important that the the update of the ui
 * will happen via <code>Platform.runLater()</code> so they happen in the FX thread.
 *
 * @author Frank
 */
public class JavaFX_GUI_Controller implements Observer {

    /*
     * FMXL injected variables are declared at the and of the file!
     */

    // -- to save and restore the last position of our window
    private static final WindowStateFX windowState = new WindowStateFX();

    private static final boolean VERBOSE_TO_SYSOUT = false;

    // reference to the _model (playroom) --
    private Playroom _model;

    // we only can tell the model the users move when we know which player we have to use --
    private HumanPlayer _moveReceiver = null;
    private final Object _moveReceiverLock = new Object();

    // important nodes
    private Stage _primaryStage;
    private BoardPane _boardPane;

    private MoveListModel _moveListModel = new MoveListModel();

    @SuppressWarnings("unused")
    private PlayerClockUpdater _clockUpdater;

    // get values from properties --
    private final BooleanProperty _showLastMove =
            new SimpleBooleanProperty(this, "showLastMove",
                    Boolean.valueOf(Chessly.getProperties().getProperty("ui.showLastMove")));
    private final BooleanProperty _showPossibleMoves =
            new SimpleBooleanProperty(this, "showPossibleMoves",
                    Boolean.valueOf(Chessly.getProperties().getProperty("ui.showPossibleMoves")));

    private final BooleanProperty _timedGame =
            new SimpleBooleanProperty(this, "timedGame",false);

    // engineInfoUpdater
    @SuppressWarnings("unused")
    private EngineInfoUpdater _whiteEngineInfoUpdater;
    @SuppressWarnings("unused")
    private EngineInfoUpdater _blackEngineInfoUpdater;

    // engine info windows with text areas
    private InfoTextArea _infoAreaW =  new InfoTextArea();;
    private Scene _infoAreaSceneW = new Scene(_infoAreaW);
    private Stage _popupW = new Stage(StageStyle.DECORATED);
    private InfoTextArea _infoAreaB =  new InfoTextArea();;
    private Scene _infoAreaSceneB = new Scene(_infoAreaB);
    private Stage _popupB = new Stage(StageStyle.DECORATED);

    // the general info pane
    private InfoTextArea _info_panel;

    // ##########################################
    // methods
    // ##########################################

    /**
     * This method is called by the FXMLLoader when initialization is complete
     */
    @FXML // This method is called by the FXMLLoader when initialization is complete
    protected void initialize() {

        this._model = Chessly.getPlayroom();

        // check FXML ID initialization
        assertFXids();

        // set convenience reference to primary stage
        _primaryStage = JavaFX_GUI.getPrimaryStage();

        // status bar
        statusbar_status_text.setText("JavaFX GUI started");

        // add board panel from previous SwingGUI
        addBoardPanel();

        // configure info_panel
        configInfoPanel();

        // configure move_list
        configMoveList();

        // add constantly updated memory info into status panel
        addMemLabelUpdater();

        // add engine info updater
        addEngineUpdater();

        // add a updater for the player clocks
        addClockUpdater();

        // bind some controls controls
        createBindings();

        // configure level menu
        configEngineLevel();

        // reset the controls to no game
        setControlsNoGame();
    }

    /**
     * Adds the board panel from the previous Swing UI.
     */
    private void addBoardPanel() {
        _boardPane = new BoardPane(this);
        board_panel_grid.getChildren().add(_boardPane);
    }

    /**
     * Configures the info_panel
     */
    private void configInfoPanel() {
        _info_panel = new InfoTextArea();
        infoTab_pane.getChildren().add(_info_panel);
        AnchorPane.setLeftAnchor(_info_panel, 0.0);
        AnchorPane.setRightAnchor(_info_panel, 0.0);
        AnchorPane.setBottomAnchor(_info_panel, 0.0);
        AnchorPane.setTopAnchor(_info_panel, 0.0);

        printToInfoln();
        printToInfoln("Java GUI started!");
    }

    /**
     * Configures the move_table
     */
    private void configMoveList() {
        move_table_number.setCellValueFactory(cellData -> cellData.getValue().numberProperty());
        move_table_white.setCellValueFactory(cellData -> cellData.getValue().whiteProperty());
        move_table_black.setCellValueFactory(cellData -> cellData.getValue().blackProperty());
        move_table.setItems(_moveListModel.getMoveList());
        move_table.setStyle("-fx-font-size: 10; -fx-font-family: \"Lucida Console\";");
        // scroll to last entry
        move_table.getItems().addListener((ListChangeListener<FullMove>)  (c -> {
            c.next();
            if (move_table.getItems().size() > 0) {
                Platform.runLater(this::scrollToEnd);
            }
        }));

    }

    /**
     *
     */
    private void scrollToEnd() {
        move_table.scrollTo(move_table.getItems().size() - 1);
    }

    /**
     * Adds an updater to the mem label in the status bar
     */
    private void addMemLabelUpdater() {
        Task<Void> dynamicTimeTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while (true) {
                    updateMessage(HelperTools.getMBytes(Runtime.getRuntime().freeMemory()) + " MB / "
                            + HelperTools.getMBytes(Runtime.getRuntime().totalMemory()) + " MB");
                    try {Thread.sleep(500);} catch (InterruptedException ex) {break;}
                }
                return null;
            }
        };
        statusbar_mem_text.textProperty().bind(dynamicTimeTask.messageProperty());
        Thread t2 = new Thread(dynamicTimeTask);
        t2.setName("Statusbar Mem Labal Updater");
        t2.setDaemon(true);
        t2.start();
    }

    private void addEngineUpdater() {

        // create updater for engine info - the updater has a thread to update the info fields regularly
        _whiteEngineInfoUpdater = new EngineInfoUpdater(GameColor.WHITE, new EngineInfoLabels(GameColor.WHITE));
        _blackEngineInfoUpdater = new EngineInfoUpdater(GameColor.BLACK, new EngineInfoLabels(GameColor.BLACK));

        // on popup close uncheck the checkbox so that external close (window close button)
        // also uncheck the checkbox
        _popupW.setOnHidden(e -> {showVerboseInfo_checkboxW.setSelected(false);});
        _popupB.setOnHidden(e -> {showVerboseInfo_checkboxB.setSelected(false);});

        // set the primary window as the owner
        _popupW.initOwner(_primaryStage);
        _popupB.initOwner(_primaryStage);

        // move the engine info windows with the main window.
        _primaryStage.xProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double offset_X_W = _popupW.getX() - oldValue.doubleValue();
                double offset_X_B = _popupB.getX() - oldValue.doubleValue();
                _popupW.setX(newValue.doubleValue()+offset_X_W);
                _popupB.setX(newValue.doubleValue()+offset_X_B);
            }
        });
        _primaryStage.yProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double offset_Y_W = _popupW.getY() - oldValue.doubleValue();
                double offset_Y_B = _popupB.getY() - oldValue.doubleValue();
                _popupW.setY(newValue.doubleValue()+offset_Y_W);
                _popupB.setY(newValue.doubleValue()+offset_Y_B);
            }
        });

        // bring the correct engine info area to front when engine info tab is selected
        white_enginetab.setOnSelectionChanged(e -> {
            if (_primaryStage.isFocused() && white_enginetab.isSelected()) _popupW.toFront();
        });

        // show the window below the main window when checkbox is selected
        showVerboseInfo_checkboxW.setOnAction(e -> {
            showVerboseInfoWhite();
        });

        // bring the correct engine info area to front when engine info tab is selected
        black_enginetab.setOnSelectionChanged(e -> {
            if (_primaryStage.isFocused() && black_enginetab.isSelected()) _popupB.toFront();
        });

        // show the window below the main window when checkbox is selected
        showVerboseInfo_checkboxB.setOnAction(e -> {
            showVerboseInfoBlack();
        });

        // should the verboseInfo windows be shown from the beginning?
        // we will call showVerboseInfo... after we created the Stage in the starting class
        boolean whiteVerboseInfo = Boolean.parseBoolean(
                JavaFX_GUI_Controller.getWindowState().getProperty("VerboseInfoWhite","false"));
        boolean blackVerboseInfo = Boolean.parseBoolean(
                JavaFX_GUI_Controller.getWindowState().getProperty("VerboseInfoBlack","false"));
        showVerboseInfo_checkboxW.setSelected(whiteVerboseInfo);
        showVerboseInfo_checkboxB.setSelected(blackVerboseInfo);
    }

    private void addClockUpdater() {
        _clockUpdater = new PlayerClockUpdater(
                whitePlayer_name, white_clock, white_playertype, white_progressbar,
                blackPlayer_name, black_clock, black_playertype, black_progressbar
                );
    }

    private void createBindings() {
        showLastMove_menu.selectedProperty().bindBidirectional(_showLastMove);
        showPossibleMoves_menu.selectedProperty().bindBidirectional(_showPossibleMoves);
        _timedGame.set(_model.isTimedGame());
        timedGame_menu.selectedProperty().bindBidirectional(_timedGame);
    }

    /**
     * Set the pre-selection for the level menu
     */
    private void configEngineLevel() {
        int lW = _model.getCurrentEngineLevelWhite();
        switch (lW) {
            case 1:
            case 2:
                levelWhite.selectToggle(levelWhite2);
                break;
            case 3:
            case 4:
                levelWhite.selectToggle(levelWhite4);
                break;
            case 5:
            case 6:
                levelWhite.selectToggle(levelWhite6);
                break;
            case 7:
            case 8:
                levelWhite.selectToggle(levelWhite8);
                break;
            case 9:
            case 10:
                levelWhite.selectToggle(levelWhite10);
                break;
            case 20:
                levelWhite.selectToggle(levelWhite20);
                break;
            case 99:
                levelWhite.selectToggle(levelWhiteMax);
                break;
            default:
                break;
        }
        int lB = _model.getCurrentEngineLevelBlack();
        switch (lB) {
            case 1:
            case 2:
                levelBlack.selectToggle(levelBlack2);
                break;
            case 3:
            case 4:
                levelBlack.selectToggle(levelBlack4);
                break;
            case 5:
            case 6:
                levelBlack.selectToggle(levelBlack6);
                break;
            case 7:
            case 8:
                levelBlack.selectToggle(levelBlack8);
                break;
            case 9:
            case 10:
                levelBlack.selectToggle(levelBlack10);
                break;
            case 20:
                levelBlack.selectToggle(levelBlack20);
                break;
            case 99:
                levelBlack.selectToggle(levelBlackMax);
                break;
            default:
                break;
        }
    }

    /**
     * Shows the verboseInfoWindow for Black if the showVerboseInfo_checkboxB is selected.
     */
    protected void showVerboseInfoWhite() {
        if (showVerboseInfo_checkboxW.isSelected()) {
            _popupW.setTitle("White Engine Info");
            _popupW.setWidth(_primaryStage.getScene().getWindow().getWidth());
            _popupW.setHeight(200);
            _popupW.setX(_primaryStage.getScene().getWindow().getX());
            _popupW.setY(_primaryStage.getScene().getWindow().getY()+_primaryStage.getScene().getWindow().getHeight());
            _popupW.setScene(_infoAreaSceneW);
            _popupW.setMaximized(false);
            _popupW.show();
        } else {
            _infoAreaW.clear();
            _popupW.hide();
        }
    }

    /**
     * Shows the verboseInfoWindow for Black if the showVerboseInfo_checkboxB is selected.
     */
    protected void showVerboseInfoBlack() {
        if (showVerboseInfo_checkboxB.isSelected()) {
            _popupB.setTitle("Black Engine Info");
            _popupB.setWidth(_primaryStage.getScene().getWindow().getWidth());
            _popupB.setHeight(200);
            _popupB.setX(_primaryStage.getScene().getWindow().getX());
            _popupB.setY(_primaryStage.getScene().getWindow().getY()+_primaryStage.getScene().getWindow().getHeight()+_popupW.getHeight());
            _popupB.setScene(_infoAreaSceneB);
            _popupB.setMaximized(false);
            _popupB.show();
        } else {
            _infoAreaB.clear();
            _popupB.hide();
        }
    }

    /**
     * @return true when highlighting last move on the board is activated
     */
    public boolean isShowLastMove() {
        return _showLastMove.get();
    }

    /**
     * @return true when highlighting possible moves on the board is activated
     */
    public boolean isShowPossibleMoves() {
        return _showPossibleMoves.get();
    }

    /**
     * This method is called from a mouse event from the user. It hands a move
     * over to a known HumanPlayer and then nulls the reference to the HumanPlayer. To
     * make a HumanPlayer known call setMoveReceiver(HumanPlayer). If no HumanPlayer is
     * known to the object nothing happens
     * @param move
     */
    public void setPlayerMove(GameMove move) {
        synchronized(_moveReceiverLock) {
            if (_moveReceiver!=null) {
                _moveReceiver.setMove(move);
            }
            // After we have handed over the move to the receiver player we delete the reference
            // to the receiver. This will be set again by setMoveReceiver by  the observer update
            // through the UI.
            _moveReceiver=null;
        }
    }

    /**
     * Is called when a HumanPlayer has notified its Oberservers (UI) that it is waiting
     * for a move from a human player.
     * If the receiving player is know to the class it accepts new moves through setPlayerMove()
     * usual from mouse input
     * @param player
     */
    public void setMoveReceiver(HumanPlayer player) {
        synchronized(_moveReceiverLock) {
            _moveReceiver = player;
        }
    }

    /**
     * Prints a string to the info_panel
     * @param s
     */
    private void printToInfo(String s) {
        if (VERBOSE_TO_SYSOUT) {
            System.out.print(String.format(s));
        }
        PlatformUtil.platformRunAndWait(() -> _info_panel.printInfo(String.format(s)));
        //info_panel.setScrollTop(Double.MAX_VALUE);
    }

    /**
     * Prints a string to the info_panel and adds a newline
     * @param s
     */
    private void printToInfoln(String s) {
        printToInfo(s+"%n");
    }

    /**
     * Prints a newline to the info_panel
     */
    private void printToInfoln() {
        printToInfo("%n");
    }

    /**
     * @return the windowstate
     */
    public static WindowStateFX getWindowState() {
        return windowState;
    }

    /*
    // ##########################################
    // Actions
    // ##########################################
     */

    @FXML
    void newGameDialog_Action(ActionEvent event) {
        NewGameDialog newGameDialog = new NewGameDialog();
        newGameDialog.showAndWait();
    }

    /**
     * @param event
     * @param whiteName
     * @param wT
     * @param blackName
     * @param bT
     */
    public void startNewGame_action(ActionEvent event, String whiteName, PlayerType wT, String blackName, PlayerType bT) {
        //System.out.println("Start New Game ACTION");
        _boardPane.resetBoard();
        _model.setNameWhitePlayer(whiteName);
        _model.setPlayerTypeWhite(wT);
        _model.setNameBlackPlayer(blackName);
        _model.setPlayerTypeBlack(bT);
        newGame();
    }

    /**
     * starts a new game
     */
    private synchronized void newGame() {
        if (!noCurrentGame()) {
            return;
        }
        _model.startPlayroom();
    }

    /**
     * determines if the game is over or stopped
     * @return true when game is over or stopped
     */
    private boolean noCurrentGame() {
        if (_model.getCurrentGame() == null) {
            return true;
        }
        return _model.getCurrentGame().isFinished();
    }

    @FXML
    void stopGame_action(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Stop currrent gamt");
        alert.setHeaderText("Stop the current game?");
        alert.setContentText("Do you really want to stop the current game?");
        alert.initOwner(_primaryStage);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            _model.stopPlayroom();
        } else {
            // ... user chose CANCEL or closed the dialog
        }
    }

    @FXML
    void pauseGame_action(ActionEvent event) {
        if (_model.getCurrentGame().isPaused()) {
            _model.getCurrentGame().resumeGame();
        } else if (_model.getCurrentGame().isRunning()) {
            _model.getCurrentGame().pauseGame();
        }
    }

    @FXML
    void resumeGame_action(ActionEvent event) {
        pauseGame_action(event);
    }

    @FXML
    void close_action(Event event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.initOwner(_primaryStage);
        alert.setTitle("Close Chessly");
        alert.setHeaderText("Close Chessly");
        alert.setContentText("Do your really want to close Chessly?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            saveWindowStates();
            Chessly.exitChessly();
        } else {
            // ... user chose CANCEL or closed the dialog
        }
    }

    /**
     * Save the current sizes and coordinates of all windows to restore them
     * when starting up the next time.
     */
    private void saveWindowStates() {
        windowState.setProperty("windowLocationX", String.valueOf(this._primaryStage.getX()));
        windowState.setProperty("windowLocationY", String.valueOf(this._primaryStage.getY()));
        windowState.setProperty("windowSizeX", String.valueOf(this._primaryStage.getWidth()));
        windowState.setProperty("windowSizeY", String.valueOf(this._primaryStage.getHeight()));
        windowState.setProperty("VerboseInfoWhite", String.valueOf(this.showVerboseInfo_checkboxW.isSelected()));
        windowState.setProperty("VerboseInfoBlack", String.valueOf(this.showVerboseInfo_checkboxB.isSelected()));
        windowState.save();
    }

    @FXML
    void undoMove_action(ActionEvent event) {
        undoMove_button.setDisable(true);
        undoMove_menu.setDisable(true);
        // take back two halfmoves
        _model.undoMove(2);
    }

    @FXML
    void timedGame_action(ActionEvent event) {
        _model.setTimedGame(_timedGame.get());
    }

    @FXML
    void timeWhite_action(ActionEvent event) {
        getTimeForPlayerFromUser(GameColor.WHITE);
    }

    @FXML
    void timeBlack_action(ActionEvent event) {
        getTimeForPlayerFromUser(GameColor.BLACK);
    }


    /**
     * This uses a dialog to ask the user for the player's time.
     * It checks the input and repeats the dialog if the input was not valid.
     * @param gameColor
     */
    private void getTimeForPlayerFromUser(GameColor gameColor) {

        String color = gameColor.equals(GameColor.WHITE) ? "white" : "black";

        // -- default question --
        Format digitFormat = new java.text.DecimalFormat("00");
        String origQuestion = "Please enter the time available for "+color+" (min:sec)";
        String question = origQuestion;
        String userInput;
        int timeInMilliSec = 0;
        do { // -- loop until there is a valid input or cancel --

            // -- get the input --
            long time = gameColor.equals(GameColor.WHITE) ? Chessly.getPlayroom().getTimeWhite() / 1000 : Chessly.getPlayroom().getTimeBlack() / 1000;

            String defaultString = (time / 60) + ":" + digitFormat.format((time % 60));

            // show input dialog
            TextInputDialog dialog = new TextInputDialog(defaultString);
            dialog.setTitle("Set Time for "+color+" player");
            dialog.setHeaderText("Time for "+color+" player");
            dialog.setContentText(question);
            dialog.initOwner(_primaryStage);
            Optional<String> result = dialog.showAndWait();

            // User entered value or canceled
            if (result.isPresent()) {
                userInput = result.get();

                // -- input must be of format min:sec --
                // -- ^((\d*:[0-5])?|(:[0-5])?|(\d*)?)\d$ --
                if (!(userInput.matches("^\\d$")
                        || userInput.matches("^\\d*\\d$")
                        || userInput.matches("^:[0-5]\\d$")
                        || userInput.matches("^\\d*:[0-5]\\d$")
                        )
                        ) {
                    question = "Wrong Format!\n" + origQuestion;
                    continue;
                }

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
                        question = "Not a valid time in sec.!\n" + origQuestion;
                    }
                }

            } else { // cancel
                return;
            }

        } while (timeInMilliSec == 0);

        if (gameColor.equals(GameColor.WHITE))
            _model.setTimeWhite(timeInMilliSec * 1000);
        else
            _model.setTimeBlack(timeInMilliSec * 1000);
    }

    @FXML
    void setLevelAction(ActionEvent event) {
        System.out.println("Set Level "+event);
        switch(((RadioMenuItem) event.getSource()).getId()) {
            case "levelWhite2":
                _model.setCurrentLevelWhite(2);
                break;
            case "levelWhite4":
                _model.setCurrentLevelWhite(4);
                break;
            case "levelWhite6":
                _model.setCurrentLevelWhite(6);
                break;
            case "levelWhite8":
                _model.setCurrentLevelWhite(8);
                break;
            case "levelWhite10":
                _model.setCurrentLevelWhite(10);
                break;
            case "levelWhite20":
                _model.setCurrentLevelWhite(20);
                break;
            case "levelWhiteMax":
                _model.setCurrentLevelWhite(99);
                break;
            case "levelBlack2":
                _model.setCurrentLevelBlack(2);
                break;
            case "levelBlack4":
                _model.setCurrentLevelBlack(4);
                break;
            case "levelBlack6":
                _model.setCurrentLevelBlack(6);
                break;
            case "levelBlack8":
                _model.setCurrentLevelBlack(8);
                break;
            case "levelBlack10":
                _model.setCurrentLevelBlack(10);
                break;
            case "levelBlack20":
                _model.setCurrentLevelBlack(20);
                break;
            case "levelBlackMax":
                _model.setCurrentLevelBlack(99);
                break;
            default:
                break;
        }
    }

    @FXML
    void showPossibleMoves_action(ActionEvent event) {
        _boardPane.drawBoard();
    }

    @FXML
    void showLastMove_action(ActionEvent event) {
        _boardPane.drawBoard();
    }

    @FXML
    void flipButton_action(ActionEvent event) {
        _boardPane.flipOrientation();
    }

    @FXML
    void aboutDialogOpen_action(ActionEvent event) {
        AboutDialog aboutDialogStage = new AboutDialog();
        aboutDialogStage.showAndWait();
    }


    // ##########################################
    // Observable updates from Model
    // ##########################################

    /**
     * Is called whenever the model has changes. Needs to update the GUI accordingly.
     *
     * The update itself need to be done in the FX thread (FAT) so it will be called via
     * <code>Platfrom.runLater()</code>. To make the caller wait for the ui to be updated
     * before the method returns we can use <code>PlatformUtil.platformRunAndWait()</code>.
     *
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @see PlatformUtil#platformRunAndWait(Runnable)
     *
     * @param model   the observable object.
     * @param event   an argument passed to the <code>notifyObservers</code>
     *                method.
     */
    @Override
    public void update(Observable model, Object event) {
        //System.out.println("JavaFX Controller: Update from "+event);
        //printToInfoln("Update from "+event);

        ModelEvent mevent = (ModelEvent)event;
        // Dispatch the update to the appropriate method
        // depending an who is calling
        // Playroom
        if (model instanceof Playroom) {
            updateFromPlayroom((Playroom) model, mevent);
            // Game
        } else if (model instanceof Game) {
            updateFromGame((Game) model, mevent);
            // HumanPlayer
        } else if (model instanceof HumanPlayer) {
            updateFromHumanPlayer((HumanPlayer) model, mevent);
        }
    }

    /**
     * Is called when model Playroom changed.
     *
     * The update itself need to be done in the FX thread (FAT) so it will be called via
     * <code>Platfrom.runLater()</code>. To make the caller wait for the ui to be updated
     * before the method returns we can use <code>PlatformUtil.platformRunAndWait()</code>.
     *
     * @see PlatformUtil#platformRunAndWait(Runnable)
     *
     * @param playroom
     * @param event
     */
    private void updateFromPlayroom(Playroom playroom, ModelEvent event) {
        //System.out.println("JavaFX Controller: Update from "+event);
        //printToInfoln("Update from "+event);

        // Playroom is playing - game exists
        if (playroom.isPlaying() && playroom.getCurrentGame() != null) {

            // -- game is initialized --
            if (event.signals(Playroom.SIG_PLAYROOM_GAME_CREATED)) {

                // clear the board panel
                PlatformUtil.platformRunAndWait(() -> {_boardPane.resetBoard();});

                // now we want to observe the game --
                playroom.getCurrentGame().addObserver(this);

                // observe the players
                if (playroom.getCurrentGame().getPlayerWhite() instanceof ModelObservable)
                    ((ModelObservable) playroom.getCurrentGame().getPlayerWhite()).addObserver(this);
                if (playroom.getCurrentGame().getPlayerBlack() instanceof ModelObservable)
                    ((ModelObservable) playroom.getCurrentGame().getPlayerBlack()).addObserver(this);

                // observe engines
                if (playroom.getCurrentGame().getPlayerWhite() instanceof ComputerPlayer) {
                    if (((ComputerPlayer) playroom.getCurrentGame().getPlayerWhite()).getEngine() instanceof ModelObservable) {
                        ((ModelObservable) ((ComputerPlayer) playroom.getCurrentGame().getPlayerWhite()).getEngine()).addObserver(this);
                    }
                }
                if (playroom.getCurrentGame().getPlayerBlack() instanceof ComputerPlayer) {
                    if (((ComputerPlayer) playroom.getCurrentGame().getPlayerBlack()).getEngine() instanceof ModelObservable) {
                        ((ModelObservable) ((ComputerPlayer) playroom.getCurrentGame().getPlayerBlack()).getEngine()).addObserver(this);
                    }
                }

                // -- check if multiple games in a row should be run --
                if (playroom.getNumberOfGames() > 1) {
                    printToInfoln();
                    printToInfoln("Game series: Game "+
                            + playroom.getCurrentGameNumber()
                            + " of "
                            + playroom.getNumberOfGames()
                            );
                }
            }

            // -- finished playing a game --
            if (playroom.getCurrentGame().isFinished() &&
                    event.signals(Playroom.SIG_PLAYROOM_GAME_FINISHED)) {
                // -- check if multiple games in a row should be run --
                if (playroom.getNumberOfGames() > 1) {
                    printToInfoln(
                            ">>> White wins: " + playroom.getCurrentWhiteWins() + '\n' +
                            ">>> Black wins: " + playroom.getCurrentBlackWins() + '\n' +
                            ">>> Draws     : " + playroom.getCurrentDraws() + '\n'
                            );
                    printToInfoln("");
                }
            }
            PlatformUtil.platformRunAndWait(() -> gameOverGuiUpdate(playroom.getCurrentGame(), event));

            // Playroom is not playing - game still exists
        } else if (!playroom.isPlaying() && playroom.getCurrentGame() != null) {
            // -- multiple games completed --
            if (playroom.getCurrentGame().isFinished()) {
                if (event.signals(Playroom.SIG_PLAYROOM_THREAD_END)) {
                    // -- check if multiple games in a row should be run --
                    if (playroom.getNumberOfGames() > 1) {
                        printToInfoln(
                                ">>> Multiple games finished:" + '\n' +
                                ">>> White wins: " + playroom.getCurrentWhiteWins() +
                                " (" + (int)(((float) playroom.getCurrentWhiteWins() / (float) (playroom.getNumberOfGames())) * 100) + '%' + ")\n" +
                                ">>> Black wins: " + playroom.getCurrentBlackWins() +
                                " (" + (int)(((float) playroom.getCurrentBlackWins() / (float) (playroom.getNumberOfGames())) * 100) + '%' + ")\n" +
                                ">>> Draws     : " + playroom.getCurrentDraws() +
                                " (" + (int)(((float) playroom.getCurrentDraws() / (float) (playroom.getNumberOfGames())) * 100) + '%' + ")\n"
                                );
                        printToInfoln("");
                    }
                }
            }
            //updateFromGame(playroom.getCurrentGame(), event);

            // No game exists
        } else {
            PlatformUtil.platformRunAndWait(() -> setControlsNoGame());
        }
    }



    /**
     * Is called when model Game changed.
     *
     * The update itself need to be done in the FX thread (FAT) so it will be called via
     * <code>Platfrom.runLater()</code>. To make the caller wait for the ui to be updated
     * before the method returns we can use <code>PlatformUtil.platformRunAndWait()</code>.
     *
     * @see PlatformUtil#platformRunAndWait(Runnable)
     *
     * @param game
     * @param event
     */
    private void updateFromGame(Game game, ModelEvent event) {
        //System.out.println("JavaFX Controller: Update from "+event);

        // -- draw the current board of the current game --
        PlatformUtil.platformRunAndWait(() -> {_boardPane.setAndDrawBoard(game.getCurBoard());});
        System.out.println(game.getCurBoard().toFENString());

        // -- update the move list according to the moves in the current game --
        PlatformUtil.platformRunAndWait(() -> {updateMoveList(game);});

        // -- get the current status of the game --
        int status = game.getStatus();
        switch (status) {
            case Game.GAME_INITIALIZED:
                PlatformUtil.platformRunAndWait(() -> gameInitializedGuiUpdate(game, event));
                break;

            case Game.GAME_RUNNING:
                PlatformUtil.platformRunAndWait(() -> gameRunningGuiUpdate(game, event));
                break;

            case Game.GAME_OVER:
                PlatformUtil.platformRunAndWait(() -> gameOverGuiUpdate(game, event));
                break;

            case Game.GAME_PAUSED:
                PlatformUtil.platformRunAndWait(() -> gamePausedGuiUpdate());
                break;

            case Game.GAME_FINISHED:
                PlatformUtil.platformRunAndWait(() -> gameFinishedGuiUpdate());
                break;

            default:
                break;
        }
    }

    /**
     * Is called when model Human Player changed.
     *
     * The update itself need to be done in the FX thread (FAT) so it will be called via
     * <code>Platfrom.runLater()</code>. To make the caller wait for the ui to be updated
     * before the method returns we can use <code>PlatformUtil.platformRunAndWait()</code>.
     *
     * @see PlatformUtil#platformRunAndWait(Runnable)
     *
     * @param hp
     * @param event
     */
    private void updateFromHumanPlayer(HumanPlayer hp, ModelEvent event) {
        //System.out.println("JavaFX Controller: Update from "+event);
        setMoveReceiver(hp);
    }

    /**
     * @param game
     */
    private void updateMoveList(Game game) {
        _moveListModel.updateList(game.getCurBoard().getMoveHistory());
    }

    /**
     * @param game
     * @param event
     */
    private void gameInitializedGuiUpdate(Game game, ModelEvent event) {
        // -- set possible actions (menu) --
        newGame_menu.setDisable(true);
        newGame_button.setDisable(true);
        stopGame_menu.setDisable(true);
        stopGame_button.setDisable(true);
        pauseGame_menu.setDisable(true);
        pauseGame_button.setDisable(true);
        resumeGame_menu.setDisable(true);
        resumeGame_button.setDisable(true);
        close_menu.setDisable(false);
        undoMove_menu.setDisable(false);
        undoMove_button.setDisable(false);
        timedGame_menu.setDisable(false);
        timeWhite_menu.setDisable(false);
        timeBlack_menu.setDisable(false);
        flip_menu.setDisable(false);
        flip_button.setDisable(false);
        showLastMove_menu.setDisable(false);
        showPossibleMoves_menu.setDisable(false);
        about_menu.setDisable(false);
        // clear move list
        _moveListModel.clear();
        statusbar_status_text.setText("New Game started.");

        //if (event.signals(Playroom.SIG_PLAYROOM_GAME_CREATED)) {
        printToInfoln();
        printToInfoln("--- New Game started ------------------");
        printToInfoln("Player WHITE: "+game.getPlayerWhite().getName());
        printToInfoln("Player BLACK: "+game.getPlayerBlack().getName());
        printToInfoln("");
        //}
    }

    /**
     * Resets all controls to game running status
     * @param event
     * @param game
     */
    private void gameRunningGuiUpdate(Game game, ModelEvent event) {
        // -- set possible actions (menu) --
        newGame_menu.setDisable(true);
        newGame_button.setDisable(true);
        stopGame_menu.setDisable(false);
        stopGame_button.setDisable(false);
        pauseGame_menu.setDisable(false);
        pauseGame_button.setDisable(false);
        resumeGame_menu.setDisable(true);
        resumeGame_button.setDisable(true);
        close_menu.setDisable(false);
        undoMove_menu.setDisable(false);
        undoMove_button.setDisable(false);
        timedGame_menu.setDisable(false);
        timeWhite_menu.setDisable(false);
        timeBlack_menu.setDisable(false);
        flip_menu.setDisable(false);
        flip_button.setDisable(false);
        showLastMove_menu.setDisable(false);
        showPossibleMoves_menu.setDisable(false);
        about_menu.setDisable(false);
        statusbar_status_text.setText("Game running.");
    }

    /**
     * Resets all controls to game pause status
     */
    private void gamePausedGuiUpdate() {
        // -- set possible actions (menu) --
        newGame_menu.setDisable(true);
        newGame_button.setDisable(true);
        stopGame_menu.setDisable(true);
        stopGame_button.setDisable(true);
        pauseGame_menu.setDisable(true);
        pauseGame_button.setDisable(true);
        resumeGame_menu.setDisable(false);
        resumeGame_button.setDisable(false);
        close_menu.setDisable(false);
        undoMove_menu.setDisable(true);
        undoMove_button.setDisable(true);
        timedGame_menu.setDisable(false);
        timeWhite_menu.setDisable(false);
        timeBlack_menu.setDisable(false);
        flip_menu.setDisable(false);
        flip_button.setDisable(false);
        showLastMove_menu.setDisable(false);
        showPossibleMoves_menu.setDisable(false);
        about_menu.setDisable(false);
        statusbar_status_text.setText("Game paused.");
    }

    /**
     * Resets all controls to game finished status
     */
    private void gameFinishedGuiUpdate() {
        // -- set possible actions (menu) --
        newGame_menu.setDisable(true);
        newGame_button.setDisable(true);
        stopGame_menu.setDisable(true);
        stopGame_button.setDisable(true);
        pauseGame_menu.setDisable(true);
        pauseGame_button.setDisable(true);
        resumeGame_menu.setDisable(true);
        resumeGame_button.setDisable(true);
        close_menu.setDisable(false);
        undoMove_menu.setDisable(false);
        undoMove_button.setDisable(false);
        timedGame_menu.setDisable(false);
        timeWhite_menu.setDisable(false);
        timeBlack_menu.setDisable(false);
        flip_menu.setDisable(false);
        flip_button.setDisable(false);
        showLastMove_menu.setDisable(false);
        showPossibleMoves_menu.setDisable(false);
        about_menu.setDisable(false);
        statusbar_status_text.setText("Game finished.");
    }

    /**
     * Resets the controls when game is over and also print game over info.
     *
     * @param game
     * @param event
     */
    private void gameOverGuiUpdate(Game game, ModelEvent event) {
        gameFinishedGuiUpdate();
        statusbar_status_text.setText("Game over.");
        // -- only write something to the infoPanel when update came from a certain status
        // -- otherwise we get multiple outputs in the infoPanel for the same state
        if (event.signals(Game.SIG_GAME_OVER)) {
            printToInfoln("Game over!");
            if (game.getGameOverCause() == Game.GAMEOVER_CHECKMATE
                    || game.getGameOverCause() == Game.GAMEOVER_STALEMATE) {
                printToInfoln("Game Over!");
            } else if (game.getGameOverCause() == Game.GAMEOVER_TIME_IS_UP_FOR_ONE_PLAYER) {
                printToInfoln("Out of time!");
            } else if (game.getGameOverCause() == Game.GAMEOVER_ONE_PLAYER_HAS_RESIGNED) {
                printToInfoln("Resign!");
            } else {
                Chessly.fatalError(this.getClass().toString() + "Game Over without a valid reason!"+game.getGameOverCause());
            }
            if (game.getGameWinnerStatus() == Game.WINNER_BLACK) {
                printToInfoln("Winner: BLACK");
            } else if (game.getGameWinnerStatus() == Game.WINNER_WHITE) {
                printToInfoln("Winner: WHITE");
            } else if (game.getGameWinnerStatus() == Game.WINNER_DRAW) {
                printToInfoln("Winner: DRAW");
            } else {
                Chessly.fatalError(this.getClass().toString() + "Game Over without a valid result!");
            }
            printToInfoln("--- Game over -------------------------");
        }
    }

    /**
     * resets all control to no game status
     */
    private void setControlsNoGame() {
        // -- set possible actions (menu) --
        newGame_menu.setDisable(false);
        newGame_button.setDisable(false);
        stopGame_menu.setDisable(true);
        stopGame_button.setDisable(true);
        pauseGame_menu.setDisable(true);
        pauseGame_button.setDisable(true);
        resumeGame_menu.setDisable(true);
        resumeGame_button.setDisable(true);
        close_menu.setDisable(false);
        undoMove_menu.setDisable(true);
        undoMove_button.setDisable(true);
        timedGame_menu.setDisable(false);
        timeWhite_menu.setDisable(false);
        timeBlack_menu.setDisable(false);
        flip_menu.setDisable(false);
        flip_button.setDisable(false);
        showLastMove_menu.setDisable(false);
        showPossibleMoves_menu.setDisable(false);
        about_menu.setDisable(false);
        statusbar_status_text.setText("No game running.");
    }

    /**
     * From FXML Scene BUIlder() - checks if all elements from the FXML are injected
     * through assertions.
     */
    private void assertFXids() {
        assert levelBlack2 != null : "fx:id=\"levelBlack2\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert newGame_menu != null : "fx:id=\"newGame_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert timeBlack_menu != null : "fx:id=\"timeBlack_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert undoMove_menu != null : "fx:id=\"undoMove_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelBlack8 != null : "fx:id=\"levelBlack8\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelBlack10 != null : "fx:id=\"levelBlack10\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelBlack6 != null : "fx:id=\"levelBlack6\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert bcUse_labelB != null : "fx:id=\"bcUse_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelBlack4 != null : "fx:id=\"levelBlack4\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert move_table_number != null : "fx:id=\"move_table_number\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert ncMisses_labelW != null : "fx:id=\"ncMisses_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert ncSize_labelW != null : "fx:id=\"ncSize_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert bcUse_labelW != null : "fx:id=\"bcUse_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert ncHits_labelW != null : "fx:id=\"ncHits_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert bcHits_labelB != null : "fx:id=\"bcHits_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert black_progressbar != null : "fx:id=\"black_progressbar\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert bcSize_labelB != null : "fx:id=\"bcSize_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert info_tab != null : "fx:id=\"info_tab\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert status_labelW != null : "fx:id=\"status_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert menu_game != null : "fx:id=\"menu_game\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert ncSize_labelB != null : "fx:id=\"ncSize_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert timeWhite_menu != null : "fx:id=\"timeWhite_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert rootPanel != null : "fx:id=\"rootPanel\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert bcSize_labelW != null : "fx:id=\"bcSize_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert tab_pane != null : "fx:id=\"tab_pane\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert ncMisses_labelB != null : "fx:id=\"ncMisses_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert showLastMove_menu != null : "fx:id=\"showLastMove_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert resumeGame_button != null : "fx:id=\"resumeGame_button\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert newGame_button != null : "fx:id=\"newGame_button\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert status_labelB != null : "fx:id=\"status_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert white_playertype != null : "fx:id=\"white_playertype\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert flip_button != null : "fx:id=\"flip_button\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert nonQuiet_labelB != null : "fx:id=\"nonQuiet_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert black_clock != null : "fx:id=\"black_clock\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert config_labelB != null : "fx:id=\"config_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert blackPlayer_name != null : "fx:id=\"blackPlayer_name\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert nonQuiet_labelW != null : "fx:id=\"nonQuiet_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert close_menu != null : "fx:id=\"close_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelWhite10 != null : "fx:id=\"levelWhite10\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert black_enginetab != null : "fx:id=\"black_enginetab\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelBlack != null : "fx:id=\"levelBlack\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert ncHits_labelB != null : "fx:id=\"ncHits_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert board_panel_grid != null : "fx:id=\"board_panel_grid\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert bestMove_labelB != null : "fx:id=\"bestMove_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert move_table_white != null : "fx:id=\"move_table_white\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert bcMisses_labelB != null : "fx:id=\"bcMisses_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert resumeGame_menu != null : "fx:id=\"resumeGame_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelBlack20 != null : "fx:id=\"levelBlack20\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert stopGame_menu != null : "fx:id=\"stopGame_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert menu_moves != null : "fx:id=\"menu_moves\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert time_labelW != null : "fx:id=\"time_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelBlackMax != null : "fx:id=\"levelBlackMax\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelWhiteMax != null : "fx:id=\"levelWhiteMax\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert white_progressbar != null : "fx:id=\"white_progressbar\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert bcHits_labelW != null : "fx:id=\"bcHits_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert time_labelB != null : "fx:id=\"time_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert bcMisses_labelW != null : "fx:id=\"bcMisses_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert currentMove_labelB != null : "fx:id=\"currentMove_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelWhite6 != null : "fx:id=\"levelWhite6\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert nodes_labelB != null : "fx:id=\"nodes_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert showVerboseInfo_checkboxB != null : "fx:id=\"showVerboseInfo_checkboxB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelWhite8 != null : "fx:id=\"levelWhite8\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert about_menu != null : "fx:id=\"about_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert bestMove_labelW != null : "fx:id=\"bestMove_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelWhite2 != null : "fx:id=\"levelWhite2\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelWhite4 != null : "fx:id=\"levelWhite4\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert stopGame_button != null : "fx:id=\"stopGame_button\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert pauseGame_button != null : "fx:id=\"pauseGame_button\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert pauseGame_menu != null : "fx:id=\"pauseGame_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert currentMove_labelW != null : "fx:id=\"currentMove_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert statusbar_mem_text != null : "fx:id=\"statusbar_mem_text\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert depth_labelW != null : "fx:id=\"depth_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert config_labelW != null : "fx:id=\"config_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert move_table_black != null : "fx:id=\"move_table_black\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert nodes_labelW != null : "fx:id=\"nodes_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert showPossibleMoves_menu != null : "fx:id=\"showPossibleMoves_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert white_clock != null : "fx:id=\"white_clock\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert boards_labelB != null : "fx:id=\"boards_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert depth_labelB != null : "fx:id=\"depth_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert whitePlayer_name != null : "fx:id=\"whitePlayer_name\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelWhite20 != null : "fx:id=\"levelWhite20\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert boards_labelW != null : "fx:id=\"boards_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert undoMove_button != null : "fx:id=\"undoMove_button\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert statusbar_copyright_test != null : "fx:id=\"statusbar_copyright_test\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert pv_labelW != null : "fx:id=\"pv_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert speed_labelB != null : "fx:id=\"speed_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert ncUse_labelW != null : "fx:id=\"ncUse_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert flip_menu != null : "fx:id=\"flip_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert speed_labelW != null : "fx:id=\"speed_labelW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert menu_help != null : "fx:id=\"menu_help\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert menu_level != null : "fx:id=\"menu_level\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert pv_labelB != null : "fx:id=\"pv_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert move_table != null : "fx:id=\"move_table\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert white_enginetab != null : "fx:id=\"white_enginetab\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert levelWhite != null : "fx:id=\"levelWhite\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert ncUse_labelB != null : "fx:id=\"ncUse_labelB\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert black_playertype != null : "fx:id=\"black_playertype\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert statusbar_status_text != null : "fx:id=\"statusbar_status_text\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert showVerboseInfo_checkboxW != null : "fx:id=\"showVerboseInfo_checkboxW\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert menu_board != null : "fx:id=\"menu_board\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert timedGame_menu != null : "fx:id=\"timedGame_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert infoTab_pane != null : "fx:id=\"infoTab_pane\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";

    }

    /**
     * Helper class to give the engineInfoUpdater access to all the labels from the FXML
     */
    class EngineInfoLabels {

        Tab engineTab;
        final InfoTextArea infoArea;
        final Label pv_label;
        final Label currentMove_label;
        final Label bestMove_label;
        final Label depth_label;
        final Label time_label;
        final Label nodes_label;
        final Label speed_label;
        final Label boards_label;
        final Label nonQuiet_label;
        final Label ncSize_label;
        final Label ncUse_label;
        final Label ncHits_label;
        final Label ncMisses_label;
        final Label bcSize_label;
        final Label bcUse_label;
        final Label bcHits_label;
        final Label bcMisses_label;
        final Label config_label;
        final Label status_label;

        /**
         * @param color
         */
        public EngineInfoLabels(GameColor color) {
            if (!(color==GameColor.WHITE || color == GameColor.BLACK)) {
                throw new IllegalArgumentException("Invalid Color");
            }
            if (color == GameColor.WHITE) {
                engineTab = white_enginetab;
                infoArea = _infoAreaW;
                pv_label = pv_labelW;
                currentMove_label = currentMove_labelW;
                bestMove_label = bestMove_labelW;
                depth_label = depth_labelW;
                time_label = time_labelW;
                nodes_label = nodes_labelW;
                speed_label = speed_labelW;
                boards_label = boards_labelW;
                nonQuiet_label = nonQuiet_labelW;
                ncSize_label = ncSize_labelW;
                ncUse_label = ncUse_labelW;
                ncHits_label = ncHits_labelW;
                ncMisses_label = ncMisses_labelW;
                bcSize_label = bcSize_labelW;
                bcUse_label = bcUse_labelW;
                bcHits_label = bcHits_labelW;
                bcMisses_label = bcMisses_labelW;
                config_label = config_labelW;
                status_label = status_labelW;
            } else {
                engineTab = black_enginetab;
                infoArea = _infoAreaB;
                pv_label = pv_labelB;
                currentMove_label = currentMove_labelB;
                bestMove_label = bestMove_labelB;
                depth_label = depth_labelB;
                time_label = time_labelB;
                nodes_label = nodes_labelB;
                speed_label = speed_labelB;
                boards_label = boards_labelB;
                nonQuiet_label = nonQuiet_labelB;
                ncSize_label = ncSize_labelB;
                ncUse_label = ncUse_labelB;
                ncHits_label = ncHits_labelB;
                ncMisses_label = ncMisses_labelB;
                bcSize_label = bcSize_labelB;
                bcUse_label = bcUse_labelB;
                bcHits_label = bcHits_labelB;
                bcMisses_label = bcMisses_labelB;
                config_label = config_labelB;
                status_label = status_labelB;
            }
        }
    }

    // -- FXML START --

    @FXML // fx:id="move_table"
    private TableView<MoveListModel.FullMove> move_table; // Value injected by FXMLLoader

    @FXML // fx:id="move_table_number"
    private TableColumn<MoveListModel.FullMove, String> move_table_number; // Value injected by FXMLLoader

    @FXML // fx:id="move_table_white"
    private TableColumn<MoveListModel.FullMove, String> move_table_white; // Value injected by FXMLLoader

    @FXML // fx:id="move_table_black"
    private TableColumn<MoveListModel.FullMove, String> move_table_black; // Value injected by FXMLLoader

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="rootPanel"
    private BorderPane rootPanel; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack2"
    private RadioMenuItem levelBlack2; // Value injected by FXMLLoader

    @FXML // fx:id="newGame_menu"
    private MenuItem newGame_menu; // Value injected by FXMLLoader

    @FXML // fx:id="timeBlack_menu"
    private MenuItem timeBlack_menu; // Value injected by FXMLLoader

    @FXML // fx:id="undoMove_menu"
    private MenuItem undoMove_menu; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack8"
    private RadioMenuItem levelBlack8; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack10"
    private RadioMenuItem levelBlack10; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack6"
    private RadioMenuItem levelBlack6; // Value injected by FXMLLoader

    @FXML // fx:id="bcUse_labelB"
    private Label bcUse_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack4"
    private RadioMenuItem levelBlack4; // Value injected by FXMLLoader

    @FXML // fx:id="ncMisses_labelW"
    private Label ncMisses_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="ncSize_labelW"
    private Label ncSize_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="bcUse_labelW"
    private Label bcUse_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="ncHits_labelW"
    private Label ncHits_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="bcHits_labelB"
    private Label bcHits_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="black_progressbar"
    private ProgressBar black_progressbar; // Value injected by FXMLLoader

    @FXML // fx:id="bcSize_labelB"
    private Label bcSize_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="info_tab"
    private Tab info_tab; // Value injected by FXMLLoader

    @FXML // fx:id="status_labelW"
    private Label status_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="menu_game"
    private Menu menu_game; // Value injected by FXMLLoader

    @FXML // fx:id="ncSize_labelB"
    private Label ncSize_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="timeWhite_menu"
    private MenuItem timeWhite_menu; // Value injected by FXMLLoader

    @FXML // fx:id="bcSize_labelW"
    private Label bcSize_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="tab_pane"
    private TabPane tab_pane; // Value injected by FXMLLoader

    @FXML // fx:id="ncMisses_labelB"
    private Label ncMisses_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="showLastMove_menu"
    private CheckMenuItem showLastMove_menu; // Value injected by FXMLLoader

    @FXML // fx:id="resumeGame_button"
    private Button resumeGame_button; // Value injected by FXMLLoader

    @FXML // fx:id="newGame_button"
    private Button newGame_button; // Value injected by FXMLLoader

    @FXML // fx:id="status_labelB"
    private Label status_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="white_playertype"
    private Label white_playertype; // Value injected by FXMLLoader

    @FXML // fx:id="flip_button"
    private Button flip_button; // Value injected by FXMLLoader

    @FXML // fx:id="nonQuiet_labelB"
    private Label nonQuiet_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="black_clock"
    private Label black_clock; // Value injected by FXMLLoader

    @FXML // fx:id="config_labelB"
    private Label config_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="blackPlayer_name"
    private Label blackPlayer_name; // Value injected by FXMLLoader

    @FXML // fx:id="nonQuiet_labelW"
    private Label nonQuiet_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="close_menu"
    private MenuItem close_menu; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite10"
    private RadioMenuItem levelWhite10; // Value injected by FXMLLoader

    @FXML // fx:id="black_enginetab"
    private Tab black_enginetab; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack"
    private ToggleGroup levelBlack; // Value injected by FXMLLoader

    @FXML // fx:id="ncHits_labelB"
    private Label ncHits_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="board_panel_grid"
    private GridPane board_panel_grid; // Value injected by FXMLLoader

    @FXML // fx:id="bestMove_labelB"
    private Label bestMove_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="bcMisses_labelB"
    private Label bcMisses_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="resumeGame_menu"
    private MenuItem resumeGame_menu; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack20"
    private RadioMenuItem levelBlack20; // Value injected by FXMLLoader

    @FXML // fx:id="stopGame_menu"
    private MenuItem stopGame_menu; // Value injected by FXMLLoader

    @FXML // fx:id="menu_moves"
    private Menu menu_moves; // Value injected by FXMLLoader

    @FXML // fx:id="time_labelW"
    private Label time_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlackMax"
    private RadioMenuItem levelBlackMax; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhiteMax"
    private RadioMenuItem levelWhiteMax; // Value injected by FXMLLoader

    @FXML // fx:id="white_progressbar"
    private ProgressBar white_progressbar; // Value injected by FXMLLoader

    @FXML // fx:id="bcHits_labelW"
    private Label bcHits_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="time_labelB"
    private Label time_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="bcMisses_labelW"
    private Label bcMisses_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="currentMove_labelB"
    private Label currentMove_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite6"
    private RadioMenuItem levelWhite6; // Value injected by FXMLLoader

    @FXML // fx:id="nodes_labelB"
    private Label nodes_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="showVerboseInfo_checkboxB"
    private CheckBox showVerboseInfo_checkboxB; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite8"
    private RadioMenuItem levelWhite8; // Value injected by FXMLLoader

    @FXML // fx:id="about_menu"
    private MenuItem about_menu; // Value injected by FXMLLoader

    @FXML // fx:id="bestMove_labelW"
    private Label bestMove_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite2"
    private RadioMenuItem levelWhite2; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite4"
    private RadioMenuItem levelWhite4; // Value injected by FXMLLoader

    @FXML // fx:id="stopGame_button"
    private Button stopGame_button; // Value injected by FXMLLoader

    @FXML // fx:id="pauseGame_button"
    private Button pauseGame_button; // Value injected by FXMLLoader

    @FXML // fx:id="pauseGame_menu"
    private MenuItem pauseGame_menu; // Value injected by FXMLLoader

    @FXML // fx:id="currentMove_labelW"
    private Label currentMove_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="statusbar_mem_text"
    private Label statusbar_mem_text; // Value injected by FXMLLoader

    @FXML // fx:id="depth_labelW"
    private Label depth_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="config_labelW"
    private Label config_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="nodes_labelW"
    private Label nodes_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="showPossibleMoves_menu"
    private CheckMenuItem showPossibleMoves_menu; // Value injected by FXMLLoader

    @FXML // fx:id="white_clock"
    private Label white_clock; // Value injected by FXMLLoader

    @FXML // fx:id="boards_labelB"
    private Label boards_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="depth_labelB"
    private Label depth_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="whitePlayer_name"
    private Label whitePlayer_name; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite20"
    private RadioMenuItem levelWhite20; // Value injected by FXMLLoader

    @FXML // fx:id="boards_labelW"
    private Label boards_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="undoMove_button"
    private Button undoMove_button; // Value injected by FXMLLoader

    @FXML // fx:id="statusbar_copyright_test"
    private Label statusbar_copyright_test; // Value injected by FXMLLoader

    @FXML // fx:id="pv_labelW"
    private Label pv_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="speed_labelB"
    private Label speed_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="ncUse_labelW"
    private Label ncUse_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="flip_menu"
    private MenuItem flip_menu; // Value injected by FXMLLoader

    @FXML // fx:id="speed_labelW"
    private Label speed_labelW; // Value injected by FXMLLoader

    @FXML // fx:id="menu_help"
    private Menu menu_help; // Value injected by FXMLLoader

    @FXML // fx:id="menu_level"
    private Menu menu_level; // Value injected by FXMLLoader

    @FXML // fx:id="pv_labelB"
    private Label pv_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="white_enginetab"
    private Tab white_enginetab; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite"
    private ToggleGroup levelWhite; // Value injected by FXMLLoader

    @FXML // fx:id="ncUse_labelB"
    private Label ncUse_labelB; // Value injected by FXMLLoader

    @FXML // fx:id="black_playertype"
    private Label black_playertype; // Value injected by FXMLLoader

    @FXML // fx:id="statusbar_status_text"
    private Label statusbar_status_text; // Value injected by FXMLLoader

    @FXML // fx:id="showVerboseInfo_checkboxW"
    private CheckBox showVerboseInfo_checkboxW; // Value injected by FXMLLoader

    @FXML // fx:id="menu_board"
    private Menu menu_board; // Value injected by FXMLLoader

    @FXML // fx:id="timedGame_menu"
    private CheckMenuItem timedGame_menu; // Value injected by FXMLLoader

    @FXML // fx:id="infoTab_pane"
    private AnchorPane infoTab_pane; // Value injected by FXMLLoader

    // -- FXML END --
    // ##############################################################

}
