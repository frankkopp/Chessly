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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 * The BoardPanel class displays the board of a given game.
 */
public class BoardPane extends Pane {

    // back reference to _ui
    private MainViewPresenter _controller;

    // copy of the current board
    private GameBoard _curBoard = null;

    // colors
    private Color _possibleMoveColor      = Color.rgb(115, 215, 115);
    private Color _selectedMoveColor      = Color.rgb(0, 215, 0);
    private Color _checkColor             = Color.rgb(255, 128, 128);
    private Color _boardBorderColor       = Color.BLACK;
    private Color _boardGridColor         = Color.BLACK;
    private Color _boardLightColor   	  = Color.rgb(30, 140, 0);
    private Color _boardDarkColor         = Color.rgb(90, 240, 0);
    private Color _lastMoveColor          = Color.rgb(64, 128, 64);

    // image objects for pieces
    private Image _wK,_wQ,_wB,_wN,_wR,_wP;
    private Image _bK,_bQ,_bB,_bN,_bR,_bP;

    // list of pieces to iterate over while drawing
    private List<Piece> _pieces = new ArrayList<>(64);

    // holds preselected field
    private GamePosition _selectedFromField = null; // GamePosition.getGamePosition("c1");

    // used to support flip board
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
    // while dragging we use a copy of the _dragPiece to show during the drag
    private Piece _dragPieceCopy;

    // avoid jumping piece while drag
    private double _dragOffsetX;
    private double _dragOffsetY;

    // supports handling of mouse press and release
    private boolean _ignoreNextRelease;

    // some elements we can reuse to not have to reate them every refresh
    private Text[] _fileLetters = new Text[DIM];
    private Text[] _rankNumbers = new Text[DIM];
    private Line[] _hlines = new Line[DIM];
    private Line[] _vlines = new Line[DIM];
    private Rectangle[] _rectangles = new Rectangle[DIM];
    private static final DoubleProperty _fontSize = new SimpleDoubleProperty();
    private static final StringExpression _fontStyle = Bindings.concat(
            "-fx-font-size: ", _fontSize.asString(), ";"
            ,"-fx-font-family: \"Arial\";"
            );

    /**
     * constructor
     * @param backReference
     */
    public BoardPane(MainViewPresenter backReference) {
        super();

        // store the reference to the _controller
        this._controller = backReference;

        // set up the pane
        this.setPadding(new Insets(0,0,0,50));
        this.setBackground(new Background(new BackgroundFill(Color.GRAY,null,null)));
        // set minimum size --
        this.setMinWidth(200);
        this.setMinHeight(200);
        // set mouse listener --
        this.setOnMousePressed(this::mousePressed);
        this.setOnMouseReleased(this::mouseReleased);
        this.setOnMouseDragged(this::mouseDragged);

        // colors from properties file
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

        // pre-load piece images
        getPieceImages();

        // prepare some elements and keep them to reference them
        for (int i=0; i<DIM; i++) {
            _fileLetters[i] = new Text(getFilesLetter(i+1));
            _rankNumbers[i] = new Text(String.valueOf(i+1));
            _hlines[i] = new Line();
            _vlines[i] = new Line();
            _rectangles[i] = new Rectangle();
        }

        // draw initial board
        drawBoard();

    }

    /**
     * Updates the known board and re-draws the BoardPane.
     * @param board - must not be null and must not change any longer.
     */
    public void setAndDrawBoard(GameBoard board) {
        // make own copy of the board to update it without side effects
        this._curBoard = new GameBoardImpl(board);
        drawBoard();
    }

    /**
     * Resets the board to standard configuration
     */
    public void resetBoard() {
        this._curBoard = null;
        drawBoard();
    }

    /**
     * Draws the current board.
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

        //Thread.dumpStack();
        //System.out.println("DRAWING");

        // clear the node to redraw everything
        this.getChildren().clear();

        // 90% of pane size to let room for files and rank designations
        _boardSize = Bindings.min(this.heightProperty(), this.widthProperty()).multiply(0.9);

        // board should leave space for rank numbers on the left and file letters on the bottom
        _offset_x = this.widthProperty().subtract(_boardSize).divide(2).add(_boardSize.multiply(0.01));
        _offset_y = this.heightProperty().subtract(this.heightProperty()).add(10);

        Rectangle rectangle = new Rectangle();
        rectangle.setStroke(_boardBorderColor);
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
     */
    private void drawLinesAndNumbers(Rectangle rectangle) {

        _fontSize.bind(_boardSize.divide(25));
        this.styleProperty().bind(_fontStyle);

        // for each file and rank
        for (int i = 0; i < DIM; i++) {
            int index =_currentOrientation == orientation.WHITE_SOUTH ?  i+1 : DIM - i;

            // File letter - calculates the middle off the letter and adds half of the checker size
            Text fileLetter = _fileLetters[index-1]; //new Text(getFilesLetter(index));
            fileLetter.xProperty().bind(_offset_x.add(_checkerSize.multiply(i).subtract(_fontSize.multiply(0.4)).add(_checkerSize.multiply(0.5))));
            fileLetter.yProperty().bind(rectangle.heightProperty().add(10).add(_fontSize));
            this.getChildren().add(fileLetter);

            // Rank digit - position the digit in the middle of the rank
            Text rankDigit = _rankNumbers[index-1];
            rankDigit.xProperty().bind(_offset_x.subtract(_fontSize.multiply(0.4)).subtract(8));
            rankDigit.yProperty().bind(_offset_y.add(_checkerSize.multiply(DIM-i).add(_fontSize.multiply(0.3)).subtract(_checkerSize.multiply(0.5))));
            this.getChildren().add(rankDigit);

            // horizontal lines
            Line h_line =_hlines[index-1];
            h_line.setStroke(_boardGridColor);
            h_line.startXProperty().bind(_offset_x);
            h_line.endXProperty().bind(_offset_x.add(_boardSize));
            h_line.startYProperty().bind(_offset_y.add(_checkerSize.multiply(index)));
            h_line.endYProperty().bind(h_line.startYProperty());
            this.getChildren().add(h_line);

            // vertical lines
            Line v_line =_vlines[index-1];
            v_line.setStroke(_boardGridColor);
            v_line.startXProperty().bind(_offset_x.add(_checkerSize.multiply(index)));
            v_line.endXProperty().bind(v_line.startXProperty());
            v_line.startYProperty().bind(_offset_y);
            v_line.endYProperty().bind(_offset_y.add(_boardSize));
            this.getChildren().add(v_line);

            // border box
            Rectangle r = _rectangles[index-1];
            r.setStroke(_boardGridColor);
            r.setFill(Color.TRANSPARENT);
            r.xProperty().bind(_offset_x);
            r.yProperty().bind(_offset_y);
            r.widthProperty().bind(_boardSize);
            r.heightProperty().bind(_boardSize);
            this.getChildren().add(r);
        }
    }

    /**
     * marks the field of the last move and draws a line from the source to the target field
     * @param curBoard
     */
    private void markLastMove(GameBoard curBoard) {
        GameMove lastMove = curBoard.getLastMove();
        //GameMove lastMove = NotationHelper.createNewMoveFromSimpleNotation(_curBoard, "b1c3"); // TEST

        if (lastMove != null && _controller != null && _controller.isShowLastMove() ) {
            // from field
            int fromFile = _currentOrientation == orientation.WHITE_SOUTH ? lastMove.getFromField().getFile() : DIM - lastMove.getFromField().getFile()+1;
            int fromRank = _currentOrientation == orientation.WHITE_SOUTH ? lastMove.getFromField().getRank() : DIM - lastMove.getFromField().getRank()+1;
            //markField(fromFile, fromRank, _lastMoveColor);

            // to field
            int toFile = _currentOrientation == orientation.WHITE_SOUTH ? lastMove.getToField().getFile() : DIM - lastMove.getToField().getFile()+1;
            int toRank = _currentOrientation == orientation.WHITE_SOUTH ? lastMove.getToField().getRank() : DIM - lastMove.getToField().getRank()+1;
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

    /**
     * marks all fields the selected piece can move to
     * @param curBoard
     */
    private void markPossibleMoves(GameBoard curBoard) {
        //_selectedFromField = GamePosition.getGamePosition("e2");
        if (_controller != null && _controller.isShowPossibleMoves() && _selectedFromField != null
                && curBoard.getPiece(_selectedFromField).getColor().equals(curBoard.getNextPlayerColor())) {

            List<GameMove> moves = curBoard.getPiece(_selectedFromField).getLegalMovesForPiece(curBoard, _selectedFromField, false);
            moves.forEach(curMove -> {
                int file = _currentOrientation == orientation.WHITE_SOUTH ? curMove.getToField().getFile() : DIM - curMove.getToField().getFile()+1;
                int rank = _currentOrientation == orientation.WHITE_SOUTH ? curMove.getToField().getRank() : DIM - curMove.getToField().getRank()+1;
                markField(file, rank, _possibleMoveColor);
            });
        }
    }

    /**
     * if a king is in check we highlight the field
     * @param curBoard
     */
    private void markKingInCheckField(GameBoard curBoard) {
        if (curBoard != null && curBoard.hasCheck()) {
            GamePosition king;
            king = curBoard.getKingField(curBoard.getNextPlayerColor());
            int file = _currentOrientation == orientation.WHITE_SOUTH ? king.getFile() : DIM - king.getFile()+1;
            int rank = _currentOrientation == orientation.WHITE_SOUTH ? king.getRank() : DIM - king.getRank()+1;
            markField(file, rank, _checkColor);
        }
    }

    /**
     * marks the field of the currently selected piece
     * @param curBoard
     */
    private void markCurrentSelectedFromField(GameBoard curBoard) {
        //_selectedFromField = GamePosition.getGamePosition("e2");
        if (curBoard !=null && _selectedFromField != null) {
            int file = _currentOrientation == orientation.WHITE_SOUTH ? _selectedFromField.getFile() : DIM - _selectedFromField.getFile()+1;
            int rank = _currentOrientation == orientation.WHITE_SOUTH ? _selectedFromField.getRank() : DIM - _selectedFromField.getRank()+1;
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
     * Draw all pieces on the board
     * @param curBoard
     */
    private void drawPieces(GameBoard curBoard) {
        // do not reset the piece list if we drag
        if (_dragPiece == null) {
            // clear the pieces array
            _pieces.clear();
            //System.out.println("CLEAR =======================================================");
            // fill the pieces array
            for (int rank = DIM; rank > 0; rank--) {
                for (int file = 1; file <= DIM; file++) {
                    final GamePiece boardPiece = curBoard.getPiece(file, rank);
                    //System.out.print("Now: "+GamePosition.getGamePosition(file, rank)+" ");
                    final int file2 = _currentOrientation == orientation.WHITE_SOUTH ? file : DIM - file+1;
                    final int rank2 = _currentOrientation == orientation.WHITE_SOUTH ? rank : DIM - rank+1;
                    //System.out.print("flip: "+GamePosition.getGamePosition(file2, rank2));
                    if (boardPiece != null) {

                        // Create ImageView
                        Image pieceImage = getPieceImage(boardPiece.getColor(), boardPiece.getType());
                        ImageView pieceView = new ImageView(pieceImage);
                        pieceView.setPreserveRatio(true);
                        pieceView.setSmooth(true);

                        // here we position the ImageView (this depends on pane size as well)
                        pieceView.xProperty().bind(_offset_x.add(_checkerSize.multiply(file2-1)));
                        pieceView.yProperty().bind(_offset_y.add(_checkerSize.multiply(DIM-rank2)));
                        // here we bind ImageView size to checker size
                        pieceView.fitHeightProperty().bind(_checkerSize);
                        pieceView.fitWidthProperty().bind(_checkerSize);

                        Piece piece = new Piece(pieceView, this);
                        _pieces.add(piece);
                        //System.out.print(" >>>> add "+GamePosition.getGamePosition(file2, rank2
                        //        )+" as "+piece.getPosition());
                    }
                    //System.out.println();
                }
            }
        }
        _pieces.stream().forEach(Piece::addToNode);
    }

    /**
     * Displays a dialog to start a new game
     */
    private static GamePiece promotionDialog(GameColor color) {

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Pawn Promotion");
        alert.setHeaderText("Please select an officer piece.");
        alert.setContentText("Choose");
        ButtonType buttonTypeOne = new ButtonType("Queen");
        ButtonType buttonTypeTwo = new ButtonType("Rook");
        ButtonType buttonTypeThree = new ButtonType("Bishop");
        ButtonType buttonTypeFour = new ButtonType("Knight");
        alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree, buttonTypeFour);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.initStyle(StageStyle.UNDECORATED);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeOne){
            return Queen.create(color);
        } else if (result.get() == buttonTypeTwo) {
            return Rook.create(color);
        } else if (result.get() == buttonTypeThree) {
            return Bishop.create(color);
        } else if (result.get() == buttonTypeFour) {
            return Knight.create(color);
        } else {
            return Queen.create(color);
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
        String imageFolder = "/images/";
        _wK = new Image(Chessly.class.getResourceAsStream(imageFolder+"wK.png"));
        _wK = new Image(Chessly.class.getResourceAsStream(imageFolder+"wK.png"));
        _bK = new Image(Chessly.class.getResourceAsStream(imageFolder+"bK.png"));
        _wQ = new Image(Chessly.class.getResourceAsStream(imageFolder+"wQ.png"));
        _bQ = new Image(Chessly.class.getResourceAsStream(imageFolder+"bQ.png"));
        _wB = new Image(Chessly.class.getResourceAsStream(imageFolder+"wB.png"));
        _bB = new Image(Chessly.class.getResourceAsStream(imageFolder+"bB.png"));
        _wN = new Image(Chessly.class.getResourceAsStream(imageFolder+"wN.png"));
        _bN = new Image(Chessly.class.getResourceAsStream(imageFolder+"bN.png"));
        _wR = new Image(Chessly.class.getResourceAsStream(imageFolder+"wR.png"));
        _bR = new Image(Chessly.class.getResourceAsStream(imageFolder+"bR.png"));
        _wP = new Image(Chessly.class.getResourceAsStream(imageFolder+"wP.png"));
        _bP = new Image(Chessly.class.getResourceAsStream(imageFolder+"bP.png"));
    }

    /**
     * chooses pre-loaded Image from color and piecetype
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
     * Calculates the file and rank from the xy coordinates of the mouse event
     * @param event
     */
    private GamePosition getGamePositionFromMouseEvent(MouseEvent event) {
        if (Chessly.getPlayroom().getCurrentGame() == null || !Chessly.getPlayroom().getCurrentGame().isRunning()) {
            return null;
        }
        if (event.getButton() != MouseButton.PRIMARY) {
            return null;
        }
        return determinePosition(event.getX(), event.getY());
    }

    /**
     * Calculates the file and rank from the given xy coordinates
     * @param x
     * @param y
     * @return A Point object representing col and row on the board
     */
    private GamePosition determinePosition(double x, double y) {
        // -- outside of board --
        if (x < _offset_x.doubleValue() || x > _offset_x.add(_boardSize).doubleValue()
                || y > _offset_y.add(_boardSize).doubleValue()  || y < _offset_y.doubleValue()) return null;

        // calculate file and rank
        // +0.1 because of rounding issue - the fieled starts when x-_offset_x.doubleValue()==0
        int file = 1 + (int) ((x - _offset_x.doubleValue()+0.1) / _checkerSize.doubleValue());
        int rank = DIM - (int) ((y - _offset_y.doubleValue()+0.1) / _checkerSize.doubleValue());
        // flip board correction
        if (_currentOrientation == orientation.WHITE_NORTH) {
            file = DIM-file+1;
            rank = DIM-rank+1;
        }
        return GamePosition.getGamePosition(file, rank);
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

        // which field?
        if (_selectedFromField == null) {
            // no field selected -> select
            if (_curBoard.getPiece(pos) != null
                    && _curBoard.getNextPlayerColor().equals(_curBoard.getPiece(pos).getColor()) ) {

                _selectedFromField = pos;
                Platform.runLater(this::drawBoard);

            } else { // no piece on the chosen field - ignore click
                return;
            }
            _ignoreNextRelease = true;
        } else {
            _ignoreNextRelease = false;
        }
        //System.out.println("SelectedFrom: "+_selectedFromField);

        // which piece?
        for (Piece p : _pieces) {
            //System.out.print(p.getPosition()+" ");
            if (pos.equals(p.getPosition())) {
                //System.out.print("HIT ");
                // found piece
                _dragOffsetX = event.getX() - p.pieceView.xProperty().doubleValue();
                _dragOffsetY = event.getY() - p.pieceView.yProperty().doubleValue();
                _dragPiece = p;
                _dragPiece.isDragged=true;
            }
        }
        //System.out.println();
        //System.out.println(" DragPiece: "+(_dragPiece!=null ? _dragPiece.getPosition() : "NULL"));
    }

    /**
     * Invoked when a mouse drag event happens on the board
     * @param event
     */
    public void mouseDragged(MouseEvent event) {

        // if we did not press on a field with a piece we can't drag anything
        if (_dragPiece == null) return;

        // resets the helper for press/release events
        _ignoreNextRelease = false;

        // remove the piece to be dragged from the piece list to not display it any longer
        // when drawBoard is called
        _pieces.remove(_dragPiece);

        //System.out.println("Mouse DRAG: "+event);
        //System.out.println("Mouse DRAG: "+_dragPiece.pieceView);

        // Create copy of the dragged piece for displaying while dragging
        if (_dragPieceCopy == null) {
            Image pieceImage = _dragPiece.pieceView.getImage();
            ImageView pieceView = new ImageView(pieceImage);
            pieceView.setPreserveRatio(true);
            pieceView.setSmooth(true);
            _dragPieceCopy = new Piece(pieceView, this);
            // here we bind ImageView size to checker size
            _dragPieceCopy.pieceView.fitHeightProperty().bind(_checkerSize);
            _dragPieceCopy.pieceView.fitWidthProperty().bind(_checkerSize);
            _pieces.add(_dragPieceCopy);
            _dragPieceCopy.addToNode();
        }

        // here we position the piece according to the mouse drag movement
        // the offset avoid a jumping piece image if the click was not in the
        // 0,0 coordinated of the field
        _dragPieceCopy.pieceView.setX(event.getX() - _dragOffsetX);
        _dragPieceCopy.pieceView.setY(event.getY() - _dragOffsetY);

        // re draw the board to display the pieces including the copy of dragged piece
        Platform.runLater(this::drawBoard);
    }

    /**
     * Invoked when a mouse button has been released on a component.
     * @param event
     */
    public void mouseReleased(MouseEvent event) {

        // helps the press/release management
        if (_ignoreNextRelease) return;

        GamePosition pos = getGamePositionFromMouseEvent(event);

        //System.out.print("Mouse RELEASE: "+pos+" ---> ");
        // if we do not have a selection from a previous press/release cycle
        // ignore the release
        if (_selectedFromField == null) {
            return;
        }

        // mouse not on board or on same field - reset selection and drag and ignore the release
        if (pos==null || pos.equals(_selectedFromField)) {
            clearMouseSelection();
            Platform.runLater(this::drawBoard);
            return;
        }

        // from and to chosen
        GamePiece fromPiece = _curBoard.getPiece(_selectedFromField);
        GamePosition fromField = _selectedFromField;
        GamePosition toField = pos;

        //System.out.print("From: "+fromField+" To: "+toField+" DragPiece: "+_dragPiece.getPosition()+" ");
        GameMove m = new GameMoveImpl(fromField, toField, fromPiece);

        // check if move would legal - if not de-select and return
        if (!_curBoard.isLegalMove(m)) {
            clearMouseSelection();
            Platform.runLater(this::drawBoard);
            return;
        }

        // pawn promotion
        if (fromPiece instanceof Pawn) {
            if (fromPiece.isWhite() && _selectedFromField.getRank() == 7 && toField.getRank() == 8) {
                // Promotion
                m.setPromotedTo(promotionDialog(GameColor.WHITE));
            } else if (fromPiece.isBlack() && _selectedFromField.getRank() == 2 && toField.getRank() == 1) {
                // Promotion
                m.setPromotedTo(promotionDialog(GameColor.BLACK));
            }
        }

        // if the toField is occupied store captured piece to Move - could be illegal move
        if (_curBoard.getPiece(pos) != null)
            m.setCapturedPiece(_curBoard.getPiece(pos));

        // clear drag piece
        clearMouseSelection();

        // reset ignore flag
        _ignoreNextRelease = false;

        // now deliver the move to the player through the Controller
        _controller.setPlayerMove(m);

    }

    /**
     * Clears selected field and also drag piece
     */
    private void clearMouseSelection() {
        _selectedFromField = null;
        if (_dragPiece != null) {
            _dragPiece.isDragged=false;
        }
        _dragPiece = null;
        _dragPieceCopy = null;
    }

    /**
     * Represents an Piece with a ImageView
     */
    private class Piece {

        private final BoardPane node;
        private final ImageView pieceView;
        @SuppressWarnings("unused")
		private boolean isDragged;

        /**
         * @param pieceView
         * @param node
         */
        public Piece(ImageView pieceView, BoardPane node) {
            this.node = node;
            this.pieceView = pieceView;
            this.isDragged = false;
        }

        void addToNode() {
            node.getChildren().add(pieceView);
        }

        GamePosition getPosition() {
            //System.out.print(" Coords: "+pieceView.xProperty().intValue()+":"+pieceView.yProperty().intValue()+" ");
            return determinePosition(pieceView.xProperty().doubleValue(), pieceView.yProperty().doubleValue());
        }
    }

}
