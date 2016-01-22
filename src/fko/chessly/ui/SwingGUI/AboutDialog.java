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
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fko.chessly.util.HelperTools;

/**
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
 * <hr/>
 *
 * The About Dialog
 */
public class AboutDialog extends AbstractDialog {

	private static final long serialVersionUID = -6090954718194598410L;

	private boolean _abort = false;
    private Thread _updater = new updateThread();
    private JLabel _maxMemory, _freeMemory, _usedMemory;
    private JLabel _currentIPAddress;

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
            public void run() {
                updateGUI();
            }
        }
    }
}

