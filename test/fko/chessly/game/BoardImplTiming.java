package fko.chessly.game;

import static org.junit.Assert.*;

import org.junit.Test;

public class BoardImplTiming {
	
    GameBoard b = new GameBoardImpl();

    @Test
    public void test() {

	int runs = 0, runsPerRound = 10;
	long begin = System.nanoTime(), end;
	do {
	    for (int i=0; i<runsPerRound; ++i) timedMethod();
	    end = System.nanoTime();
	    runs += runsPerRound;
	    runsPerRound *= 2;
	} while (10000000000L < end-begin);
	
	System.out.println("Time for timedMethod() is " + 0.0000000001 * (end-begin) / runs + " seconds");
    }

    void timedMethod() {
	@SuppressWarnings("unused")
	GameBoard c = new GameBoardImpl(b);
    }

}
