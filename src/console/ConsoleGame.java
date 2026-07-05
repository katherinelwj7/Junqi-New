package console;

import game.Game;
import game.GameState;
import piece.Piece;
import piece.Rank;
import piece.Team;

import java.util.Scanner;

public class ConsoleGame {

    public static void run() {

        Game game = new Game();

        game.setupDefaultLayout();

        Team setupViewer = Team.RED;

        //setupDemoGame(game);

        //game.startGame();

        Scanner scanner = new Scanner(System.in);

        printHelp();

        while (game.getState() != GameState.FINISHED) {

            System.out.println();

            if (game.getState() == GameState.SETUP) {
                System.out.println("Setup viewer: " + setupViewer);
            } else if (game.getState() == GameState.PLAYING) {
                System.out.println("Current turn: " + game.getCurrentTurn());
            }

            System.out.print("> ");

            String input = scanner.nextLine().trim();

            if (input.length() == 0) {
                continue;
            }

            String[] parts = input.split("\\s+");

            String command = parts[0].toLowerCase();

            if (command.equals("quit")) {
                System.out.println("Quitting game.");
                break;
            }

            if (command.equals("help")) {
                printHelp();
            }

            else if (command.equals("print")) {
                handlePrint(game, parts, setupViewer);
            }

            else if (command.equals("rules")) {
                ConsoleDisplay.printGameIntro();
            }

            else if (command.equals("map")) {
                game.getBoard().printMap();
            }

            else if (command.equals("state")) {
                printState(game);
            }

            else if (command.equals("move")) {
                handleMove(game, parts, setupViewer);
            }

            else if (command.equals("split")) {
                handleSplit(game, parts, setupViewer);
            }

            else if (command.equals("draw")) {
                game.agreeDraw();
            }

            else if (command.equals("swap")) {
                handleSwap(game, parts, setupViewer);
            }

            else if (command.equals("view")) {
                setupViewer = handleView(parts, setupViewer);
            }

            else if (command.equals("start")) {
                game.startGame();
            }

            else {
                System.out.println("Unknown command. Type 'help' for commands.");
            }
        }

        System.out.println("Game ended.");
        printState(game);

        scanner.close();
    }

    private static void setupDemoGame(Game game) {

        /*
         * 这是临时 demo 布局。
         * 之后我们会做真正的 console setup 阶段。
         *
         * 这里建议用 forcePlacePieceForTesting，
         * 因为现在我们不是测试布局规则，
         * 而是想快速进入 PLAYING 阶段玩游戏。
         */

        game.forcePlacePieceForTesting(10, 0,
                new Piece(Rank.GENERAL, Team.RED));

        game.forcePlacePieceForTesting(10, 1,
                new Piece(Rank.BRIGADE, Team.RED));

        game.forcePlacePieceForTesting(10, 2,
                new Piece(Rank.PRIVATE, Team.RED));

        game.forcePlacePieceForTesting(11, 1,
                new Piece(Rank.FLAG, Team.RED));

        game.forcePlacePieceForTesting(1, 0,
                new Piece(Rank.GENERAL, Team.BLUE));

        game.forcePlacePieceForTesting(1, 1,
                new Piece(Rank.BRIGADE, Team.BLUE));

        game.forcePlacePieceForTesting(1, 2,
                new Piece(Rank.PRIVATE, Team.BLUE));

        game.forcePlacePieceForTesting(0, 1,
                new Piece(Rank.FLAG, Team.BLUE));

        System.out.println("Demo pieces placed.");
        game.getBoard().printBoard();
    }

    private static void handleMove(Game game, String[] parts, Team setupViewer) {

        if (parts.length != 5) {
            System.out.println("Usage: move r1 c1 r2 c2");
            return;
        }

        int r1 = parseInt(parts[1]);
        int c1 = parseInt(parts[2]);
        int r2 = parseInt(parts[3]);
        int c2 = parseInt(parts[4]);

        if (!validNumbers(r1, c1, r2, c2)) {
            System.out.println("Invalid coordinates.");
            return;
        }

        boolean success = game.move(r1, c1, r2, c2);

        if (success) {
            System.out.println("Move succeeded.");
        } else {
            System.out.println("Move failed.");
        }

        printBoardByState(game, setupViewer);
    }

    private static void handleSplit(Game game, String[] parts, Team setupViewer) {

        if (parts.length != 5) {
            System.out.println("Usage: split r1 c1 r2 c2");
            return;
        }

        int r1 = parseInt(parts[1]);
        int c1 = parseInt(parts[2]);
        int r2 = parseInt(parts[3]);
        int c2 = parseInt(parts[4]);

        if (!validNumbers(r1, c1, r2, c2)) {
            System.out.println("Invalid coordinates.");
            return;
        }

        boolean success = game.split(r1, c1, r2, c2);

        if (success) {
            System.out.println("Split succeeded.");
        } else {
            System.out.println("Split failed.");
        }

        printBoardByState(game, setupViewer);
    }

    private static void handleSwap(Game game, String[] parts, Team setupViewer) {

        if (parts.length != 5) {
            System.out.println("Usage: swap r1 c1 r2 c2");
            return;
        }

        int r1 = parseInt(parts[1]);
        int c1 = parseInt(parts[2]);
        int r2 = parseInt(parts[3]);
        int c2 = parseInt(parts[4]);

        if (!validNumbers(r1, c1, r2, c2)) {
            System.out.println("Invalid coordinates.");
            return;
        }

        boolean success = game.swapSetupPieces(setupViewer, r1, c1, r2, c2);

        if (success) {
            System.out.println("Swap succeeded.");
        } else {
            System.out.println("Swap failed.");
        }

        printBoardByState(game, setupViewer);
    }

    private static Team handleView(String[] parts, Team currentViewer) {

        if (parts.length != 2) {
            System.out.println("Usage: view red / view blue");
            return currentViewer;
        }

        if (parts[1].equalsIgnoreCase("red")) {
            return Team.RED;
        }

        if (parts[1].equalsIgnoreCase("blue")) {
            return Team.BLUE;
        }

        System.out.println("Usage: view red / view blue");
        return currentViewer;
    }

    private static void handlePrint(Game game, String[] parts, Team setupViewer) {

        // print: 在PLAYING阶段打印当前回合玩家的视角
        if (parts.length == 1) {
            printBoardByState(game, setupViewer);
            return;
        }

        // print red: 打印红方视角
        if (parts[1].equalsIgnoreCase("red")) {
            if (game.getState() == GameState.SETUP) {
                game.printSetupBoardForViewer(Team.RED);
            } else {
                game.printBoardForViewer(Team.RED);
            }
            return;
        }

        // print blue: 打印蓝方视角
        if (parts[1].equalsIgnoreCase("blue")) {
            if (game.getState() == GameState.SETUP) {
                game.printSetupBoardForViewer(Team.BLUE);
            } else {
                game.printBoardForViewer(Team.BLUE);
            }
            return;
        }

        // print full: debug 用，正式暗棋时可以不用
        if (parts[1].equalsIgnoreCase("full")) {
            game.getBoard().printBoard();
            return;
        }

        System.out.println("Usage: print / print red / print blue / print full");
    }

    private static void printBoardByState(Game game, Team setupViewer) {

        if (game.getState() == GameState.SETUP) {
            game.printSetupBoardForViewer(setupViewer);
        } else if (game.getState() == GameState.PLAYING) {
            game.printBoardForViewer(game.getCurrentTurn());
        } else {
            game.getBoard().printBoard();
        }
    }

    private static int parseInt(String s) {

        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static boolean validNumbers(int r1, int c1, int r2, int c2) {

        return r1 >= 0 && c1 >= 0 && r2 >= 0 && c2 >= 0;
    }

    private static void printState(Game game) {

        System.out.println("State: " + game.getState());
        System.out.println("Current turn: " + game.getCurrentTurn());
        System.out.println("Winner: " + game.getWinner());
        System.out.println("Draw: " + game.isDraw());
    }

    private static void printHelp() {

        System.out.println("Commands:");
        System.out.println("  print");
        System.out.println("      Print board from current player's view during PLAYING");
        System.out.println("  print red");
        System.out.println("      Print board from RED's view");
        System.out.println("  print blue");
        System.out.println("      Print board from BLUE's view");
        System.out.println("  print full");
        System.out.println("      Print full board for debugging");
        System.out.println("  rules");
        System.out.println("      Show introduction and full game rules");
        System.out.println("  map");
        System.out.println("      Print map layout");
        System.out.println("  state");
        System.out.println("      Print game state");
        System.out.println("  move r1 c1 r2 c2");
        System.out.println("      Move piece from (r1,c1) to (r2,c2)");
        System.out.println("  split r1 c1 r2 c2");
        System.out.println("      Split piece from (r1,c1) to (r2,c2)");
        System.out.println("  draw");
        System.out.println("      Agree to draw");
        System.out.println("  help");
        System.out.println("      Show commands");
        System.out.println("  quit");
        System.out.println("      Quit console game");
        System.out.println("  swap r1 c1 r2 c2");
        System.out.println("      Swap two pieces during SETUP");
        System.out.println("  view red");
        System.out.println("      Switch console setup view to RED");
        System.out.println("  view blue");
        System.out.println("      Switch console setup view to BLUE");
        System.out.println("  start");
        System.out.println("      Start game after setup");
    }
}