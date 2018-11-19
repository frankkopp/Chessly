package fko.chessly.ui.JavaFX_GUI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * GameHistoryView
 */
public class GameHistoryView extends Stage {

  private static final Logger LOG = LoggerFactory.getLogger(GameHistoryView.class);

  public GameHistoryView() {
    super();

    try {
      // Load the fxml file and create a new stage for the popup dialog.
      FXMLLoader loader = new FXMLLoader();
      loader.setLocation(MainView.class.getResource("/fxml/GameHistoryView.fxml"));
      AnchorPane pane = loader.load();
      Scene scene = new Scene(pane);

      this.setTitle("Game History");
      this.initModality(Modality.NONE);
      Stage stage = MainView.getPrimaryStage();
      this.initOwner(stage);
      this.setScene(scene);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
