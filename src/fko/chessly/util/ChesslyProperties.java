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

package fko.chessly.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import fko.chessly.Chessly;

/**
 * <p>Properties class for Chessly.</p>
 * <p>Reads its properties from ./properties/chessly/chessly.properties
 * (This is the default path. An option might override that later.)</p>
 *
 * <p>This class is an singleton as we only will have one properties file
 * for configuration.</p>
 *
 * <p>The default properties file is: ./properties/chessly/chessly.properties</p>
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
    private final static String propertiesFileRoot = "/chessly.properties";
    private final static String propertiesFileDefault = "/properties/chessly.properties";

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
        
        URL url;
        
        if ((url=Chessly.class.getResource(propertiesFileRoot)) == null) {
        		url=Chessly.class.getResource(propertiesFileDefault);
        }
        
        if (url==null) {
        		Chessly.minorError("Could not load properties file");
        		return;
        } 
        
//        System.out.println("Loading properties from: "+url.toString());
        
        InputStream in = null;
        try {
            in = url.openStream(); 
            load(in);
        } catch (FileNotFoundException e) {
            Chessly.criticalError("Properties file " + url.getFile() + " not found!");
        } catch (IOException e) {
            Chessly.criticalError("Properties file " + url.getFile() + " could not be loaded!");
        } finally {
            try {
                if (in!=null) {
                    in.close();
                }
            } catch (IOException e) {/*ignore*/}
        }
    }

}
