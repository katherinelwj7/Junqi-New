package board;

import piece.Piece;

public class Tile {

    // 格子类型
    public TileType type;

    // 格子上的棋子
    public Piece piece;

    // 四个方向的连接
    public ConnectionType up;
    public ConnectionType down;
    public ConnectionType left;
    public ConnectionType right;
    public ConnectionType upLeft;
    public ConnectionType upRight;
    public ConnectionType downLeft;
    public ConnectionType downRight;

    public Tile(TileType type) {

        this.type = type;
        this.piece = null;

        // 默认没有连接
        up = ConnectionType.NONE;
        down = ConnectionType.NONE;
        left = ConnectionType.NONE;
        right = ConnectionType.NONE;
        upLeft = ConnectionType.NONE;
        upRight = ConnectionType.NONE;
        downLeft = ConnectionType.NONE;
        downRight = ConnectionType.NONE;
    }
}