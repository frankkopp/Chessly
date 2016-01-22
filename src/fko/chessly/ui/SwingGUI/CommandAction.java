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

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class CommandAction extends AbstractAction {

    private static final long serialVersionUID = -5931196119308802091L;

    private Command command;  // The command to execute in response to an ActionEvent

    /**
     * Create an Action object that has the various specified attributes,
     * and invokes the specified Command object in response to ActionEvents
     */
    public CommandAction(Command newCommand, String label,
                         Icon icon, String tooltip,
                         KeyStroke accelerator, int mnemonic,
                         boolean enabledOrNot) {
        this.command = newCommand;  // Remember the command to invoke

        // Set the various action attributes with putValue()
        if (label != null) {
            putValue(NAME, label);
        }
        if (icon != null) {
            putValue(SMALL_ICON, icon);
        }
        if (tooltip != null) {
            putValue(SHORT_DESCRIPTION, tooltip);
        }
        if (accelerator != null) {
            putValue(ACCELERATOR_KEY, accelerator);
        }
        if (mnemonic != KeyEvent.VK_UNDEFINED) {
            putValue(MNEMONIC_KEY, Integer.valueOf(mnemonic));
        }

        // Tell the action whether it is currently enabled or not
        setEnabled(enabledOrNot);
    }

    /**
     * This method implements ActionListener, which is a super-interface of
     * Action.  When a component generates an ActionEvent, it is passed to
     * this method.  This method simply passes it on to the Command object
     * which is also an ActionListener object
     */
    public void actionPerformed(ActionEvent e) {
        command.actionPerformed(e);
    }

    // These constants are defined by Action in Java 1.3.
    // For compatibility with Java 1.2, we re-define them here.
    public static final String ACCELERATOR_KEY = "AcceleratorKey";
    public static final String MNEMONIC_KEY = "MnemonicKey";
}

