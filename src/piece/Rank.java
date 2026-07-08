package piece;

public enum Rank {
    FLAG,       // 军旗（不能动）
    MINE,       // 地雷（不能动）
    BOMB,       // 炸弹

    NEW_RECRUIT, // 新兵
    ENGINEER,   // 工兵
    PRIVATE,    // 排长
    SQUAD,      // 连长
    PLATOON,    // 营长
    COMPANY,    // 团长
    BATTALION,  // 旅长
    REGIMENT,   // 师长
    BRIGADE,    // 军长
    GENERAL;     // 司令

    // 判断大小
    public int battlePower() {
        switch (this) {
            case NEW_RECRUIT:
            case ENGINEER:
                return 1;

            case PRIVATE:
                return 2;

            case SQUAD:
                return 3;

            case PLATOON:
                return 4;

            case COMPANY:
                return 5;

            case BATTALION:
                return 6;

            case REGIMENT:
                return 7;

            case BRIGADE:
                return 8;

            case GENERAL:
                return 9;

            default:
                return 0;
        }
    }

    public String notationsForPrinting() {

        switch(this) {
            case FLAG:
                return "FL";

            case MINE:
                return "MI";

            case BOMB:
                return "BO";

            case NEW_RECRUIT:
                return "NR";

            case ENGINEER:
                return "EN";

            case PRIVATE:
                return "PR";

            case SQUAD:
                return "SQ";

            case PLATOON:
                return "PL";

            case COMPANY:
                return "CO";

            case BATTALION:
                return "BT";

            case REGIMENT:
                return "RE";

            case BRIGADE:
                return "BG";

            case GENERAL:
                return "GL";

            default:
                return "0";
        }
    }

    public Rank nextRank() {

        switch(this) {

            case ENGINEER:
            case NEW_RECRUIT:
                return Rank.PRIVATE;

            case PRIVATE:
                return Rank.SQUAD;

            case SQUAD:
                return Rank.PLATOON;

            case PLATOON:
                return Rank.COMPANY;

            case COMPANY:
                return Rank.BATTALION;

            case BATTALION:
                return Rank.REGIMENT;

            case REGIMENT:
                return Rank.BRIGADE;

            default:
                return null;
        }
    }

    public Rank previousRank() {

        switch(this) {

            case PRIVATE:
                return Rank.NEW_RECRUIT;

            case SQUAD:
                return Rank.PRIVATE;

            case PLATOON:
                return Rank.SQUAD;

            case COMPANY:
                return Rank.PLATOON;

            case BATTALION:
                return Rank.COMPANY;

            case REGIMENT:
                return Rank.BATTALION;

            case BRIGADE:
                return Rank.REGIMENT;

            default:
                return null;
        }
    }
}