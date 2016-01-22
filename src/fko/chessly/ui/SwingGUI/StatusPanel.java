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

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import fko.chessly.util.HelperTools;

/**
 * This class represents the windows status bar
 */
public class StatusPanel extends JPanel {

    private static final long serialVersionUID = -552455606345206363L;

    private boolean _abort = false;

    private updateThread _updater = new updateThread();

    private String  _currentStatusMsg = "";
    private JPanel statusLineServer;
    private JLabel statusLine, statusLineMem;

    public StatusPanel() {
	super(new BorderLayout(5,0));

	statusLine = new JLabel("Chessly started!");
	this.add(statusLine, BorderLayout.CENTER);

	statusLineMem = new JLabel("meminfo");
	statusLineMem.setHorizontalTextPosition(SwingConstants.RIGHT);
	this.add(statusLineMem, BorderLayout.EAST);

	updateGUI();
	startUpdate();
    }

    public String getStatusMsg() {
	return _currentStatusMsg;
    }

    public void setStatusMsg(String newStatusMsg) {
	this._currentStatusMsg = newStatusMsg;
    }

    private void updateGUI() {
	statusLineMem.setText(HelperTools.getMBytes(Runtime.getRuntime().freeMemory()) + " MB / "
		+ HelperTools.getMBytes(Runtime.getRuntime().totalMemory()) + " MB");
	statusLine.setText(_currentStatusMsg);
    }

    private void startUpdate() {
	_updater.start();
    }

    private class updateThread extends Thread {
	private updateThread() {
	    super("Statusline Updater");
	    setPriority(Thread.MIN_PRIORITY);
	    setDaemon(true);
	}
	@Override
	public void run() {
	    updateRunnable aUpdateRunnable = new updateRunnable();
	    while (true) {
		try {
		    Thread.sleep(200);
		} catch (InterruptedException e) {
		    // -- ignore --
		}
		if (_abort) {
		    return;
		}

		SwingUtilities.invokeLater(aUpdateRunnable);
	    }
	}

	private class updateRunnable implements Runnable {
	    public void run() {
		updateGUI();
	    }
	}
    }
}
