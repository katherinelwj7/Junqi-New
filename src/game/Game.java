package game;

import board.Board;
import piece.Piece;
import piece.Rank;
import utils.MoveValidator;
import piece.Team;
import board.Tile;
import board.TileType;
import console.ConsoleDisplay;

public class Game {
    private Board board;
    private GameState state;
    private Team winner;
    private boolean draw;
    private int movesWithoutCapture;
    private Team currentTurn;
    private int setupHighlightRow = -1;
    private int setupHighlightCol = -1;
    private String setupHighlightMessage = "";
    private String lastMessage = "";

    public Game() {
        board = new Board(12, 5);
        board.initializeMap();
        state = GameState.SETUP;
        winner = null;
        draw = false;
        movesWithoutCapture = 0;
    }

    public Board getBoard() {
        return board;
    }

    public GameState getState() {
        return state;
    }

    public Team getWinner() {
        return winner;
    }

    public boolean isDraw() {
        return draw;
    }

    public int getMovesWithoutCapture() {
        return movesWithoutCapture;
    }

    public Team getCurrentTurn() {
        return currentTurn;
    }

    // Highlight布局问题
    private void setSetupHighlight(int r, int c, String message) {
        setupHighlightRow = r;
        setupHighlightCol = c;
        setupHighlightMessage = message;
    }

    // 撤销Highlight布局问题
    private void clearSetupHighlight() {
        setupHighlightRow = -1;
        setupHighlightCol = -1;
        setupHighlightMessage = "";
    }

    // 判断一个坐标是否被Highlight
    private boolean isSetupHighlight(int r, int c) {
        return r == setupHighlightRow && c == setupHighlightCol;
    }

    // 判断如何对用户显示棋子
    public String getPieceDisplayAt(int r, int c, Team viewer) {

        Piece piece = board.getPiece(r, c);

        if (piece == null) {
            return ".";
        }

        // 自己的棋子：永远可见
        if (piece.team == viewer) {
            return piece.rank.notationsForPrinting();
        }

        // 对方已亮出的军旗：可见
        if (piece.rank == Rank.FLAG && piece.isRevealed()) {
            return "FL";
        }

        // 对方其他棋子：只能看到这里有棋子
        return "??";
    }

    // SETUP时：打印单视角棋盘
    public void printSetupBoardForViewer(Team viewer) {

        System.out.println("SETUP board view for " + viewer);
        System.out.println();

        System.out.print("    ");

        for (int c = 0; c < 5; c++) {
            System.out.printf("%8d", c);
        }

        System.out.println();

        for (int r = 0; r < 12; r++) {

            System.out.printf("%3d ", r);

            for (int c = 0; c < 5; c++) {

                String display = getPieceDisplayAt(r, c, viewer);

                if (isSetupHighlight(r, c) &&
                        board.getPiece(r, c).team == viewer) {

                    display = underline(display);
                }

                System.out.printf("%8s", display);
            }

            System.out.print("     ");
            System.out.println(ConsoleDisplay.getLegendLine(2 * r));
            System.out.print("                                                 ");
            System.out.println(ConsoleDisplay.getLegendLine(2 * r + 1));

        }

        printRemainingLegendLines(24);
    }

    // PLAYING时：对用户显示单视角棋盘
    public void printBoardForViewer(Team viewer) {

        if (viewer == null) {
            System.out.println("Cannot print board view: viewer is null");
            return;
        }

        System.out.println("Board view for " + viewer);
        System.out.println();

        System.out.print("    ");

        for (int c = 0; c < 5; c++) {
            System.out.printf("%8d", c);
        }

        System.out.println();

        for (int r = 0; r < 12; r++) {

            System.out.printf("%3d ", r);

            for (int c = 0; c < 5; c++) {
                String display = getPieceDisplayAt(r, c, viewer);
                System.out.printf("%8s", display);
            }

            System.out.print("     ");
            System.out.println(ConsoleDisplay.getLegendLine(2 * r));
            System.out.print("                                                 ");
            System.out.println(ConsoleDisplay.getLegendLine(2 * r + 1));

        }

        printRemainingLegendLines(24);
    }

    // 打印剩余图例
    private void printRemainingLegendLines(int alreadyPrintedLines) {

        for (int i = alreadyPrintedLines; i < ConsoleDisplay.BOARD_LEGEND.length; i++) {
            System.out.println("                                                 " + ConsoleDisplay.getLegendLine(i));
        }
    }

    // 临时用来测试70步不吃子和棋的helper
    public void setMovesWithoutCaptureForTesting(int moves) {
        movesWithoutCapture = moves;
    }

    // 临时用来指定先手的测试helper
    public void setCurrentTurnForTesting(Team team) {
        currentTurn = team;
    }

    // 临时摆放“不符合正式布局”的特殊位置的helper
    public void forcePlacePieceForTesting(int r, int c, Piece piece) {
        board.placePiece(r, c, piece);
    }

    // 开局前：布局
    public boolean placePiece(int r, int c, Piece piece) {

        clearLastMessage();

        if (state != GameState.SETUP) {
            addMessage("Cannot place pieces after game starts");
            //System.out.println("Cannot place pieces after game starts");
            return false;
        }

        if (piece == null) {
            board.placePiece(r, c, null);
            return true;
        }

        Tile tile = board.getTile(r, c);

        // 不能放到对方阵地
        if (!isOwnTerritory(piece.team, r)) {
            addMessage("Cannot place piece in opponent territory");
            //System.out.println("Cannot place piece in opponent territory");
            return false;
        }



        // 开局不能放行营
        if (tile.type == TileType.CAMP) {
            addMessage("Cannot place piece in CAMP during setup");
            //System.out.println("Cannot place piece in CAMP during setup");
            return false;
        }

        board.placePiece(r, c, piece);
        return board.placePiece(r, c, piece);
    }

    // 检查棋子是不是放在自己的阵地内
    private boolean isOwnTerritory(Team team, int row) {
        if (team == Team.BLUE) {
            return row >= 0 && row <= 5;
        }
        if (team == Team.RED) {
            return row >= 6 && row <= 11;
        }

        return false;
    }

    // 默认布局
    public void setupDefaultLayout() {
        if (state != GameState.SETUP) {
            addMessage("Can only set default layout during SETUP");
            //System.out.println("Can only set default layout during SETUP");
            return;
        }

        setupDefaultLayoutForTeam(Team.BLUE);
        setupDefaultLayoutForTeam(Team.RED);
    }

    // 每个Team的默认布局
    public void setupDefaultLayoutForTeam(Team team) {

        if (team == Team.RED) {
            Rank[] layout = {
                    Rank.REGIMENT, Rank.PRIVATE, Rank.PLATOON, Rank.ENGINEER, Rank.GENERAL,
                    Rank.BOMB, Rank.SQUAD, Rank.BATTALION,
                    Rank.BRIGADE, Rank.BOMB, Rank.ENGINEER, Rank.COMPANY,
                    Rank.SQUAD, Rank.COMPANY, Rank.PLATOON,
                    Rank.BATTALION, Rank.MINE, Rank.REGIMENT, Rank.ENGINEER, Rank.SQUAD,
                    Rank.MINE, Rank.FLAG, Rank.MINE, Rank.PRIVATE, Rank.PRIVATE
            };

            int index = 0;

            for (int r = 6; r < 12; r++) {
                for (int c = 0; c < 5; c++) {

                    if (isSetupSquare(team, r, c)) {
                        board.placePiece(r, c, new Piece(layout[index], team));
                        index++;
                    }
                }
            }
        }

        if (team == Team.BLUE) {
            Rank[] layout = {
                    Rank.PRIVATE, Rank.PRIVATE, Rank.MINE, Rank.FLAG, Rank.MINE,
                    Rank.SQUAD, Rank.ENGINEER, Rank.REGIMENT, Rank.MINE, Rank.BATTALION,
                    Rank.PLATOON, Rank.COMPANY, Rank.SQUAD,
                    Rank.COMPANY, Rank.ENGINEER, Rank.BOMB, Rank.BRIGADE,
                    Rank.BATTALION, Rank.SQUAD, Rank.BOMB,
                    Rank.GENERAL, Rank.ENGINEER, Rank.PLATOON, Rank.PRIVATE, Rank.REGIMENT
            };

            int index = 0;

            for (int r = 0; r < 6; r++) {
                for (int c = 0; c < 5; c++) {

                    if (isSetupSquare(team, r, c)) {
                        board.placePiece(r, c, new Piece(layout[index], team));
                        index++;
                    }
                }
            }
        }
    }

    // 检查：开局布局时，棋子只能在自己的阵地，且不在行营
    private boolean isSetupSquare(Team team, int r, int c) {
         if (!isOwnTerritory(team, r)) {
             return false;
         }

         if (board.getTile(r, c).type == TileType.CAMP) {
             return false;
         }

         return true;
    }

    // 布局时交换两个棋子的位置
    public boolean swapSetupPieces(Team setupPlayer, int r1, int c1, int r2, int c2) {

        clearLastMessage();

        if (state != GameState.SETUP) {
            addMessage("Can only swap pieces during SETUP");
            //System.out.println("Can only swap pieces during SETUP");
            return false;
        }

        Piece p1 = board.getPiece(r1, c1);
        Piece p2 = board.getPiece(r2, c2);

        if (p1 == null || p2 == null) {
            addMessage("Both squares must contain pieces");
            //System.out.println("Both squares must contain pieces");
            return false;
        }

        if (p1.team != setupPlayer || p2.team != setupPlayer) {
            addMessage("You can only swap your own pieces during SETUP");
            //System.out.println("You can only swap your own pieces during SETUP");
            return false;
        }

        if (!isSetupSquare(p1.team, r1, c1) ||
                !isSetupSquare(p1.team, r2, c2)) {

            addMessage("Can only swap within your own setup area, and not in camps");
            //System.out.println("Can only swap within your own setup area, and not in camps");
            return false;
        }

        /*
         * p1 原本在 (r1, c1)，玩家试图把它换到 (r2, c2)
         * 如果 p1 不能去目标位置，就标注 p1 的原位置。
         */
        if (!isLegalSetupPosition(p1, r2, c2)) {
            setSetupHighlight(r1, c1,
                    getSetupRestrictionMessage(p1, r2, c2));

            System.out.println(setupHighlightMessage);
            return false;
        }

        /*
         * p2 原本在 (r2, c2)，玩家试图把它换到 (r1, c1)
         * 如果 p2 不能去目标位置，就标注 p2 的原位置。
         */
        if (!isLegalSetupPosition(p2, r1, c1)) {
            setSetupHighlight(r2, c2,
                    getSetupRestrictionMessage(p2, r1, c1));

            System.out.println(setupHighlightMessage);
            return false;
        }

        board.placePiece(r1, c1, p2);
        board.placePiece(r2, c2, p1);

        clearSetupHighlight();

        return true;
    }

    // 检查swap是否合法
    private boolean isLegalSetupPosition(Piece piece, int r, int c) {

        if (piece == null) {
            return false;
        }

        if (!isSetupSquare(piece.team, r, c)) {
            return false;
        }

        if (piece.rank == Rank.FLAG) {
            return board.getTile(r, c).type == TileType.BASE;
        }

        if (piece.rank == Rank.MINE) {
            return isBackTwoRows(piece.team, r);
        }

        if (piece.rank == Rank.BOMB) {
            return !isFrontRow(piece.team, r);
        }

        return true;
    }

    // 如果布局不合法：给出不合法的原因
    private String getSetupRestrictionMessage(Piece piece, int targetRow, int targetCol) {

        if (piece == null) {
            addMessage("No piece selected");
            return "No piece selected";
        }

        if (!isSetupSquare(piece.team, targetRow, targetCol)) {
            addMessage(piece.rank + " cannot be placed outside setup area or in CAMP");
            return piece.rank + " cannot be placed outside setup area or in CAMP";
        }

        if (piece.rank == Rank.FLAG) {
            addMessage("FLAG must be placed in BASE");
            return "FLAG must be placed in BASE";
        }

        if (piece.rank == Rank.MINE) {
            addMessage("MINE can only be placed in the back two rows");
            return "MINE can only be placed in the back two rows";
        }

        if (piece.rank == Rank.BOMB) {
            addMessage("BOMB cannot be placed in the first row");
            return "BOMB cannot be placed in the first row";
        }

        addMessage("Illegal setup position");
        return "Illegal setup position";
    }

    // 布局不合法：underline helper
    private String underline(String text) {
        return "_" + text + "_";
    }

    // 是不是后两排？
    private boolean isBackTwoRows(Team team, int row) {

        if (team == Team.RED) {
            return row == 10 || row == 11;
        }

        if (team == Team.BLUE) {
            return row == 0 || row == 1;
        }

        return false;
    }

    // 是不是第一排？
    private boolean isFrontRow(Team team, int row) {

        if (team == Team.RED) {
            return row == 6;
        }

        if (team == Team.BLUE) {
            return row == 5;
        }

        return false;
    }

    // 检查最终布局是否合法
    public boolean isValidSetup(Team team) {

        int total = 0;

        for (Rank rank : Rank.values()) {
            int count = countPieces(team, rank);
            int expected = expectedCount(rank);

            if (count != expected) {
                System.out.println(team + " has wrong number of "
                + rank + ". Expected: " + expected + ", Actual: " + count);
                return false;
            }

            total += count;
        }

        if (total != 25) {
            System.out.println(team + "should have exactly 25 pieces");
            return false;
        }

        return true;
    }

    // 数某个rank有几个piece
    private int countPieces(Team team, Rank rank) {

        int count = 0;

        for (int r = 0; r < board.grid.length; r++) {
            for (int c = 0; c < board.grid[0].length; c++) {

                Piece p = board.getPiece(r, c);

                if (p != null && p.team == team && p.rank == rank) {
                    count++;
                }
            }
        }

        return count;
    }

    // 每个rank预期棋子数
    private int expectedCount(Rank rank) {

        switch (rank) {

            case FLAG:
                return 1;

            case MINE:
                return 3;

            case BOMB:
                return 2;

            case GENERAL:
                return 1;

            case BRIGADE:
                return 1;

            case REGIMENT:
                return 2;

            case BATTALION:
                return 2;

            case COMPANY:
                return 2;

            case PLATOON:
                return 2;

            case SQUAD:
                return 3;

            case PRIVATE:
                return 3;

            case ENGINEER:
                return 3;

            case NEW_RECRUIT:
                return 0;

            default:
                return 0;
        }
    }

    // 开始游戏
    public boolean startGame() {

        clearLastMessage();

        if (state != GameState.SETUP) {
            addMessage("Game can only start immediately after SETUP");
            //System.out.println("Game can only start immediately after SETUP");
            return false;
        }

        if (!isValidSetup(Team.RED) || !isValidSetup(Team.BLUE)) {
            addMessage("Cannot start: invalid setup");
            //System.out.println("Cannot start: invalid setup");
            return false;
        }

        state = GameState.PLAYING;

        // 随机先手
        if (Math.random() < 0.5) {
            currentTurn = Team.RED;
        }
        else {
            currentTurn = Team.BLUE;
        }

        System.out.println("Game started. " + currentTurn + " moves first.");

        return true;
    }

    // 开始游戏的测试helper（不需要完整布局）
    public boolean startGameForTesting() {
        if (state != GameState.SETUP) {
            addMessage("Game can only start immediately after SETUP");
            //System.out.println("Game can only start immediately after SETUP");
            return false;
        }

        state = GameState.PLAYING;

        // 随机先手
        if (Math.random() < 0.5) {
            currentTurn = Team.RED;
        }
        else {
            currentTurn = Team.BLUE;
        }

        System.out.println("Game started. " + currentTurn + " moves first.");

        return true;
    }

    // 换回合
    private void switchTurn() {
        currentTurn = getOpponent(currentTurn);
    }

    // 移动
    public boolean move(int r1, int c1, int r2, int c2) {

        clearLastMessage();

        if (state != GameState.PLAYING) {
            addMessage("Cannot move unless game is PLAYING");
            //System.out.println("Cannot move unless game is PLAYING");
            return false;
        }

        Piece attacker = board.getPiece(r1, c1);
        Piece defender = board.getPiece(r2, c2);

        if (attacker == null)  {
            addMessage("Invalid move：no piece");
            //System.out.println("Invalid move：no piece");
            return false;
        }

        if (attacker.team != currentTurn) {
            addMessage("It is not this team's turn");
            //System.out.println("It is not this team's turn");
            return false;
        }

        Team attackerTeam = attacker.team;

        if (!attacker.movable) {
            addMessage("Invalid move: piece cannot move");
            //System.out.println("Invalid move: piece cannot move");
            return false;
        }

        if (!MoveValidator.canMove(board, attacker, r1, c1, r2, c2)) {
            addMessage("Invalid move: illegal movement");
            //System.out.println("Invalid move: illegal movement");
            return false;
        }

        // 己方棋子：尝试merge
        if (defender != null &&
                attacker.team == defender.team) {

            if (!MoveValidator.canMerge(attacker, defender)) {
                addMessage("Invalid move: same team");
                //System.out.println("Invalid move: same team");
                return false;
            }

            merge(r1, c1, r2, c2);

            finishSuccessfulAction(attackerTeam, false);
            return true;
        }

        // 空格：普通移动
        if (defender == null) {

            board.placePiece(r2, c2, attacker);
            board.placePiece(r1, c1, null);

            promoteNewRecruitIfNeeded(r2, c2);
            finishSuccessfulAction(attackerTeam, false);
            return true;
        }

        // 敌方棋子：battle
        boolean targetIsFlag = defender.rank == Rank.FLAG;

        Piece result = MoveValidator.resolveBattle(attacker, defender);

        // 如果司令阵亡，亮军旗
        if (attacker.rank == Rank.GENERAL && result != attacker) {
            revealFlag(attacker.team);
        }
        if (defender.rank == Rank.GENERAL && result != defender) {
            revealFlag(defender.team);
        }

        board.placePiece(r2, c2, result);
        board.placePiece(r1, c1, null);

        // 军旗被夺？
        if (targetIsFlag) {
            endWithWinner(attackerTeam);
            return true;
        }

        finishSuccessfulAction(attackerTeam, true);
        return true;
    }

    // 融合
    public void merge(int r1, int c1,
                      int r2, int c2) {

        if (state != GameState.PLAYING) {
            addMessage("Cannot merge unless game is PLAYING");
            //System.out.println("Cannot merge unless game is PLAYING");
            return;
        }

        Piece a = board.getPiece(r1, c1);
        Piece b = board.getPiece(r2, c2);

        Rank newRank = a.rank.nextRank();

        Piece merged =
                new Piece(newRank, a.team);

        board.placePiece(r2, c2, merged);

        board.placePiece(r1, c1, null);
    }

    // 分裂
    public boolean split(int r1, int c1,
                         int r2, int c2) {

        clearLastMessage();

        if (state != GameState.PLAYING) {
            addMessage("Cannot split unless game is PLAYING");
            //System.out.println("Cannot split unless game is PLAYING");
            return false;
        }

        Piece piece = board.getPiece(r1, c1);

        if (piece == null) {
            addMessage("No piece to split");
            //System.out.println("No piece to split");
            return false;
        }

        if (piece.team != currentTurn) {
            addMessage("It is not this team's turn");
            //System.out.println("It is not this team's turn");
            return false;
        }

        Team attackerTeam = piece.team;
        Piece defender = board.getPiece(r2, c2);

        // 能不能 split
        if (!MoveValidator.canSplit(piece)) {

            addMessage("Cannot split");
            //System.out.println("Cannot split");
            return false;
        }

        // 排长只能在自己的阵地分裂
        if (piece.rank == Rank.PRIVATE) {
            if (!isOwnTerritory(piece.team, r1) || !isOwnTerritory(piece.team, r2)) {

                addMessage("PRIVATE can only split within own territory");
                //System.out.println("PRIVATE can only split within own territory");
                return false;
            }
        }

        Rank lower = piece.rank.previousRank();

        // 原位置留下的新棋子
        Piece originalHalf =
                new Piece(lower, piece.team);

        // 分裂出去的新棋子
        Piece movingHalf =
                new Piece(lower, piece.team);

        // 是否是允许分裂+融合进入行营的特殊情况？
        boolean allowOccupiedCamp = defender != null
                && defender.team == piece.team
                && MoveValidator.canMerge(movingHalf, defender);

        // 必须是合法移动
        if (!MoveValidator.canMove(
                board,
                piece,
                r1, c1,
                r2, c2, allowOccupiedCamp) ||
        !MoveValidator.isOneStep(r1, c1, r2, c2)) {

            addMessage("Invalid split move");
            //System.out.println("Invalid split move");
            return false;
        }

        // 目标为空格：正常分裂
        if (defender == null) {

            board.placePiece(r1, c1, originalHalf);
            board.placePiece(r2, c2, movingHalf);

            finishSuccessfulAction(attackerTeam, false);

            return true;
        }

        // 目标是己方：尝试分裂后融合
        if (defender.team == piece.team) {
            if (!MoveValidator.canMerge(movingHalf, defender)) {
                addMessage("Cannot split onto teammate unless they can merge");
                //System.out.println("Cannot split onto teammate unless they can merge");
                return false;
            }

            Rank mergedRank = movingHalf.rank.nextRank();
            Piece mergedPiece = new Piece(mergedRank, piece.team);

            board.placePiece(r1, c1, originalHalf);
            board.placePiece(r2, c2, mergedPiece);

            finishSuccessfulAction(attackerTeam, false);

            return true;
        }

        // 目标为敌方：分裂出的棋子battle
        boolean targetIsFlag = defender.rank == Rank.FLAG;

        Piece result =
                MoveValidator.resolveBattle(
                        movingHalf,
                        defender);

        board.placePiece(r1, c1, originalHalf);
        board.placePiece(r2, c2, result);

        if (targetIsFlag) {
            endWithWinner(attackerTeam); // 军旗被夺
            return true;
        }

        finishSuccessfulAction(attackerTeam, true);
        return true;
    }

    // 亮旗
    public void revealFlag(Team team) {

        Piece flag = board.findFlag(team);

        if (flag != null) {

            flag.reveal();

            addMessage(team + " flag revealed!");
            //System.out.println(team + " flag revealed!");
        }
    }

    // 判断对方是谁
    private Team getOpponent(Team team) {
        if (team == Team.RED) {
            return Team.BLUE;
        }
        else {
            return Team.RED;
        }
    }

    // 判断棋子是否处在对方领地
    private boolean isEnemyTerritory(Team team, int row) {
        if (team == Team.RED) {
            return row >= 0 && row <= 5;
        }

        if (team == Team.BLUE) {
            return row >= 6 && row <= 11;
        }

        return false;
    }

    // 如果需要，新兵升级成工兵
    private void promoteNewRecruitIfNeeded(int r, int c) {
        Piece piece = board.getPiece(r, c);

        if (piece == null) {
            return;
        }

        if (piece.rank == Rank.NEW_RECRUIT && isEnemyTerritory(piece.team, r)) {

            board.placePiece(r, c, new Piece(Rank.ENGINEER, piece.team));
        }
    }

    // 回合结算
    private void finishSuccessfulAction(Team attackerTeam, boolean captureHappened) {

        if (captureHappened) {
            movesWithoutCapture = 0;
        }
        else {
            movesWithoutCapture++;
        }

        if (movesWithoutCapture >= 70) {
            endWithDraw();
            return;
        }

        Team opponent = getOpponent(attackerTeam);

        if (!hasMovablePiece(opponent)) {
            endWithWinner(attackerTeam);
            return;
        }

        switchTurn();
    }

    // 和棋
    public boolean agreeDraw() {
        if (state != GameState.PLAYING) {
            addMessage("Can only negotiate draw during PLAYING");
            //System.out.println("Can only negotiate draw during PLAYING");
            return false;
        }

        endWithDraw();
        return true;
    }

    // 对方无可移动棋子
    private boolean hasMovablePiece(Team team) {
        for (int r = 0; r < board.grid.length; r++) {
            for (int c = 0; c < board.grid[0].length; c++) {
                Piece p = board.getPiece(r, c);

                if (p!= null && p.team == team && p.movable &&
                board.getTile(r, c).type != TileType.BASE) {
                    return true;
                }
            }
        }

        return false;
    }

    // 结束游戏
    public boolean finishGame() {
        if (state == GameState.FINISHED) {
            addMessage("Game already finished");
            //System.out.println("Game already finished");
            return false;
        }

        state = GameState.FINISHED;
        System.out.println("Game finished");
        return true;
    }

    // 宣布赢家
    private void endWithWinner(Team team) {
        winner = team;
        draw = false;
        finishGame();
        System.out.println(team + " wins!");
    }

    // 宣布和棋
    private void endWithDraw() {
        winner = null;
        draw = true;
        finishGame();
        System.out.println("Game ends in a draw");
    }

    // 处理给玩家的提示
    public String getLastMessage() {
        return lastMessage;
    }

    private void clearLastMessage() {
        lastMessage = "";
    }

    private void addMessage(String message) {

        if (lastMessage.isEmpty()) {
            lastMessage = message;
        } else {
            lastMessage += "\n" + message;
        }

        System.out.println(message);
    }
}