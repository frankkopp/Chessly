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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.KeyStroke;

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
    @Override
    public void actionPerformed(ActionEvent e) {
        command.actionPerformed(e);
    }

    // These constants are defined by Action in Java 1.3.
    // For compatibility with Java 1.2, we re-define them here.
    public static final String ACCELERATOR_KEY = "AcceleratorKey";
    public static final String MNEMONIC_KEY = "MnemonicKey";
}

