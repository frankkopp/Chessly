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


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventType;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * This is an extension of a TextArea which limits the amount of output to avoid out of memory
 * if a lot of text is added.
 * If text is appended and the content amount is over the limit the
 * text will be shortened from the beginning.
 *
 * @author Frank Kopp
 */
public class InfoTextArea extends TextArea implements ChangeListener<String> {


    /**
     * Who many characters will this InfoPanel show at maximum.
     */
    public int _maxLength = 100000;

    /**
     * Creates a TextArea with limited content.
     * If text is appended and the content amount is over the limit the
     * text will be shortened from the beginning.
     */
    public InfoTextArea() {
        super();
        init();
    }

    /**
     * Creates a TextArea with limited content with the given text as content.
     * If text is appended and the content amount is over the limit the
     * text will be shortened from the beginning.
     * @param text
     */
    public InfoTextArea(String text) {
        super(text);
        init();
    }

    /**
     * Does necessary configurations like Font, etc.
     */
    private void init() {
        this.setEditable(false);
        Font font = Font.font("Lucida Console", FontWeight.NORMAL, 11);
        this.setFont(font);
        this.textProperty().addListener(this);
    }

    /**
     * print a text into the info pane
     * @param text
     */
    public synchronized void printInfo(String text) {
        /*
         *  There seems to be a bug in the text.append which in conjunction
         *  with deleting text from the start in my listener throws an exception
         *  This try-catch block simply hides this exception
         */
        try {
            this.appendText(text);
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * print a text plus newline into the info pane
     * @param text
     */
    public synchronized void printInfoln(String text) {
        printInfo(String.format("%s%n",text));
    }

    /**
     * This listener checks the length of the text of this textarea and cuts away text
     * from the start if the length of the text is bigger than the allowed size set by
     * setMaxLength(int)
     * @see javafx.beans.value.ChangeListener#changed(javafx.beans.value.ObservableValue, java.lang.Object, java.lang.Object)
     */
    @Override
    public synchronized void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        int oversize = this.getText().length() - _maxLength;
        if (oversize > 0) {
            this.setText(this.getText().substring(oversize+1000));
        }
    }

    /**
     * @return the maxLines
     */
    public int getMaxLength() {
        return this._maxLength;
    }

    /**
     * @param maxLines the maxLines to set
     */
    public void setMaxLength(int maxLines) {
        this._maxLength = maxLines;
    }



}
