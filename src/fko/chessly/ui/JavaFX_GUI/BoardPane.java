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
import fko.chessly.game.GamePiece;
import fko.chessly.game.GamePieceType;
import fko.chessly.game.GamePosition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;

/**
 * The BoardPanel class displays the board of a given game.
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

    private static final int DIM = 8;

    // to avoid having to move parameters around these are fields
    // easier readable code
    private NumberBinding _boardSize;
    private NumberBinding _offset_x;
    private NumberBinding _offset_y;
    private NumberBinding _checkerSize;

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

        this.setPadding(new Insets(0,0,0,50));
        this.setBackground(new Background(new BackgroundFill(Color.GRAY,null,null)));
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

        // draw initial board
        drawBoard();

    }

    /**
     * Draws the board
     * @param board
     */
    public synchronized void setAndDrawBoard(GameBoard board) {
        // make own copy of the board to update it without side effects
        this._curBoard = new GameBoardImpl(board);
        drawBoard();
    }

    /**
     * draws the actual graphical board
     * TODO: where and when is this called in JavaFX?
     */
    protected void drawBoard() {
        if (_curBoard == null) { // no board yet - use new board with standard setup
            _curBoard = new GameBoardImpl();
        }
        drawBoard(_curBoard);
    }

    /**
     * This method draws the board with numbers, lines and stones
     * @param curBoard
     */
    private void drawBoard(GameBoard curBoard) {

        // clear the node to redraw everything
        this.getChildren().clear();

        // 90% of pane size to let room for files and rank designations
        _boardSize = Bindings.min(this.heightProperty(), this.widthProperty()).multiply(0.9);

        // board should leave space for rank numbers on the left and file letters on the bottom
        _offset_x = this.widthProperty().subtract(_boardSize).divide(2).add(_boardSize.multiply(0.01));
        _offset_y = this.heightProperty().subtract(this.heightProperty()).add(10);

        Rectangle rectangle = new Rectangle();
        rectangle.setStroke(_boardDarkColor);
        rectangle.setFill(_boardLightColor);
        // here we position the rectangle (this depends on pane size as well)
        rectangle.xProperty().bind(_offset_x);
        rectangle.yProperty().bind(_offset_y);
        // here we bind rectangle size to pane size
        rectangle.heightProperty().bind(_boardSize);
        rectangle.widthProperty().bind(_boardSize);
        this.getChildren().add(rectangle);

        // -- draw checkers
        drawCheckers(rectangle);

        // -- lines & numbers
        drawLinesAndNumbers(rectangle);


        // -- mark last move field --
        markLastMove(curBoard);

        // -- mark possible moves --
        markPossibleMoves(curBoard);

        // -- mark king in check field --
        markKingInCheckField(curBoard);

        // - mark current selected from Field --
        markCurrentSelectedFromField(curBoard);

        // -- draw stones
        drawPieces(curBoard);
    }

    /**
     * draws the checkers in the chess board
     * @param rectangle
     * @param boardSize
     */
    private void drawCheckers(Rectangle rectangle) {
        _checkerSize = rectangle.widthProperty().divide(DIM);
        for (int c=0;c<DIM;c++) {
            for (int r=0;r<DIM;r++) {
                if ((c+r)%2==1) {
                    Rectangle checkerField = new Rectangle();
                    checkerField.setFill(_boardDarkColor);
                    // here we position rectangles (this depends on pane size as well)
                    checkerField.xProperty().bind(_offset_x.add(_checkerSize.multiply(c)));
                    checkerField.yProperty().bind(_offset_y.add(_checkerSize.multiply(r)));
                    // here we bind rectangle size to pane size
                    checkerField.heightProperty().bind(_checkerSize);
                    checkerField.widthProperty().bind(_checkerSize);
                    this.getChildren().add(checkerField);
                }
            }
        }
    }

    /**
     * draws lines and numbers
     * @param rectangle
     * @param boardSize
     */
    private void drawLinesAndNumbers(Rectangle rectangle) {

        // hack as there is no property binding for fontsize in Font
        DoubleProperty fontSize = new SimpleDoubleProperty();
        fontSize.bind(_boardSize.divide(25));
        this.styleProperty().bind(Bindings.concat(
                "-fx-font-size: ", fontSize.asString(), ";"
                ,"-fx-font-family: \"Arial\";"
                ));

        // for each file and rank
        for (int i = 0; i < DIM; i++) {
            int index =_currentOrientation == orientation.WHITE_SOUTH ?  i+1 : DIM - i;

            // File letter - calculates the middle off the letter and adds half of the checker size
            Text fileLetter = new Text(getFilesLetter(index));
            fileLetter.xProperty().bind(_offset_x.add(_checkerSize.multiply(i).subtract(fontSize.multiply(0.4)).add(_checkerSize.multiply(0.5))));
            fileLetter.yProperty().bind(rectangle.heightProperty().add(10).add(fontSize));
            this.getChildren().add(fileLetter);

            // Rank digit - position the digit in the middle of the rank
            Text rankDigit = new Text(String.valueOf(index));
            rankDigit.xProperty().bind(_offset_x.subtract(fontSize.multiply(0.4)).subtract(8));
            rankDigit.yProperty().bind(_offset_y.add(_checkerSize.multiply(DIM-i).add(fontSize.multiply(0.3)).subtract(_checkerSize.multiply(0.5))));
            this.getChildren().add(rankDigit);

            // horizontal lines
            Line h_line = new Line();
            h_line.setStroke(_boardGridColor);
            h_line.startXProperty().bind(_offset_x);
            h_line.endXProperty().bind(_offset_x.add(_boardSize));
            h_line.startYProperty().bind(_offset_y.add(_checkerSize.multiply(index)));
            h_line.endYProperty().bind(h_line.startYProperty());
            this.getChildren().add(h_line);

            // vertical lines
            Line v_line = new Line();
            v_line.setStroke(_boardGridColor);
            v_line.startXProperty().bind(_offset_x.add(_checkerSize.multiply(index)));
            v_line.endXProperty().bind(v_line.startXProperty());
            v_line.startYProperty().bind(_offset_y);
            v_line.endYProperty().bind(_offset_y.add(_boardSize));
            this.getChildren().add(v_line);

            // border box
            Rectangle r = new Rectangle();
            r.setStroke(_boardGridColor);
            r.setFill(Color.TRANSPARENT);
            r.xProperty().bind(_offset_x);
            r.yProperty().bind(_offset_y);
            r.widthProperty().bind(_boardSize);
            r.heightProperty().bind(_boardSize);
            this.getChildren().add(r);
        }
    }

    private void markLastMove(GameBoard curBoard) {
        GameMove lastMove = curBoard.getLastMove();
        //GameMove lastMove = NotationHelper.createNewMoveFromSimpleNotation(_curBoard, "b1c3"); // TEST

        if (lastMove != null && _controller != null && _controller.isShowLastMove() ) {
            // from field
            int fromFile = _currentOrientation == orientation.WHITE_SOUTH ? lastMove.getFromField().x : DIM - lastMove.getFromField().x+1;
            int fromRank = _currentOrientation == orientation.WHITE_SOUTH ? lastMove.getFromField().y : DIM - lastMove.getFromField().y+1;
            //markField(fromFile, fromRank, _lastMoveColor);

            // to field
            int toFile = _currentOrientation == orientation.WHITE_SOUTH ? lastMove.getToField().x : DIM - lastMove.getToField().x+1;
            int toRank = _currentOrientation == orientation.WHITE_SOUTH ? lastMove.getToField().y : DIM - lastMove.getToField().y+1;
            markField(toFile, toRank, _lastMoveColor);

            // line from source field to target field
            Line line = new Line();
            line.setStroke(Color.BLUE);
            line.setStrokeLineCap(StrokeLineCap.ROUND);
            line.setSmooth(true);
            line.setStrokeWidth(5);
            line.startXProperty().bind(_offset_x.add(_checkerSize.multiply(fromFile)).subtract(_checkerSize.multiply(0.5)));
            line.startYProperty().bind(_offset_y.add(_checkerSize.multiply(DIM-fromRank)).add(_checkerSize.multiply(0.5)));
            line.endXProperty().bind(_offset_x.add(_checkerSize.multiply(toFile)).subtract(_checkerSize.multiply(0.5)));
            line.endYProperty().bind(_offset_y.add(_checkerSize.multiply(DIM-toRank)).add(_checkerSize.multiply(0.5)));
            this.getChildren().add(line);

        }
    }

    private void markPossibleMoves(GameBoard curBoard) {
        //_selectedFromField = GamePosition.getGamePosition("e2");
        if (_controller != null && _controller.isShowPossibleMoves() && _selectedFromField != null
                && curBoard.getPiece(_selectedFromField).getColor().equals(curBoard.getNextPlayerColor())) {

            List<GameMove> moves = curBoard.getPiece(_selectedFromField).getLegalMovesForPiece(curBoard, _selectedFromField, false);
            moves.forEach(curMove -> {
                int file = _currentOrientation == orientation.WHITE_SOUTH ? curMove.getToField().x : DIM - curMove.getToField().x+1;
                int rank = _currentOrientation == orientation.WHITE_SOUTH ? curMove.getToField().y : DIM - curMove.getToField().y+1;
                markField(file, rank, _possibleMoveColor);
            });
        }
    }

    private void markKingInCheckField(GameBoard curBoard) {
        if (curBoard != null && curBoard.hasCheck()) {
            GamePosition king;
            king = curBoard.getKingField(curBoard.getNextPlayerColor());
            int file = _currentOrientation == orientation.WHITE_SOUTH ? king.x : DIM - king.x+1;
            int rank = _currentOrientation == orientation.WHITE_SOUTH ? king.y : DIM - king.y+1;
            markField(file, rank, _checkColor);
        }
    }

    private void markCurrentSelectedFromField(GameBoard curBoard) {
        //_selectedFromField = GamePosition.getGamePosition("e2");
        if (curBoard !=null && _selectedFromField != null) {
            int file = _currentOrientation == orientation.WHITE_SOUTH ? _selectedFromField.x : DIM - _selectedFromField.x+1;
            int rank = _currentOrientation == orientation.WHITE_SOUTH ? _selectedFromField.y : DIM - _selectedFromField.y+1;
            markField(file, rank, _selectedMoveColor);
        }
    }

    /**
     * Paints a field in a different color
     * @param file
     * @param rank
     * @param color
     */
    private void markField(int file, int rank, Color color) {
        Rectangle checkerField = new Rectangle();
        checkerField.setFill(color);
        // here we position rectangles (this depends on pane size as well)
        checkerField.xProperty().bind(_offset_x.add(_checkerSize.multiply(file-1)));
        checkerField.yProperty().bind(_offset_y.add(_checkerSize.multiply(DIM-rank)));
        // here we bind rectangle size to pane size
        checkerField.heightProperty().bind(_checkerSize);
        checkerField.widthProperty().bind(_checkerSize);
        this.getChildren().add(checkerField);
    }

    /**
     * @param curBoard
     * @param board
     */
    private void drawPieces(GameBoard curBoard) {
        // -- do not reset the piece list if we drag
        if (_dragPiece == null) {
            // clear the pieces array
            _pieces.clear();
            // fill the pieces array
            for (int file = 1; file <= DIM; file++) {
                for (int rank = DIM; rank > 0; rank--) {
                    if (curBoard.getPiece(file, rank) != null) {

                        int file2 = _currentOrientation == orientation.WHITE_SOUTH ? file : DIM - file+1;
                        int rank2 = _currentOrientation == orientation.WHITE_SOUTH ? rank : DIM - rank+1;

                        // Create ImageView
                        Image pieceImage = getPieceImage(curBoard.getPiece(file, rank).getColor(), curBoard.getPiece(file, rank).getType());
                        ImageView pieceView = new ImageView(pieceImage);
                        pieceView.setPreserveRatio(true);
                        pieceView.setSmooth(true);

                        // Add mouse handler
                        pieceView.setOnMousePressed((e) -> { System.out.println("Mouse Click: "+e);});
                        pieceView.setOnMouseDragged((e) -> { System.out.println("Mouse Drag: "+e);});

                        // here we position the ImageView (this depends on pane size as well)
                        pieceView.xProperty().bind(_offset_x.add(_checkerSize.multiply(file2-1)));
                        pieceView.yProperty().bind(_offset_y.add(_checkerSize.multiply(DIM-rank2)));
                        // here we bind ImageView size to checker size
                        pieceView.fitHeightProperty().bind(_checkerSize);
                        pieceView.fitWidthProperty().bind(_checkerSize);

                        this.getChildren().add(pieceView);
                    }
                }
            }
        }
    }

    /**
     * displays a dialog to start a new game
     */
    private GamePiece promotionDialog(GameColor color) {
        /*        Object[] options = {
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
        }*/
        return null;
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
        String imageFolder = "file:"+_userDir+"/images/";
        _wK = new Image(imageFolder+"wK.png");
        _bK = new Image(imageFolder+"bK.png");
        _wQ = new Image(imageFolder+"wQ.png");
        _bQ = new Image(imageFolder+"bQ.png");
        _wB = new Image(imageFolder+"wB.png");
        _bB = new Image(imageFolder+"bB.png");
        _wN = new Image(imageFolder+"wN.png");
        _bN = new Image(imageFolder+"bN.png");
        _wR = new Image(imageFolder+"wR.png");
        _bR = new Image(imageFolder+"bR.png");
        _wP = new Image(imageFolder+"wP.png");
        _bP = new Image(imageFolder+"bP.png");

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
        /*
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

        return GamePosition.getGamePosition(col, row);*/
        return null;
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
        drawBoard();
    }

    /**
     * flips the current orientation
     */
    public void flipOrientation() {
        this._currentOrientation = _currentOrientation == orientation.WHITE_SOUTH ? orientation.WHITE_NORTH : orientation.WHITE_SOUTH;
        drawBoard();
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     * @param event
     */
    public void mousePressed(MouseEvent event) {
        /*        GamePosition pos = getGamePositionFromMouseEvent(event);
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
         */
        //this.repaint();
        //System.out.println("SelectedFrom: "+_selectedFromField+" DragPiece: "+_dragPiece.getPosition());
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    public void mouseDragged(MouseEvent e) {
        /*        if (_dragPiece == null) return;
        //System.out.println("Mouse DRAG: "+e);
        _ignoreNextRelease = false;
        _dragPiece.x=e.getX() - _dragOffsetX;
        _dragPiece.y=e.getY() - _dragOffsetY;
        this.repaint();*/
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e) {
        /*        if (_ignoreNextRelease) return;

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
         */
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
            //_graphics.drawImage(this.img, mouseX, mouseY, (int)_stoneSize, (int)_stoneSize, null);
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

        private BoardPane getOuterType() {
            return BoardPane.this;
        }




    }

}
