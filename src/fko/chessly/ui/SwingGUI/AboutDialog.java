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
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fko.chessly.util.HelperTools;

/**
 * The About Dialog
 */
public class AboutDialog extends AbstractDialog {

    private static final long serialVersionUID = -6090954718194598410L;

    private boolean _abort = false;
    private Thread _updater = new updateThread();
    private JLabel _maxMemory, _freeMemory, _usedMemory;
    private JLabel _currentIPAddress;

    /**
     * The About Dialog window
     */
    public AboutDialog() {
        super(null, "About Chessly by Frank Kopp", false);
        setName("AboutDialog");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setupPanel();
        updateGUI();
        startUpdate();
    }

    private void setupPanel() {
        // create gui
        JPanel pane = new JPanel();
        pane.setLayout(new GridBagLayout());
        getContentPane().setLayout(new GridBagLayout());

        JPanel seperator1 = new JPanel();
        seperator1.setOpaque(false);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);

        // ToDo: Contact and Copyright

        // ToDo: GPL Text

        // memory
        JLabel maxMemoryText = new JLabel("Max Memory: ");
        _maxMemory = new JLabel();

        JLabel freeMemoryText = new JLabel("Free Memory: ");
        _freeMemory = new JLabel();

        JLabel usedMemoryText = new JLabel("Used Memory: ");
        _usedMemory = new JLabel();

        JLabel currentIPAdsress = new JLabel("IP Address: ");
        _currentIPAddress = new JLabel();

        // button
        JButton okayButton = new JButton("Close");
        okayButton.addActionListener(new Disposer());

        GridBagHelper.constrain(infoPanel, maxMemoryText, 0, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(infoPanel, _maxMemory,    1, 0, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0, 0, 0, 4, 0);

        GridBagHelper.constrain(infoPanel, freeMemoryText, 0, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(infoPanel, _freeMemory,    1, 1, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0, 0, 0, 4, 0);

        GridBagHelper.constrain(infoPanel, usedMemoryText, 0, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(infoPanel, _usedMemory,    1, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0, 0, 0, 4, 0);

        GridBagHelper.constrain(infoPanel, currentIPAdsress,  0, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.EAST, 0.0, 0.0, 0, 0, 4, 4);
        GridBagHelper.constrain(infoPanel, _currentIPAddress, 1, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.WEST, 0.0, 0.0, 0, 0, 4, 0);

        GridBagHelper.constrain(getContentPane(), pane, 0, 0, 0, 0, GridBagConstraints.VERTICAL, GridBagConstraints.NORTH, 1.0, 1.0, 0, 0, 0, 0);

        GridBagHelper.constrain(pane, seperator1, 0, 0, 1, 1, GridBagConstraints.VERTICAL, GridBagConstraints.CENTER, 0.0, 1.0, 0, 8, 0, 8);
        GridBagHelper.constrain(pane, infoPanel, 0, 2, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 4, 8, 4, 8);
        GridBagHelper.constrain(pane, okayButton, 0, 3, 1, 1, GridBagConstraints.NONE, GridBagConstraints.CENTER, 0.0, 0.0, 6, 8, 4, 8);

        this.getRootPane().setDefaultButton(okayButton);

        validateAll(infoPanel);
        pack();
    }

    @Override
    public void dispose() {
        _abort = true;
        _updater.interrupt();
        super.dispose();
    }

    private void updateGUI() {
        _maxMemory.setText(HelperTools.getMBytes(Runtime.getRuntime().maxMemory()));
        _freeMemory.setText(HelperTools.getMBytes((Runtime.getRuntime().freeMemory())));
        _usedMemory.setText(HelperTools.getMBytes((Runtime.getRuntime().totalMemory())));
        try {
            _currentIPAddress.setText(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            // -- ignore --
        }
    }

    private void startUpdate() {
        _updater.start();
    }

    private final class updateThread extends Thread {
        private updateThread() {
            super("MemoryDialogUpdater");
            setPriority(Thread.MIN_PRIORITY);
            setDaemon(true);
        }
        @Override
        public void run() {
            updateRunnable aUpdateRunnable = new updateRunnable();
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // -- ignore --
                }
                if (_abort) {
                    return;
                }
                SwingUtilities.invokeLater(aUpdateRunnable);
            }
        }

        private final class updateRunnable implements Runnable {
            @Override
            public void run() {
                updateGUI();
            }
        }
    }
}

