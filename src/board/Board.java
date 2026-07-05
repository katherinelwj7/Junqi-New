package board;

import piece.Piece;
import piece.Team;
import piece.Rank;
import game.GameState;
import console.ConsoleDisplay;

public class Board {

    public Tile[][] grid;

    public Board(int rows, int cols) {
        grid = new Tile[rows][cols];

        // 初始化所有格子
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                grid[i][j] = new Tile(TileType.NORMAL);
            }
        }
    }

    // 放棋子
    public boolean placePiece(int r, int c, Piece p) {

        Tile tile = grid[r][c];

        // 检查军旗摆放是否符合规则
        if (p != null && p.rank == Rank.FLAG && tile.type != TileType.BASE) {
            System.out.println("Flag must be placed in BASE");
            return false;
        }

        // 检查地雷摆放是否符合规则
        if (p != null && p.rank == Rank.MINE) {

            if (p.team == Team.RED && r < 10) {
                System.out.println("Mine must be in last two rows");
                return false;
            }

            if (p.team == Team.BLUE && r > 1) {
                System.out.println("Mine must be in last two rows");
                return false;
            }
        }

        // 检查炸弹摆放是否符合规则
        if (p != null && p.rank == Rank.BOMB) {

            if (p.team == Team.BLUE && r == 5) {
                System.out.println("Bomb cannot be in first row");
                return false;
            }

            if (p.team == Team.RED && r == 6) {
                System.out.println("Bomb cannot be in first row");
                return false;
            }
        }

        grid[r][c].piece = p;
        return true;
    }

    // 获取棋子
    public Piece getPiece(int r, int c) {
        return grid[r][c].piece;
    }

    // 获取格子
    public Tile getTile(int r, int c) {
        return grid[r][c];
    }

    // 找到军旗
    public Piece findFlag(Team team) {

        for (int r = 0; r < grid.length; r++) {

            for (int c = 0; c < grid[0].length; c++) {

                Piece p = getPiece(r, c);

                if (p != null
                        && p.rank == Rank.FLAG
                        && p.team == team) {

                    return p;
                }
            }
        }

        return null;
    }

    // 打印棋盘
    public void printBoard() {

        System.out.print("    ");

        for (int c = 0; c < 5; c++) {
            System.out.printf("%8d", c);
        }

        System.out.println();

        for (int r = 0; r < grid.length; r++) {

            System.out.printf("%3d ", r);

            for (int c = 0; c < grid[0].length; c++) {

                if (grid[r][c].piece == null) {
                    System.out.printf("%8s", ".");
                }
                else if (grid[r][c].piece.rank == Rank.FLAG &&
                        !grid[r][c].piece.isRevealed()) {
                    System.out.printf("%8s", "?*");
                }
                else {
                    //System.out.print(
                    //        grid[i][j].piece.rank.name().charAt(0) + " "
                    //);
                    System.out.printf("%8s", grid[r][c].piece.rank.notationsForPrinting());
                }
            }

            System.out.print("     ");
            System.out.println(ConsoleDisplay.getLegendLine(2 * r));
            System.out.print("                                                 ");
            System.out.println(ConsoleDisplay.getLegendLine(2 * r + 1));
            //System.out.print("                                                 ");
            //System.out.println(ConsoleDisplay.getLegendLine(3 * r + 2));


            //System.out.println();
        }

        printRemainingLegendLines(2 * grid.length);
    }

    // 打印剩余图例
    private void printRemainingLegendLines(int alreadyPrintedLines) {

        for (int i = alreadyPrintedLines; i < ConsoleDisplay.BOARD_LEGEND.length; i++) {
            System.out.println("                                                 " + ConsoleDisplay.getLegendLine(i));
        }
    }

    // 设置格子类型
    public void setTileType(int r, int c, TileType type) {

        grid[r][c].type = type;
    }

    // 设置连接方式（路）
        // 水平连接
    public void connectRight(int r, int c, ConnectionType type) {

        grid[r][c].right = type;
        grid[r][c+1].left = type;
    }
        //垂直连接
    public void connectDown(int r, int c, ConnectionType type) {

        grid[r][c].down = type;
        grid[r+1][c].up = type;
    }
        // 斜向连接
    public void connectDownRight(int r, int c, ConnectionType type) {

        grid[r][c].downRight = type;
        grid[r+1][c+1].upLeft = type;
    }
    public void connectDownLeft(int r, int c, ConnectionType type) {

        grid[r][c].downLeft = type;
        grid[r+1][c-1].upRight = type;
    }

    // 初始化格子类型
    public void initializeTileTypes() {
        setTileType(0, 1, TileType.BASE);
        setTileType(0, 3, TileType.BASE);

        setTileType(2, 1, TileType.CAMP);
        setTileType(2, 3, TileType.CAMP);
        setTileType(3, 2, TileType.CAMP);
        setTileType(4, 1, TileType.CAMP);
        setTileType(4, 3, TileType.CAMP);

        setTileType(7, 1, TileType.CAMP);
        setTileType(7, 3, TileType.CAMP);
        setTileType(8, 2, TileType.CAMP);
        setTileType(9, 1, TileType.CAMP);
        setTileType(9, 3, TileType.CAMP);

        setTileType(11, 1, TileType.BASE);
        setTileType(11, 3, TileType.BASE);
    }

    // 初始化铁路连接
    public void initializeRailways() {
        connectRight(1, 0, ConnectionType.RAILWAY);
        connectRight(1, 1, ConnectionType.RAILWAY);
        connectRight(1, 2, ConnectionType.RAILWAY);
        connectRight(1, 3, ConnectionType.RAILWAY);

        connectRight(5, 0, ConnectionType.RAILWAY);
        connectRight(5, 1, ConnectionType.RAILWAY);
        connectRight(5, 2, ConnectionType.RAILWAY);
        connectRight(5, 3, ConnectionType.RAILWAY);

        connectDown(1, 0, ConnectionType.RAILWAY);
        connectDown(2, 0, ConnectionType.RAILWAY);
        connectDown(3, 0, ConnectionType.RAILWAY);
        connectDown(4, 0, ConnectionType.RAILWAY);
        connectDown(5, 0, ConnectionType.RAILWAY);
        connectDown(6, 0, ConnectionType.RAILWAY);
        connectDown(7, 0, ConnectionType.RAILWAY);
        connectDown(8, 0, ConnectionType.RAILWAY);
        connectDown(9, 0, ConnectionType.RAILWAY);

        connectDown(1, 4, ConnectionType.RAILWAY);
        connectDown(2, 4, ConnectionType.RAILWAY);
        connectDown(3, 4, ConnectionType.RAILWAY);
        connectDown(4, 4, ConnectionType.RAILWAY);
        connectDown(5, 4, ConnectionType.RAILWAY);
        connectDown(6, 4, ConnectionType.RAILWAY);
        connectDown(7, 4, ConnectionType.RAILWAY);
        connectDown(8, 4, ConnectionType.RAILWAY);
        connectDown(9, 4, ConnectionType.RAILWAY);

        connectRight(6, 0, ConnectionType.RAILWAY);
        connectRight(6, 1, ConnectionType.RAILWAY);
        connectRight(6, 2, ConnectionType.RAILWAY);
        connectRight(6, 3, ConnectionType.RAILWAY);

        connectRight(10, 0, ConnectionType.RAILWAY);
        connectRight(10, 1, ConnectionType.RAILWAY);
        connectRight(10, 2, ConnectionType.RAILWAY);
        connectRight(10, 3, ConnectionType.RAILWAY);

        connectDown(5, 2, ConnectionType.RAILWAY);
    }

    // 初始化公路连接
    public void initializeRoads() {
        connectRight(0, 0, ConnectionType.ROAD);
        connectRight(0, 1, ConnectionType.ROAD);
        connectRight(0, 2, ConnectionType.ROAD);
        connectRight(0, 3, ConnectionType.ROAD);

        connectDown(0, 0, ConnectionType.ROAD);
        connectDown(0, 1, ConnectionType.ROAD);
        connectDown(0, 2, ConnectionType.ROAD);
        connectDown(0, 3, ConnectionType.ROAD);
        connectDown(0, 4, ConnectionType.ROAD);

        connectDownRight(1, 0, ConnectionType.ROAD);
        connectDown(1, 1, ConnectionType.ROAD);
        connectDownLeft(1, 2, ConnectionType.ROAD);
        connectDownRight(1, 2, ConnectionType.ROAD);
        connectDown(1, 3, ConnectionType.ROAD);
        connectDownLeft(1, 4, ConnectionType.ROAD);

        connectRight(2, 0, ConnectionType.ROAD);
        connectRight(2, 1, ConnectionType.ROAD);
        connectDownLeft(2, 1, ConnectionType.ROAD);
        connectDown(2, 1, ConnectionType.ROAD);
        connectDownRight(2, 1, ConnectionType.ROAD);
        connectRight(2, 2, ConnectionType.ROAD);
        connectDown(2, 2, ConnectionType.ROAD);
        connectRight(2, 3, ConnectionType.ROAD);
        connectDownLeft(2, 3, ConnectionType.ROAD);
        connectDown(2, 3, ConnectionType.ROAD);
        connectDownRight(2, 3, ConnectionType.ROAD);

        connectRight(3, 0, ConnectionType.ROAD);
        connectDownRight(3, 0, ConnectionType.ROAD);
        connectRight(3, 1, ConnectionType.ROAD);
        connectDown(3, 1, ConnectionType.ROAD);
        connectRight(3, 2, ConnectionType.ROAD);
        connectDownLeft(3, 2, ConnectionType.ROAD);
        connectDown(3, 2, ConnectionType.ROAD);
        connectDownRight(3, 2, ConnectionType.ROAD);
        connectRight(3, 3, ConnectionType.ROAD);
        connectDown(3, 3, ConnectionType.ROAD);
        connectDownLeft(3, 4, ConnectionType.ROAD);

        connectRight(4, 0, ConnectionType.ROAD);
        connectRight(4, 1, ConnectionType.ROAD);
        connectDownLeft(4, 1, ConnectionType.ROAD);
        connectDown(4, 1, ConnectionType.ROAD);
        connectDownRight(4, 1, ConnectionType.ROAD);
        connectRight(4, 2, ConnectionType.ROAD);
        connectDown(4, 2, ConnectionType.ROAD);
        connectRight(4, 3, ConnectionType.ROAD);
        connectDownLeft(4, 3, ConnectionType.ROAD);
        connectDown(4, 3, ConnectionType.ROAD);
        connectDownRight(4, 3, ConnectionType.ROAD);

        connectDownRight(6, 0, ConnectionType.ROAD);
        connectDown(6, 1, ConnectionType.ROAD);
        connectDownLeft(6, 2, ConnectionType.ROAD);
        connectDown(6, 2, ConnectionType.ROAD);
        connectDownRight(6, 2, ConnectionType.ROAD);
        connectDown(6, 3, ConnectionType.ROAD);
        connectDownLeft(6, 4, ConnectionType.ROAD);

        connectRight(7, 0, ConnectionType.ROAD);
        connectRight(7, 1, ConnectionType.ROAD);
        connectDownLeft(7, 1, ConnectionType.ROAD);
        connectDown(7, 1, ConnectionType.ROAD);
        connectDownRight(7, 1, ConnectionType.ROAD);
        connectRight(7, 2, ConnectionType.ROAD);
        connectDown(7, 2, ConnectionType.ROAD);
        connectRight(7, 3, ConnectionType.ROAD);
        connectDownLeft(7, 3, ConnectionType.ROAD);
        connectDown(7, 3, ConnectionType.ROAD);
        connectDownRight(7, 3, ConnectionType.ROAD);

        connectRight(8, 0, ConnectionType.ROAD);
        connectDownRight(8, 0, ConnectionType.ROAD);
        connectRight(8, 1, ConnectionType.ROAD);
        connectDown(8, 1, ConnectionType.ROAD);
        connectRight(8, 2, ConnectionType.ROAD);
        connectDownLeft(8, 2, ConnectionType.ROAD);
        connectDown(8, 2, ConnectionType.ROAD);
        connectDownRight(8, 2, ConnectionType.ROAD);
        connectRight(8, 3, ConnectionType.ROAD);
        connectDown(8, 3, ConnectionType.ROAD);
        connectDownLeft(8, 4, ConnectionType.ROAD);

        connectRight(9, 0, ConnectionType.ROAD);
        connectRight(9, 1, ConnectionType.ROAD);
        connectDownLeft(9, 1, ConnectionType.ROAD);
        connectDown(9, 1, ConnectionType.ROAD);
        connectDownRight(9, 1, ConnectionType.ROAD);
        connectRight(9, 2, ConnectionType.ROAD);
        connectDown(9, 2, ConnectionType.ROAD);
        connectRight(9, 3, ConnectionType.ROAD);
        connectDownLeft(9, 3, ConnectionType.ROAD);
        connectDown(9, 3, ConnectionType.ROAD);
        connectDownRight(9, 3, ConnectionType.ROAD);

        connectDown(10, 0, ConnectionType.ROAD);
        connectDown(10, 1, ConnectionType.ROAD);
        connectDown(10, 2, ConnectionType.ROAD);
        connectDown(10, 3, ConnectionType.ROAD);
        connectDown(10, 4, ConnectionType.ROAD);

        connectRight(11, 0, ConnectionType.ROAD);
        connectRight(11, 1, ConnectionType.ROAD);
        connectRight(11, 2, ConnectionType.ROAD);
        connectRight(11, 3, ConnectionType.ROAD);
    }

    // 初始化整个地图
    public void initializeMap() {

        initializeTileTypes();
        initializeRoads();
        initializeRailways();
    }

    // 打印所有格子类型
    public void printTileTypes() {

        for (int r = 0; r < grid.length; r++) {

            for (int c = 0; c < grid[0].length; c++) {

                Tile tile = grid[r][c];

                switch (tile.type) {

                    case NORMAL:
                        System.out.print(". ");
                        break;

                    case CAMP:
                        System.out.print("C ");
                        break;

                    case BASE:
                        System.out.print("B ");
                        break;
                }
            }

            System.out.println();
        }
    }

    // 打印所有铁路连接
    public void printRailways() {

        for (int r = 0; r < grid.length; r++) {

            for (int c = 0; c < grid[0].length; c++) {

                Tile tile = grid[r][c];

                boolean railway =

                        tile.up == ConnectionType.RAILWAY ||
                                tile.down == ConnectionType.RAILWAY ||
                                tile.left == ConnectionType.RAILWAY ||
                                tile.right == ConnectionType.RAILWAY;

                if (railway) {
                    System.out.print("R ");
                } else {
                    System.out.print(". ");
                }
            }

            System.out.println();
        }
    }

    // 打印所有公路连接
    public void printRoads() {

        for (int r = 0; r < grid.length; r++) {

            for (int c = 0; c < grid[0].length; c++) {

                Tile tile = grid[r][c];

                boolean road =

                        tile.up == ConnectionType.ROAD ||
                                tile.down == ConnectionType.ROAD ||
                                tile.left == ConnectionType.ROAD ||
                                tile.right == ConnectionType.ROAD ||
                                tile.upLeft == ConnectionType.ROAD ||
                                tile.upRight == ConnectionType.ROAD ||
                                tile.downLeft == ConnectionType.ROAD ||
                                tile.downRight == ConnectionType.ROAD;

                if (road) {
                    System.out.print("O ");
                } else {
                    System.out.print(". ");
                }
            }

            System.out.println();
        }
    }

    // 打印显示格子类型和铁路的综合地图
    public void printMap() {

        for (int r = 0; r < grid.length; r++) {

            for (int c = 0; c < grid[0].length; c++) {

                Tile tile = grid[r][c];

                if (tile.type == TileType.BASE) {

                    System.out.print("B ");

                } else if (tile.type == TileType.CAMP) {

                    System.out.print("C ");

                } else {

                    boolean railway =

                            tile.up == ConnectionType.RAILWAY ||
                                    tile.down == ConnectionType.RAILWAY ||
                                    tile.left == ConnectionType.RAILWAY ||
                                    tile.right == ConnectionType.RAILWAY;

                    if (railway) {

                        System.out.print("R ");

                    } else {

                        System.out.print(". ");
                    }
                }
            }

            System.out.println();
        }
    }
}