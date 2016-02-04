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
import fko.chessly.mvc.ModelEvents.ModelEvent;
import fko.chessly.player.HumanPlayer;
import fko.chessly.player.PlayerType;
import fko.chessly.ui.JavaFX_GUI.MoveListModel.FullMove;
import fko.chessly.util.HelperTools;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * Main Controller for the Chessly JavaFX user interface.
 *
 * @author Frank
 */
public class JavaFX_GUI_Controller implements Observer {

    /*
     * FMXL injected variables are declared at the and of the file!
     */

    // limits the text stored in the info area to prevent out of memory
    private static final int MAX_LENGTH_INFO_AREA = 20000;

    // reference to the _model (playroom) --
    private Playroom _model;

    // we only can tell the model the users move when we know which player we have to use --
    private HumanPlayer _moveReceiver = null;
    private final Object _moveReceiverLock = new Object();

    // important nodes
    @SuppressWarnings("unused")
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
        _primaryStage = JavaFX_GUI.getStage();

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

        // add a updater for the player clocks
        _clockUpdater = new PlayerClockUpdater(
                whitePlayer_name, white_clock, white_playertype, white_progressbar,
                blackPlayer_name, black_clock, black_playertype, black_progressbar
                );

        // bind some controls controls
        showLastMove_menu.selectedProperty().bindBidirectional(_showLastMove);
        showPossibleMoves_menu.selectedProperty().bindBidirectional(_showPossibleMoves);
        _timedGame.set(_model.isTimedGame());
        timedGame_menu.selectedProperty().bindBidirectional(_timedGame);

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

    /**
     * Configures the info_panel
     */
    private void configInfoPanel() {
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
            final int size = move_table.getItems().size();
            if (size > 0) {
                move_table.scrollTo(size - 1);
            }
        }));

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
        if (info_panel.getLength() > MAX_LENGTH_INFO_AREA) {
            info_panel.deleteText(0, info_panel.getLength() - MAX_LENGTH_INFO_AREA);
        }
        info_panel.appendText(String.format(s));
        info_panel.setScrollTop(Double.MAX_VALUE);
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
        System.out.println("Start New Game ACTION");
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
        alert.setTitle("Close Chessly");
        alert.setHeaderText("Close Chessly");
        alert.setContentText("Do your really want to close Chessly?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            Chessly.exitChessly();
        } else {
            // ... user chose CANCEL or closed the dialog
        }
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
     * This method is called whenever the observed object is changed. An
     * application calls an <tt>Observable</tt> object's
     * <code>notifyObservers</code> method to have all the object's
     * observers notified of the change.
     *
     * @param model   the observable object.
     * @param event   an argument passed to the <code>notifyObservers</code>
     *                method.
     */
    @Override
    public void update(Observable model, Object event) {
        System.out.println("JavaFX Controller: Update from "+event);

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
     * Is called when model Playroom changed
     * @param playroom
     * @param event
     */
    private void updateFromPlayroom(Playroom playroom, ModelEvent event) {
        //System.out.println("JavaFX Controller: Update from "+event);

        // Playroom is playing - game exists
        if (playroom.isPlaying() && playroom.getCurrentGame() != null) {
            // -- game is initialized --
            if (event.signals(Playroom.SIG_PLAYROOM_GAME_CREATED)) {
                // -- now we want to observe the game --
                playroom.getCurrentGame().addObserver(this);
                // -- there are human players we need to observe them as well to see if they
                // -- want to have a move
                if (playroom.getCurrentGame().getPlayerBlack() instanceof HumanPlayer) {
                    ((Observable) playroom.getCurrentGame().getPlayerBlack()).addObserver(this);
                }
                if (playroom.getCurrentGame().getPlayerWhite() instanceof HumanPlayer) {
                    ((Observable) playroom.getCurrentGame().getPlayerWhite()).addObserver(this);
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
                            ">>> Black wins: " + playroom.getCurrentBlackWins() + '\n' +
                            ">>> White wins: " + playroom.getCurrentWhiteWins() + '\n' +
                            ">>> Draws     : " + playroom.getCurrentDraws() + '\n'
                            );
                    printToInfoln("");
                }
            }
            Platform.runLater(() -> gameOverGuiUpdate(playroom.getCurrentGame(), event));
            updateFromGame(playroom.getCurrentGame(), event);

            // Playroom is not playing - game still exists
        } else if (!playroom.isPlaying() && playroom.getCurrentGame() != null) {
            // -- multiple games completed --
            if (playroom.getCurrentGame().isFinished()) {
                if (event.signals(Playroom.SIG_PLAYROOM_THREAD_END)) {
                    // -- check if multiple games in a row should be run --
                    if (playroom.getNumberOfGames() > 1) {
                        printToInfoln(
                                ">>> Multiple games finished:" + '\n' +
                                ">>> Black wins: " + playroom.getCurrentBlackWins() +
                                " (" + (int)(((float) playroom.getCurrentBlackWins() / (float) (playroom.getNumberOfGames())) * 100) + '%' + ")\n" +
                                ">>> White wins: " + playroom.getCurrentWhiteWins() +
                                " (" + (int)(((float) playroom.getCurrentWhiteWins() / (float) (playroom.getNumberOfGames())) * 100) + '%' + ")\n" +
                                ">>> Draws     : " + playroom.getCurrentDraws() +
                                " (" + (int)(((float) playroom.getCurrentDraws() / (float) (playroom.getNumberOfGames())) * 100) + '%' + ")\n"
                                );
                        printToInfoln("");
                    }
                }
            }
            updateFromGame(playroom.getCurrentGame(), event);
            // No game exists
        } else {
            Platform.runLater(() -> setControlsNoGame());
        }
    }

    /**
     * Is called when model Game changed
     * @param game
     * @param event
     */
    private void updateFromGame(Game game, ModelEvent event) {
        //System.out.println("JavaFX Controller: Update from "+event);

        // -- draw the current board of the current game --
        Platform.runLater(() -> _boardPane.setAndDrawBoard(game.getCurBoard()));

        // -- update the move list according to the moves in the current game --
        //moveList.drawMove(game);
        Platform.runLater(() -> updateMoveList(game));

        // -- get the current status of the game --
        int status = game.getStatus();
        switch (status) {
            case Game.GAME_INITIALIZED:
                Platform.runLater(() -> gameInitializedGuiUpdate(game, event));
                break;

            case Game.GAME_RUNNING:
                Platform.runLater(() -> gameRunningGuiUpdate(game, event));
                break;

            case Game.GAME_OVER:
                Platform.runLater(() -> gameOverGuiUpdate(game, event));
                break;

            case Game.GAME_PAUSED:
                Platform.runLater(() -> gamePausedGuiUpdate());
                break;

            case Game.GAME_FINISHED:
                Platform.runLater(() -> gameFinishedGuiUpdate());
                break;
            default:
                break;
        }
    }

    /**
     * Is called when model Human Player changed
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
        tab_pane.getSelectionModel().select(info_tab);
        // clear move list
        _moveListModel.clear();
        statusbar_status_text.setText("New Game started.");
        //if (event.signals(Playroom.SIG_PLAYROOM_GAME_CREATED)) {
        printToInfoln();
        printToInfoln("--- New Game started ------------------");
        printToInfoln("Player BLACK: "+game.getPlayerBlack().getName());
        printToInfoln("Player WHITE: "+game.getPlayerWhite().getName());
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
        tab_pane.getSelectionModel().select(info_tab);
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
        tab_pane.getSelectionModel().select(info_tab);
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
        tab_pane.getSelectionModel().select(info_tab);
        // clear move list
        _moveListModel.clear();
        statusbar_status_text.setText("Game finished.");
    }

    /**
     * Resets the controls when game is over and also print game info
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
        tab_pane.getSelectionModel().select(info_tab);
        statusbar_status_text.setText("No game running.");
    }

    /**
     *
     */
    private void assertFXids() {
        assert newGame_button != null : "fx:id=\"newGame_button\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert undoMove_button != null : "fx:id=\"undoMove_button\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert timeBlack_menu != null : "fx:id=\"timeBlack_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert white_playertype != null : "fx:id=\"white_playertype\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert flip_button != null : "fx:id=\"flip_button\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert statusbar_copyright_test != null : "fx:id=\"statusbar_copyright_test\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert black_clock != null : "fx:id=\"black_clock\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert undoMove_menu != null : "fx:id=\"undoMove_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert about_menu != null : "fx:id=\"menu_about\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert flip_menu != null : "fx:id=\"flip_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert move_table_number != null : "fx:id=\"move_table_number\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert blackPlayer_name != null : "fx:id=\"blackPlayer_name\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert menu_help != null : "fx:id=\"menu_help\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert stopGame_button != null : "fx:id=\"stopGame_button\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert pauseGame_button != null : "fx:id=\"pauseGame_button\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert pauseGame_menu != null : "fx:id=\"pauseGame_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert menu_level != null : "fx:id=\"menu_level\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert black_progressbar != null : "fx:id=\"black_progressbar\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert move_table != null : "fx:id=\"move_table\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert close_menu != null : "fx:id=\"close_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert black_enginetab != null : "fx:id=\"black_enginetab\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert white_enginetab != null : "fx:id=\"white_enginetab\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert statusbar_mem_text != null : "fx:id=\"statusbar_mem_text\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert info_tab != null : "fx:id=\"info_tab\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert board_panel_grid != null : "fx:id=\"board_panel_grid\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert move_table_white != null : "fx:id=\"move_table_white\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert move_table_black != null : "fx:id=\"move_table_black\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert resumeGame_menu != null : "fx:id=\"resumeGame_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert newGame_menu != null : "fx:id=\"menu_newGame\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert menu_game != null : "fx:id=\"menu_game\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert stopGame_menu != null : "fx:id=\"stopGame_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert menu_moves != null : "fx:id=\"menu_moves\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert white_clock != null : "fx:id=\"white_clock\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert black_playertype != null : "fx:id=\"black_playertype\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert statusbar_status_text != null : "fx:id=\"statusbar_status_text\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert timeWhite_menu != null : "fx:id=\"timeWhite_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert menu_board != null : "fx:id=\"menu_board\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert timedGame_menu != null : "fx:id=\"timedGame_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert rootPanel != null : "fx:id=\"rootPanel\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert tab_pane != null : "fx:id=\"tab_pane\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert info_panel != null : "fx:id=\"info_panel\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert white_progressbar != null : "fx:id=\"white_progressbar\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert whitePlayer_name != null : "fx:id=\"whitePlayer_name\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert showPossibleMoves_menu != null : "fx:id=\"showPossibleMoves_menu1\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert showLastMove_menu != null : "fx:id=\"showLastMove_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert resumeGame_button != null : "fx:id=\"resumeGame_button\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";

    }

    // -- FXML START --

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="rootPanel"
    private BorderPane rootPanel; // Value injected by FXMLLoader

    @FXML // fx:id="info_panel"
    private TextArea info_panel; // Value injected by FXMLLoader

    @FXML // fx:id="move_table"
    private TableView<MoveListModel.FullMove> move_table; // Value injected by FXMLLoader

    @FXML // fx:id="move_table_number"
    private TableColumn<MoveListModel.FullMove, String> move_table_number; // Value injected by FXMLLoader

    @FXML // fx:id="move_table_white"
    private TableColumn<MoveListModel.FullMove, String> move_table_white; // Value injected by FXMLLoader

    @FXML // fx:id="move_table_black"
    private TableColumn<MoveListModel.FullMove, String> move_table_black; // Value injected by FXMLLoader

    @FXML // fx:id="newGame_button"
    private Button newGame_button; // Value injected by FXMLLoader

    @FXML // fx:id="undoMove_button"
    private Button undoMove_button; // Value injected by FXMLLoader

    @FXML // fx:id="timeBlack_menu"
    private MenuItem timeBlack_menu; // Value injected by FXMLLoader

    @FXML // fx:id="white_playertype"
    private Label white_playertype; // Value injected by FXMLLoader

    @FXML // fx:id="flip_button"
    private Button flip_button; // Value injected by FXMLLoader

    @FXML // fx:id="statusbar_copyright_test"
    private Label statusbar_copyright_test; // Value injected by FXMLLoader

    @FXML // fx:id="black_clock"
    private Label black_clock; // Value injected by FXMLLoader

    @FXML // fx:id="undoMove_menu"
    private MenuItem undoMove_menu; // Value injected by FXMLLoader

    @FXML // fx:id="menu_about"
    private MenuItem about_menu; // Value injected by FXMLLoader

    @FXML // fx:id="flip_menu"
    private MenuItem flip_menu; // Value injected by FXMLLoader

    @FXML // fx:id="blackPlayer_name"
    private Label blackPlayer_name; // Value injected by FXMLLoader

    @FXML // fx:id="menu_help"
    private Menu menu_help; // Value injected by FXMLLoader

    @FXML // fx:id="stopGame_button"
    private Button stopGame_button; // Value injected by FXMLLoader

    @FXML // fx:id="pauseGame_button"
    private Button pauseGame_button; // Value injected by FXMLLoader

    @FXML // fx:id="pauseGame_menu"
    private MenuItem pauseGame_menu; // Value injected by FXMLLoader

    @FXML // fx:id="menu_level"
    private Menu menu_level; // Value injected by FXMLLoader

    @FXML // fx:id="black_progressbar"
    private ProgressBar black_progressbar; // Value injected by FXMLLoader

    @FXML // fx:id="close_menu"
    private MenuItem close_menu; // Value injected by FXMLLoader

    @FXML // fx:id="black_enginetab"
    private Tab black_enginetab; // Value injected by FXMLLoader

    @FXML // fx:id="white_enginetab"
    private Tab white_enginetab; // Value injected by FXMLLoader

    @FXML // fx:id="statusbar_mem_text"
    private Label statusbar_mem_text; // Value injected by FXMLLoader

    @FXML // fx:id="tab_pane"
    private TabPane tab_pane; // Value injected by FXMLLoader

    @FXML // fx:id="info_tab"
    private Tab info_tab; // Value injected by FXMLLoader

    @FXML // fx:id="board_panel_grid"
    private GridPane board_panel_grid; // Value injected by FXMLLoader

    @FXML // fx:id="resumeGame_menu"
    private MenuItem resumeGame_menu; // Value injected by FXMLLoader

    @FXML // fx:id="menu_newGame"
    private MenuItem newGame_menu; // Value injected by FXMLLoader

    @FXML // fx:id="menu_game"
    private Menu menu_game; // Value injected by FXMLLoader

    @FXML // fx:id="stopGame_menu"
    private MenuItem stopGame_menu; // Value injected by FXMLLoader

    @FXML // fx:id="showLastMove_menu"
    private CheckMenuItem showLastMove_menu; // Value injected by FXMLLoader

    @FXML // fx:id="showPossibleMoves_menu"
    private CheckMenuItem showPossibleMoves_menu; // Value injected by FXMLLoader

    @FXML // fx:id="menu_moves"
    private Menu menu_moves; // Value injected by FXMLLoader

    @FXML // fx:id="white_clock"
    private Label white_clock; // Value injected by FXMLLoader

    @FXML // fx:id="black_playertype"
    private Label black_playertype; // Value injected by FXMLLoader

    @FXML // fx:id="statusbar_status_text"
    private Label statusbar_status_text; // Value injected by FXMLLoader

    @FXML // fx:id="timeWhite_menu"
    private MenuItem timeWhite_menu; // Value injected by FXMLLoader

    @FXML // fx:id="menu_board"
    private Menu menu_board; // Value injected by FXMLLoader

    @FXML // fx:id="timedGame_menu"
    private CheckMenuItem timedGame_menu; // Value injected by FXMLLoader

    @FXML // fx:id="white_progressbar"
    private ProgressBar white_progressbar; // Value injected by FXMLLoader

    @FXML // fx:id="whitePlayer_name"
    private Label whitePlayer_name; // Value injected by FXMLLoader

    @FXML // fx:id="resumeGame_button"
    private Button resumeGame_button; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack2"
    private RadioMenuItem levelBlack2; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite6"
    private RadioMenuItem levelWhite6; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack8"
    private RadioMenuItem levelBlack8; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite8"
    private RadioMenuItem levelWhite8; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack10"
    private RadioMenuItem levelBlack10; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack6"
    private RadioMenuItem levelBlack6; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack4"
    private RadioMenuItem levelBlack4; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite2"
    private RadioMenuItem levelWhite2; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite4"
    private RadioMenuItem levelWhite4; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack"
    private ToggleGroup levelBlack; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite"
    private ToggleGroup levelWhite; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlack20"
    private RadioMenuItem levelBlack20; // Value injected by FXMLLoader

    @FXML // fx:id="levelBlackMax"
    private RadioMenuItem levelBlackMax; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhiteMax"
    private RadioMenuItem levelWhiteMax; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite10"
    private RadioMenuItem levelWhite10; // Value injected by FXMLLoader

    @FXML // fx:id="levelWhite20"
    private RadioMenuItem levelWhite20; // Value injected by FXMLLoader

    // -- FXML END --
    // ##############################################################

}
