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
            @Override
            public void run() {
                updateGUI();
            }
        }
    }
}
