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

package fko.chessly.util;

import fko.chessly.Chessly;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>Properties class for Chessly.</p>
 * <p>Reads its properties from ./properties/reversi/reversi.properties
 * (This is the default path. An option might override that later.)</p>
 *
 * <p>This class is an singleton as we only will have one properties file
 * for configuration.</p>
 *
 * <p>The default properties file is: ./properties/reversi/reversi.properties</p>
 *
 * <p>This class extends java.util.Properties.</p>
 *
 * @see java.util.Properties
 * @author Frank Kopp (frank@familie-kopp.de)
 */
public class ChesslyProperties extends java.util.Properties {

	private static final long serialVersionUID = -6813211399102798292L;

	// Singleton instance
    private final static ChesslyProperties _instance = new ChesslyProperties();

    // Default properties file
    private final static String propertiesFile = "./properties/chessly/chessly.properties";

    /**
     * ReversiProperties is a Singleton so use getInstance()
     * @return ReversiProperties instance
     */
    public static ChesslyProperties getInstance() {
        return _instance;
    }

    private ChesslyProperties() {
        // -- call constructor of java.util.Properties
        super();
        String filename = propertiesFile;
        InputStream in = null;
        try {
            in = new FileInputStream(filename);
            load(in);
        } catch (FileNotFoundException e) {
            Chessly.criticalError("Properties file " + filename + " not found!");
        } catch (IOException e) {
            Chessly.criticalError("Properties file " + filename + " could not be loaded!");
        } finally {
            try {
                if (in!=null) {
                    in.close();
                }
            } catch (IOException e) {/*ignore*/}
        }
    }

    /**
     * Creates a shallow copy of this hashtable. All the structure of the
     * hashtable itself is copied, but the keys and values are not cloned.
     * This is a relatively expensive operation.
     *
     * @return a clone of the hashtable.
     */
    @Override
	public synchronized Object clone() {
        return super.clone();
    }
}
