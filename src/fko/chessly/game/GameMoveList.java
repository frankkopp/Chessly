package fko.chessly.game;

import java.util.ArrayList;
import java.util.Collection;

import fko.chessly.game.GameMove;

public class GameMoveList extends ArrayList<GameMove> {

	private static final long serialVersionUID = 324138104775502009L;
	
	public GameMoveList() {
		super();
	}

	public GameMoveList(Collection<? extends GameMove> c) {
		super(c);
	}

	public GameMoveList(int initialCapacity) {
		super(initialCapacity);
	}

	public String toString() {
		
		String s = "";
		
		for(GameMove m : this) {
			s += m.toString();
			s += " ("+m.getValue()+") ";
		}
		
		return s;
		
	}

	public GameMove getLast() {
		if (this.size() <= 0) return null;
		return this.get(this.size()-1);
	}
	
	public GameMove removeLast() {
	    if (this.size() <= 0) return null;
		return this.remove(this.size()-1);
	}

}
