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

    @Override
    public void start(Stage primaryStage) {

        try {
            BorderPane root = (BorderPane)FXMLLoader.load(getClass().getResource("Frank_Sample.fxml"));
            Scene scene = new Scene(root,735,790);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update(Observable o, Object arg) {
        // TODO Auto-generated method stub

    }
}
