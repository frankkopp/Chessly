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
package fko.chessly.ui.JavaFX_GUI;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

/**
 * The BoardPanel class displays the board of a given game.
 *
 * TODO: Minor bug - dragged piece jumps shortly back to from field before painting on to field
 *
 */
public class BoardPane extends Pane {

    // -- back reference to _ui --
    private JavaFX_GUI_Controller _controller;

    // -- copy of the current board --
    private GameBoard _curBoard = null;

    // -- colors --
    private Color _possibleMoveColor      = Color.rgb(115, 215, 115);
    private Color _selectedMoveColor      = Color.rgb(0, 215, 0);
    private Color _checkColor             = Color.rgb(255, 128, 128);
    private Color _boardBorderColor       = Color.BLACK;
    private Color _boardGridColor         = Color.BLACK;
    private Color _boardLightColor   	  = Color.rgb(30, 140, 0);
    private Color _boardDarkColor         = Color.rgb(90, 240, 0);
    private Color _lastMoveColor          = Color.rgb(64, 128, 64);

    // -- image objects for pieces
    private Image _wK,_wQ,_wB,_wN,_wR,_wP;
    private Image _bK,_bQ,_bB,_bN,_bR,_bP;

    // list of pieces to iterate over while drawing
    private Set<Piece> _pieces = new LinkedHashSet<Piece>(64);

    // -- holds preselected field --
    private GamePosition _selectedFromField = null; // GamePosition.getGamePosition("c1");

    //
    static enum orientation { WHITE_SOUTH, WHITE_NORTH };
    private orientation _currentOrientation = orientation.WHITE_SOUTH;

    // to avoid having to move parameters around these are fields
    // easier readable code
    //private Graphics _graphics;
    private double _boardSize;
    private double _currentWidth;
    private double _currentHeight;
    private static final int DIM = 8;
    private double _positionX;
    private int _positionY;
    private double _distance;
    private double _stoneSize;

    // the piece picked for dragging
    private Piece _dragPiece;
    // avoid jumping piece while drag
    private int _dragOffsetX;
    private int _dragOffsetY;

    // supports handling of mouse press and release
    private boolean _ignoreNextRelease;

    /**
     * constructor
     * @param backReference
     */
    public BoardPane(JavaFX_GUI_Controller backReference) {
        super();

        this._controller = backReference;

        // -- set border --
        //this.setBorder(new BevelBorder(BevelBorder.LOWERED));
        // -- set background color --
        //this.setBackground(Color.GRAY);
        this.setBackground(new Background(new BackgroundFill(Paint.valueOf("#cccccc"),null,null)));
        // -- set minimum size --
        this.setMinWidth(200);
        this.setMinHeight(200);
        // -- set mouse listener --
        this.setOnMousePressed(this::mousePressed);
        this.setOnMouseReleased(this::mouseReleased);
        this.setOnMouseDragged(this::mouseDragged);

        // -- colors from properties file --
        String[] colors;
        colors = String.valueOf(Chessly.getProperties().getProperty("ui.possibleMoveColor")).split(":");
        _possibleMoveColor = Color.rgb(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
        colors = String.valueOf(Chessly.getProperties().getProperty("ui.selectedMoveColor")).split(":");
        _selectedMoveColor = Color.rgb(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
        colors = String.valueOf(Chessly.getProperties().getProperty("ui.checkColor")).split(":");
        _checkColor = Color.rgb(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
        colors = String.valueOf(Chessly.getProperties().getProperty("ui.boardBorderColor")).split(":");
        _boardBorderColor = Color.rgb(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
        colors = String.valueOf(Chessly.getProperties().getProperty("ui.boardGridColor")).split(":");
        _boardGridColor = Color.rgb(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
        colors = String.valueOf(Chessly.getProperties().getProperty("ui.boardLightColor")).split(":");
        _boardLightColor = Color.rgb(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
        colors = String.valueOf(Chessly.getProperties().getProperty("ui.boardDarkColor")).split(":");
        _boardDarkColor = Color.rgb(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
        colors = String.valueOf(Chessly.getProperties().getProperty("ui.lastMoveColor")).split(":");
        _lastMoveColor = Color.rgb(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2]));
        colors = String.valueOf(Chessly.getProperties().getProperty("ui.blackGradientFromColor")).split(":");

        // load piece images
        getPieceImages();

    }

    /**
     * Draws the board
     * @param board
     */
    public void setAndDrawBoard(GameBoard board) {
        // make own copy of the board to update it without side effects
        this._curBoard = new GameBoardImpl(board);
        // TODO: repaint()???
        this.requestLayout();
    }

    /**
     * draws the actual graphical board
     * TODO: where and when is this called in JavaFX?
     */
    private void drawBoard() {
        Insets insets = this.getInsets();
        _currentWidth = this.getWidth() - insets.getLeft() - insets.getRight();
        _currentHeight = getHeight() - insets.getTop() - insets.getBottom();
        _boardSize = Math.min(_currentHeight, _currentWidth) -50;
        if (_curBoard == null) { // no board yet - use new board with standard setup
            _curBoard = new GameBoardImpl();
        }
        drawCurrentBoard();
    }

    /**
     * This method draws the board with numbers, lines and stones
     */
    private void drawCurrentBoard() {

        double actualBoardSize = _boardSize;
        actualBoardSize -= (actualBoardSize % DIM);
        _positionX = (_currentWidth / 2) - (actualBoardSize / 2) + actualBoardSize/25;
        _positionY = 8;
        _distance = actualBoardSize / DIM;
        _stoneSize = _distance * 0.9d;

        // -- draw board background --
        this.setBackground(new Background(new BackgroundFill(_boardLightColor,null,null)));
        Rectangle rectangle = new Rectangle(_positionX, _positionY, actualBoardSize, actualBoardSize);
        rectangle.setFill(_boardLightColor);
        this.getChildren().add(rectangle);
        //_graphics.fill3DRect(_positionX, _positionY, actualBoardSize, actualBoardSize, true);
        // ((Graphics2D) _graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // ((Graphics2D) _graphics).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        // -- lines & numbers
        drawLinesAndNumbers(actualBoardSize);

        // -- draw checkers
        drawCheckers(actualBoardSize);

        // -- mark last move field --
        markLastMove();

        // -- mark possible moves --
        markPossibleMoves();

        // -- mark king in check field --
        markKingInCheckField();

        // - mark current selected from Field --
        markCurrentSelectedFromField();

        // -- draw stones
        drawPieces();
    }

    /**
     * draws lines and numbers
     * @param actualBoardSize
     */
    private void drawLinesAndNumbers(double actualBoardSize) {
        // -- board lines and numbers --
        double fontSize = actualBoardSize/25;

        Text text = new Text();
        text.setFont(new Font(20));
        text.setWrappingWidth(200);
        text.setTextAlignment(TextAlignment.JUSTIFY)
        text.setText("The quick brown fox jumps over the lazy dog");

        _graphics.setColor(_boardGridColor);
        _graphics.setFont(new Font("Arial Unicode MS", Font.PLAIN, fontSize));

        for (int i = 1; i <= DIM; i++) {
            int index =_currentOrientation == orientation.WHITE_SOUTH ?  i : DIM - i +1 ;
            _graphics.drawString(getFilesLetter(index), _positionX + ((i-1)*_distance) + (_distance>>1) - (fontSize>>1), _positionY + actualBoardSize + fontSize + 2); // horizontal
            _graphics.drawString(String.valueOf(index), _positionX - (fontSize), _positionY + actualBoardSize - ((i-1)*_distance) - (_distance>>1) + (fontSize>>1)); // vertical
            // lines
            _graphics.drawLine(_positionX + (i * _distance), _positionY, _positionX + (i * _distance), _positionY + actualBoardSize);
            _graphics.drawLine(_positionX, _positionY + (i * _distance), _positionX + actualBoardSize, _positionY + (i * _distance));
        }

        _graphics.setColor(_boardBorderColor);
        _graphics.draw3DRect(_positionX, _positionY, actualBoardSize, actualBoardSize, true);
    }

    /**
     * draws the checkers in the chess board
     * @param actualBoardSize
     */
    private void drawCheckers(double actualBoardSize) {
        _graphics.setColor(_boardDarkColor);
        for (int c=1;c<=DIM;c++) {
            for (int r=1;r<=DIM;r++) {
                if ((c+r)%2==0) {
                    _graphics.fillRect((c-1) * _distance + _positionX + 1,(DIM-r) * _distance + _positionY + 1, _distance - 1, _distance - 1);
                }
            }
        }

    }

    /**
     * @param board
     */
    private void markLastMove() {
        GameMove lastMove = _curBoard.getLastMove();
        if (lastMove != null) {
            int x = _currentOrientation == orientation.WHITE_SOUTH ? lastMove.getToField().x : DIM - lastMove.getToField().x+1;
            int y = _currentOrientation == orientation.WHITE_SOUTH ? lastMove.getToField().y : DIM - lastMove.getToField().y+1;
            Rectangle rectangle = new Rectangle(
                    (x - 1) * _distance + _positionX + 1,
                    (DIM - y) * _distance + _positionY + 1,
                    _distance-1,
                    _distance-1);
            rectangle.setFill(_lastMoveColor);
            this.getChildren().add(rectangle);

        }
    }

    /**
     * @param board
     */
    private void markPossibleMoves() {
        if (_controller != null && _controller.is_showPossibleMoves() && _selectedFromField != null
                && _curBoard.getPiece(_selectedFromField).getColor().equals(_curBoard.getNextPlayerColor())) {

            List<GameMove> moves = _curBoard.getPiece(_selectedFromField).getLegalMovesForPiece(_curBoard, _selectedFromField, false);
            moves.forEach(curMove -> {
                int x = _currentOrientation == orientation.WHITE_SOUTH ? curMove.getToField().x : DIM - curMove.getToField().x+1;
                int y = _currentOrientation == orientation.WHITE_SOUTH ? curMove.getToField().y : DIM - curMove.getToField().y+1;
                Rectangle rectangle = new Rectangle(
                        (x - 1) * _distance + _positionX + 1,
                        (DIM - y) * _distance + _positionY + 1,
                        _distance-1,
                        _distance-1);
                rectangle.setFill(_possibleMoveColor);
                this.getChildren().add(rectangle);
            });
        }
    }

    /**
     */
    private void markKingInCheckField() {
        if (_curBoard != null && _curBoard.hasCheck()) {
            GamePosition king;
            king = _curBoard.getKingField(_curBoard.getNextPlayerColor());
            int x = _currentOrientation == orientation.WHITE_SOUTH ? king.x : DIM - king.x+1;
            int y = _currentOrientation == orientation.WHITE_SOUTH ? king.y : DIM - king.y+1;
            Rectangle rectangle = new Rectangle(
                    (x - 1) * _distance + _positionX + 1,
                    (DIM - y) * _distance + _positionY + 1,
                    _distance-1,
                    _distance-1);
            rectangle.setFill(_checkColor);
            this.getChildren().add(rectangle);
        }
    }

    /**
     * @param board
     */
    private void markCurrentSelectedFromField() {
        if (_curBoard !=null && _selectedFromField != null) {
            int x = _currentOrientation == orientation.WHITE_SOUTH ? _selectedFromField.x : DIM - _selectedFromField.x+1;
            int y = _currentOrientation == orientation.WHITE_SOUTH ? _selectedFromField.y : DIM - _selectedFromField.y+1;
            Rectangle rectangle = new Rectangle(
                    (x - 1) * _distance + _positionX + 1,
                    (DIM - y) * _distance + _positionY + 1,
                    _distance-1,
                    _distance-1);
            rectangle.setFill(_selectedMoveColor);
            this.getChildren().add(rectangle);
        }
    }

    /**
     * @param board
     */
    private void drawPieces() {

        // -- do not reset the piece list if we drag
        if (_dragPiece == null) {
            // clear the pieces array
            _pieces.clear();
            // fill the pieces array
            for (int col = 1; col <= DIM; col++) {
                for (int row = DIM; row > 0; row--) {
                    if (_curBoard.getPiece(col, row) != null) {

                        // >> equals division by 2
                        int r = _currentOrientation == orientation.WHITE_SOUTH ? DIM-row+1 : row ;
                        double rowOffset = r * _distance - (_distance / 2) - (_stoneSize / 2) + _positionY;

                        // >> equals division by 2
                        int c = _currentOrientation == orientation.WHITE_SOUTH ? col : DIM-col+1;
                        double colOffset = c * _distance - (_distance / 2) - (_stoneSize / 2) + _positionX +1; // +1 small correction due to rounding

                        // create piece to display
                        Image pieceImage = getPieceImage(_curBoard.getPiece(col, row).getColor(), _curBoard.getPiece(col, row).getType());
                        Piece piece = new Piece(pieceImage,(int)colOffset,(int)rowOffset);

                        _pieces.add(piece);
                    }
                }
            }
        } else {
            // we drag a piece
            _pieces.remove(_dragPiece);
            _pieces.add(_dragPiece);
        }

        // now draw each piece
        // (geeky new Java 8 lambda way - and parallel as well :) )
        _pieces.stream().forEach(Piece::draw);

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
     * @param e
     */
    private GamePosition getGamePositionFromMouseEvent(MouseEvent e) {
        if (Chessly.getPlayroom().getCurrentGame() == null || !Chessly.getPlayroom().getCurrentGame().isRunning()) {
            return null;
        }
        if (e.getButton() != e.getButton().PRIMARY) {
            return null;
        }
        return determinePosition(e.getX(), e.getY());
    }

    /**
     * Determines the board coordinates of a user click
     * @param d
     * @param e
     * @return A Point object representing col and row on the board
     */
    private GamePosition determinePosition(double d, double e) {

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
        if (d < positionX || d > positionX + size || e > positionY + size || e < positionY) return null;

        int col = 1 + (d - positionX) / distance;
        int row = 1 + dim - 1 - (e - positionY) / distance;

        if (_currentOrientation == orientation.WHITE_NORTH) {
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
        this._currentOrientation = _currentOrientation == orientation.WHITE_SOUTH ? orientation.WHITE_NORTH : orientation.WHITE_SOUTH;
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param event
     */
    public void mousePressed(MouseEvent event) {
        GamePosition pos = getGamePositionFromMouseEvent(event);
        // mouse not on board - ignore mouse press
        if (pos==null) {
            _ignoreNextRelease = false;
            return;
        }

        //System.out.print("Mouse PRESS: "+pos+" ");
        // which field?
        if (_selectedFromField == null) {
            // no field selected -> select
            if (_curBoard.getPiece(pos) != null
                    && _curBoard.getNextPlayerColor().equals(_curBoard.getPiece(pos).getColor()) ) {
                _selectedFromField = pos;
                this.repaint();
            } else { // no piece on the chosen field - ignore click
                //System.out.println();
                return;
            }
            _ignoreNextRelease = true;
        } else {
            _ignoreNextRelease = false;
        }

        // which piece?
        for (Piece p : _pieces) {
            if (pos.equals(p.getPosition())) {
                // found piece
                _dragOffsetX = event.getX() - p.x;
                _dragOffsetY = event.getY() - p.y;
                _dragPiece = p;
                _dragPiece.isDragged=true;
            }
        }

        //this.repaint();
        //System.out.println("SelectedFrom: "+_selectedFromField+" DragPiece: "+_dragPiece.getPosition());
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
        if (_dragPiece == null) return;
        //System.out.println("Mouse DRAG: "+e);
        _ignoreNextRelease = false;
        _dragPiece.x=e.getX() - _dragOffsetX;
        _dragPiece.y=e.getY() - _dragOffsetY;
        this.repaint();
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        if (_ignoreNextRelease) return;

        GamePosition pos = getGamePositionFromMouseEvent(e);
        // mouse not on board - reset selection and drag and ignore the release
        if (pos==null) {
            _selectedFromField = null;
            if (_dragPiece != null) {
                _dragPiece.isDragged=false;
                _dragPiece = null;
            }
            this.repaint();
            return;
        }

        //System.out.print("Mouse RELEASE: "+pos+" ---> ");

        if (_selectedFromField == null) {
            return;
        }

        if (pos.equals(_selectedFromField)) {
            // no selection - ignore release
            //System.out.println("de-select: "+_selectedFromField);
            if (_dragPiece != null) {
                _dragPiece.isDragged=false;
                _dragPiece = null;
            }
            _selectedFromField = null;
            this.repaint();
            //System.out.println();
            return;
        }

        // from and to chosen
        GamePiece fromPiece = _curBoard.getPiece(_selectedFromField);
        GamePosition fromField = _selectedFromField;
        GamePosition toField = pos;

        //System.out.print("From: "+fromField+" To: "+toField+" DragPiece: "+_dragPiece.getPosition()+" ");

        GameMove m = new GameMoveImpl(fromField, toField, fromPiece);

        if (!_curBoard.isLegalMove(m)) {
            if (_dragPiece != null) {
                _dragPiece.isDragged=false;
                _dragPiece = null;
            }
            _selectedFromField = null;
            this.repaint();
            return;
        }

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

        // clear drag piece
        if (_dragPiece != null) {
            _dragPiece.isDragged=false;
            _dragPiece = null;
        }
        // reset ignore flag
        _ignoreNextRelease = false;
        // de-select
        _selectedFromField = null;

        //System.out.print("Move: "+m);
        _controller.setPlayerMove(m);

        return;
    }

    /**
     * Represents an Images with x and y coordinates
     * @author Frank Kopp
     */
    private class Piece {

        private Image img;
        private int x;
        private int y;
        private boolean isDragged;

        public Piece(Image img, int x, int y) {
            this.img = img;
            this.x = x;
            this.y = y;
            this.isDragged = false;
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
            _graphics.drawImage(this.img, mouseX, mouseY, (int)_stoneSize, (int)_stoneSize, null);
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
            result = prime * result + (this.isDragged ? 1231 : 1237);
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
            } else if (!this.img.equals(other.img)) { return false; }
            if (this.isDragged != other.isDragged) { return false; }
            if (!this.getPosition().equals(((Piece)obj).getPosition())) return false;
            return true;
        }

        private BoardPanel getOuterType() {
            return BoardPanel.this;
        }




    }

}
