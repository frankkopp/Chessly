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
 */package fko.chessly.game.pieces;

 import java.io.Serializable;

 import fko.chessly.game.GameBoard;
 import fko.chessly.game.GameBoardImpl;
 import fko.chessly.game.GameColor;
 import fko.chessly.game.GameMoveImpl;
 import fko.chessly.game.GameMoveList;
 import fko.chessly.game.GamePiece;
 import fko.chessly.game.GamePieceType;
 import fko.chessly.game.GamePosition;

 /**
  * @author Frank
  *
  */
 public class Pawn extends PieceAbstractImpl implements GamePiece, Serializable {

     private static final long serialVersionUID = 6979893115895070545L;

     private static final GamePieceType _pieceType = GamePieceType.PAWN;

     private static final int[][] blackPawnMoveVectors = { { -1, -1 }, { 1, -1 } };
     private static final int[][] whitePawnMoveVectors = { { -1,  1 }, { 1, 1 } };

     private static final Pawn whitePawn = new Pawn(GameColor.WHITE);
     private static final Pawn blackPawn = new Pawn(GameColor.BLACK);

     /**
      * @param color
      */
     private Pawn(GameColor color) {
         super(_pieceType, color);
     }

     /**
      * Returns a pawn object of the given color.
      * @param color
      * @return A reference to a Pawn object of the given color.
      */
     public static Pawn createPawn(GameColor color) {
         return color==GameColor.WHITE ? whitePawn : blackPawn;
     }

     /**
      * Generates Pawn moves. Attention!! Promotion generates several moves.
      *
      */
     @Override
     public GameMoveList getLegalMovesForPiece(GameBoard board,
             GamePosition fromPos, boolean capturingOnly) {

         GameMoveList allMoves = getPseudoLegalMovesForPiece(board, fromPos,
                 capturingOnly);
         return board.filterLegalMovesOnly(allMoves);
     }

     @Override
     public GameMoveList getPseudoLegalMovesForPiece(GameBoard board,
             GamePosition fromPos, boolean capturingOnly) {

         GameMoveList allMoves = new GameMoveList();

         GameColor pawnColor = board.getNextPlayerColor();
         switch (pawnColor) {
             case WHITE:
                 whitePawnMoves(board, fromPos, allMoves, capturingOnly);
                 break;
             case BLACK:
                 blackPawnMoves(board, fromPos, allMoves, capturingOnly);
                 break;
             default:
                 throw new IllegalArgumentException("No valid pawnColor");
         }
         return allMoves;
     }

     private void whitePawnMoves(GameBoard board, GamePosition fromPos,
             GameMoveList legalMoves, boolean capturingOnly) {

         GameColor pawnColor = GameColor.WHITE;

         // forward for white
         int direction = GameBoardImpl.WHITE_DIRECTION;
         int pawnBase = GameBoardImpl.WHITE_BASE_ROW + direction;
         int promotionRow = GameBoardImpl.BLACK_BASE_ROW;

         if (!capturingOnly) {
             // allow 1 field forward
             forwardOneField(board, fromPos, legalMoves, pawnColor, direction,
                     promotionRow);

             // if on base line allow 2 fields
             forwardTwoFields(board, fromPos, legalMoves, direction, pawnBase);
         }

         // attack 1 diagonal
         int[][] deltas = whitePawnMoveVectors;
         attackMoves(board, fromPos, legalMoves, pawnColor, promotionRow, deltas);
     }

     private void blackPawnMoves(GameBoard board, GamePosition fromPos,
             GameMoveList legalMoves, boolean capturingOnly) {

         GameColor pawnColor = GameColor.BLACK;

         // forward for black
         int direction = GameBoardImpl.BLACK_DIRECTION;
         int pawnBase = GameBoardImpl.BLACK_BASE_ROW + direction;
         int promotionRow = GameBoardImpl.WHITE_BASE_ROW;

         if (!capturingOnly) {
             // allow 1 field forward
             forwardOneField(board, fromPos, legalMoves, pawnColor, direction,
                     promotionRow);

             // if on base line allow 2 fields
             forwardTwoFields(board, fromPos, legalMoves, direction, pawnBase);
         }

         // attack 1 diagonal
         int[][] deltas = blackPawnMoveVectors;
         attackMoves(board, fromPos, legalMoves, pawnColor, promotionRow, deltas);
     }

     private void forwardOneField(GameBoard board, GamePosition fromPos,
             GameMoveList legalMoves, GameColor pawnColor, int direction,
             int promotionRow) {

         int new_row = fromPos.y + direction;
         if (board.getPiece(fromPos.x, new_row) == null) {
             final GamePosition newPos = GamePosition.getGamePosition(fromPos.x, new_row);
             GameMoveImpl m = new GameMoveImpl(fromPos, newPos, this);
             if (new_row == promotionRow) {
                 m.setPromotedTo(Queen.createQueen(pawnColor));
                 legalMoves.add(m);
                 m = new GameMoveImpl(fromPos, newPos, this);
                 m.setPromotedTo(Rook.createRook(pawnColor));
                 legalMoves.add(m);
                 m = new GameMoveImpl(fromPos, newPos, this);
                 m.setPromotedTo(Bishop.createBishop(pawnColor));
                 legalMoves.add(m);
                 m = new GameMoveImpl(fromPos, newPos, this);
                 m.setPromotedTo(Knight.createKnight(pawnColor));
                 legalMoves.add(m);
             } else {
                 legalMoves.add(m);
             }

         }
     }

     private void forwardTwoFields(GameBoard board, GamePosition fromPos,
             GameMoveList legalMoves, int direction, int pawnBase) {

         if (fromPos.y == pawnBase) {
             int new_row = fromPos.y + (2 * direction);
             if (board.getPiece(fromPos.x, pawnBase + direction) == null
                     && board.getPiece(fromPos.x, new_row) == null) {
                 final GameMoveImpl m = new GameMoveImpl(fromPos,
                         GamePosition.getGamePosition(fromPos.x, new_row), this);
                 m.setEnPassantNextMovePossible(true);
                 // TODO: we could set en passant position here but this will be done later
                 // when move is actually done as it would require creating
                 // a new position object every time here.
                 legalMoves.add(m);
             }
         }
     }

     private void attackMoves(GameBoard board, GamePosition fromPos,
             GameMoveList legalMoves, GameColor pawnColor, int promotionRow,
             int[][] deltas) {

         for (int i = 0; i < deltas.length; i++) {
             int col_inc = deltas[i][0];
             int row_inc = deltas[i][1];
             int new_col = fromPos.x + col_inc;
             int new_row = fromPos.y + row_inc;
             GamePosition newPos = GamePosition.getGamePosition(new_col, new_row);

             if (board.isWithinBoard(newPos)) {
                 if (board.getPiece(new_col, new_row) != null) {
                     if (board.getPiece(new_col, new_row).getColor()
                             .equals(this._color.getInverseColor())) {
                         GameMoveImpl m = new GameMoveImpl(fromPos, newPos, this);
                         if (newPos.y == promotionRow) {
                             m.setPromotedTo(Queen.createQueen(pawnColor));
                             m.setCapturedPiece(board.getPiece(new_col, new_row));
                             legalMoves.add(m);
                             m = new GameMoveImpl(fromPos, newPos, this);
                             m.setPromotedTo(Rook.createRook(pawnColor));
                             m.setCapturedPiece(board.getPiece(new_col, new_row));
                             legalMoves.add(m);
                             m = new GameMoveImpl(fromPos, newPos, this);
                             m.setPromotedTo(Bishop.createBishop(pawnColor));
                             m.setCapturedPiece(board.getPiece(new_col, new_row));
                             legalMoves.add(m);
                             m = new GameMoveImpl(fromPos, newPos, this);
                             m.setPromotedTo(Knight.createKnight(pawnColor));
                             m.setCapturedPiece(board.getPiece(new_col, new_row));
                             legalMoves.add(m);
                         } else {
                             m.setCapturedPiece(board.getPiece(new_col, new_row));
                             legalMoves.add(m);
                         }
                     }
                 } else { // en passant
                     if (board.hasEnPassantCapturable()
                             && board.getEnPassantCapturable().x == new_col
                             && board.getEnPassantCapturable().y == fromPos.y) {
                         final GameMoveImpl m = new GameMoveImpl(fromPos, newPos, this);
                         m.setCapturedPiece(board.getPiece(new_col, fromPos.y));
                         m.setWasEnPassantCapture(true);
                         legalMoves.add(m);
                     }
                 }
             }
         }
     }

 }

