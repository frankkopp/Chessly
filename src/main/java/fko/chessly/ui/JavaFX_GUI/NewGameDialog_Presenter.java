/**
 * The MIT License (MIT)
 *
 * "Chessly by Frank Kopp"
 *
 * mail-to:frank@familie-kopp.de
 *
 * Copyright (c) 2016 Frank Kopp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package fko.chessly.ui.JavaFX_GUI;

import java.util.ResourceBundle;

import fko.chessly.Chessly;
import fko.chessly.player.PlayerType;

import java.net.URL;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

/**
 * @author fkopp
 *
 */
public class NewGameDialog_Presenter {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="cancel_button"
    private Button cancel_button; // Value injected by FXMLLoader

    @FXML // fx:id="blackPlayerType"
    private ToggleGroup blackPlayerType; // Value injected by FXMLLoader

    @FXML // fx:id="blackPlayerType_human"
    private RadioButton blackPlayerType_human; // Value injected by FXMLLoader

    @FXML // fx:id="whitePlayerType_engine"
    private RadioButton whitePlayerType_engine; // Value injected by FXMLLoader

    @FXML // fx:id="whitePlayerType"
    private ToggleGroup whitePlayerType; // Value injected by FXMLLoader

    @FXML // fx:id="start_button"
    private Button start_button; // Value injected by FXMLLoader

    @FXML // fx:id="blackPlayerType_engine"
    private RadioButton blackPlayerType_engine; // Value injected by FXMLLoader

    @FXML // fx:id="blackPlayerName_text"
    private TextField blackPlayerName_text; // Value injected by FXMLLoader

    @FXML // fx:id="whitePlayerName_text"
    private TextField whitePlayerName_text; // Value injected by FXMLLoader

    @FXML // fx:id="whitePlayerType_human"
    private RadioButton whitePlayerType_human; // Value injected by FXMLLoader

    @FXML
    void cancelNewGame_action(ActionEvent event) {
        ((Stage) cancel_button.getScene().getWindow()).close();
    }

    @FXML
    void startNewGame_action(ActionEvent event) {
        PlayerType wT;
        PlayerType bT;

        if (this.whitePlayerType.getSelectedToggle().equals(whitePlayerType_engine)) {
            wT = PlayerType.COMPUTER;
        } else {
            wT = PlayerType.HUMAN;
        }

        if (this.blackPlayerType.getSelectedToggle().equals(blackPlayerType_engine)) {
            bT = PlayerType.COMPUTER;
        } else {
            bT = PlayerType.HUMAN;
        }

        JavaFX_GUI._controller.startNewGame_action(event,
                this.whitePlayerName_text.getText(), wT,
                this.blackPlayerName_text.getText(), bT
                );

        ((Stage) cancel_button.getScene().getWindow()).close();

    }

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert cancel_button != null : "fx:id=\"cancel_button\" was not injected: check your FXML file 'NewGameDialog.fxml'.";
        assert blackPlayerType != null : "fx:id=\"blackPlayerType\" was not injected: check your FXML file 'NewGameDialog.fxml'.";
        assert blackPlayerType_human != null : "fx:id=\"blackPlayerType_human\" was not injected: check your FXML file 'NewGameDialog.fxml'.";
        assert whitePlayerType_engine != null : "fx:id=\"whitePlayerType_engine\" was not injected: check your FXML file 'NewGameDialog.fxml'.";
        assert whitePlayerType != null : "fx:id=\"whitePlayerType\" was not injected: check your FXML file 'NewGameDialog.fxml'.";
        assert start_button != null : "fx:id=\"start_button\" was not injected: check your FXML file 'NewGameDialog.fxml'.";
        assert blackPlayerType_engine != null : "fx:id=\"blackPlayerType_engine\" was not injected: check your FXML file 'NewGameDialog.fxml'.";
        assert blackPlayerName_text != null : "fx:id=\"blackPlayerName_text\" was not injected: check your FXML file 'NewGameDialog.fxml'.";
        assert whitePlayerName_text != null : "fx:id=\"whitePlayerName_text\" was not injected: check your FXML file 'NewGameDialog.fxml'.";
        assert whitePlayerType_human != null : "fx:id=\"whitePlayerType_human\" was not injected: check your FXML file 'NewGameDialog.fxml'.";

        whitePlayerName_text.setText(Chessly.getPlayroom().getNameWhitePlayer());
        blackPlayerName_text.setText(Chessly.getPlayroom().getNameBlackPlayer());

        // -- preselect player types BLACK --
        if (Chessly.getPlayroom().getPlayerTypeBlack() == PlayerType.HUMAN) {
            blackPlayerType_human.setSelected(true);
        } else if (Chessly.getPlayroom().getPlayerTypeBlack() == PlayerType.COMPUTER) {
            blackPlayerType_engine.setSelected(true);
        }

        // -- preselect player types WHITE --
        if (Chessly.getPlayroom().getPlayerTypeWhite() == PlayerType.HUMAN) {
            whitePlayerType_human.setSelected(true);
        } else if (Chessly.getPlayroom().getPlayerTypeWhite() == PlayerType.COMPUTER) {
            whitePlayerType_engine.setSelected(true);
        }

    }
}
