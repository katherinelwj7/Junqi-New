package console;

public class ConsoleDisplay {

    public static final String[] BOARD_LEGEND = {
            "Legend:",
            /*"  GL = GENERAL",
            "  BG = BRIGADE",
            "  RE = REGIMENT",
            "  BT = BATTALION",
            "  CO = COMPANY",
            "  PL = PLATOON",
            "  SQ = SQUAD",
            "  PR = PRIVATE",
            "  EN = ENGINEER",
            "  NR = NEW RECRUIT",
            "  BO = BOMB",
            "  MI = MINE",
            "  FL = FLAG",
            "  ?? = Unknown enemy piece",*/
            "  GL = GENERAL     BG = BRIGADE",
            "  RE = REGIMENT    BT = BATTALION",
            "  CO = COMPANY     PL = PLATOON",
            "  SQ = SQUAD       PR = PRIVATE",
            "  EN = ENGINEER    NR = NEW RECRUIT",
            "  BO = BOMB        MI = MINE",
            "  FL = FLAG        ?? = Unknown enemy piece",
            "",
            "Power order:",
            "  GL > BG > RE > BT > CO > PL > SQ > PR > EN = NR",
            "",
            "Special rules:",
            "  BOMB: destroys both pieces",
            "  MINE: beats attackers except ENGINEER/BOMB",
            "  ENGINEER: removes MINE and can turn on railway",
            "  NEW RECRUIT: same battling power as ENGINEER, ",
            "       but cannot remove MINE / turn on railway",
            "  FLAG: can be captured by any movable piece, ",
            "       captured means that side loses",
            "",
            "Common commands:",
            "  move r1 c1 r2 c2",
            "  split r1 c1 r2 c2",
            "  swap r1 c1 r2 c2"
    };

    public static String getLegendLine(int index) {
        if (index < 0 || index >= BOARD_LEGEND.length) {
            return "";
        }

        return BOARD_LEGEND[index];
    }

    public static void printGameIntro() {
        System.out.println("How to play:");
        System.out.println();

        System.out.println("Victory Conditions: \n" +
                "\n" +
                "Victory is achieved if: \n" +
                "Your opponent has no movable pieces left, OR\n" +
                "Any of your movable pieces capture the opponent's FLAG. \n" +
                "A draw occurs if no capture is made by either side for 70 consecutive moves.\n" +
                "\n" +
                "\n" +
                "Capture Rules: \n" +
                "\n" +
                "Battling power: \n" +
                "GENERAL > BRIGADE > REGIMENT > BATTALION > COMPANY > PLATOON > SQUAD > PRIVATE > ENGINEER = NEW RECRUIT. \n" +
                "ENGINEER can capture MINE; BOMB destroys both itself and any piece it encounters. \n" +
                "\n" +
                "CAMP: \n" +
                "Entering a CAMP (there are 5 for each side, marked by green circles at coordinates (2,1), (2,3), (3,2), (4,1), (4,3), (7,1), (7,3), (8,2), (9,1), (9,3)) signifies entering an absolute safe zone; pieces inside a CAMP cannot be attacked by other pieces. No pieces may be placed inside CAMP before the game begins.\n" +
                "\n" +
                "\n" +
                "Setup Rules: \n" +
                "\n" +
                "BOMB cannot be placed in the first row; \n" +
                "MINE must be placed in the last two rows; \n" +
                "FLAG must be placed in one of the two BASEs on the back row (the square frames at (0,1), (0,3), (11,1), (11,3)), while the other BASE holds any piece acting as a decoy flag. \n" +
                "\n" +
                "Apart from these restrictions, players may customize their setup before the game starts, but MINE and FLAG cannot be moved once their starting positions are set. \n" +
                "\n" +
                "Pieces that have entered BASE cannot move. If the opponent captures a piece inside your BASE, the enemy piece that entered BASE also becomes immobile.\n" +
                "\n" +
                "\n" +
                "Movement Rules: \n" +
                "\n" +
                "Paths consist of RAILWAY (marked with striped lines) and ROAD (single lines). \n" +
                "\n" +
                "Except for immobile pieces, pieces on RAILWAY can move any distance in a straight line; \n" +
                "\n" +
                "ENGINEER can turn corners while on RAILWAY (provided the path is not blocked by any friendly or enemy pieces). \n" +
                "\n" +
                "On ROAD, any movable piece can only move one step at a time (either straight or diagonally, provided a connecting line exists).\n" +
                "\n" +
                "\n" +
                "Splitting and Merging: \n" +
                "\n" +
                "Each piece can split into two pieces of the next lower rank (except for GENERAL). Ex. BRIGADE can split into two REGIMENTs. \n" +
                "\n" +
                "Two identical pieces can merge into a piece of the next higher rank; Ex. two REGIMENTs can merge into one BRIGADE. \n" +
                "\n" +
                "When a piece splits, it produces two pieces of a lower rank, 'a' and 'b'; 'a' remains in the original position, while 'b' appears one space away. The player can choose where 'b' appears upon splitting, provided the placement is valid. \n" +
                "\n" +
                "Merging occurs when piece 'a' moves to the position of piece 'b', transforming them into a single piece of a higher rank. \n" +
                "\n" +
                "GENERAL cannot split, nor can two BRIGADEs merge to become a GENERAL. \n" +
                "\n" +
                "Within their own territory, a PRIVATE can split into two NEW RECRUITs; NEW RECRUIT is equivalent in rank to ENGINEER but cannot clear mines or make turns on RAILWAY. \n" +
                "\n" +
                "If NEW RECRUIT crosses the mountain boundary into the opponent's territory, it transforms into ENGINEER.\n" +
                "\n" +
                "\n" +
                "Special Notes:\n" +
                "\n" +
                "Once GENERAL is eliminated, the location of that side's FLAG is revealed.\n" +
                "BOMB can collide with MINE, resulting in both pieces being eliminated.\n" +
                "Except for ENGINEER and BOMB, any piece that collides with MINE is eliminated by it (meaning the piece is removed, but MINE remains).\n" +
                "Pieces that cannot move include FLAG, MINE, and any piece that has entered BASE.\n");
    }
}