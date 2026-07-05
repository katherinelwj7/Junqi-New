import game.Game;
import piece.Piece;
import piece.Team;
import piece.Rank;
import board.TileType;
import board.ConnectionType;
import test.TestRunner;
import console.ConsoleGame;

public class Main {
    public static void main(String[] args) {

        TestRunner.runAllTests();

        //ConsoleGame.run();

        /*Piece general = new Piece(Rank.GENERAL, Team.RED);
        Piece brigade = new Piece(Rank.BRIGADE, Team.BLUE);

        game.placePiece(6,0, general);
        game.placePiece(5,0, brigade);

        game.startGame();
        game.setCurrentTurnForTesting(Team.RED);

        // 司令吃军长
        System.out.println("Before move: ");
        game.getBoard().printBoard();

        boolean success = game.move(6,0,5,0);
        if (success) {
            printResult(game, 5, 0);
            System.out.println("After move: ");
            game.getBoard().printBoard();
        }
        System.out.println("Done");//*/

        // 测试：铁路长距离
        /*Piece company = new Piece(Rank.COMPANY, Team.RED);
        Piece platoon = new Piece(Rank.PLATOON, Team.BLUE);
        game.placePiece(8,4, company);
        game.placePiece(3, 4, platoon);

        game.getBoard().printBoard();

        game.startGame();
        game.setCurrentTurnForTesting(Team.RED);

        // 测试成功运行？
        System.out.println(game.move(8, 4, 3, 4));

        game.getBoard().printBoard();
        System.out.println();//*/

        // 测试公路
        // 正交
        /*ame.placePiece(6, 3, new Piece(Rank.ENGINEER, Team.RED)); //加工兵
        game.getBoard().printBoard(); //棋盘

        game.startGame();
        game.setCurrentTurnForTesting(Team.RED);

        System.out.println(game.move(6, 3, 7, 3)); //成功？
        game.getBoard().printBoard(); //棋盘
        System.out.println();//*/
        // 斜
        /*game.placePiece(1, 0, new Piece(Rank.REGIMENT, Team.BLUE)); //加师长
        game.getBoard().printBoard();

        game.startGame();
        game.setCurrentTurnForTesting(Team.BLUE);

        System.out.println(game.move(1, 0, 2, 1));
        game.getBoard().printBoard();//*/

        // 测试工兵拐弯（测试时需要把前面都变成comment）
            // 加工兵
        /*Piece engineer = new Piece(Rank.ENGINEER, Team.RED);
        game.placePiece(10,3, engineer);

            // 如果测试不让工兵拐弯，就在(6, 4)和(6, 0)这两个位置各放一个棋子
        //Piece squad = new Piece(Rank.SQUAD, Team.RED);
        //game.placePiece(6, 4, squad);
        //Piece platoon = new Piece(Rank.PLATOON, Team.RED);
        //game.placePiece(6, 0, platoon);

            // 测试工兵从(10, 3)到(1, 1)
        game.getBoard().printBoard();

        game.startGame();
        game.setCurrentTurnForTesting(Team.RED);

        System.out.println(game.move(10,3, 1, 1));
        game.getBoard().printBoard();//*/

        // 测试融合(merge)
            // 正常merge：REGIMENT + REGIMENT = BRIGADE
        /*Piece regiment1 = new Piece(Rank.REGIMENT, Team.BLUE);
        Piece regiment2 = new Piece(Rank.REGIMENT, Team.BLUE);
        game.placePiece(0, 0, regiment1);
        game.placePiece(0, 1, regiment2);
        game.getBoard().printBoard();

        game.startGame();
        game.setCurrentTurnForTesting(Team.BLUE);

        System.out.println(game.move(0, 0, 0, 1));
        game.getBoard().printBoard();//*/
            // 不同rank
        /*Piece battalion = new Piece(Rank.BATTALION, Team.BLUE);
        Piece company = new Piece(Rank.COMPANY, Team.BLUE);
        game.placePiece(0, 0, battalion);
        game.placePiece(0, 1, company);
        game.getBoard().printBoard();

        game.startGame();
        game.setCurrentTurnForTesting(Team.BLUE);

        System.out.println(game.move(0, 0, 0, 1));
        game.getBoard().printBoard();//*/
            // 不同team
        /*Piece regimentRed = new Piece(Rank.REGIMENT, Team.RED);
        Piece regimentBlue = new Piece(Rank.REGIMENT, Team.BLUE);
        game.placePiece(6, 0, regimentRed);
        game.placePiece(5, 0, regimentBlue);
        game.getBoard().printBoard();

        game.startGame();
        game.setCurrentTurnForTesting(Team.RED);

        System.out.println(game.move(6, 0, 5, 0));
        game.getBoard().printBoard();//*/
            // 尝试merge两个军长（应该失败）
        /*Piece brigade1 = new Piece(Rank.BRIGADE, Team.BLUE);
        Piece brigade2 = new Piece(Rank.BRIGADE, Team.BLUE);
        game.placePiece(0, 0, brigade1);
        game.placePiece(0, 1, brigade2);
        game.getBoard().printBoard();

        game.startGame();
        game.setCurrentTurnForTesting(Team.BLUE);

        System.out.println(game.move(0, 0, 0, 1));
        game.getBoard().printBoard();//*/

        // 测试分裂(split)
            // 正常split：军长变成两个师长 (145-150加入师长和司令是为了测试行营的特殊情况)
        /*Piece brigade = new Piece(Rank.BRIGADE, Team.BLUE);
        game.placePiece(2, 0, brigade);
        //game.placePiece(3, 0, new Piece(Rank.REGIMENT, Team.BLUE));
        //game.placePiece(6, 0, new Piece(Rank.GENERAL, Team.RED));
        //game.startGame();
        //game.setCurrentTurnForTesting(Team.BLUE);
        //game.move(3, 0, 2, 1);
        //game.move(6, 0, 5, 0); // 动一下红司令来遵守轮流走棋
        game.getBoard().printBoard();

        game.startGame(); // 测试特殊情况的时候把这两行comment掉
        game.setCurrentTurnForTesting(Team.BLUE);

        System.out.println(game.split(2, 0, 2, 1));
        game.getBoard().printBoard();//*/
            // 尝试split司令
        /*Piece general = new Piece(Rank.GENERAL, Team.BLUE);
        game.placePiece(0, 0, general);
        game.getBoard().printBoard();

        game.startGame();
        game.setCurrentTurnForTesting(Team.BLUE);

        System.out.println(game.split(0, 0, 0, 1));
        game.getBoard().printBoard();//*/
            // 目标格已有棋子
        /*//Piece PrivateBlue = new Piece(Rank.PRIVATE, Team.BLUE);
        Piece PrivateRed = new Piece(Rank.PRIVATE, Team.RED);
        game.placePiece(6, 0, PrivateRed);
        //game.placePiece(1, 0, PrivateBlue); // 在(1, 0)放自己的排长
        Piece brigade = new Piece(Rank.BRIGADE, Team.BLUE);
        game.placePiece(0, 0, brigade);

        game.startGame();
        game.setCurrentTurnForTesting(Team.RED);

        game.move(6, 0, 1, 0); // 在(1, 0)放对方的排长
        game.getBoard().printBoard();
        System.out.println(game.split(0, 0, 1, 0));
        game.getBoard().printBoard();//*/

        // 测试司令阵亡亮军旗
        /*Piece general = new Piece(Rank.GENERAL, Team.BLUE);
        game.placePiece(1, 0, general); // 加司令
        Piece bomb = new Piece(Rank.BOMB, Team.RED);
        game.placePiece(7, 0, bomb); // 加炸弹
        Piece BlueFlag = new Piece(Rank.FLAG, Team.BLUE);
        game.placePiece(0, 3, BlueFlag); // 加军旗

        game.getBoard().printBoard();

        game.startGame();
        game.setCurrentTurnForTesting(Team.RED);

        System.out.println(game.move(7, 0, 1, 0)); // 炸司令
        System.out.println(BlueFlag.isRevealed());
        game.getBoard().printBoard();//*/

        // 测试打印地图：格子类型
        /*game.getBoard().printTileTypes();
        game.getBoard().printRailways();
        game.getBoard().printRoads();
        game.getBoard().printMap();//*/


        // 测试不合法的棋子摆放位置
        /*game.placePiece(7, 2, new Piece(Rank.FLAG, Team.RED)); // 军旗普通格
        game.placePiece(11, 1, new Piece(Rank.FLAG, Team.RED)); // 军旗放大本营
        game.placePiece(8, 0, new Piece(Rank.MINE, Team.RED)); // 地雷放中间
        game.placePiece(10, 0, new Piece(Rank.MINE, Team.RED)); // 地雷放后两排
        game.placePiece(5, 4, new Piece(Rank.BOMB, Team.BLUE)); // 炸弹放第一排
        game.placePiece(4, 4, new Piece(Rank.BOMB, Team.BLUE)); // 炸弹放第二排//*/

        // 不合法的移动
        /*game.placePiece(11, 3, new Piece(Rank.PRIVATE, Team.RED)); // 大本营里放排长
        game.placePiece(8, 0, new Piece(Rank.REGIMENT, Team.RED)); // 放师长
        game.placePiece(3, 0, new Piece(Rank.GENERAL, Team.BLUE)); // 放司令

        game.startGame();
        game.setCurrentTurnForTesting(Team.BLUE);

        game.move(3, 0, 7, 0); // 司令靠近行营
        game.move(11, 3, 11, 4); // 试图移动大本营里的排长
        game.move(8, 0, 7, 1); // 师长进营
        game.move(7, 0, 7, 1); // 司令试图进营吃师长*/

        // 测试不同GameState的合法操作
            // SETUP时可以放置棋子，但不能move
        /*Piece general = new Piece(Rank.GENERAL, Team.RED);
        Piece bomb = new Piece(Rank.BOMB, Team.BLUE);
        game.placePiece(10, 0, general);
        game.placePiece(1, 0, bomb);
        game.getBoard().printBoard();
        System.out.println(game.getState()); // 应该得到"SETUP"
        System.out.println(game.move(10, 0, 1, 0)); // 应该得到"false"

            // 游戏开始(startGame())后可以move，但是不能继续布局
        game.startGame(); // 开始游戏
        game.setCurrentTurnForTesting(Team.RED);
        System.out.println(game.getState()); // 应该得到"PLAYING"
        boolean placed = game.placePiece(10, 1, new Piece(Rank.REGIMENT, Team.RED)); // 试图放新师长
        System.out.println(placed); // 应该得到"false"
        System.out.println(game.move(10, 0, 2, 0)); // 应该得到"true"
        game.getBoard().printBoard();

            // 游戏结束(finishGame())后不能move/split/placePiece
        game.finishGame(); // 结束游戏
        System.out.println(game.getState()); // 应该得到"FINISHED"
        boolean movedAfterFinish = game.move(2, 0, 1, 0); // 司令试图移动
        System.out.println(movedAfterFinish); // 应该得到"false"//*/

        // 测试胜利条件：军旗被夺
        /*Piece blueFlag = new Piece(Rank.FLAG, Team.BLUE);
        Piece blueSquad = new Piece(Rank.SQUAD, Team.BLUE);
        Piece redEngineer = new Piece(Rank.ENGINEER, Team.RED);
        game.placePiece(0, 4, blueSquad);
        game.placePiece(0, 1, blueFlag);
        game.placePiece(6, 1, redEngineer);

        game.startGame();
        game.setCurrentTurnForTesting(Team.RED);

        game.getBoard().printBoard();
        game.move(6, 1, 1, 1);
        game.getBoard().printBoard();
        game.move(0, 4, 1, 4);
        game.getBoard().printBoard();
        game.move(1, 1, 0, 1);
        game.getBoard().printBoard();//*/

        // 双方同意和棋
        /*Piece blueSquad = new Piece(Rank.SQUAD, Team.BLUE);
        Piece redEngineer = new Piece(Rank.ENGINEER, Team.RED);
        game.placePiece(0, 4, blueSquad);
        game.placePiece(6, 1, redEngineer);
        game.startGame();
        game.agreeDraw();
        System.out.println(game.isDraw());
        System.out.println(game.getState());//*/

        // 70步不吃子和棋
        /*game.placePiece(10, 0, new Piece(Rank.GENERAL, Team.RED));
        game.placePiece(1, 0, new Piece(Rank.GENERAL, Team.BLUE));

        game.startGame();
        game.setCurrentTurnForTesting(Team.RED);

        game.setMovesWithoutCaptureForTesting(69);

        System.out.println(game.move(10, 0, 10, 1));
        System.out.println(game.isDraw()); // 应该是true
        System.out.println(game.getState()); // 应该是FINISHED//*/

        // 随机先手
        /*game.startGame();
        System.out.println(game.getCurrentTurn());//*/

        // 轮流走棋
        /*game.placePiece(0, 0, new Piece(Rank.GENERAL, Team.BLUE));
        game.placePiece(11, 0, new Piece(Rank.GENERAL, Team.RED));
        game.startGame();
        game.setCurrentTurnForTesting(Team.RED);//*/

            // 不是当前回合不能走
        //System.out.println(game.move(0, 0, 1, 0)); // 应得false

            // 当前回合成功行动后换人
        //System.out.println(game.move(11, 0, 10, 0)); // 应得true
        //System.out.println(game.getCurrentTurn()); // 应得BLUE

            // 非法行动不换人
        //System.out.println(game.move(11, 0, 6, 0)); // false
        //System.out.println(game.getCurrentTurn()); // RED

        // 测试新兵
        /*game.placePiece(6, 0, new Piece(Rank.PRIVATE, Team.RED));
        game.placePiece(6, 4, new Piece(Rank.PRIVATE, Team.RED));
        game.placePiece(0, 4, new Piece(Rank.SQUAD, Team.BLUE));
        game.placePiece(1, 0, new Piece(Rank.MINE, Team.BLUE));
        game.placePiece(5, 1, new Piece(Rank.ENGINEER, Team.BLUE));
        game.startGame();
        game.setCurrentTurnForTesting(Team.RED);
        game.getBoard().printBoard();
        game.split(6, 0, 7, 0); // 排长在己方阵地分裂，应成功
        game.getBoard().printBoard();
        game.move(0, 4, 1, 4);
        game.split(6, 4, 5, 4); // 排长试图分裂到敌方阵地，应失败
        game.getBoard().printBoard();
        game.move(6, 0, 1, 0); // 新兵撞地雷，应地雷赢
        game.getBoard().printBoard();
        game.move(5, 1, 7, 0); // 工兵撞新兵，应兑掉
        game.getBoard().printBoard();
        game.split(6, 4, 7, 4); // 排长分裂成两个新兵
        game.getBoard().printBoard();
        game.move(1, 4, 0, 4);
        game.move(6, 4, 5, 4); // 新兵走到对方阵地，应变成工兵
        game.getBoard().printBoard();
        game.move(0, 4, 1, 4);
        game.move(7, 4, 6, 3); // 新兵尝试拐弯，应失败
        game.move(5, 4, 1, 0); // 升级成工兵后尝试拐弯和排雷，应成功
        game.getBoard().printBoard();//*/
    }

    public static void printResult(Game game, int r, int c) {
        Piece p = game.getBoard().getPiece(r, c);
        if (p == null) {
            System.out.println("Result: null (同归于尽)");
        } else {
            System.out.println("Result: " + p.rank);
        }
    }
}