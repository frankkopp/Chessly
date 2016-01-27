package fko.chessly.ui.JavaFX_GUI;

import java.util.Observable;

import javax.swing.JFrame;

import com.sun.javafx.application.PlatformImpl;

import fko.chessly.Chessly;
import fko.chessly.ui.UserInterface;
import javafx.application.Application;
import javafx.application.Platform;
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

    public static UserInterface _instance;
    public static Stage _stage;
    private JavaFX_GUI_Controller _controller;
    private Parent _root;
    private JFrame _frame;

    /**
     * Creates the JavaFX UI through a Swing JPanel
     */
    @SuppressWarnings("restriction")
    public JavaFX_GUI() {
        // Startup the JavaFX platform
        PlatformImpl.startup(() -> {});
        Platform.setImplicitExit(false);
    }

    /**
     * Standard way to start a JavaFX application.
     *
     */
    @Override
    public void start(Stage primaryStage) {

        JavaFX_GUI._instance = this;
        JavaFX_GUI._stage = primaryStage;

        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("JavaFX_GUI.fxml"));
            BorderPane root = (BorderPane)fxmlLoader.load();

            _root = fxmlLoader.getRoot();
            _controller = fxmlLoader.getController();

            Scene scene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setMinHeight(scene.getHeight());
            primaryStage.setMinWidth(scene.getWidth());
            primaryStage.show();
            primaryStage.setOnCloseRequest(event -> { Chessly.exitChessly(); });

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * The UI can be started separately for test purposes. Does not start
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * @return controller
     */
    public JavaFX_GUI_Controller getController() {
        return _controller;
    }

    /**
     * @return the primary stage which has been stored as a static field
     */
    public static Stage getStage() {
        return JavaFX_GUI._stage;
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
    public boolean is_showPossibleMoves() {
        return _controller.is_showPossibleMoves();
    }
}
