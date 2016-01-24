/**
 *
 */
package fko.chessly.openingbook;

import static org.junit.Assert.fail;

import java.nio.file.FileSystems;

import org.junit.Test;

import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.GameMove;
import fko.chessly.game.NotationHelper;
import fko.chessly.openingbook.OpeningBookImpl.Mode;
import fko.chessly.util.HelperTools;

/**
 * @author fkopp
 *
 */
public class OpeningBookImpl_Test {

    @Test
    public void testBook() {

        long memStart = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        OpeningBook book = new OpeningBookImpl(null, FileSystems.getDefault().getPath("./book/book.txt"),Mode.SIMPLE);
        ((OpeningBookImpl) book)._config.FORCE_CREATE = true;

        book.initialize();

        System.out.format("Testing Book...");
        GameBoard currentBoard = new GameBoardImpl(NotationHelper.StandardBoardFEN);
        GameMove bookMove = null;
        while ((bookMove = book.getBookMove(currentBoard.toFEN())) != null) {
            //System.out.format("%s ==> %s%n",currentBoard.toFEN(),bookMove);
            currentBoard.makeMove(bookMove);
        }
        if (currentBoard.getMoveHistory().size() < 2) {
            fail("no board moves played in test!");
        }
        System.out.format("Book OK%n%n");

        long memMid = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        book = null;

        System.gc();

        long memEnd = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        System.out.println("Test End");
        System.out.format("Memory used at Start: %s MB %n",HelperTools.getMBytes(memStart));
        System.out.format("Memory used at Mid: %s MB %n",HelperTools.getMBytes(memMid));
        System.out.format("Memory used at End: %s MB%n%n",HelperTools.getMBytes(memEnd));

    }

    //@Test
    public void timingTest() {

        int runs = 0, runsPerRound = 1;
        long begin = System.nanoTime(), end;
        do {
            for (int i=0; i<runsPerRound; ++i) timedMethod();
            runs += runsPerRound;
        } while ((20*1000000000L) > System.nanoTime()-begin);
        end = System.nanoTime();

        final double rt = ((end-begin) / runs) * 0.000000001;
        System.out.println("Time for timedMethod() is " + rt + " seconds");
        System.out.format("Runs: %d in %f seconds", runs, ((end-begin)*0.000000001));
    }

    void timedMethod() {
        OpeningBook b = new OpeningBookImpl(null);
        b.initialize();
    }

}
