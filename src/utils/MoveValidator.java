package utils;

import piece.Piece;
import piece.Rank;
import board.Board;
import board.Tile;
import board.ConnectionType;
import board.TileType;
import piece.Team;


public class MoveValidator {

    public static Piece resolveBattle(Piece attacker, Piece defender) {

        // 炸弹：同归于尽
        if (attacker.isBomb() || defender.isBomb()) {
            return null;
        }

        // 地雷逻辑
        if (defender.isMine()) {
            if (attacker.isEngineer()) {
                return attacker; // 工兵赢
            } else {
                return defender; // 地雷赢
            }
        }

        // 普通比较
        if (attacker.rank.battlePower() > defender.rank.battlePower()) {
            return attacker;
        } else if (attacker.rank.battlePower() < defender.rank.battlePower()) {
            return defender;
        } else {
            return null; // 同归于尽
        }
    }

    public static boolean canMove(Board board, Piece piece, int r1, int c1, int r2, int c2) {
        // 地雷 & 军旗不能动
        if (piece.isMine() || piece.isFlag()) {
            return false;
        }

        int dr = Math.abs(r1 - r2);
        int dc = Math.abs(c1 - c2);

        // 原地不动
        if (dr + dc == 0) {
            return false;
        }

        // 大本营里的棋子不能动
        Tile startTile = board.getTile(r1, c1);

        if (startTile.type == TileType.BASE) {
            System.out.println("Piece in BASE cannot move");

            return false;
        }

        // 行营里已有棋子时，其它棋子不能进入
        Tile targetTile = board.getTile(r2, c2);

        if (targetTile.type == TileType.CAMP && targetTile.piece != null) {
            System.out.println("Camp already occupied");

            return false;
        }

        // 工兵拐弯
        if (piece.isEngineer()) {

            if (engineerCanReach(board, r1, c1, r2, c2)) {
                return true;
            }
        }

        // 铁路线直线移动
        if (isStraightLine(r1,c1,r2,c2)) {

            if (clearRailwayPath(board, r1,c1,r2,c2)) {
                return true;
            }
        }

        if (validRoadMove(board, r1, c1, r2, c2)) {
            return true;
        }

        return false;
    }

    // 新增重载canMove()：用于允许分裂进行营然后融合的情况

    public static boolean canMove(Board board, Piece piece,
                                  int r1, int c1, int r2, int c2,
                                  boolean allowOccupiedCamp) {
        // 地雷 & 军旗不能动
        if (piece.isMine() || piece.isFlag()) {
            return false;
        }

        int dr = Math.abs(r1 - r2);
        int dc = Math.abs(c1 - c2);

        // 原地不动
        if (dr + dc == 0) {
            return false;
        }

        // 大本营里的棋子不能动
        Tile startTile = board.getTile(r1, c1);

        if (startTile.type == TileType.BASE) {
            System.out.println("Piece in BASE cannot move");

            return false;
        }

        // 行营里已有棋子：普通情况不能进入，但是split+merge允许
        Tile targetTile = board.getTile(r2, c2);

        if (targetTile.type == TileType.CAMP && targetTile.piece != null && !allowOccupiedCamp) {
            System.out.println("Camp already occupied");

            return false;
        }

        // 工兵拐弯
        if (piece.isEngineer()) {

            if (engineerCanReach(board, r1, c1, r2, c2)) {
                return true;
            }
        }

        // 铁路线直线移动
        if (isStraightLine(r1, c1, r2, c2)) {

            if (clearRailwayPath(board, r1, c1, r2, c2)) {
                return true;
            }
        }

        if (validRoadMove(board, r1, c1, r2, c2)) {
            return true;
        }

        return false;
    }

    public static boolean isStraightLine(int r1, int c1, int r2, int c2) {
        return (r1 == r2 || c1 == c2);
    }

    public static boolean clearRailwayPath(Board board,
                                           int r1, int c1,
                                           int r2, int c2) {

        // 水平
        if (r1 == r2) {

            int step = (c2 > c1) ? 1 : -1;

            for (int c = c1; c != c2; c += step) {

                Tile current = board.getTile(r1, c);

                // 检查连接
                if (step == 1 &&
                        current.right != ConnectionType.RAILWAY) {
                    return false;
                }

                if (step == -1 &&
                        current.left != ConnectionType.RAILWAY) {
                    return false;
                }

                // 中间有棋子
                if (c != c1 &&
                        board.getPiece(r1, c) != null) {
                    return false;
                }
            }

            return true;
        }

        // 垂直移动
        if (c1 == c2) {

            int step = (r2 > r1) ? 1 : -1;

            for (int r = r1; r != r2; r += step) {

                Tile current = board.getTile(r, c1);

                // 检查铁路连接
                if (step == 1 &&
                        current.down != ConnectionType.RAILWAY) {
                    return false;
                }

                if (step == -1 &&
                        current.up != ConnectionType.RAILWAY) {
                    return false;
                }

                // 中间不能有棋子
                if (r != r1 &&
                        board.getPiece(r, c1) != null) {
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    // 检查公路移动是否合理
    public static boolean validRoadMove(Board board,
                                        int r1, int c1,
                                        int r2, int c2) {

        int dr = r2 - r1;
        int dc = c2 - c1;

        // 只能走一步
        if (Math.abs(dr) > 1 || Math.abs(dc) > 1) {
            return false;
        }

        // 不能原地
        if (dr == 0 && dc == 0) {
            return false;
        }

        Tile current = board.getTile(r1, c1);

        // 上
        if (dr == -1 && dc == 0) {
            return current.up == ConnectionType.ROAD;
        }

        // 下
        if (dr == 1 && dc == 0) {
            return current.down == ConnectionType.ROAD;
        }

        // 左
        if (dr == 0 && dc == -1) {
            return current.left == ConnectionType.ROAD;
        }

        // 右
        if (dr == 0 && dc == 1) {
            return current.right == ConnectionType.ROAD;
        }

        // 左上
        if (dr == -1 && dc == -1) {
            return current.upLeft == ConnectionType.ROAD;
        }

        // 右上
        if (dr == -1 && dc == 1) {
            return current.upRight == ConnectionType.ROAD;
        }

        // 左下
        if (dr == 1 && dc == -1) {
            return current.downLeft == ConnectionType.ROAD;
        }

        // 右下
        if (dr == 1 && dc == 1) {
            return current.downRight == ConnectionType.ROAD;
        }

        return false;
    }

    // 工兵拐弯 wrapper
    public static boolean engineerCanReach(Board board,
                                           int r1, int c1,
                                           int r2, int c2) {

        boolean[][] visited =
                new boolean[board.grid.length][board.grid[0].length];

        return dfs(board, r1, c1, r2, c2, visited);
    }

    // DFS
    public static boolean dfs(Board board,
                              int r, int c,
                              int targetR, int targetC,
                              boolean[][] visited) {

        // 到达终点
        if (r == targetR && c == targetC) {
            return true;
        }

        // 已经访问过
        if (visited[r][c]) {
            return false;
        }

        // 标记访问
        visited[r][c] = true;

        Tile current = board.getTile(r, c);

        // 上
        if (current.up == ConnectionType.RAILWAY) {

            int nr = r - 1;
            int nc = c;

            if (board.getPiece(nr, nc) == null ||
                    (nr == targetR && nc == targetC)) {

                if (dfs(board, nr, nc,
                        targetR, targetC, visited)) {
                    return true;
                }
            }
        }

        // 下
        if (current.down == ConnectionType.RAILWAY) {

            int nr = r + 1;
            int nc = c;

            if (board.getPiece(nr, nc) == null ||
                    (nr == targetR && nc == targetC)) {

                if (dfs(board, nr, nc,
                        targetR, targetC, visited)) {
                    return true;
                }
            }
        }

        // 左
        if (current.left == ConnectionType.RAILWAY) {

            int nr = r;
            int nc = c - 1;

            if (board.getPiece(nr, nc) == null ||
                    (nr == targetR && nc == targetC)) {

                if (dfs(board, nr, nc,
                        targetR, targetC, visited)) {
                    return true;
                }
            }
        }

        // 右
        if (current.right == ConnectionType.RAILWAY) {

            int nr = r;
            int nc = c + 1;

            if (board.getPiece(nr, nc) == null ||
                    (nr == targetR && nc == targetC)) {

                if (dfs(board, nr, nc,
                        targetR, targetC, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    // 判断两个棋子是否可以融合
    public static boolean canMerge(Piece a,
                                   Piece b) {

        // 必须同队
        if (a.team != b.team) {
            return false;
        }

        // 必须同 rank
        if (a.rank != b.rank) {
            return false;
        }

        // 必须能升级
        if (a.rank.nextRank() == null) {
            return false;
        }

        return true;
    }

    // 判断一个棋子是否可以分裂
    public static boolean canSplit(Piece piece) {

        return piece.rank.previousRank() != null;
    }

    // 判断是不是只走了一步
    public static boolean isOneStep(int r1, int c1, int r2, int c2) {
        int dr = Math.abs(r1 - r2);
        int dc = Math.abs(c1 - c2);
        return dr <= 1 && dc <= 1 && dr + dc > 0;
    }
}