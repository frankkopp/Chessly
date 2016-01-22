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

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import fko.chessly.Chessly;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.GameMove;
import fko.chessly.game.GameMoveImpl;
import fko.chessly.game.GamePiece;
import fko.chessly.game.GameColor;
import fko.chessly.game.GamePieceType;
import fko.chessly.game.GamePosition;
import fko.chessly.game.pieces.Bishop;
import fko.chessly.game.pieces.Knight;
import fko.chessly.game.pieces.Pawn;
import fko.chessly.game.pieces.Queen;
import fko.chessly.game.pieces.Rook;

/**
 * The BoardPanel class displays the board of a given game.
 */
public class BoardPanel extends JPanel implements MouseListener {

    private static final long serialVersionUID = -9147393642054887281L;

    // -- back reference to _ui --
    private SwingGUI _ui;

    // -- copy of the current board --
    private GameBoard _curBoard = null;

    // -- colors --
    private Color _possibleMoveColor      = new Color(115, 215, 115);
    private Color _selectedMoveColor      = new Color(0, 215, 0);
    private Color _checkColor             = new Color(255, 128, 128);
    private Color _boardBorderColor       = Color.BLACK;
    private Color _boardGridColor         = Color.BLACK;
    private Color _boardLightColor   	  = new Color(30, 140, 0);
    private Color _boardDarkColor         = new Color(90, 240, 0);
    private Color _lastMoveColor          = new Color(64, 128, 64);
    private Color _blackGradientFromColor = Color.BLACK;
    private Color _blackGradientToColor   = Color.GRAY;
    private Color _whiteGradientFromColor = Color.GRAY;
    private Color _whiteGradientToColor   = Color.WHITE;

    // -- image objects for pieces
    Image _wK,_wQ,_wB,_wN,_wR,_wP;
    Image _bK,_bQ,_bB,_bN,_bR,_bP;

    // -- holds pre selected filed --
    GamePosition _selectedFromField = null;

    /**
     * constructor
     */
    public BoardPanel(SwingGUI backReference) {
	super();

	this._ui = backReference;

	// -- set border --
	this.setBorder(new BevelBorder(BevelBorder.LOWERED));
	// -- set background color --
	this.setBackground(Color.GRAY);
	// -- set mouse listener --
	this.addMouseListener(this);

	// -- colors from properties file --
	String[] colors;
	colors = String.valueOf(Chessly.getProperties().getProperty("ui.possibleMoveColor")).split(":");
	_possibleMoveColor = new Color(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
	colors = String.valueOf(Chessly.getProperties().getProperty("ui.selectedMoveColor")).split(":");
	_selectedMoveColor = new Color(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
	colors = String.valueOf(Chessly.getProperties().getProperty("ui.checkColor")).split(":");
	_checkColor = new Color(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
	colors = String.valueOf(Chessly.getProperties().getProperty("ui.boardBorderColor")).split(":");
	_boardBorderColor = new Color(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
	colors = String.valueOf(Chessly.getProperties().getProperty("ui.boardGridColor")).split(":");
	_boardGridColor = new Color(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
	colors = String.valueOf(Chessly.getProperties().getProperty("ui.boardLightColor")).split(":");
	_boardLightColor = new Color(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
	colors = String.valueOf(Chessly.getProperties().getProperty("ui.boardDarkColor")).split(":");
	_boardDarkColor = new Color(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
	colors = String.valueOf(Chessly.getProperties().getProperty("ui.lastMoveColor")).split(":");
	_lastMoveColor = new Color(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
	colors = String.valueOf(Chessly.getProperties().getProperty("ui.blackGradientFromColor")).split(":");
	_blackGradientFromColor = new Color(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
	colors = String.valueOf(Chessly.getProperties().getProperty("ui.blackGradientToColor")).split(":");
	_blackGradientToColor = new Color(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
	colors = String.valueOf(Chessly.getProperties().getProperty("ui.whiteGradientFromColor")).split(":");
	_whiteGradientFromColor = new Color(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
	colors = String.valueOf(Chessly.getProperties().getProperty("ui.whiteGradientToColor")).split(":");
	_whiteGradientToColor = new Color(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));

	getPieceImages();

    }

    /**
     * Draws the board
     * @param board
     */
    public synchronized void drawBoard(GameBoard board) {
	this._curBoard = board;
	this.repaint();
    }

    /**
     * Overrides the JComponent paintComponent method to redraw the board
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
	super.paintComponent(g);
	drawBoard(g);
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e) {
	if (Chessly.getPlayroom().getCurrentGame() == null
		|| !Chessly.getPlayroom().getCurrentGame().isRunning()) {
	    return;
	}
	if (e.getButton() != MouseEvent.BUTTON1) {
	    return;
	}
	int x = e.getX();
	int y = e.getY();

	Point p = determineField(x, y);
	if (p==null) return;

	GamePosition tmpField = GamePosition.getGamePosition(p.x, p.y);//,_curBoard.getField(p.x,p.y).getPiece());

	if (_selectedFromField == null) { // no field chosen -> select
	    if (_curBoard.getPiece(tmpField) == null) return; // no piece on the chosen field - ignore click
	    _selectedFromField = tmpField;
	    this.repaint();
	} else {
	    if (_selectedFromField.equals(tmpField)) { // same field clicked again -> deselect
		_selectedFromField=null;
		this.repaint();
	    } else { // from and to chosen 

		GamePiece fromPiece = _curBoard.getPiece(_selectedFromField);
		GamePosition toField = tmpField;

		GameMove m = new GameMoveImpl(_selectedFromField, tmpField, fromPiece);

		// pawn promotion
		if (fromPiece instanceof Pawn) {
		    if (fromPiece.isWhite() && _selectedFromField.y == 7 && toField.y == 8) {
			// Promotion
			m.setPromotedTo(promotionDialog(GameColor.WHITE));
		    } else if (fromPiece.isBlack() && _selectedFromField.y == 2 && toField.y == 1) {
			// Promotion
			m.setPromotedTo(promotionDialog(GameColor.BLACK));
		    }
		}

		// if the toField is occupied store captured piece to Move - could be illegal move
		if (_curBoard.getPiece(tmpField) != null) 
		    m.setCapturedPiece(_curBoard.getPiece(tmpField));

		_ui.getController().setPlayerMove(m);

		_selectedFromField=null;
		this.repaint();

	    }
	}
    }

    /**
     * displays a dialog to start a new game
     */
    private GamePiece promotionDialog(GameColor color) {
	Object[] options = {
		"QUEEN",
		"ROOK",
		"BISHOP",
		"KNIGHT"
	};
	int n = JOptionPane.showOptionDialog(this,
		"Which piece would you like?",
		"Pawn Promotion",
		JOptionPane.YES_NO_CANCEL_OPTION,
		JOptionPane.QUESTION_MESSAGE,
		null,
		options,
		options[0]);

	switch (n) {
	case 0: return Queen.createQueen(color);
	case 1: return Rook.createRook(color);
	case 2: return Bishop.createBishop(color);
	case 3: return Knight.createKnight(color);
	default: return Pawn.createPawn(color);
	}
    }

    /**
     * determins the board coordinates of a user click
     * @param x
     * @param y
     * @return A Point object representing col and row on the board
     */
    private Point determineField(int x, int y) {

	Insets insets = this.getInsets();
	int currentWidth = getWidth() - insets.left - insets.right;
	int currentHeight = getHeight() - insets.top - insets.bottom;
	int size = Math.min(currentHeight, currentWidth) - 50;

	int dim = 8;
	size -= (size % dim);
	int positionX = (currentWidth >> 1) - (size >> 1) + size/25;
	int positionY = 8;
	int distance = size / dim;

	// -- outside of board --
	if (x < positionX || x > positionX + size || y > positionY + size || y < positionY) return null;

	int col = 1 + (x - positionX) / distance;
	int row = 1 + dim - 1 - (y - positionY) / distance;

	return new Point(col, row);
    }

    /**
     * draws the actual graphical board
     * @param g
     */
    private void drawBoard(Graphics g) {
	Graphics2D g2 = (Graphics2D) g;

	Insets insets = this.getInsets();
	int currentWidth = getWidth() - insets.left - insets.right;
	int currentHeight = getHeight() - insets.top - insets.bottom;
	int size = Math.min(currentHeight, currentWidth) -50;

	if (_curBoard == null) {
	    _drawCurrentBoard(new GameBoardImpl(), size, currentWidth, g2);
	} else {
	    _drawCurrentBoard(_curBoard, size, currentWidth, g2);
	}
    }

    /**
     * This method draws the board with numbers, lines and stones
     *
     * @param size
     * @param currentWidth
     * @param g2
     */
    private void _drawCurrentBoard(GameBoard board, int size, int currentWidth, Graphics2D g2) {

	int size1 = size;
	int dim = 8;
	size1 -= (size1 % dim);
	int positionX = (currentWidth >> 1) - (size1 >> 1) + size1/25; // >> equals division by 2
	int positionY = 8;
	int distance = size1 / dim;

	// -- draw board background --
	g2.setColor(_boardLightColor);
	g2.fill3DRect(positionX, positionY, size1, size1, true);
	drawLinesAndNumbers(g2, dim, positionX, positionY, distance, size1);
	drawCheckers(g2, dim, positionX, positionY, distance, size1);

	g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		RenderingHints.VALUE_INTERPOLATION_BICUBIC);

	// -- stones --
	GradientPaint stoneColor;
	float stoneSize = distance * 0.9f;

	// -- mark last move field --
	GameMove lastMove = board.getLastMove();
	g2.setColor(_lastMoveColor);
	if (lastMove != null) {
	    g2.fillRect((lastMove.getToField().x-1) * distance + positionX + 1,
		    (dim - lastMove.getToField().y) * distance + positionY + 1,
		    distance - 1,
		    distance - 1);
	}

	// -- mark possible moves --
	if (_ui.is_showPossibleMoves() && _selectedFromField != null 
		&& _curBoard.getPiece(_selectedFromField).getColor().equals(board.getNextPlayerColor())) { 

	    List<GameMove> moves = _curBoard.getPiece(_selectedFromField).getLegalMovesForPiece(board, _selectedFromField, false);
	    g2.setColor(_possibleMoveColor);
	    for (GameMove curMove : moves) {
		g2.fillRect((curMove.getToField().x - 1) * distance + positionX + 1,
			(dim - curMove.getToField().y) * distance + positionY + 1,
			distance - 1,
			distance - 1);
	    }
	} 

	// -- mark king in check field --
	if (_curBoard != null && _curBoard.hasCheck()) {
	    GamePosition king;
	    king = _curBoard.getKingField(_curBoard.getNextPlayerColor());
	    g2.setColor(_checkColor);
	    g2.fillRect((king.x-1) * distance + positionX + 1,
		    (dim - king.y) * distance + positionY + 1,
		    distance - 1,
		    distance - 1);
	}

	// - mark current selected from Field --
	if (board !=null && _selectedFromField != null) {
	    g2.setColor(_selectedMoveColor);
	    g2.fillRect((_selectedFromField.x - 1) * distance + positionX + 1,
		    (dim - _selectedFromField.y) * distance + positionY + 1,
		    distance - 1,
		    distance - 1);
	}

	// -- draw stones
	for (int col = 1; col <= dim; col++) {
	    for (int row = dim; row > 0; row--) {
		if (board.getPiece(col, row) != null) {
		    // >> equals division by 2
		    float rowOffset = (dim - row + 1) * distance - (distance >> 1) - (stoneSize / 2) + positionY;
		    // >> equals division by 2
		    float colOffset = col * distance - (distance >> 1) - (stoneSize / 2) + positionX;
		    if (board.getPiece(col, row).getColor() == GameColor.BLACK) {
			stoneColor = new GradientPaint(colOffset, rowOffset, _blackGradientToColor,
				colOffset + stoneSize, rowOffset + stoneSize, _blackGradientFromColor);
		    } else if (board.getPiece(col, row).getColor() == GameColor.WHITE) {
			stoneColor = new GradientPaint(colOffset, rowOffset, _whiteGradientToColor,
				colOffset + stoneSize, rowOffset + stoneSize, _whiteGradientFromColor);
		    } else {
			throw new RuntimeException("Field has invalid color");
		    }
		    g2.setPaint(stoneColor);
		    // http://en.wikipedia.org/wiki/Chess_symbols_in_Unicode
		    //g2.drawString("\u2654", positionX + ((i-1)*distance) + (distance>>1) - (fontSize>>1), positionY + size + fontSize + 2); // horizontal
		    g2.drawImage(getPieceImage(board.getPiece(col, row).getColor(), 
			    board.getPiece(col, row).getType()),
			    (int)colOffset, (int)rowOffset, (int)stoneSize, (int)stoneSize, null);
		}
	    }
	}
    }

    /**
     * draws lines and numbers
     * @param g2
     * @param dim
     * @param positionX
     * @param positionY
     * @param distance
     * @param size
     */
    private void drawLinesAndNumbers(Graphics2D g2, int dim, int positionX, int positionY, int distance, int size) {
	// -- board lines and numbers --
	int fontSize = size/25;
	g2.setColor(_boardGridColor);
	g2.setFont(new Font("Arial Unicode MS", Font.PLAIN, fontSize));
	for (int i = 1; i <= dim; i++) {
	    g2.drawString(getFilesLetter(i), positionX + ((i-1)*distance) + (distance>>1) - (fontSize>>1), positionY + size + fontSize + 2); // horizontal
	    g2.drawString(String.valueOf(i), positionX - (fontSize), positionY + size - ((i-1)*distance) - (distance>>1) + (fontSize>>1)); // vertical
	    // lines
	    g2.drawLine(positionX + (i * distance), positionY, positionX + (i * distance), positionY + size);
	    g2.drawLine(positionX, positionY + (i * distance), positionX + size, positionY + (i * distance));

	}
	g2.setColor(_boardBorderColor);
	g2.draw3DRect(positionX, positionY, size, size, true);
    }

    /**
     * draws the checkers in the chess board
     * @param g2
     * @param dim
     * @param positionX
     * @param positionY
     * @param distance
     * @param size1
     */
    private void drawCheckers(Graphics2D g2, int dim, int positionX, int positionY, int distance, int size1) {
	g2.setColor(_boardDarkColor);
	for (int c=1;c<=dim;c++) {
	    for (int r=1;r<=dim;r++) {
		if ((c+r)%2==0) { 
		    g2.fillRect((c-1) * distance + positionX + 1,(dim-r) * distance + positionY + 1, distance - 1, distance - 1);	
		}
	    }
	}

    }

    /**
     * Translates Column numbers into A...H
     * @param i 1..8
     * @return Letter String
     */
    private static String getFilesLetter(int i) {
	assert i>0 && i<=8;
	switch(i) {
	case 1: return "A";
	case 2: return "B";
	case 3: return "C";
	case 4: return "D";
	case 5: return "E";
	case 6: return "F";
	case 7: return "G";
	case 8: return "H";
	}
	return "";
    }

    /**
     * Load all pice images
     */
    private void getPieceImages() {
	// -- get images for pieces --
	String _userDir = System.getProperty("user.dir");
	String imageFolder = _userDir+"/images/";
	_wK = Toolkit.getDefaultToolkit().getImage(imageFolder+"wK.png");
	_bK = Toolkit.getDefaultToolkit().getImage(imageFolder+"bK.png");
	_wQ = Toolkit.getDefaultToolkit().getImage(imageFolder+"wQ.png");
	_bQ = Toolkit.getDefaultToolkit().getImage(imageFolder+"bQ.png");
	_wB = Toolkit.getDefaultToolkit().getImage(imageFolder+"wB.png");
	_bB = Toolkit.getDefaultToolkit().getImage(imageFolder+"bB.png");
	_wN = Toolkit.getDefaultToolkit().getImage(imageFolder+"wN.png");
	_bN = Toolkit.getDefaultToolkit().getImage(imageFolder+"bN.png");
	_wR = Toolkit.getDefaultToolkit().getImage(imageFolder+"wR.png");
	_bR = Toolkit.getDefaultToolkit().getImage(imageFolder+"bR.png");
	_wP = Toolkit.getDefaultToolkit().getImage(imageFolder+"wP.png");
	_bP = Toolkit.getDefaultToolkit().getImage(imageFolder+"bP.png");
    }

    /**
     * chooses preloaded Image from color and piecetype
     * @param color
     * @param piecetype
     * @return piece Image Object or null (should not happen)
     */
    private Image getPieceImage(GameColor color, GamePieceType piecetype) {
	assert color == GameColor.WHITE || color == GameColor.BLACK;
	if (color == GameColor.WHITE) {
	    switch (piecetype) {
	    case KING:   return _wK;
	    case QUEEN:  return _wQ;
	    case ROOK:   return _wR;
	    case BISHOP: return _wB;
	    case KNIGHT: return _wN;
	    case PAWN:   return _wP;
	    }
	} else {
	    switch (piecetype) {
	    case KING:   return _bK;
	    case QUEEN:  return _bQ;
	    case ROOK:   return _bR;
	    case BISHOP: return _bB;
	    case KNIGHT: return _bN;
	    case PAWN:   return _bP;
	    }
	}
	return null;
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e) {}

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e) {}
}
