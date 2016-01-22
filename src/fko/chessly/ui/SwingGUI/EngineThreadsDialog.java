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
package fko.chessly.ui.SwingGUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;

import fko.chessly.Chessly;

public class EngineThreadsDialog extends AbstractDialog {

    private static final long serialVersionUID = -5665698951409321001L;

    private SwingGUI _ui;

    private int _dialogNumber;

    private JSlider _numberOfThreadsJSlider;

    /**
     * Create the dialog.
     */
    public EngineThreadsDialog(SwingGUI ui) {
        super(ui.getMainWindow(), "Number of Threads", true);
        _ui = ui;

        _dialogNumber = Integer.parseInt(Chessly.getProperties().getProperty("engine.numberOfThreads", "1"));

        setName("NumberOfThreadsDialog");
        setTitle("Number of Threads");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);

        // create gui
        JPanel pane = new JPanel();
        pane.setLayout(new GridBagLayout());
        getContentPane().setLayout(new GridBagLayout());

        // -- BUTTONS --
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        // Start button
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new OkButtonAction());
        // Cancel button
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new CancelButtonAction());
        GridBagHelper.constrain(buttonPanel, okButton, 1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 5, 8, 5, 8);
        GridBagHelper.constrain(buttonPanel, cancelButton, 2, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 5, 8, 5, 8);

        JPanel  sliderPanel = new JPanel(new GridBagLayout());

        _numberOfThreadsJSlider = new JSlider();

        _numberOfThreadsJSlider.setMinimum(1);
        _numberOfThreadsJSlider.setMaximum(16);
        _numberOfThreadsJSlider.setValue(_dialogNumber);

        _numberOfThreadsJSlider.setLabelTable(_numberOfThreadsJSlider.createStandardLabels(4, 4));
        _numberOfThreadsJSlider.setMajorTickSpacing(4);
        _numberOfThreadsJSlider.setSnapToTicks(true);
        _numberOfThreadsJSlider.setMinorTickSpacing(1);
        _numberOfThreadsJSlider.setPaintTicks(true);
        _numberOfThreadsJSlider.setPaintLabels(true);


        GridBagHelper.constrain(sliderPanel, _numberOfThreadsJSlider, 0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 5, 8, 5, 8);

        // -- layout pane --
        GridBagHelper.constrain(getContentPane(), pane, 0, 0, 0, 0, GridBagConstraints.VERTICAL, GridBagConstraints.NORTH,  1.0, 1.0, 0, 0, 0, 0);

        GridBagHelper.constrain(pane, sliderPanel, 1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 5, 8, 5, 8);
        GridBagHelper.constrain(pane, buttonPanel, 1, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 5, 8, 5, 8);

        // -- set default button --
        getRootPane().setDefaultButton(okButton);

        // -- pack --
        pack();

    }

    /**
     * Start game and dispose a dialog instance on request.
     */
    private class OkButtonAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Chessly.getProperties().setProperty("engine.numberOfThreads", ""+_numberOfThreadsJSlider.getValue());
            dispose();
        }
    }

    /**
     * Dispose a dialog instance on request.
     */
    private class CancelButtonAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }


}
