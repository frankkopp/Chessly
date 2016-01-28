package fko.chessly.ui.JavaFX_GUI;

import java.util.Observable;

import javax.swing.JFrame;

import com.sun.javafx.application.PlatformImpl;

import fko.chessly.Chessly;
import fko.chessly.ui.UserInterface;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * @author Frank
 *
 */
public class JavaFX_GUI extends Application implements UserInterface {

    /**
     * The singleton instance of this class
     */
    private static UserInterface _instance = null;

    /**
     * The primary stage
     */
    private static Stage _stage;


    /**
     * The main controller for this JavaFX application
     */
    public static JavaFX_GUI_Controller _controller;
    private BorderPane _root;

    /**
     * Creates the JavaFX UI through a Swing JPanel
     * It is not possible to instantiate JavaFX_GUI more than once - throws RunTime Exception.
     */
    public JavaFX_GUI() {
        if (_instance != null) throw new RuntimeException("It is not possible to instantiate JavaFX_GUI more than once!");
        // Startup the JavaFX platform
        Platform.setImplicitExit(false);
        PlatformImpl.startup(() -> {
            final Stage primaryStage = new Stage();
            primaryStage.setTitle("Chessly by Frank Kopp (c) "+Chessly.VERSION);
            start(primaryStage);
        });
        JavaFX_GUI._instance = this;
    }

    /**
     * Standard way to start a JavaFX application.
     *
     */
    @Override
    public void start(Stage primaryStage) {

        JavaFX_GUI._stage = primaryStage;

        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("JavaFX_GUI.fxml"));
            _root = (BorderPane)fxmlLoader.load();
            _controller = fxmlLoader.getController();

            Scene scene = new Scene(_root,_root.getPrefWidth(),_root.getPrefHeight());
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

            _stage.setScene(scene);
            _stage.setMinHeight(_root.getMinHeight()+40);
            _stage.setMinWidth(_root.getMinWidth()+20);
            _stage.show();
            _stage.setOnCloseRequest(event -> { Chessly.exitChessly(); });

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    //    /**
    //     * The UI can be started separately for test purposes. Does not start
    //     * @param args
    //     */
    //    public static void main(String[] args) {
    //        launch(args);
    //    }

    /**
     * @return controller
     */
    public static JavaFX_GUI_Controller getController() {
        return _controller;
    }

    /**
     * @return the primary stage which has been stored as a static field
     */
    public static Stage getStage() {
        return JavaFX_GUI._stage;
    }

    /**
     * @return the _instance
     */
    public static UserInterface getInstance() {
        return _instance;
    }

    /**
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable o, Object arg) {
        if (_controller!=null) _controller.update(o, arg);
    }

    /**
     * @return true when highlighting possible moves on the board is activated
     */
    public static boolean is_showPossibleMoves() {
        return _controller.is_showPossibleMoves();
    }
}
