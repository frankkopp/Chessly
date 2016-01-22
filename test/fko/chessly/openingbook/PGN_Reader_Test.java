/**
 * 
 */
package fko.chessly.openingbook;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import fko.chessly.Chessly;
import fko.chessly.openingbook.OpeningBookImpl.Mode;

/**
 * @author fkopp
 *
 */
public class PGN_Reader_Test {
    
    @Test
    public void testPGN_Reader() {

	List<String> lines = readAllLinesFromFile();

	PGN_Reader pgnReader = new PGN_Reader(lines);

	if (!pgnReader.startProcessing()) {
	    System.out.println("Could not process lines from PGN file");
	    fail();
	}

	List<PGN_Reader.pgnGame> gameList = pgnReader.getGames();
	
	//System.out.format("%nGame List: %n%s%n",gameList.toString());
	System.out.format("%nNumber of Games: %s%n",gameList.size());
	System.out.println();
	
    }

    //@Test
    public void patternTest() {
        // (([KQRBN]?[a-h][1-8])|(O-O(-O)?)|(\$\d{1,3})|((\d{1,3})(( )*(\.{1,3}))?))
        // (([KQRBN]?[a-h][1-8])|(O-O(-O)?)|(\\$\\d{1,3})|((\\d{1,3})(( )*(\\.{1,3}))?))
        // SAN Moves: ([KQRBN]?[a-h][1-8])
        // SAN Caslte: (O-O(-O)?)
        // NAG (\\$\\d{1,3})
        // (\\d{1,3})(( )*(\\.{1,3}))?
        // 1... d52.c4$50 e5 3Nf3 Nc6 4 . Nc3 Nf6 Bc4 Bc5 O-O O-O-O
        
        String line = "1... d5 2.c4$50(Nf3?) e5 3Nf3 Nc6 4 . Nc3 Nf6 Bc4 Bc5 O-O O-O-O";
    
        // Movetext NAG (Numeric Annotation Glyph)
        line = line.replaceAll("(\\$\\d{1,3})", ""); // no annotation supported
    
        // Movetext RAV (Recursive Annotation Variation)
        line = line.replaceAll("\\([^\\(.]*\\)", ""); // no variations supported
        
        // Move number indicators 1.e4 and 1... e5 
        line = line.replaceAll("\\d{1,3} *\\.{1,3}", ""); // no numbers supported
        
        System.out.println(line);
        
        //test = "Nf3";
        String p = "([KQRBN]?[a-h][1-8])";
        
        Pattern pattern = Pattern.compile(p);
        Matcher matcher = pattern.matcher(line);
        
        while (matcher.find()) {
            for (int i=0;i<matcher.groupCount();i++) {
        	if (matcher.group(i) != null) 
        	    System.out.println("Group "+i+" "+matcher.group(i)+", ");
            }
            System.out.println();
        }
        
    }

    private List<String> readAllLinesFromFile() {

	final String folderPath = "./book/";
	//final String fileNamePlain = "pgn_test.pgn";
	//final String fileNamePlain = "Test_PGN/perle.pgn";
	final String fileNamePlain = "Openings-PGN/8moves_GM_LB.pgn";

	Path path = FileSystems.getDefault().getPath(folderPath, fileNamePlain);
	Charset charset = Charset.forName("ISO-8859-1");
	List<String> lines = null;
	try {
	    System.out.println("Reading Opening Book as SIMPLE notation: "+path);
	    lines = Files.readAllLines(path, charset);
	    System.out.format("Finished reading %d lines. Creating internal book...%n",lines.size());
	    System.out.flush();
	} catch (FileNotFoundException e) {
	    Chessly.criticalError("Opening Book file '" + path + "' could not be found!");
	} catch (java.nio.charset.CharacterCodingException e) {
	    Chessly.criticalError("Opening Book file '" + path + "' has illegal characters for ISO-8859-1!");
	} catch (IOException e) {
	    Chessly.criticalError("Opening Book file '" + path + "' could not be loaded!");
	} 

	return lines;
    }

}
