package fko.chessly.ui.JavaFX_GUI;

import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.ResourceBundle;

import fko.chessly.Chessly;
import fko.chessly.Playroom;
import fko.chessly.game.Game;
import fko.chessly.game.GameMove;
import fko.chessly.mvc.ModelEvents.ModelEvent;
import fko.chessly.player.HumanPlayer;
import fko.chessly.player.PlayerType;
import fko.chessly.util.HelperTools;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * @author Frank
 *
 */
public class JavaFX_GUI_Controller implements Observer {

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

    // -- FXML END --
    // ##############################################################

    private static final int MAX_LENGTH_INFO_AREA = 20000;

    // -- reference to the _model (playroom) --
    private Playroom _model;

    // -- we only can tell the model the users move when we know which player we have to use --
    private HumanPlayer _moveReceiver = null;
    private final Object _moveReceiverLock = new Object();

    // -- important nodes
    private Stage _primaryStage;
    private BoardPane _boardPane;
    MoveListModel _moveListModel = new MoveListModel();

    // -- get values from properties --
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


    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {

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

        // add clock updater
        PlayerClockUpdater _clockUpdater =
                new PlayerClockUpdater(
                        whitePlayer_name, white_clock, white_playertype, white_progressbar,
                        blackPlayer_name, black_clock, black_playertype, black_progressbar
                        );

        // TODO: initialize all controls
        showLastMove_menu.selectedProperty().bindBidirectional(_showLastMove);
        showPossibleMoves_menu.selectedProperty().bindBidirectional(_showPossibleMoves);
        _timedGame.set(_model.isTimedGame());
        timedGame_menu.selectedProperty().bindBidirectional(_timedGame);

        setControlsNoGame();
    }

    @FXML
    void aboutDialogOpen_action(ActionEvent event) {
        AboutDialog aboutDialogStage = new AboutDialog();
        aboutDialogStage.showAndWait();
    }

    @FXML
    void flipButton_action(ActionEvent event) {
        _boardPane.flipOrientation();
    }

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

    }

    @FXML
    void pauseGame_action(ActionEvent event) {

    }

    @FXML
    void resumeGame_action(ActionEvent event) {

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
            return;
        }

    }

    @FXML
    void undoMove_action(ActionEvent event) {

    }

    @FXML
    void timedGame_action(ActionEvent event) {
        _model.setTimedGame(_timedGame.get());
    }

    @FXML
    void timeWhite_action(ActionEvent event) {

    }

    @FXML
    void timeBlack_action(ActionEvent event) {

    }

    @FXML
    void showPossibleMoves_action(ActionEvent event) {
        _boardPane.drawBoard();
    }

    @FXML
    void showLastMove_action(ActionEvent event) {
        _boardPane.drawBoard();
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
            // TODO: rethink this
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
     * Adds the board panel from the previous Swing UI.
     * TODO: Rebuild board panel with JavaFX.
     */
    private void addBoardPanel() {
        _boardPane = new BoardPane(this);
        board_panel_grid.getChildren().add(_boardPane);
    }

    /**
     * Adds an updater to the mem label
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
     *
     */
    private void configInfoPanel() {
        info_panel.textProperty().addListener(
                new ChangeListener<Object>() {
                    @Override
                    public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                        Platform.runLater(() -> {
                            info_panel.setScrollTop(Double.MAX_VALUE); //this will scroll to the bottom
                        });
                    }
                });
        printToInfoln();
        printToInfoln("Java GUI started!");
    }

    /**
     *
     */
    private void configMoveList() {
        move_table_number.setCellValueFactory(cellData -> cellData.getValue().numberProperty());
        move_table_white.setCellValueFactory(cellData -> cellData.getValue().whiteProperty());
        move_table_black.setCellValueFactory(cellData -> cellData.getValue().blackProperty());
        move_table.setItems(_moveListModel.getMoveList());
        move_table.setStyle("-fx-font-size: 10; -fx-font-family: \"Lucida Console\";");
    }

    /**
     * @param game
     */
    private void updateMoveList(Game game) {
        _moveListModel.updateList(game.getCurBoard().getMoveHistory());
    }

    private void printToInfo(String s) {
        Platform.runLater(() -> {
            if (info_panel.getLength() > MAX_LENGTH_INFO_AREA) {
                info_panel.deleteText(0, info_panel.getLength() - MAX_LENGTH_INFO_AREA);
            }
        });
        info_panel.appendText(String.format(s));
    }

    private void printToInfoln(String s) {
        printToInfo(s+"%n");
    }

    private void printToInfoln() {
        printToInfo("%n");
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
        //timedGame_menu.setDisable(false); // bind to property
        timeWhite_menu.setDisable(false);
        timeBlack_menu.setDisable(false);
        // TODO levels
        flip_menu.setDisable(false);
        flip_button.setDisable(false);
        showLastMove_menu.setDisable(false);
        showPossibleMoves_menu.setDisable(false);
        about_menu.setDisable(false);
        tab_pane.getSelectionModel().select(info_tab);
        statusbar_status_text.setText("No game running.");
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
        pauseGame_menu.setDisable(true); // evtl. bind to property
        pauseGame_button.setDisable(true); // evtl. bind to property
        resumeGame_menu.setDisable(true); // evtl. bind to property
        resumeGame_button.setDisable(true); // evtl. bind to property
        close_menu.setDisable(false);
        undoMove_menu.setDisable(false);
        undoMove_button.setDisable(false);
        //timedGame_menu.setDisable(false); // bind to property
        timeWhite_menu.setDisable(false);
        timeBlack_menu.setDisable(false);
        // TODO levels
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
     * resets all control to game running status
     * @param event
     * @param game
     */
    private void gameRunningGuiUpdate(Game game, ModelEvent event) {
        // -- set possible actions (menu) --
        newGame_menu.setDisable(true);
        newGame_button.setDisable(true);
        stopGame_menu.setDisable(false);
        stopGame_button.setDisable(false);
        pauseGame_menu.setDisable(false); // evtl. bind to property
        pauseGame_button.setDisable(false); // evtl. bind to property
        resumeGame_menu.setDisable(true); // evtl. bind to property
        resumeGame_button.setDisable(true); // evtl. bind to property
        close_menu.setDisable(false);
        undoMove_menu.setDisable(false);
        undoMove_button.setDisable(false);
        //timedGame_menu.setDisable(false); // bind to property
        timeWhite_menu.setDisable(false);
        timeBlack_menu.setDisable(false);
        // TODO levels
        flip_menu.setDisable(false);
        flip_button.setDisable(false);
        showLastMove_menu.setDisable(false);
        showPossibleMoves_menu.setDisable(false);
        about_menu.setDisable(false);
        tab_pane.getSelectionModel().select(info_tab);
        statusbar_status_text.setText("Game running.");
    }

    /**
     *
     */
    private void gamePausedGuiUpdate() {
        // -- set possible actions (menu) --
        newGame_menu.setDisable(true);
        newGame_button.setDisable(true);
        stopGame_menu.setDisable(true);
        stopGame_button.setDisable(true);
        pauseGame_menu.setDisable(true); // evtl. bind to property
        pauseGame_button.setDisable(true); // evtl. bind to property
        resumeGame_menu.setDisable(false); // evtl. bind to property
        resumeGame_button.setDisable(false); // evtl. bind to property
        close_menu.setDisable(false);
        undoMove_menu.setDisable(true);
        undoMove_button.setDisable(true);
        //timedGame_menu.setDisable(false); // bind to property
        timeWhite_menu.setDisable(false);
        timeBlack_menu.setDisable(false);
        // TODO levels
        flip_menu.setDisable(false);
        flip_button.setDisable(false);
        showLastMove_menu.setDisable(false);
        showPossibleMoves_menu.setDisable(false);
        about_menu.setDisable(false);
        tab_pane.getSelectionModel().select(info_tab);
        statusbar_status_text.setText("Game running.");
    }

    /**
     *
     */
    private void gameFinishedGuiUpdate() {
        setControlsNoGame();
        statusbar_status_text.setText("Game finished.");
    }

    /**
     * @param game
     * @param event
     */
    private void gameOverGuiUpdate(Game game, ModelEvent event) {
        setControlsNoGame();
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
        ModelEvent mevent = (ModelEvent)event;
        // Dispatch the update to the appropriate method
        // depending an who is calling
        // Playroom
        if (model instanceof Playroom) {
            Platform.runLater(new updateFromPlayroomRunnable((Playroom) model, mevent));
            // Game
        } else if (model instanceof Game) {
            Platform.runLater(new updateFromGameRunnable((Game) model, mevent));
            // HumanPlayer
        } else if (model instanceof HumanPlayer) {
            Platform.runLater(new updateFromHumanPlayerRunnable((HumanPlayer)model, mevent));
        }
    }

    /**
     * is called when model Playroom changed
     */
    private void updateFromPlayroom(Playroom playroom, ModelEvent event) {

        System.out.println("JavaFX Controller: Update from "+event);

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
            gameInitializedGuiUpdate(playroom.getCurrentGame(), event);
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
            setControlsNoGame();
        }
    }

    /**
     * is called when model Game changed
     */
    private void updateFromGame(Game game, ModelEvent event) {

        System.out.println("JavaFX Controller: Update from "+event);

        // -- draw the current board of the current game --
        _boardPane.setAndDrawBoard(game.getCurBoard());

        // -- update the move list according to the moves in the current game --
        //moveList.drawMove(game);
        updateMoveList(game);

        // -- get the current status of the game --
        int status = game.getStatus();
        switch (status) {
            case Game.GAME_INITIALIZED:
                gameInitializedGuiUpdate(game, event);
                break;

            case Game.GAME_RUNNING:
                gameRunningGuiUpdate(game, event);
                break;

            case Game.GAME_OVER:
                gameOverGuiUpdate(game, event);
                break;

            case Game.GAME_PAUSED:
                gamePausedGuiUpdate();
                break;

            case Game.GAME_FINISHED:
                gameFinishedGuiUpdate();
                break;
            default:
                break;
        }
    }

    private void updateFromHumanPlayer(HumanPlayer hp, ModelEvent event) {

        System.out.println("JavaFX Controller: Update from "+event);

        //_MVController.setMoveReceiver(hp);
    }


    /**
     * Runnable called by the Observer update() method
     * through invokeAndWait() or invokeLater()
     */
    private class updateFromPlayroomRunnable implements Runnable {
        private final Playroom _playroom;

        private final ModelEvent _event;


        private updateFromPlayroomRunnable(Playroom playroom, ModelEvent newEvent) {
            this._playroom = playroom;
            this._event = newEvent;
        }

        @Override
        public void run() {
            updateFromPlayroom(_playroom, _event);
            //mainWindow.repaint();
        }
    }

    /**
     * Runnable called by the Observer update() method
     * through invokeAndWait() or invokeLater()
     */
    private class updateFromGameRunnable implements Runnable {
        private final Game _game;
        private final ModelEvent _event;

        private updateFromGameRunnable(Game game, ModelEvent newParameter) {
            this._game = game;
            this._event = newParameter;
        }

        @Override
        public void run() {
            updateFromGame(_game, _event);
            //mainWindow.repaint();
        }
    }

    /**
     * Runnable called by the Observer update() method
     * through invokeAndWait() or invokeLater()
     */
    private class updateFromHumanPlayerRunnable implements Runnable {
        private final HumanPlayer _humanPlayer;
        private final ModelEvent _event;

        private updateFromHumanPlayerRunnable(HumanPlayer humanPlayer, ModelEvent newEvent) {
            this._humanPlayer = humanPlayer;
            this._event = newEvent;
        }

        @Override
        public void run() {
            updateFromHumanPlayer(_humanPlayer, _event);
            //mainWindow.repaint();
        }
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
        //assert tab_pane != null : "fx:id=\"tab_pane\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert info_panel != null : "fx:id=\"info_panel\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert white_progressbar != null : "fx:id=\"white_progressbar\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert whitePlayer_name != null : "fx:id=\"whitePlayer_name\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert showPossibleMoves_menu != null : "fx:id=\"showPossibleMoves_menu1\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert showLastMove_menu != null : "fx:id=\"showLastMove_menu\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";
        assert resumeGame_button != null : "fx:id=\"resumeGame_button\" was not injected: check your FXML file 'JavaFX_GUI.fxml'.";

    }

}
