/*
 * <p>GPL Dislaimer</p>
 * <p>
 * "Chessly by Frank Kopp"
 * Copyright (c) 2003-2015 Frank Kopp
 * mail-to:frank@familie-kopp.de
 *
 * This file is part of "Chessly by Frank Kopp".
 *
 * "Chessly by Frank Kopp" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * "Chessly by Frank Kopp" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with "Chessly by Frank Kopp"; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * </p>
 *
 *
 */

package fko.chessly.ui.SwingGUI;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import fko.chessly.Chessly;
import fko.chessly.player.PlayerType;


/**
 * This dialog is shown when the user clicked on New Game in Menu Game or Ctrl+N to start
 * a new game.
 *
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public class NewGameDialog extends AbstractDialog {

    private static final long serialVersionUID = -2481823944059204110L;

    private SwingGUI _ui;

    private JTextField blackName, whiteName;
    private JPanel _inputPanel;

    public NewGameDialog(SwingGUI uiParam) {
	super(uiParam.getMainWindow(), "New Game", true);
	_ui = uiParam;

	setName("NewGameDialog");
	setTitle("New Game");
	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	setResizable(true);

	// create gui
	JPanel pane = new JPanel();
	pane.setLayout(new GridBagLayout());
	getContentPane().setLayout(new GridBagLayout());

	// -- BUTTONS --
	JPanel buttonPanel = new JPanel(new GridBagLayout());
	// Start button
	JButton startButton = new JButton("Start");
	startButton.addActionListener(new StartButtonAction());
	// Cancel button
	JButton cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(new CancelButtonAction());
	GridBagHelper.constrain(buttonPanel, startButton, 1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 5, 8, 5, 8);
	GridBagHelper.constrain(buttonPanel, cancelButton, 2, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 5, 8, 5, 8);

	setupDialog();

	// -- layout pane --
	GridBagHelper.constrain(getContentPane(), pane, 0, 0, 0, 0, GridBagConstraints.VERTICAL, GridBagConstraints.NORTH, 1.0, 1.0, 0, 0, 0, 0);
	GridBagHelper.constrain(pane, _inputPanel, 1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 5, 8, 5, 8);
	GridBagHelper.constrain(pane, new JPanel(), 1, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 5, 8, 5, 8);
	GridBagHelper.constrain(pane, buttonPanel, 1, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 5, 8, 5, 8);

	// -- set default button --
	getRootPane().setDefaultButton(startButton);

	// -- pack --
	pack();

    }

    private void setupDialog() {

	_inputPanel = new JPanel(new GridBagLayout());

	// -- Players Names --
	JPanel blackNamePanel = new JPanel();
	blackName = new JTextField(Chessly.getPlayroom().getNameBlackPlayer());
	blackName.setColumns(15);
	blackNamePanel.add(blackName);
	blackNamePanel.setBorder(new TitledBorder(new EtchedBorder(), "Name Black Player"));
	JPanel whiteNamePanel = new JPanel();
	whiteName = new JTextField(Chessly.getPlayroom().getNameWhitePlayer());
	whiteName.setColumns(15);
	whiteNamePanel.add(whiteName);
	whiteNamePanel.setBorder(new TitledBorder(new EtchedBorder(), "Name White Player"));
	GridBagHelper.constrain(_inputPanel, whiteNamePanel, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.WEST, 1.0, 0.0, 2, 2, 2, 2);
	GridBagHelper.constrain(_inputPanel, blackNamePanel, 2, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.WEST, 1.0, 0.0, 2, 2, 2, 2);

	// -- Player Types --

	// -- Black --
	JPanel blackPlayerTypePanel = new JPanel(new GridBagLayout());
	blackPlayerTypePanel.setBorder(new TitledBorder(new EtchedBorder(), "Black Player is ..."));
	ButtonGroup blackPlayerTypeButtonGroup = new ButtonGroup();

	// -- Human --
	JRadioButton blackPlayerType_Human = new JRadioButton("Human");
	blackPlayerType_Human.setEnabled(true);
	blackPlayerType_Human.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		_ui.getController().setPlayerTypeBlackAction(PlayerType.HUMAN);
	    }
	});
	blackPlayerTypeButtonGroup.add(blackPlayerType_Human);

	// -- Computer --
	JRadioButton blackPlayerType_Computer = new JRadioButton("Computer");
	blackPlayerType_Computer.setEnabled(true);
	blackPlayerType_Computer.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		_ui.getController().setPlayerTypeBlackAction(PlayerType.COMPUTER);
	    }
	});
	blackPlayerTypeButtonGroup.add(blackPlayerType_Computer);

	// -- layout blackPlayerTypePanel --
	GridBagHelper.constrain(blackPlayerTypePanel, blackPlayerType_Human,    1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.WEST, 1.0, 0.0, 2, 2, 2, 2);
	GridBagHelper.constrain(blackPlayerTypePanel, blackPlayerType_Computer, 1, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.WEST, 1.0, 0.0, 2, 2, 2, 2);

	// -- White --
	JPanel whitePlayerTypePanel = new JPanel(new GridBagLayout());
	whitePlayerTypePanel.setBorder(new TitledBorder(new EtchedBorder(), "White Player is ..."));
	ButtonGroup whitePlayerTypeButtonGroup = new ButtonGroup();

	// -- Human --
	JRadioButton whitePlayerType_Human = new JRadioButton("Human");
	whitePlayerType_Human.setEnabled(true);
	whitePlayerType_Human.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		_ui.getController().setPlayerTypeWhiteAction(PlayerType.HUMAN);
	    }
	});
	whitePlayerTypeButtonGroup.add(whitePlayerType_Human);

	// -- Computer --
	JRadioButton whitePlayerType_Computer = new JRadioButton("Computer");
	whitePlayerType_Computer.setEnabled(true);
	whitePlayerType_Computer.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		_ui.getController().setPlayerTypeWhiteAction(PlayerType.COMPUTER);
	    }
	});
	whitePlayerTypeButtonGroup.add(whitePlayerType_Computer);

	// -- preselect player types BLACK --
	if (Chessly.getPlayroom().getPlayerTypeBlack() == PlayerType.HUMAN) {
	    blackPlayerType_Human.setSelected(true);
	} else if (Chessly.getPlayroom().getPlayerTypeBlack() == PlayerType.COMPUTER) {
	    blackPlayerType_Computer.setSelected(true);
	} 

	// -- preselect player types WHITE --
	if (Chessly.getPlayroom().getPlayerTypeWhite() == PlayerType.HUMAN) {
	    whitePlayerType_Human.setSelected(true);
	} else if (Chessly.getPlayroom().getPlayerTypeWhite() == PlayerType.COMPUTER) {
	    whitePlayerType_Computer.setSelected(true);
	} 

	// -- layout whitePlayerTypePanel --
	GridBagHelper.constrain(whitePlayerTypePanel, whitePlayerType_Human,    1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.WEST, 1.0, 0.0, 2, 2, 2, 2);
	GridBagHelper.constrain(whitePlayerTypePanel, whitePlayerType_Computer, 1, 2, 1, 1, GridBagConstraints.EAST, GridBagConstraints.WEST, 1.0, 0.0, 2, 2, 2, 2);

	// -- layout inputPanel --
	GridBagHelper.constrain(_inputPanel, whitePlayerTypePanel, 1, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 2, 2, 2, 2);
	GridBagHelper.constrain(_inputPanel, blackPlayerTypePanel, 2, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 2, 2, 2, 2);

    }

    /**
     * Start game and dispose a dialog instance on request.
     */
    private class StartButtonAction implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    _ui.getController().startNewGame(blackName.getText(), whiteName.getText());
	    dispose();
	}
    }

    /**
     * Dispose a dialog instance on request.
     */
    private class CancelButtonAction implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    dispose();
	}
    }
}
