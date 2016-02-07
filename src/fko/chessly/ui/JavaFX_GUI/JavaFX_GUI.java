package fko.chessly.ui.JavaFX_GUI;

import java.util.Observable;

import com.sun.javafx.application.PlatformImpl;

import fko.chessly.Chessly;
import fko.chessly.ui.UserInterface;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
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

        waitForUI();

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
            _stage.setMinWidth(740);
            _stage.setMinHeight(700);

            // get last window position and size
            double windowLocX = Double.parseDouble(
                    JavaFX_GUI_Controller.getWindowState().getProperty("windowLocationX", "100"));
            double windowLocY = Double.parseDouble(
                    JavaFX_GUI_Controller.getWindowState().getProperty("windowLocationY", "200"));
            double windowSizeX = Double.parseDouble(
                    JavaFX_GUI_Controller.getWindowState().getProperty("windowSizeX", "740"));
            double windowSizeY = Double.parseDouble(
                    JavaFX_GUI_Controller.getWindowState().getProperty("windowSizeY", "700"));

            // position and resize the window
            _stage.setX(windowLocX);
            _stage.setY(windowLocY);
            _stage.setWidth(windowSizeX);
            _stage.setHeight(windowSizeY);

            // now show the window
            _stage.show();

            // try to show the verboseInfoWindows - if checkbox was unchecked nothing will happen
            _controller.showVerboseInfoWhite();
            _controller.showVerboseInfoBlack();

            // closeAction - close through close action
            scene.getWindow().setOnCloseRequest(event -> {
                _controller.close_action(event);
                event.consume();
            });

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Waits for the UI to show
     */
    @Override
    public void waitForUI() {
        // wait for the UI to show before returning
        do {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //return;
            }
        } while (_stage == null || !_stage.isShowing());
    }

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

}
