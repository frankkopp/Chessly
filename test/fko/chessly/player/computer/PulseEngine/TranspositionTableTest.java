/*
 * Copyright (C) 2007-2014 Phokham Nonava
 *
 * This file is part of Flux Chess.
 *
 * Flux Chess is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flux Chess is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flux Chess.  If not, see <http://www.gnu.org/licenses/>.
 */
package fko.chessly.player.computer.PulseEngine;

import org.junit.Test;

import static org.junit.Assert.*;

public class TranspositionTableTest {

  @Test
  public void testTranspositionTable() {
    TranspositionTable table = new TranspositionTable(10);
    int move1 = Move.createNewMove("e2-e4", Color.WHITE);

    // Put an entry into the table
    table.put(1L, 1, 100, Bound.EXACT, move1, 0);

    TranspositionTable.TranspositionTableEntry entry = table.get(1L);
    assertNotNull(entry);

    assertEquals(1, entry.depth);
    assertEquals(100, entry.getValue(0));
    assertEquals(Bound.EXACT, entry.type);
    assertEquals(move1, entry.move);

    // Overwrite the entry with a new one
    table.put(1L, 2, 200, Bound.LOWER, move1, 0);

    entry = table.get(1L);
    assertNotNull(entry);

    assertEquals(2, entry.depth);
    assertEquals(200, entry.getValue(0));
    assertEquals(Bound.LOWER, entry.type);
    assertEquals(move1, entry.move);

    // Put an mate entry into the table
    table.put(2L, 0, Value.CHECKMATE - 5, Bound.EXACT, move1, 3);

    entry = table.get(2L);
    assertNotNull(entry);

    assertEquals(0, entry.depth);
    assertEquals(Value.CHECKMATE - 4, entry.getValue(2));
    assertEquals(Bound.EXACT, entry.type);
    assertEquals(move1, entry.move);

  }

  @Test
  public void testSize() {
    System.out.println("Testing Transposition Table size:");
    int[] megabytes = {4, 8, 16, 32, 64, 128, 256, 512};
    for (int i : megabytes) {
      int numberOfEntries = i * 1024 * 1024 / TranspositionTable.ENTRYSIZE;

      System.gc();
      long usedMemoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
      TranspositionTable tt = new TranspositionTable(numberOfEntries);
      long usedMemoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
      	
      long hashAllocation = (usedMemoryAfter - usedMemoryBefore) / (1024 * 1024);
      System.out.format("TT Size (config): %dMB = %dMB real size - Nodes: %d%n", i, hashAllocation, numberOfEntries);
      tt = null;
    }
  }

}
