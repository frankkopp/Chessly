package fko.chessly.ui.JavaFX_GUI;

import java.net.URL;
import java.util.Calendar;
import java.util.Observable;
import java.util.ResourceBundle;

import fko.chessly.Playroom;
import fko.chessly.game.GameMove;
import fko.chessly.player.HumanPlayer;
import fko.chessly.ui.SwingGUI.SwingGUI;
import fko.chessly.util.HelperTools;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * @author Frank
 *
 */
public class JavaFX_GUI_Controller {

    // -- reference to the _model (playroom) --
    private Playroom _model;

    // -- we only can tell the model the users move when we know which player we have to use --
    private HumanPlayer _moveReceiver = null;
    private final Object _moveReceiverLock = new Object();

    // -- FXML START --

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="rootPanel"
    private BorderPane rootPanel; // Value injected by FXMLLoader

    @FXML // fx:id="statusbar_status_text"
    private Label statusbar_status_text; // Value injected by FXMLLoader

    @FXML // fx:id="statusbar_mem_text"
    private Label statusbar_mem_text; // Value injected by FXMLLoader

    @FXML // fx:id="board_panel_grid"
    private GridPane board_panel_grid; // Value injected by FXMLLoader

    @FXML // fx:id="statusbar_copyright_test"
    private Label statusbar_copyright_test; // Value injected by FXMLLoader

    @FXML // fx:id="board_panel"
    private AnchorPane board_panel; // Value injected by FXMLLoader

    @FXML // fx:id="menu_newGame"
    private MenuItem menu_newGame; // Value injected by FXMLLoader

    @FXML // fx:id="menu_about"
    private MenuItem menu_about; // Value injected by FXMLLoader

    @FXML // fx:id="aboutDialog_OK_button"
    private Button aboutDialog_OK_button; // Value injected by FXMLLoader

    // -- FXML END --

    private Stage _primaryStage;
    private Stage _aboutDialogStage;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {

        _primaryStage = JavaFX_GUI.getStage();

        if (statusbar_status_text != null) statusbar_status_text.setText("JavaFX GUI started");

        // add board panel from previous SwingGUI
        addBoardPanel();

        // add constantly updated memory info into status panel
        addMemLabelUpdater();

        // TODO: initialize all controls


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

        /* alternative implementation
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                statusbar_mem_text.setText(HelperTools.getMBytes(Runtime.getRuntime().freeMemory()) + " MB / "
                        + HelperTools.getMBytes(Runtime.getRuntime().totalMemory()) + " MB");
            }
        }), new KeyFrame(Duration.millis(500)));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
         */
    }

    /**
     * Adds the board panel from the previous Swing UI.
     * TODO: Rebuild board panel with JavaFX.
     */
    private void addBoardPanel() {
        SwingNode boardPanelSwingNode = new SwingNode();
        BoardPanel p = new BoardPanel(this);
        boardPanelSwingNode.setContent(p);
        board_panel_grid.getChildren().add(boardPanelSwingNode);
    }

    @FXML
    void aboutDialog_openAction(ActionEvent event) {
        _aboutDialogStage = new AboutDialog();
        _aboutDialogStage.showAndWait();
    }

    @FXML
    void newGameDialog_Action(ActionEvent event) {
        // TODO newGameDialog
    }

    /**
     * @return true when highlighting possible moves on the board is activated
     */
    public boolean is_showPossibleMoves() {
        // TODO Auto-generated method stub
        return true;
    }

    /**
     * Is called whenever the model has changes. Needs to update the GUI accordingly.
     * @param o
     * @param arg
     */
    public void update(Observable o, Object arg) {

        // TODO: update UI

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


}
