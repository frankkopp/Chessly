package fko.chessly.ui.JavaFX_GUI;

import java.util.Observable;

import fko.chessly.ui.UserInterface;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


/**
 * @author Frank
 *
 */
public class JavaFX_GUI extends Application implements UserInterface {

    private static Stage _stage;
    private JavaFX_GUI_Controller _controller;

    @Override
    public void start(Stage primaryStage) {

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("JavaFX_GUI.fxml"));
            BorderPane root = (BorderPane)loader.load();

            Scene scene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

            JavaFX_GUI._stage = primaryStage;

            primaryStage.setScene(scene);
            primaryStage.setMinHeight(scene.getHeight());
            primaryStage.setMinWidth(scene.getWidth());
            primaryStage.show();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    /**
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
        // TODO Auto-generated method stub

    }

    /**
     * @return true when highlighting possible moves on the board is activated
     */
    public boolean is_showPossibleMoves() {
        return _controller.is_showPossibleMoves();
    }
}
