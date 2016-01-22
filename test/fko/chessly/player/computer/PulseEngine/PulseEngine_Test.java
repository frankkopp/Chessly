package fko.chessly.player.computer.PulseEngine;

import org.junit.BeforeClass;
import org.junit.Test;

import fko.chessly.Chessly;
import fko.chessly.game.Game;
import fko.chessly.game.GameBoard;
import fko.chessly.game.GameBoardImpl;
import fko.chessly.game.GameColor;
import fko.chessly.game.GameMove;
import fko.chessly.game.NotationHelper;
import fko.chessly.player.Player;
import fko.chessly.player.PlayerFactory;
import fko.chessly.player.PlayerType;

@SuppressWarnings("javadoc")
public class PulseEngine_Test {

    private static Game _myGame;
    private static GameBoard _myBoard;

    private static PulseEngine_v2 _myEngine;
    private static Player _playerBlack;
    private static Player _playerWhite;
    private static Player _player1;
    private static Player _player2;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        _myEngine = new PulseEngine_v2();

        _playerBlack = createPlayer(GameColor.BLACK);
        _playerWhite = createPlayer(GameColor.WHITE);
        _myGame = new Game(_playerWhite, _playerBlack, 60000, 60000, false);
        Chessly.getPlayroom().setCurrentLevelBlack(99);

        _playerBlack.startPlayer(_myGame);
        _playerWhite.startPlayer(_myGame);

        _player1 = _playerBlack;
        _player2 = _playerWhite;
    }

    /**
     * Calls the search for the best move.
     */
    @Test
    public void testGetNextMove() {

        Chessly.getPlayroom().setCurrentLevelBlack(2);

        /*
         * 1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - - bm Qd1+; id "BK.01";
         * 3r1k2/4npp1/1ppr3p/p6P/P2PPPP1/1NR5/5K2/2R5 w - - bm d5; id "BK.02";
         * 2q1rr1k/3bbnnp/p2p1pp1/2pPp3/PpP1P1P1/1P2BNNP/2BQ1PRK/7R b - - bm f5; id "BK.03";
         * rnbqkb1r/p3pppp/1p6/2ppP3/3N4/2P5/PPP1QPPP/R1B1KB1R w KQkq - bm e6; id "BK.04";
         * r1b2rk1/2q1b1pp/p2ppn2/1p6/3QP3/1BN1B3/PPP3PP/R4RK1 w - - bm Nd5 a4; id "BK.05";
         * r3qb1k/1b4p1/p2pr2p/3n4/Pnp1N1N1/6RP/1B3PP1/1B1QR1K1 w - - 0 1 Nxh6
         * 8/7K/8/8/8/8/R7/7k w - - 0 1 Mate in 8
         * 4R1kr/6p1/1p6/p4pNp/4pn2/P1N1b1PP/1PP1P1B1/7K b - - 0 25
         * 3Rb1kr/6p1/1p6/p3npNp/4p3/P1N1b1PP/1PP1P1B1/7K b - - 0 24
         * 2rr3k/pp3pp1/1nnqbN1p/3pN3/2pP4/2P3Q1/PPB4P/R4RK1 w - - bm g3g6;
         * 8/7p/5k2/5p2/p1p2P2/Pr1pPK2/1P1R3P/8 b - - bm b3b2;
         * 5rk1/1ppb3p/p1pb4/6q1/3P1p1r/2P1R2P/PP1BQ1P1/5RKN w - - bm e3g3;
         * r1bq2rk/pp3pbp/2p1p1pQ/7P/3P4/2PB1N2/PP3PPR/2KR4 w - - bm h6h7;
         * 5k2/6pp/p1qN4/1p1p4/3P4/2PKP2Q/PP3r2/3R4 b - - bm c6c4;
         * 7k/p7/1R5K/6r1/6p1/6P1/8/8 w - - bm b6b7;
         * rnbqkb1r/pppp1ppp/8/4P3/6n1/7P/PPPNPPP1/R1BQKBNR b KQkq - bm g4e3;id WAC007;
         * r4q1k/p2bR1rp/2p2Q1N/5p2/5p2/2P5/PP3PPP/R5K1 w - - bm e7f7;id WAC008;
         * 3q1rk1/p4pp1/2pb3p/3p4/6Pr/1PNQ4/P1PB1PP1/4RRK1 b - - bm d6h2;id WAC009;
         * 2br2k1/2q3rn/p2NppQ1/2p1P3/Pp5R/4P3/1P3PPP/3R2K1 w - - bm h4h7;id WAC010;
         * 8/5p1p/1p2pPk1/p1p1P3/P1P1K2b/4B3/1P5P/8 w - - bm b2b4; id BS2830; FAIL
         * r3kb1r/3n1pp1/p6p/2pPp2q/Pp2N3/3B2PP/1PQ2P2/R3K2R w KQkq - id LCT2 (POS-1); bm d5d6;
         * 3B4/8/2B5/1K6/8/8/3p4/3k4 w - - bm Ka6; id "BT2450.05"; c0 "Only winning move for white, Ka6 gewinnt";
         *
         * 2r1k2r/1pq2ppp/3b4/p2ppb2/3n4/P2P4/1P3PPP/1RBKQBNR w k -
         */
        String fen = "2r1k2r/1pq2ppp/3b4/p2ppb2/3n4/P2P4/1P3PPP/1RBKQBNR w k -";

        Chessly.getPlayroom().setCurrentLevelWhite(6);
        Chessly.getPlayroom().setCurrentLevelBlack(4);

        _myBoard = new GameBoardImpl(fen);
        System.out.println(_myBoard);

        GameMove move = _playerWhite.getNextMove(_myBoard);

        System.out.println();
        System.out.println(_myBoard.toFEN());
        System.out.format("Move: %s%n", move);

    }

    /**
     * Calls the search trying the pondering.
     */
    //@Test
    public void testPondering() {

        Chessly.getPlayroom().setCurrentLevelWhite(7);
        Chessly.getPlayroom().setCurrentLevelBlack(7);

        _player1 = _playerWhite;
        _player2 = _playerBlack;

        _myBoard = new GameBoardImpl(NotationHelper.StandardBoardFEN);
        System.out.println(_myBoard);

        GameMove move = _player1.getNextMove(_myBoard);
        _myBoard.makeMove(move);

        move = _player2.getNextMove(_myBoard);
        _myBoard.makeMove(move);

        move = _player1.getNextMove(_myBoard);
        _myBoard.makeMove(move);

        // sleep
        try { Thread.sleep(5000);} catch (InterruptedException e) { e.printStackTrace();}

        System.out.println(_myBoard);
        System.out.println("Test End");

    }

    private static Player createPlayer(GameColor color) {
        final Player newPlayer;
        try {
            if (color==GameColor.BLACK) {
                newPlayer = PlayerFactory.createPlayer(PlayerType.COMPUTER, "BLACK", GameColor.BLACK);
            } else {
                newPlayer = PlayerFactory.createPlayer(PlayerType.COMPUTER, "WHITE", GameColor.WHITE);
            }
        } catch (PlayerFactory.PlayerCreationException e) {
            throw new RuntimeException("Error creating player.",e);
        }
        return newPlayer;
    }

}
