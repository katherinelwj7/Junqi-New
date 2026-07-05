package piece;

public class Piece {
    public Rank rank;      // 数字表示大小
    public boolean movable;
    public Team team;
    private boolean revealed = false;

    public Piece(Rank rank, Team team) {
        this.rank = rank;
        this.team = team;

        // 自动判断能不能动
        if (rank == Rank.FLAG || rank == Rank.MINE) {
            this.movable = false;
        } else {
            this.movable = true;
        }
    }

    // 类型判断
    public boolean isBomb() {
        return rank == Rank.BOMB;
    }

    public boolean isMine() {
        return rank == Rank.MINE;
    }

    public boolean isEngineer() {
        return rank == Rank.ENGINEER;
    }

    public boolean isFlag() {
        return rank == Rank.FLAG;
    }

    public boolean isNewRecruit() {
        return rank == Rank.NEW_RECRUIT;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void reveal() {
        revealed = true;
    }
}