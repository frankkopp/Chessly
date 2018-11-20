package fko.chessly.ui.JavaFX_GUI;

import fko.chessly.Playroom;
import fko.chessly.game.Match;
import javafx.beans.property.ListProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** GameHistoryView */
public class GameHistoryView extends Stage {

  private static final Logger LOG = LoggerFactory.getLogger(GameHistoryView.class);

  public GameHistoryView(final Playroom _model) {
    super();

    FXMLLoader fxmlLoader;
    AnchorPane anchorPane;
    Scene scene;
    try {
      // Load the fxml file and create a new stage for the popup dialog.
      fxmlLoader = new FXMLLoader();
      fxmlLoader.setLocation(MainView.class.getResource("/fxml/GameHistoryView.fxml"));
      anchorPane = fxmlLoader.load();
      scene = new Scene(anchorPane);

      this.setScene(scene);
      this.setTitle("Game History");
      this.initModality(Modality.NONE);
      Stage stage = MainView.getPrimaryStage();
      this.initOwner(stage);

    } catch (IOException e) {
      LOG.error("Error loading FXML from /fxml/GameHistoryView.fxml", e);
      e.printStackTrace();
    }
  }
}
