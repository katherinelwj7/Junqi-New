package test;

import game.Game;
import piece.Piece;
import piece.Team;
import piece.Rank;
import board.TileType;
import board.ConnectionType;
import game.GameState;
import board.Board;
import board.Tile;

public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void runAllTests() {
        System.out.println("Running tests...");

        testGeneralCapturesBrigade();
        testRailwayLongDistanceMove();
        testMiddleRailwayMoveWorks();
        testRoadOrthogonalMove();
        testRoadDiagonalMove();
        testEngineerTurnsOnRailway();
        testRoadBlockedEngineerFailsTurning();
        testMergeSuccess();
        testMergeRanks();
        testMergeDifferentRankFails();
        testSameRankDifferentTeamBattlesNotMerge();
        testMergeBrigadeFails();
        testSplitSuccess();
        testSplitCannotUseLongRailway();
        testSplitIntoOccupiedCampAndMerge();
        testSplitGeneralFails();
        testSplitOntoOwnDifferentRankFails();
        testSplitOntoEnemyBattles();
        testInvalidPiecePlacement();
        testInvalidMapBasedMovement();
        testGeneralDeathRevealsFlag();
        testGameStateRules();
        testWinByCapturingFlag();
        testAgreeDraw();
        testDrawAfter70Moves();
        testTurnSystem();
        testNewRecruitPromotion();

        System.out.println();
        System.out.println("All tests finished.");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
    }


    // Movement tests


    private static void testGeneralCapturesBrigade() {
        System.out.println("Test: GENERAL captures BRIGADE");

        Game game = new Game();

        Piece general = new Piece(Rank.GENERAL, Team.RED);
        Piece brigade = new Piece(Rank.BRIGADE, Team.BLUE);

        game.placePiece(6, 0, general);
        game.placePiece(5, 0, brigade);

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        boolean success = game.move(6, 0, 5, 0);

        assertTrue(success, "Move should succeed");

        Piece result = game.getBoard().getPiece(5, 0);

        assertNotNull(result, "Target square should contain a piece");
        assertEquals(Rank.GENERAL, result.rank, "GENERAL should remain on target square");
        assertEquals(Team.RED, result.team, "Winning piece should belong to RED");

        Piece originalSquare = game.getBoard().getPiece(6, 0);
        assertNull(originalSquare, "Original square should be empty");

        System.out.println("Passed: GENERAL captures BRIGADE");
    }

    private static void testRailwayLongDistanceMove() {
        Game game = new Game();

        Piece company = new Piece(Rank.COMPANY, Team.RED);
        Piece platoon = new Piece(Rank.PLATOON, Team.BLUE);

        game.forcePlacePieceForTesting(8, 4, company);
        game.forcePlacePieceForTesting(3, 4, platoon);

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        boolean success = game.move(8, 4, 3, 4);

        assertTrue(success, "Railway long distance move should succeed");

        Piece result = game.getBoard().getPiece(3, 4);
        assertNotNull(result, "Target should contain winning piece");
        assertEquals(Rank.COMPANY, result.rank, "COMPANY should capture PLATOON");
        assertEquals(Team.RED, result.team, "Winning piece should be RED");
        assertNull(game.getBoard().getPiece(8, 4 ), "Original square should be empty");

        pass("Railway long distance move");
    }

    private static void testMiddleRailwayMoveWorks() {
        Game game = new Game();

        game.forcePlacePieceForTesting(6, 2,
                new Piece(Rank.COMPANY, Team.RED));

        game.forcePlacePieceForTesting(0, 0,
                new Piece(Rank.SQUAD, Team.BLUE));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        boolean success = game.move(6, 2, 5, 2);

        assertTrue(success, "Middle railway move should succeed");
        assertPiece(game, 5, 2, Rank.COMPANY, Team.RED);
        assertNull(game.getBoard().getPiece(6, 2),
                "Original square should be empty after middle railway move");

        pass("Middle railway move works");
    }

    private static void testRoadOrthogonalMove() {
        Game game = new Game();

        game.forcePlacePieceForTesting(6, 3, new Piece(Rank.ENGINEER, Team.RED));
        // 防止BLUE无可移动棋子直接判负
        game.forcePlacePieceForTesting(0, 0, new Piece(Rank.SQUAD, Team.BLUE));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        boolean success = game.move(6, 3, 7, 3);

        assertTrue(success, "Orthogonal road move should succeed");
        assertPiece(game, 7, 3, Rank.ENGINEER, Team.RED);
        assertNull(game.getBoard().getPiece(6, 3), "Original square should be empty");

        pass("Road orthogonal move");
    }

    private static void testRoadDiagonalMove() {
        Game game = new Game();

        game.forcePlacePieceForTesting(1, 0,
                new Piece(Rank.REGIMENT, Team.BLUE));

        game.forcePlacePieceForTesting(11, 0,
                new Piece(Rank.SQUAD, Team.RED));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.BLUE);

        boolean success = game.move(1, 0, 2, 1);

        assertTrue(success, "Diagonal road move should succeed");
        assertPiece(game, 2, 1, Rank.REGIMENT, Team.BLUE);
        assertNull(game.getBoard().getPiece(1, 0), "Original square should be empty");

        pass("Road diagonal move");
    }

    private static void testEngineerTurnsOnRailway() {
        Game game = new Game();

        game.forcePlacePieceForTesting(10, 3,
                new Piece(Rank.ENGINEER, Team.RED));

        game.forcePlacePieceForTesting(0, 4,
                new Piece(Rank.SQUAD, Team.BLUE));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        boolean success = game.move(10, 3, 1, 1);

        assertTrue(success, "Engineer should be able to turn on railway");
        assertPiece(game, 1, 1, Rank.ENGINEER, Team.RED);
        assertNull(game.getBoard().getPiece(10, 3), "Original square should be empty");

        pass("Engineer railway turning");
    }

    private static void testRoadBlockedEngineerFailsTurning() {
        Game game = new Game();

        game.forcePlacePieceForTesting(10, 3,
                new Piece(Rank.ENGINEER, Team.RED));

        game.forcePlacePieceForTesting(0, 4,
                new Piece(Rank.SQUAD, Team.BLUE));

        game.forcePlacePieceForTesting(6, 0,
                new Piece(Rank.SQUAD, Team.RED));

        game.forcePlacePieceForTesting(6, 4,
                new Piece(Rank.PLATOON, Team.RED));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        boolean success = game.move(10, 3, 1, 1);

        assertFalse(success, "Engineer should be blocked");
        assertPiece(game, 10, 3, Rank.ENGINEER, Team.RED);
        assertNull(game.getBoard().getPiece(1, 1), "Target should be empty because road blocked");

        pass("Engineer fails turning because road is blocked");
    }

    // Merge tests


    private static void testMergeSuccess() {
        Game game = new Game();

        game.forcePlacePieceForTesting(0, 0,
                new Piece(Rank.REGIMENT, Team.BLUE));

        game.forcePlacePieceForTesting(0, 1,
                new Piece(Rank.REGIMENT, Team.BLUE));

        game.forcePlacePieceForTesting(11, 0,
                new Piece(Rank.SQUAD, Team.RED));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.BLUE);

        boolean success = game.move(0, 0, 0, 1);

        assertTrue(success, "Same-rank same-team merge should succeed");
        assertPiece(game, 0, 1, Rank.BRIGADE, Team.BLUE);
        assertNull(game.getBoard().getPiece(0, 0), "Original square should be empty");

        pass("Merge success");
    }

    private static void testMergeRanks() {
        Game game = new Game();

        game.forcePlacePieceForTesting(0, 0,
                new Piece(Rank.ENGINEER, Team.BLUE));
        game.forcePlacePieceForTesting(1, 0,
                new Piece(Rank.ENGINEER, Team.BLUE));

        game.forcePlacePieceForTesting(2, 0,
                new Piece(Rank.NEW_RECRUIT, Team.BLUE));
        game.forcePlacePieceForTesting(3, 0,
                new Piece(Rank.NEW_RECRUIT, Team.BLUE));

        game.forcePlacePieceForTesting(4, 0,
                new Piece(Rank.PRIVATE, Team.BLUE));
        game.forcePlacePieceForTesting(5, 0,
                new Piece(Rank.PRIVATE, Team.BLUE));

        game.forcePlacePieceForTesting(0, 4,
                new Piece(Rank.SQUAD, Team.BLUE));
        game.forcePlacePieceForTesting(1, 4,
                new Piece(Rank.SQUAD, Team.BLUE));

        game.forcePlacePieceForTesting(11, 0,
                new Piece(Rank.PLATOON, Team.RED));
        game.forcePlacePieceForTesting(10, 0,
                new Piece(Rank.PLATOON, Team.RED));

        game.forcePlacePieceForTesting(9, 0,
                new Piece(Rank.COMPANY, Team.RED));
        game.forcePlacePieceForTesting(8, 0,
                new Piece(Rank.COMPANY, Team.RED));

        game.forcePlacePieceForTesting(7, 0,
                new Piece(Rank.BATTALION, Team.RED));
        game.forcePlacePieceForTesting(6, 0,
                new Piece(Rank.BATTALION, Team.RED));

        game.forcePlacePieceForTesting(11, 4,
                new Piece(Rank.REGIMENT, Team.RED));
        game.forcePlacePieceForTesting(10, 4,
                new Piece(Rank.REGIMENT, Team.RED));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.BLUE);

        boolean success = game.move(0, 0, 1, 0);

        assertTrue(success, "Same-rank same-team merge should succeed");
        assertPiece(game, 1, 0, Rank.PRIVATE, Team.BLUE);
        assertNull(game.getBoard().getPiece(0, 0), "Original square should be empty");

        success = game.move(11, 0, 10, 0);
        assertTrue(success, "Same-rank same-team merge should succeed");
        assertPiece(game, 10, 0, Rank.COMPANY, Team.RED);
        assertNull(game.getBoard().getPiece(11, 0), "Original square should be empty");

        success = game.move(2, 0, 3, 0);
        assertTrue(success, "Same-rank same-team merge should succeed");
        assertPiece(game, 3, 0, Rank.PRIVATE, Team.BLUE);
        assertNull(game.getBoard().getPiece(2, 0), "Original square should be empty");

        success = game.move(9, 0, 8, 0);
        assertTrue(success, "Same-rank same-team merge should succeed");
        assertPiece(game, 8, 0, Rank.BATTALION, Team.RED);
        assertNull(game.getBoard().getPiece(9, 0), "Original square should be empty");

        success = game.move(4, 0, 5, 0);
        assertTrue(success, "Same-rank same-team merge should succeed");
        assertPiece(game, 5, 0, Rank.SQUAD, Team.BLUE);
        assertNull(game.getBoard().getPiece(4, 0), "Original square should be empty");

        success = game.move(7, 0, 6, 0);
        assertTrue(success, "Same-rank same-team merge should succeed");
        assertPiece(game, 6, 0, Rank.REGIMENT, Team.RED);
        assertNull(game.getBoard().getPiece(7, 0), "Original square should be empty");

        success = game.move(0, 4, 1, 4);
        assertTrue(success, "Same-rank same-team merge should succeed");
        assertPiece(game, 1, 4, Rank.PLATOON, Team.BLUE);
        assertNull(game.getBoard().getPiece(0, 4), "Original square should be empty");

        success = game.move(11, 4, 10, 4);
        assertTrue(success, "Same-rank same-team merge should succeed");
        assertPiece(game, 10, 4, Rank.BRIGADE, Team.RED);
        assertNull(game.getBoard().getPiece(11, 4), "Original square should be empty");

        pass("Merge Ranks");
    }

    private static void testMergeDifferentRankFails() {
        Game game = new Game();

        game.forcePlacePieceForTesting(0, 0,
                new Piece(Rank.BATTALION, Team.BLUE));

        game.forcePlacePieceForTesting(0, 1,
                new Piece(Rank.COMPANY, Team.BLUE));

        game.forcePlacePieceForTesting(11, 0,
                new Piece(Rank.SQUAD, Team.RED));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.BLUE);

        boolean success = game.move(0, 0, 0, 1);

        assertFalse(success, "Different-rank merge should fail");
        assertPiece(game, 0, 0, Rank.BATTALION, Team.BLUE);
        assertPiece(game, 0, 1, Rank.COMPANY, Team.BLUE);

        pass("Merge different rank fails");
    }

    private static void testSameRankDifferentTeamBattlesNotMerge() {
        Game game = new Game();

        game.forcePlacePieceForTesting(6, 0,
                new Piece(Rank.REGIMENT, Team.RED));

        game.forcePlacePieceForTesting(5, 0,
                new Piece(Rank.REGIMENT, Team.BLUE));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        boolean success = game.move(6, 0, 5, 0);

        assertTrue(success, "Same-rank different-team move should be a valid battle");
        assertNull(game.getBoard().getPiece(6, 0), "Original square should be empty");
        assertNull(game.getBoard().getPiece(5, 0), "Same-rank enemies should both disappear");

        pass("Same rank different team battles");
    }

    private static void testMergeBrigadeFails() {
        Game game = new Game();

        game.forcePlacePieceForTesting(0, 0,
                new Piece(Rank.BRIGADE, Team.BLUE));

        game.forcePlacePieceForTesting(0, 1,
                new Piece(Rank.BRIGADE, Team.BLUE));

        game.forcePlacePieceForTesting(11, 0,
                new Piece(Rank.SQUAD, Team.RED));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.BLUE);

        boolean success = game.move(0, 0, 0, 1);

        assertFalse(success, "BRIGADE + BRIGADE should not merge into GENERAL");
        assertPiece(game, 0, 0, Rank.BRIGADE, Team.BLUE);
        assertPiece(game, 0, 1, Rank.BRIGADE, Team.BLUE);

        pass("Merge BRIGADE fails");
    }


    // Split tests


    private static void testSplitSuccess() {
        Game game = new Game();

        game.forcePlacePieceForTesting(2, 0,
                new Piece(Rank.BRIGADE, Team.BLUE));

        game.forcePlacePieceForTesting(11, 0,
                new Piece(Rank.SQUAD, Team.RED));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.BLUE);

        boolean success = game.split(2, 0, 2, 1);

        assertTrue(success, "BRIGADE should split successfully");
        assertPiece(game, 2, 0, Rank.REGIMENT, Team.BLUE);
        assertPiece(game, 2, 1, Rank.REGIMENT, Team.BLUE);

        pass("Split success");
    }

    private static void testSplitCannotUseLongRailway() {
        Game game = new Game();

        game.forcePlacePieceForTesting(8, 4,
                new Piece(Rank.BRIGADE, Team.RED));

        game.forcePlacePieceForTesting(0, 0,
                new Piece(Rank.SQUAD, Team.BLUE));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        boolean success = game.split(8, 4, 3, 4);

        assertFalse(success, "Split should not be able to use long railway movement");
        assertPiece(game, 8, 4, Rank.BRIGADE, Team.RED);
        assertNull(game.getBoard().getPiece(3, 4), "Long-distance target should remain empty");

        pass("Split cannot use long railway");
    }

    private static void testSplitIntoOccupiedCampAndMerge() {
        Game game = new Game();

        // BRIGADE split 后 movingHalf 是 REGIMENT
        game.forcePlacePieceForTesting(2, 0,
                new Piece(Rank.BRIGADE, Team.BLUE));

        // 行营里已有己方 REGIMENT
        game.forcePlacePieceForTesting(2, 1,
                new Piece(Rank.REGIMENT, Team.BLUE));

        game.forcePlacePieceForTesting(11, 0,
                new Piece(Rank.GENERAL, Team.RED));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.BLUE);

        boolean success = game.split(2, 0, 2, 1);

        assertTrue(success, "Split into occupied camp should succeed if it can merge");
        assertPiece(game, 2, 0, Rank.REGIMENT, Team.BLUE);
        assertPiece(game, 2, 1, Rank.BRIGADE, Team.BLUE);

        pass("Split into occupied camp and merge");
    }

    private static void testSplitGeneralFails() {
        Game game = new Game();

        game.forcePlacePieceForTesting(0, 0,
                new Piece(Rank.GENERAL, Team.BLUE));

        game.forcePlacePieceForTesting(11, 0,
                new Piece(Rank.SQUAD, Team.RED));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.BLUE);

        boolean success = game.split(0, 0, 0, 1);

        assertFalse(success, "GENERAL should not be able to split");
        assertPiece(game, 0, 0, Rank.GENERAL, Team.BLUE);
        assertNull(game.getBoard().getPiece(0, 1), "Target should stay empty");

        pass("Split GENERAL fails");
    }

    private static void testSplitOntoOwnDifferentRankFails() {
        Game game = new Game();

        game.forcePlacePieceForTesting(0, 0,
                new Piece(Rank.BRIGADE, Team.BLUE));

        game.forcePlacePieceForTesting(1, 0,
                new Piece(Rank.PRIVATE, Team.BLUE));

        game.forcePlacePieceForTesting(11, 0,
                new Piece(Rank.SQUAD, Team.RED));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.BLUE);

        boolean success = game.split(0, 0, 1, 0);

        assertFalse(success, "Split onto teammate should fail if movingHalf cannot merge with target");
        assertPiece(game, 0, 0, Rank.BRIGADE, Team.BLUE);
        assertPiece(game, 1, 0, Rank.PRIVATE, Team.BLUE);

        pass("Split onto own different rank fails");
    }

    private static void testSplitOntoEnemyBattles() {
        Game game = new Game();

        game.forcePlacePieceForTesting(0, 0,
                new Piece(Rank.BRIGADE, Team.BLUE));

        game.forcePlacePieceForTesting(1, 0,
                new Piece(Rank.PRIVATE, Team.RED));

        game.forcePlacePieceForTesting(11, 0,
                new Piece(Rank.SQUAD, Team.RED));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.BLUE);

        boolean success = game.split(0, 0, 1, 0);

        assertTrue(success, "Split onto enemy should battle");
        assertPiece(game, 0, 0, Rank.REGIMENT, Team.BLUE);
        assertPiece(game, 1, 0, Rank.REGIMENT, Team.BLUE);

        pass("Split onto enemy battles");
    }

    // Special rule tests


    private static void testInvalidPiecePlacement() {
        Game game = new Game();

        boolean invalidFlag = game.placePiece(7, 2,
                new Piece(Rank.FLAG, Team.RED));

        assertFalse(invalidFlag, "FLAG should not be placed on normal tile");
        assertNull(game.getBoard().getPiece(7, 2), "Invalid FLAG placement should not change board");

        boolean validFlag = game.placePiece(11, 1,
                new Piece(Rank.FLAG, Team.RED));

        assertTrue(validFlag, "FLAG should be allowed in BASE");
        assertPiece(game, 11, 1, Rank.FLAG, Team.RED);

        boolean invalidMine = game.placePiece(8, 0,
                new Piece(Rank.MINE, Team.RED));

        assertFalse(invalidMine, "MINE should not be placed outside last two rows");
        assertNull(game.getBoard().getPiece(8, 0), "Invalid MINE placement should not change board");

        boolean validMine = game.placePiece(10, 0,
                new Piece(Rank.MINE, Team.RED));

        assertTrue(validMine, "MINE should be allowed in last two rows");
        assertPiece(game, 10, 0, Rank.MINE, Team.RED);

        boolean invalidBomb = game.placePiece(5, 4,
                new Piece(Rank.BOMB, Team.BLUE));

        assertFalse(invalidBomb, "BOMB should not be placed on first row");
        assertNull(game.getBoard().getPiece(5, 4), "Invalid BOMB placement should not change board");

        boolean validBomb = game.placePiece(4, 4,
                new Piece(Rank.BOMB, Team.BLUE));

        assertTrue(validBomb, "BOMB should be allowed outside first row");
        assertPiece(game, 4, 4, Rank.BOMB, Team.BLUE);

        pass("Invalid piece placement");
    }

    private static void testInvalidMapBasedMovement() {
        Game game = new Game();

        // BASE 里的棋子不能移动
        game.forcePlacePieceForTesting(11, 3,
                new Piece(Rank.PRIVATE, Team.RED));

        game.forcePlacePieceForTesting(0, 0,
                new Piece(Rank.GENERAL, Team.BLUE));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        boolean baseMove = game.move(11, 3, 11, 4);

        assertFalse(baseMove, "Piece in BASE should not move");
        assertPiece(game, 11, 3, Rank.PRIVATE, Team.RED);

        // 行营已有棋子，敌方不能进入攻击
        Game game2 = new Game();

        game2.forcePlacePieceForTesting(7, 1,
                new Piece(Rank.REGIMENT, Team.RED));

        game2.forcePlacePieceForTesting(7, 0,
                new Piece(Rank.GENERAL, Team.BLUE));

        game2.startGame();
        game2.setCurrentTurnForTesting(Team.BLUE);

        boolean enterOccupiedCamp = game2.move(7, 0, 7, 1);

        assertFalse(enterOccupiedCamp, "Cannot enter occupied CAMP to attack");
        assertPiece(game2, 7, 1, Rank.REGIMENT, Team.RED);
        assertPiece(game2, 7, 0, Rank.GENERAL, Team.BLUE);

        pass("Invalid map-based movement");
    }

    private static void testGeneralDeathRevealsFlag() {
        Game game = new Game();

        Piece blueFlag = new Piece(Rank.FLAG, Team.BLUE);

        game.forcePlacePieceForTesting(1, 0,
                new Piece(Rank.GENERAL, Team.BLUE));

        game.forcePlacePieceForTesting(7, 0,
                new Piece(Rank.BOMB, Team.RED));

        game.forcePlacePieceForTesting(0, 3, blueFlag);

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        boolean success = game.move(7, 0, 1, 0);

        assertTrue(success, "Bomb should be able to attack GENERAL");
        assertTrue(blueFlag.isRevealed(), "BLUE flag should be revealed after BLUE GENERAL dies");
        assertNull(game.getBoard().getPiece(1, 0), "Bomb and GENERAL should both disappear");

        pass("General death reveals flag");
    }

    private static void testGameStateRules() {
        Game game = new Game();

        game.forcePlacePieceForTesting(10, 0,
                new Piece(Rank.GENERAL, Team.RED));

        game.forcePlacePieceForTesting(1, 0,
                new Piece(Rank.GENERAL, Team.BLUE));

        assertEquals(GameState.SETUP, game.getState(), "Initial state should be SETUP");

        boolean moveBeforeStart = game.move(10, 0, 9, 0);
        assertFalse(moveBeforeStart, "Cannot move during SETUP");

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        assertEquals(GameState.PLAYING, game.getState(), "State should be PLAYING after startGame");

        boolean placeAfterStart = game.placePiece(10, 1,
                new Piece(Rank.REGIMENT, Team.RED));

        assertFalse(placeAfterStart, "Cannot place pieces after game starts");

        game.finishGame();

        assertEquals(GameState.FINISHED, game.getState(), "State should be FINISHED after finishGame");

        boolean moveAfterFinish = game.move(10, 0, 9, 0);
        assertFalse(moveAfterFinish, "Cannot move after game finishes");

        pass("GameState rules");
    }

    private static void testWinByCapturingFlag() {
        Game game = new Game();

        game.forcePlacePieceForTesting(0, 1,
                new Piece(Rank.FLAG, Team.BLUE));

        game.forcePlacePieceForTesting(0, 4,
                new Piece(Rank.SQUAD, Team.BLUE));

        game.forcePlacePieceForTesting(6, 1,
                new Piece(Rank.ENGINEER, Team.RED));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        boolean move1 = game.move(6, 1, 1, 1);
        assertTrue(move1, "RED engineer should move near flag");

        boolean move2 = game.move(0, 4, 1, 4);
        assertTrue(move2, "BLUE should make a normal move");

        boolean captureFlag = game.move(1, 1, 0, 1);
        assertTrue(captureFlag, "RED should capture BLUE flag");

        assertEquals(Team.RED, game.getWinner(), "RED should win by capturing BLUE flag");
        assertEquals(GameState.FINISHED, game.getState(), "Game should finish after flag capture");
        assertPiece(game, 0, 1, Rank.ENGINEER, Team.RED);

        pass("Win by capturing flag");
    }

    private static void testAgreeDraw() {
        Game game = new Game();

        game.forcePlacePieceForTesting(0, 4,
                new Piece(Rank.SQUAD, Team.BLUE));

        game.forcePlacePieceForTesting(6, 1,
                new Piece(Rank.ENGINEER, Team.RED));

        game.startGameForTesting();

        boolean success = game.agreeDraw();

        assertTrue(success, "agreeDraw should succeed during PLAYING");
        assertTrue(game.isDraw(), "Game should be marked as draw");
        assertEquals(GameState.FINISHED, game.getState(), "Game should finish after agreed draw");
        assertNull(game.getWinner(), "Winner should be null in draw");

        pass("Agree draw");
    }

    private static void testDrawAfter70Moves() {
        Game game = new Game();

        game.forcePlacePieceForTesting(10, 0,
                new Piece(Rank.GENERAL, Team.RED));

        game.forcePlacePieceForTesting(1, 0,
                new Piece(Rank.GENERAL, Team.BLUE));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);
        game.setMovesWithoutCaptureForTesting(69);

        boolean success = game.move(10, 0, 10, 1);

        assertTrue(success, "70th non-capture move should succeed");
        assertTrue(game.isDraw(), "Game should be draw after 70 moves without capture");
        assertEquals(GameState.FINISHED, game.getState(), "Game should be FINISHED after draw");

        pass("Draw after 70 moves without capture");
    }

    private static void testTurnSystem() {
        Game game = new Game();

        game.forcePlacePieceForTesting(0, 0,
                new Piece(Rank.GENERAL, Team.BLUE));

        game.forcePlacePieceForTesting(11, 0,
                new Piece(Rank.GENERAL, Team.RED));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        boolean blueMoveOnRedTurn = game.move(0, 0, 1, 0);
        assertFalse(blueMoveOnRedTurn, "BLUE should not move on RED turn");
        assertEquals(Team.RED, game.getCurrentTurn(), "Illegal move should not switch turn");

        boolean redMove = game.move(11, 0, 10, 0);
        assertTrue(redMove, "RED should move on RED turn");
        assertEquals(Team.BLUE, game.getCurrentTurn(), "Successful move should switch turn to BLUE");

        pass("Turn system");
    }

    private static void testNewRecruitPromotion() {
        Game game = new Game();

        game.forcePlacePieceForTesting(6, 0,
                new Piece(Rank.PRIVATE, Team.RED));

        game.forcePlacePieceForTesting(6, 4,
                new Piece(Rank.PRIVATE, Team.RED));

        game.forcePlacePieceForTesting(0, 4,
                new Piece(Rank.SQUAD, Team.BLUE));

        game.startGameForTesting();
        game.setCurrentTurnForTesting(Team.RED);

        boolean splitSuccess = game.split(6, 0, 7, 0);

        assertTrue(splitSuccess, "PRIVATE should split into NEW_RECRUIT in own territory");
        assertPiece(game, 6, 0, Rank.NEW_RECRUIT, Team.RED);
        assertPiece(game, 7, 0, Rank.NEW_RECRUIT, Team.RED);

        game.setCurrentTurnForTesting(Team.RED);
        boolean splitIntoOpponentTerritorySuccess = game.split(6, 4, 5, 4);

        assertFalse(splitIntoOpponentTerritorySuccess,
                "PRIVATE can only split within own territory");
        assertPiece(game, 6, 4, Rank.PRIVATE, Team.RED);
        assertNull(game.getBoard().getPiece(5, 4), "Target square should stay empty because illegal split move");

        game.setCurrentTurnForTesting(Team.RED);
        boolean moveToEnemyTerritory = game.move(6, 0, 5, 0);

        assertTrue(moveToEnemyTerritory, "NEW_RECRUIT should move into enemy territory");
        assertPiece(game, 5, 0, Rank.ENGINEER, Team.RED);

        pass("New recruit promotion");
    }

    // Assert helpers


    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            fail(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            fail(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected != actual) {
            fail(message + " | Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            fail(message);
        }
    }

    private static void assertNull(Object obj, String message) {
        if (obj != null) {
            fail(message + " | Actual: " + obj);
        }
    }

    private static void assertPiece(Game game, int r, int c,
                                     Rank expectedRank, Team expectedTeam) {
        Piece p = game.getBoard().getPiece(r, c);

        assertNotNull(p, "Expected piece at (" + r + ", " + c + ")");

        if (p != null) {
            assertEquals(expectedRank, p.rank,
                    "Wrong rank at (" + r + ", " + c + ")");
            assertEquals(expectedTeam, p.team,
                    "Wrong team at (" + r + ", " + c + ")");
        }
    }



    private static void pass(String testName) {
        passed++;
        System.out.println("PASSED: " + testName);
    }

    private static void fail(String message) {
        failed++;
        System.out.println("FAILED: " + message);
    }
}
