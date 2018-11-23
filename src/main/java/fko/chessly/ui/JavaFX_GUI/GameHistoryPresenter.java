package fko.chessly.ui.JavaFX_GUI;

import fko.chessly.Chessly;
import fko.chessly.Playroom;
import fko.chessly.game.Match;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

/** GameHistoryPresenter */
public class GameHistoryPresenter {

  private static final Logger LOG = LoggerFactory.getLogger(GameHistoryPresenter.class);

  private ListProperty<Match> matchHistory; // the match history from the Playroom

  private final ObservableList<MatchListItemModel> matchListItemModels;

  public GameHistoryPresenter() {
    this.matchListItemModels = FXCollections.observableArrayList(new ArrayList<>());
  }

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assertFXMLInjection();

    // resize table columns
    matchTable_number.prefWidthProperty().bind(matchTable.widthProperty().multiply(0.1));
    matchTable_white.prefWidthProperty().bind(matchTable.widthProperty().multiply(0.25));
    matchTable_black.prefWidthProperty().bind(matchTable.widthProperty().multiply(0.25));
    matchTable_result.prefWidthProperty().bind(matchTable.widthProperty().multiply(0.05));
    matchTable_date.prefWidthProperty().bind(matchTable.widthProperty().multiply(0.2));
    matchTable_plys.prefWidthProperty().bind(matchTable.widthProperty().multiply(0.05));

    // bind table model columns to table view columns
    matchTable_number.setCellValueFactory(cellData -> cellData.getValue().numberProperty());
    matchTable_white.setCellValueFactory(cellData -> cellData.getValue().whiteProperty());
    matchTable_black.setCellValueFactory(cellData -> cellData.getValue().blackProperty());
    matchTable_result.setCellValueFactory(cellData -> cellData.getValue().resultProperty());
    matchTable_date.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
    matchTable_plys.setCellValueFactory(cellData -> cellData.getValue().noOfMovesProperty());

    // set the model table as list for the table view
    matchTable.setItems(matchListItemModels);

    // bind Playroom MoveHistory to this
    matchHistory = Playroom.getInstance().getMatchHistory();
    matchHistory.addListener((ListChangeListener<Object>) this::updateMatchHistory);

    // allow multiple selections
    //matchTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    // add selection listener
    matchTable.getSelectionModel().selectedItemProperty().addListener(this::selectRow);
  }

  private void updateMatchHistory(final ListChangeListener.Change<?> change) {
    // clear the list
    matchListItemModels.clear();

    // fill table model with data from matchHistory
    ListIterator<Match> matchListIterator = matchHistory.listIterator();
    while (matchListIterator.hasNext()) {
      Match match = matchListIterator.next();

      String number = String.valueOf(matchListIterator.nextIndex());
      String white = match.getWhite();
      String black = match.getBlack();
      String result;
      switch (match.getResult()) {
        case WHITE:
          result = "1-0";
          break;
        case BLACK:
          result = "0-1";
          break;
        case DRAW:
          result = "1/2-1/2";
          break;
        case UNKNOWN:
          // fall through
        default:
          result = "*";
          break;
      }
      String date =
          DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.ENGLISH).format(match.getDate());
      String noOfMoves = String.valueOf(match.getMoveList().size());

      MatchListItemModel mm =
          new MatchListItemModel(number, white, black, result, date, noOfMoves, match);
      matchListItemModels.add(mm);
    }
  }

  private void selectRow(
      final ObservableValue<? extends MatchListItemModel> obs,
      final MatchListItemModel oldSelection,
      final MatchListItemModel newSelection) {

    textareaPGN.clear();
    ObservableList<MatchListItemModel> selectedItems =
        matchTable.getSelectionModel().getSelectedItems();
    if (selectedItems.size() > 0) {
      for (MatchListItemModel match : selectedItems) {
        final String toString = match.getMatch().toString();
        textareaPGN.appendText(toString);
      }
    }
  }

  @FXML
  void actionClipboard(ActionEvent event) {
    textareaPGN.selectAll();
    textareaPGN.copy();
  }

  @FXML
  void actionSave(ActionEvent event) {
    // check if matchTable has a selection
    if (matchTable.getSelectionModel().getSelectedItems().isEmpty()) return;
    checkSaveFolder();
    List<MatchListItemModel> selectedItems = matchTable.getSelectionModel().getSelectedItems();
    savePGNFileList(selectedItems);
  }

  @FXML
  void actionSaveAll(ActionEvent event) {
    checkSaveFolder();
    List<MatchListItemModel> allItems = matchTable.getItems();
    savePGNFileList(allItems);
  }

  @FXML
  void actionSaveFolder(ActionEvent event) {

    final DirectoryChooser directoryChooser = new DirectoryChooser();

    // Set title for DirectoryChooser
    directoryChooser.setTitle("Select Save Folder");

    // Set Initial Directory
    directoryChooser.setInitialDirectory(new File("./var/save_games"));

    File dir = directoryChooser.showDialog(MainView.getPrimaryStage());
    if (dir != null) {
      labelPathToSaveFolder.setText(dir.getAbsolutePath());
      labelPathToSaveFolder.setTooltip(new Tooltip(dir.getAbsolutePath()));
    } else {
      labelPathToSaveFolder.setText(null);
    }
  }

  private void savePGNFileList(final List<MatchListItemModel> list) {
    for (MatchListItemModel item : list){
      final String fileName =
              item.date.getValue().replaceAll("\\.", "")
                      + "_"
                      + item.white.getValue().replaceAll(" ", "")
                      + "_vs_"
                      + item.black.getValue().replaceAll(" ", "");
      savePGNFile(item, fileName);
    }
  }

  private void savePGNFile(final MatchListItemModel item, final String fileName) {
    Path _filePath =
            FileSystems.getDefault()
                    .getPath(
                            labelPathToSaveFolder.getText()
                                    + FileSystems.getDefault().getSeparator()
                                    + fileName
                                    + ".pgn");

    int i = 1;
    while (Files.exists(_filePath)) {
      _filePath =
              FileSystems.getDefault()
                      .getPath(
                              labelPathToSaveFolder.getText()
                                      + FileSystems.getDefault().getSeparator()
                                      + fileName
                                      + "_"
                                      + i++
                                      + ".pgn");
    }

    try (OutputStream outputStream = Files.newOutputStream(_filePath)) {
      final PrintWriter printWriter = new PrintWriter(outputStream);
      printWriter.println(item.getMatch().toString());
      printWriter.close();
    } catch (IOException e) {
      Chessly.criticalError("While saving match: File " + _filePath + " could not be saved!");
      e.printStackTrace();
    }
  }

  private void checkSaveFolder() {
    // check path to save folder
    Path folderPath = FileSystems.getDefault().getPath(labelPathToSaveFolder.getText());

    // Check if folder exists and if not try to create it.
    if (!Files.exists(folderPath)) {
      Chessly.minorError(
              String.format(
                      "While saving match: Path %s could not be found. Trying to create it.",
                      folderPath.toString()));
      try {
        Files.createDirectories(folderPath);
      } catch (IOException e) {
        Chessly.fatalError(
                String.format(
                        "While saving match: Path %s could not be created." + e.getMessage() + " ",
                        folderPath.toString()));
      }
    }
  }

  public class MatchListItemModel {
    // For the list we use Simple Properties
    private SimpleStringProperty number;
    private SimpleStringProperty white;
    private SimpleStringProperty black;
    private SimpleStringProperty result;
    private SimpleStringProperty date;
    private SimpleStringProperty noOfMoves;

    // keep a reference to the Match object
    private Match match;

    public MatchListItemModel(
        String number,
        String white,
        String black,
        String result,
        String date,
        String noOfMoves,
        Match match) {
      this.number = new SimpleStringProperty(number);
      this.white = new SimpleStringProperty(white);
      this.black = new SimpleStringProperty(black);
      this.result = new SimpleStringProperty(result);
      this.date = new SimpleStringProperty(date);
      this.noOfMoves = new SimpleStringProperty(noOfMoves);
      this.match = match;
    }

    public Match getMatch() {
      return match;
    }

    public String getNumber() {
      return number.get();
    }

    public SimpleStringProperty numberProperty() {
      return number;
    }

    public void setNumber(final String number) {
      this.number.set(number);
    }

    public String getWhite() {
      return white.get();
    }

    public SimpleStringProperty whiteProperty() {
      return white;
    }

    public void setWhite(final String white) {
      this.white.set(white);
    }

    public String getBlack() {
      return black.get();
    }

    public SimpleStringProperty blackProperty() {
      return black;
    }

    public void setBlack(final String black) {
      this.black.set(black);
    }

    public String getResult() {
      return result.get();
    }

    public SimpleStringProperty resultProperty() {
      return result;
    }

    public void setResult(final String result) {
      this.result.set(result);
    }

    public String getDate() {
      return date.get();
    }

    public SimpleStringProperty dateProperty() {
      return date;
    }

    public void setDate(final String date) {
      this.date.set(date);
    }

    public String getNoOfMoves() {
      return noOfMoves.get();
    }

    public SimpleStringProperty noOfMovesProperty() {
      return noOfMoves;
    }

    public void setNoOfMoves(final String noOfMoves) {
      this.noOfMoves.set(noOfMoves);
    }
  }

  /* #############################################
   * FXML Injection
   * #############################################*/

  @FXML // fx:id="buttonSaveAll"
  private Button buttonSaveAll; // Value injected by FXMLLoader

  @FXML // fx:id="buttonSaveFolder"
  private Button buttonSaveFolder; // Value injected by FXMLLoader

  @FXML // fx:id="labelPathToSaveFolder"
  private Label labelPathToSaveFolder; // Value injected by FXMLLoader

  @FXML // fx:id="matchTable"
  private TableView<MatchListItemModel> matchTable; // Value injected by FXMLLoader

  @FXML // fx:id="matchTable_number"
  private TableColumn<MatchListItemModel, String> matchTable_number; // Value injected by FXMLLoader

  @FXML // fx:id="matchTable_white"
  private TableColumn<MatchListItemModel, String> matchTable_white; // Value injected by FXMLLoader

  @FXML // fx:id="matchTable_black"
  private TableColumn<MatchListItemModel, String> matchTable_black; // Value injected by FXMLLoader

  @FXML // fx:id="matchTable_result"
  private TableColumn<MatchListItemModel, String> matchTable_result; // Value injected by FXMLLoader

  @FXML // fx:id="matchTable_plys"
  private TableColumn<MatchListItemModel, String> matchTable_plys; // Value injected by FXMLLoader

  @FXML // fx:id="matchTable_date"
  private TableColumn<MatchListItemModel, String> matchTable_date; // Value injected by FXMLLoader

  @FXML // fx:id="buttonSave"
  private Button buttonSave; // Value injected by FXMLLoader

  @FXML // fx:id="buttonClipboard"
  private Button buttonClipboard; // Value injected by FXMLLoader

  @FXML // fx:id="textareaPGN"
  private TextArea textareaPGN; // Value injected by FXMLLoader

  private void assertFXMLInjection() {
    assert buttonSaveAll != null
        : "fx:id=\"buttonSaveAll\" was not injected: check your FXML file 'GameHistoryView.fxml'.";
    assert labelPathToSaveFolder != null
        : "fx:id=\"labelPathToSaveFolder\" was not injected: check your FXML file 'GameHistoryView.fxml'.";
    assert matchTable != null
        : "fx:id=\"matchTable\" was not injected: check your FXML file 'GameHistoryView.fxml'.";
    assert matchTable_number != null
        : "fx:id=\"matchTable_number\" was not injected: check your FXML file 'GameHistoryView.fxml'.";
    assert matchTable_white != null
        : "fx:id=\"matchTable_white\" was not injected: check your FXML file 'GameHistoryView.fxml'.";
    assert matchTable_black != null
        : "fx:id=\"matchTable_black\" was not injected: check your FXML file 'GameHistoryView.fxml'.";
    assert matchTable_result != null
        : "fx:id=\"matchTable_result\" was not injected: check your FXML file 'GameHistoryView.fxml'.";
    assert matchTable_plys != null
        : "fx:id=\"matchTable_plys\" was not injected: check your FXML file 'GameHistoryView.fxml'.";
    assert matchTable_date != null
        : "fx:id=\"matchTable_date\" was not injected: check your FXML file 'GameHistoryView.fxml'.";
    assert buttonSave != null
        : "fx:id=\"buttonSave\" was not injected: check your FXML file 'GameHistoryView.fxml'.";
    assert buttonClipboard != null
        : "fx:id=\"buttonClipboard\" was not injected: check your FXML file 'GameHistoryView.fxml'.";
    assert textareaPGN != null
        : "fx:id=\"textareaPNG\" was not injected: check your FXML file 'GameHistoryView.fxml'.";
  }
}
