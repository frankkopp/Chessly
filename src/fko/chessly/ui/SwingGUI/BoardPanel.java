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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import fko.chessly.Chessly;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.game.GameMoveImpl;
import fko.chessly.game.GamePiece;
import fko.chessly.game.GamePieceType;
import fko.chessly.game.GamePosition;
import fko.chessly.game.pieces.Bishop;
import fko.chessly.game.pieces.Knight;
import fko.chessly.game.pieces.Pawn;
import fko.chessly.game.pieces.Queen;
import fko.chessly.game.pieces.Rook;

/**
 * The BoardPanel class displays the board of a given game.
 *
 * TODO: drag&drop
 *
 */
public class BoardPanel extends JPanel implements MouseListener, MouseMotionListener {

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
    /*
    private Color _blackGradientFromColor = Color.BLACK;
    private Color _blackGradientToColor   = Color.GRAY;
    private Color _whiteGradientFromColor = Color.GRAY;
    private Color _whiteGradientToColor   = Color.WHITE;
     */

    // -- image objects for pieces
    private Image _wK,_wQ,_wB,_wN,_wR,_wP;
    private Image _bK,_bQ,_bB,_bN,_bR,_bP;

    private List<Piece> _pieces = new ArrayList<>(64);

    // -- holds preselected field --
    private GamePosition _selectedFromField = null; // GamePosition.getGamePosition("c1");

    static enum orientation { NORTH, SOUTH };
    private orientation _currentOrientation = orientation.NORTH;

    // to avoid having to move parameters around these are fields
    // easier readable code
    private Graphics2D _graphics2D;
    private int _boardSize;
    private int _currentWidth;
    private int _currentHeight;
    private static final int DIM = 8;
    private int _positionX;
    private int _positionY;
    private int _distance;
    private float _stoneSize;

    // the piece picked for dragging
    private Piece _dragPiece;
    private GamePosition _dragFromPosition;
    // avoid jumping piece while drag
    private int _dragOffsetX;
    private int _dragOffsetY;

    /**
     * constructor
     * @param backReference
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
        // -- set mouse motion listener --
        this.addMouseMotionListener(this);

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
        getPieceImages();

    }

    /**
     * Draws the board
     * @param board
     */
    public synchronized void setAndDrawBoard(GameBoard board) {
        this._curBoard = board;
        this.repaint();
    }

    /**
     * @param g
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        _graphics2D = (Graphics2D) g;
        drawBoard();
    }

    /**
     * draws the actual graphical board
     */
    private void drawBoard() {
        Insets insets = this.getInsets();
        _currentWidth = getWidth() - insets.left - insets.right;
        _currentHeight = getHeight() - insets.top - insets.bottom;
        _boardSize = Math.min(_currentHeight, _currentWidth) -50;

        if (_curBoard == null) {
            drawCurrentBoard(new GameBoardImpl());
        } else {
            drawCurrentBoard(_curBoard);
        }
    }

    /**
     * This method draws the board with numbers, lines and stones
     */
    private void drawCurrentBoard(GameBoard board) {

        int actualBoardSize = _boardSize;
        actualBoardSize -= (actualBoardSize % DIM);
        _positionX = (_currentWidth >> 1) - (actualBoardSize >> 1) + actualBoardSize/25;
        _positionY = 8;
        _distance = actualBoardSize / DIM;
        _stoneSize = _distance * 0.9f;

        // -- draw board background --
        _graphics2D.setColor(_boardLightColor);
        _graphics2D.fill3DRect(_positionX, _positionY, actualBoardSize, actualBoardSize, true);
        _graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // -- lines & numbers
        drawLinesAndNumbers(actualBoardSize);

        // -- draw checkers
        drawCheckers(actualBoardSize);

        // -- mark last move field --
        markLastMove(board);

        // -- mark possible moves --
        markPossibleMoves(board);

        // -- mark king in check field --
        markKingInCheckField();

        // - mark current selected from Field --
        markCurrentSelectedFromField(board);

        // -- draw stones
        drawPieces(board);
    }

    /**
     * @param board
     */
    private void drawPieces(GameBoard board) {

        // -- do not reset the piece list if we drag
        if (_dragPiece == null) {

            // clear the pieces array
            _pieces.clear();

            // fill the pieces array
            for (int col = 1; col <= DIM; col++) {
                for (int row = DIM; row > 0; row--) {
                    if (board.getPiece(col, row) != null) {

                        // >> equals division by 2
                        int r = _currentOrientation == orientation.NORTH ? BoardPanel.DIM-row+1 : row ;
                        float rowOffset = r * _distance - (_distance >> 1) - (_stoneSize / 2) + _positionY;

                        // >> equals division by 2
                        int c = _currentOrientation == orientation.NORTH ? col : BoardPanel.DIM-col+1;
                        float colOffset = c * _distance - (_distance >> 1) - (_stoneSize / 2) + _positionX +1; // +1 small correction due to rounding

                        // create piece to display
                        Image pieceImage = getPieceImage(board.getPiece(col, row).getColor(), board.getPiece(col, row).getType());
                        Piece piece = new Piece(pieceImage,(int)colOffset,(int)rowOffset);

                        _pieces.add(piece);
                    }
                }
            }
        } else {
            // determine if we drag a piece
            _pieces.remove(_dragPiece);
            _pieces.add(0, _dragPiece);
        }

        // now draw each piece
        for (Piece p : _pieces) {
            p.draw();
        }

    }

    /**
     * @param board
     */
    private void markCurrentSelectedFromField(GameBoard board) {
        if (board !=null && _selectedFromField != null) {
            _graphics2D.setColor(_selectedMoveColor);
            int x = _currentOrientation == orientation.NORTH ? _selectedFromField.x : DIM - _selectedFromField.x+1;
            int y = _currentOrientation == orientation.NORTH ? _selectedFromField.y : DIM - _selectedFromField.y+1;
            _graphics2D.fillRect((x - 1) * _distance + _positionX + 1,
                    (DIM - y) * _distance + _positionY + 1,
                    _distance - 1,
                    _distance - 1);
        }
    }

    /**
     */
    private void markKingInCheckField() {
        if (_curBoard != null && _curBoard.hasCheck()) {
            GamePosition king;
            king = _curBoard.getKingField(_curBoard.getNextPlayerColor());
            _graphics2D.setColor(_checkColor);
            int x = _currentOrientation == orientation.NORTH ? king.x : DIM - king.x+1;
            int y = _currentOrientation == orientation.NORTH ? king.y : DIM - king.y+1;
            _graphics2D.fillRect((x-1) * _distance + _positionX + 1,
                    (DIM - y) * _distance + _positionY + 1,
                    _distance - 1,
                    _distance - 1);
        }
    }

    /**
     * @param board
     */
    private void markPossibleMoves(GameBoard board) {
        if (_ui != null && _ui.is_showPossibleMoves() && _selectedFromField != null
                && _curBoard.getPiece(_selectedFromField).getColor().equals(board.getNextPlayerColor())) {

            List<GameMove> moves = _curBoard.getPiece(_selectedFromField).getLegalMovesForPiece(board, _selectedFromField, false);
            _graphics2D.setColor(_possibleMoveColor);
            for (GameMove curMove : moves) {
                int x = _currentOrientation == orientation.NORTH ? curMove.getToField().x : DIM - curMove.getToField().x+1;
                int y = _currentOrientation == orientation.NORTH ? curMove.getToField().y : DIM - curMove.getToField().y+1;
                _graphics2D.fillRect((x - 1) * _distance + _positionX + 1,
                        (DIM - y) * _distance + _positionY + 1,
                        _distance - 1,
                        _distance - 1);
            }
        }
    }

    /**
     * @param board
     */
    private void markLastMove(GameBoard board) {
        GameMove lastMove = board.getLastMove();
        _graphics2D.setColor(_lastMoveColor);
        if (lastMove != null) {
            int x = _currentOrientation == orientation.NORTH ? lastMove.getToField().x : DIM - lastMove.getToField().x+1;
            int y = _currentOrientation == orientation.NORTH ? lastMove.getToField().y : DIM - lastMove.getToField().y+1;
            _graphics2D.fillRect((x-1) * _distance + _positionX + 1,
                    (DIM - y) * _distance + _positionY + 1,
                    _distance - 1,
                    _distance - 1);
        }
    }

    /**
     * draws lines and numbers
     * @param size
     */
    private void drawLinesAndNumbers(int size) {
        // -- board lines and numbers --
        int fontSize = size/25;
        _graphics2D.setColor(_boardGridColor);
        _graphics2D.setFont(new Font("Arial Unicode MS", Font.PLAIN, fontSize));

        for (int i = 1; i <= DIM; i++) {
            int index =_currentOrientation == orientation.NORTH ?  i : DIM - i +1 ;
            _graphics2D.drawString(getFilesLetter(index), _positionX + ((i-1)*_distance) + (_distance>>1) - (fontSize>>1), _positionY + size + fontSize + 2); // horizontal
            _graphics2D.drawString(String.valueOf(index), _positionX - (fontSize), _positionY + size - ((i-1)*_distance) - (_distance>>1) + (fontSize>>1)); // vertical
            // lines
            _graphics2D.drawLine(_positionX + (i * _distance), _positionY, _positionX + (i * _distance), _positionY + size);
            _graphics2D.drawLine(_positionX, _positionY + (i * _distance), _positionX + size, _positionY + (i * _distance));
        }

        _graphics2D.setColor(_boardBorderColor);
        _graphics2D.draw3DRect(_positionX, _positionY, size, size, true);
    }

    /**
     * draws the checkers in the chess board
     * @param actuaSize
     */
    private void drawCheckers(int actuaSize) {
        _graphics2D.setColor(_boardDarkColor);
        for (int c=1;c<=DIM;c++) {
            for (int r=1;r<=DIM;r++) {
                if ((c+r)%2==0) {
                    _graphics2D.fillRect((c-1) * _distance + _positionX + 1,(DIM-r) * _distance + _positionY + 1, _distance - 1, _distance - 1);
                }
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
            default:
                break;
        }
        return "";
    }

    /**
     * Load all piece images
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
                default:
                    break;
            }
        } else {
            switch (piecetype) {
                case KING:   return _bK;
                case QUEEN:  return _bQ;
                case ROOK:   return _bR;
                case BISHOP: return _bB;
                case KNIGHT: return _bN;
                case PAWN:   return _bP;
                default:
                    break;
            }
        }
        return null;
    }

    /**
     * Determines the board coordinates of a user click
     * @param x
     * @param y
     * @return A Point object representing col and row on the board
     */
    private GamePosition determinePosition(int x, int y) {

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

        if (_currentOrientation == orientation.SOUTH) {
            col = dim-col+1;
            row = dim-row+1;
        }

        return GamePosition.getGamePosition(col, row);
    }

    /**
     * @return the currentOrientation
     */
    public orientation getCurrentOrientation() {
        return this._currentOrientation;
    }

    /**
     * @param currentOrientation the currentOrientation to set
     */
    public void setCurrentOrientation(orientation currentOrientation) {
        this._currentOrientation = currentOrientation;
    }

    /**
     * flips the current orientation
     */
    public void flipOrientation() {
        this._currentOrientation = _currentOrientation == orientation.NORTH ? orientation.SOUTH : orientation.NORTH;
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        GamePosition pos = getGamePositionFromMouseEvent(e);
        // mouse not on board
        if (pos==null) return;

        System.out.println("Mouse PRESS: "+pos+" "+e);

        if (_selectedFromField == null) { // no field chosen -> select
            if (_curBoard.getPiece(pos) != null) { // no piece on the chosen field - ignore click
                _selectedFromField = pos;
                this.repaint();
            }
        }

        // which piece?
        for (Piece p : _pieces) {
            if (pos.equals(p.getPosition())) {
                // found piece
                _dragOffsetX = e.getX() - p.x;
                _dragOffsetY = e.getY() - p.y;
                _dragPiece = p;
                _dragFromPosition = p.getPosition();
            }
        }

        // put on top of the pieces list
        if (_dragPiece != null) {
            _pieces.remove(_dragPiece);
            _pieces.add(0,_dragPiece);
        }

        this.repaint();

    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        System.out.println("Mouse DRAG: "+e);
        if (_dragPiece == null) return;
        _dragPiece.x=e.getX() - _dragOffsetX;
        _dragPiece.y=e.getY() - _dragOffsetY;
        this.repaint();
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        GamePosition pos = getGamePositionFromMouseEvent(e);
        // mouse not on board
        if (pos==null) return;

        System.out.println("Mouse RELEASE: "+pos+" "+e);

        // this was a click (or multiple clicks)
        if (e.getClickCount() >= 1) {

            if (_selectedFromField == null) { // no field chosen -> select
                return;
            }

            // clear drag piece
            _dragPiece = null;

            // from and to chosen
            GamePiece fromPiece = _curBoard.getPiece(_selectedFromField);

            GamePosition toField = pos;

            GameMove m = new GameMoveImpl(_selectedFromField, pos, fromPiece);

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
            if (_curBoard.getPiece(pos) != null)
                m.setCapturedPiece(_curBoard.getPiece(pos));

            _ui.getController().setPlayerMove(m);

            this.repaint();
        }

        // no click - press & release

        // non valid field
        if (_dragPiece != null) {
            _dragPiece = null;
        }

        this.repaint();
    }

    /**
     * Invoked when the mouse button has been clicked (pressed
     * and released) on a component.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        //System.out.println("Mouse CLICK: "+e);
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        //System.out.println("Mouse moved: "+e);
    }

    /**
     * Invoked when the mouse enters a component.
     */
    @Override
    public void mouseEntered(MouseEvent e) { /* empty */}

    /**
     * Invoked when the mouse exits a component.
     */
    @Override
    public void mouseExited(MouseEvent e) { /* empty */}

    /**
     * @param e
     */
    private GamePosition getGamePositionFromMouseEvent(MouseEvent e) {
        if (Chessly.getPlayroom().getCurrentGame() == null || !Chessly.getPlayroom().getCurrentGame().isRunning()) {
            return null;
        }

        if (e.getButton() != MouseEvent.BUTTON1) {
            return null;
        }

        int x = e.getX();
        int y = e.getY();

        return determinePosition(x, y);
    }

    /**
     * Represents an Images with x and y coordinates
     * @author Frank Kopp
     */
    private class Piece {

        private Image img;
        private int x;
        private int y;
        public Piece(Image img, int x, int y) {
            this.img = img;
            this.x = x;
            this.y = y;
        }

        /**
         */
        void draw() {
            draw(this.x, this.y);
        }

        /**
         * @param mouseX
         * @param mouseY
         */
        void draw(int mouseX, int mouseY) {
            _graphics2D.drawImage(this.img, mouseX, mouseY, (int)_stoneSize, (int)_stoneSize, null);
        }

        GamePosition getPosition() {
            return determinePosition(x, y);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            result = prime * result + ((this.img == null) ? 0 : this.img.hashCode());
            result = prime * result + this.x;
            result = prime * result + this.y;
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) { return true; }
            if (obj == null) { return false; }
            if (!(obj instanceof Piece)) { return false; }
            Piece other = (Piece) obj;
            if (!getOuterType().equals(other.getOuterType())) { return false; }
            if (this.img == null) {
                if (other.img != null) { return false; }
            } else if (this.img != other.img) { return false; }
            if (!this.getPosition().equals(((Piece)obj).getPosition())) return false;
            return true;
        }

        private BoardPanel getOuterType() {
            return BoardPanel.this;
        }




    }

}
