package ui;

import game.Game;
import game.GameState;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import board.ConnectionType;
import board.TileType;
import piece.Piece;
import piece.Team;

public class JunqiApp extends Application {

    private enum ActionMode {
        MOVE,
        SPLIT
    }

    private Game game;
    private Team localViewer;

    private Button[][] cellButtons;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private Label statusLabel;

    private ActionMode actionMode = ActionMode.MOVE;
    private Label modeLabel;

    private static final int ROWS = 12;
    private static final int COLS = 5;

    private static final double CELL_W = 80;
    private static final double CELL_H = 50;

    private static final double BUTTON_W = 56;
    private static final double BUTTON_H = 34;

    private static final double BOARD_PADDING = 30;

    @Override
    public void start(Stage stage) {

        game = new Game();
        game.setupDefaultLayout();

        localViewer = Team.RED;
        cellButtons = new Button[12][5];

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));

        statusLabel = new Label("Setup phase. Viewer: " + localViewer);

        Pane boardPane = createBoardPane();

        VBox sidePanel = createSidePanel();

        ScrollPane sideScrollPane = new ScrollPane(sidePanel);
        sideScrollPane.setFitToWidth(true);
        sideScrollPane.setPrefWidth(280);
        sideScrollPane.setMinWidth(230);
        sideScrollPane.setMaxWidth(360);

        root.setTop(statusLabel);
        root.setCenter(boardPane);
        root.setRight(sideScrollPane);

        refreshBoard();

        Scene scene = new Scene(root, 800, 600);
        scene.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            updateResponsiveStyle(root, newWidth.doubleValue());
        });

        stage.setTitle("Junqi");
        stage.setScene(scene);
        updateResponsiveStyle(root, 800);
        stage.show();
    }

    private void updateResponsiveStyle(BorderPane root, double width) {

        int fontSize;

        if (width < 700) {
            fontSize = 11;
        } else if (width < 1000) {
            fontSize = 13;
        } else {
            fontSize = 15;
        }

        root.setStyle("-fx-font-size: " + fontSize + "px;");
    }

    private Pane createBoardPane() {

        Pane pane = new Pane();

        double width = BOARD_PADDING * 2 + COLS * CELL_W;
        double height = BOARD_PADDING * 2 + ROWS * CELL_H;

        pane.setPrefSize(width, height);
        pane.setMinSize(width, height);

        drawConnections(pane);

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {

                Button button = new Button();

                button.setPrefSize(BUTTON_W, BUTTON_H);
                /*button.setPrefSize(CELL_W, CELL_H);
                button.setMinSize(CELL_W, CELL_H);
                button.setMaxSize(CELL_W, CELL_H);*/
                button.setFocusTraversable(false);

                double x = BOARD_PADDING + c * CELL_W + (CELL_W - BUTTON_W) / 2;
                double y = BOARD_PADDING + r * CELL_H + (CELL_H - BUTTON_H) / 2;

                button.setLayoutX(x);
                button.setLayoutY(y);

                final int row = r;
                final int col = c;

                button.setOnAction(e -> handleCellClick(row, col));

                cellButtons[r][c] = button;
                pane.getChildren().add(button);
            }
        }

        return pane;
    }

    private void drawConnections(Pane pane) {

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {

                if (game.getBoard().getTile(r, c).right != ConnectionType.NONE) {
                    addConnectionLine(pane, r, c, r, c + 1,
                            game.getBoard().getTile(r, c).right);
                }

                if (game.getBoard().getTile(r, c).down != ConnectionType.NONE) {
                    addConnectionLine(pane, r, c, r + 1, c,
                            game.getBoard().getTile(r, c).down);
                }

                if (game.getBoard().getTile(r, c).downRight != ConnectionType.NONE) {
                    addConnectionLine(pane, r, c, r + 1, c + 1,
                            game.getBoard().getTile(r, c).downRight);
                }

                if (game.getBoard().getTile(r, c).downLeft != ConnectionType.NONE) {
                    addConnectionLine(pane, r, c, r + 1, c - 1,
                            game.getBoard().getTile(r, c).downLeft);
                }
            }
        }
    }

    private void addConnectionLine(Pane pane,
                                   int r1, int c1,
                                   int r2, int c2,
                                   ConnectionType type) {

        if (r2 < 0 || r2 >= ROWS || c2 < 0 || c2 >= COLS) {
            return;
        }

        double x1 = getCellCenterX(c1);
        double y1 = getCellCenterY(r1);
        double x2 = getCellCenterX(c2);
        double y2 = getCellCenterY(r2);

        Line line = new Line(x1, y1, x2, y2);

        if (type == ConnectionType.RAILWAY) {
            line.setStrokeWidth(4);
            line.setStyle("-fx-stroke: #4A4A4A;");
        } else if (type == ConnectionType.ROAD) {
            line.setStrokeWidth(2);
            line.setStyle("-fx-stroke: #A57952;");
        }

        pane.getChildren().add(line);
    }

    private double getCellCenterX(int c) {
        return BOARD_PADDING + c * CELL_W + CELL_W / 2;
    }

    private double getCellCenterY(int r) {
        return BOARD_PADDING + r * CELL_H + CELL_H / 2;
    }

    private VBox createSidePanel() {

        VBox sidePanel = new VBox(10);
        sidePanel.setPadding(new Insets(10));

        Button viewRedButton = new Button("View RED");
        Button viewBlueButton = new Button("View BLUE");
        Button startButton = new Button("Start Game");

        modeLabel = new Label("Mode: MOVE");

        Button moveModeButton = new Button("Move Mode");
        Button splitModeButton = new Button("Split Mode");
        Button clearSelectionButton = new Button("Clear Selection");

        Label legendTitle = new Label("Legend");
        Label legendLabel = new Label(getLegendText());

        Label ruleTitle = new Label("Special Rules");
        Label ruleLabel = new Label(getShortRulesText());

        Button tutorialButton = new Button("Open Tutorial");

        viewRedButton.setOnAction(e -> {
            localViewer = Team.RED;
            selectedRow = -1;
            selectedCol = -1;
            refreshBoard();
            updateStatus();
        });

        viewBlueButton.setOnAction(e -> {
            localViewer = Team.BLUE;
            selectedRow = -1;
            selectedCol = -1;
            refreshBoard();
            updateStatus();
        });

        startButton.setOnAction(e -> {
            boolean success = game.startGame();

            if (success) {
                statusLabel.setText("Game started. Current turn: " + game.getCurrentTurn());
            } else {
                statusLabel.setText("Could not start game.");
            }

            refreshBoard();
            updateStatus();
        });

        moveModeButton.setOnAction(e -> {
            actionMode = ActionMode.MOVE;
            selectedRow = -1;
            selectedCol = -1;
            modeLabel.setText("Mode: MOVE");
            statusLabel.setText("Move mode selected.");
            refreshBoard();
        });

        splitModeButton.setOnAction(e -> {
            actionMode = ActionMode.SPLIT;
            selectedRow = -1;
            selectedCol = -1;
            modeLabel.setText("Mode: SPLIT");
            statusLabel.setText("Split mode selected.");
            refreshBoard();
        });

        clearSelectionButton.setOnAction(e -> {
            selectedRow = -1;
            selectedCol = -1;
            statusLabel.setText("Selection cleared.");
            refreshBoard();
        });

        tutorialButton.setOnAction(e -> showTutorialWindow());

        legendLabel.setWrapText(true);
        ruleLabel.setWrapText(true);

        sidePanel.getChildren().addAll(
                viewRedButton,
                viewBlueButton,
                startButton,
                modeLabel,
                moveModeButton,
                splitModeButton,
                clearSelectionButton,
                legendTitle,
                legendLabel,
                ruleTitle,
                ruleLabel,
                tutorialButton
        );

        sidePanel.setMinWidth(260);

        return sidePanel;
    }

    private void handleCellClick(int row, int col) {

        if (selectedRow == -1) {
            selectedRow = row;
            selectedCol = col;

            statusLabel.setText("Selected: (" + row + ", " + col + ")");
            refreshBoard();
            return;
        }

        int fromRow = selectedRow;
        int fromCol = selectedCol;

        selectedRow = -1;
        selectedCol = -1;

        if (game.getState() == GameState.SETUP) {

            boolean success = game.swapSetupPieces(localViewer, fromRow, fromCol, row, col);

            if (success) {
                statusLabel.setText("Swap succeeded.");
            } else {
                statusLabel.setText("Swap failed.");
            }

        } else if (game.getState() == GameState.PLAYING) {

            boolean success;

            if (actionMode == ActionMode.MOVE) {
                success = game.move(fromRow, fromCol, row, col);

                if (success) {
                    statusLabel.setText("Move succeeded. Current turn: " + game.getCurrentTurn());
                } else {
                    statusLabel.setText("Move failed. Current turn: " + game.getCurrentTurn());
                }

            } else {
                success = game.split(fromRow, fromCol, row, col);

                if (success) {
                    statusLabel.setText("Split succeeded. Current turn: " + game.getCurrentTurn());
                } else {
                    statusLabel.setText("Split failed. Current turn: " + game.getCurrentTurn());
                }
            }

        } else {
            statusLabel.setText("Game is finished.");
        }

        refreshBoard();
        updateStatus();
    }

    private String getLegendText() {
        return
                "GL = GENERAL\n" +
                "BG = BRIGADE\n" +
                "RE = REGIMENT\n" +
                "BT = BATTALION\n" +
                "CO = COMPANY\n" +
                "PL = PLATOON\n" +
                "SQ = SQUAD\n" +
                "PR = PRIVATE\n" +
                "EN = ENGINEER\n" +
                "NR = NEW RECRUIT\n" +
                "BO = BOMB\n" +
                "MI = MINE\n" +
                "FL = FLAG\n" +
                "?? = Unknown enemy piece";
    }

    private String getShortRulesText() {
        return
                "Power:\n" +
                        "GL > BG > RE > BT > CO > PL > SQ > PR > EN = NR\n\n" +
                        "BOMB: destroys both pieces.\n" +
                        "MINE: beats attackers except ENGINEER/BOMB.\n" +
                        "ENGINEER: removes MINE and turns on railway.\n" +
                        "NEW RECRUIT: same battling power as ENGINEER,\n" +
                        "but cannot remove MINE / turn on railway\n" +
                        "FLAG: can be captured by any movable piece,\n" +
                        "captured means that side loses.";
    }

    private String getTutorialText() {
        return
                "Victory Conditions: \n" +
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
                        "Apart from these restrictions, players may customize their setup before the game starts. After the game begins, MINE and FLAG cannot move. \n" +
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
                        "Pieces that cannot move include FLAG, MINE, and any piece that has entered BASE.\n";
    }

    private void showTutorialWindow() {

        Stage tutorialStage = new Stage();

        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setText(getTutorialText());

        textArea.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Arial';"
        );

        Scene scene = new Scene(textArea, 700, 600);

        tutorialStage.setTitle("How to Play");
        tutorialStage.setScene(scene);
        tutorialStage.show();
    }

    private void refreshBoard() {

        for (int r = 0; r < 12; r++) {
            for (int c = 0; c < 5; c++) {

                String text = game.getPieceDisplayAt(r, c, localViewer);

                cellButtons[r][c].setText(text);

                String style = getCellBaseStyle(r, c) + getPieceTextStyle(r, c);

                if (r == selectedRow && c == selectedCol) {
                    style += "-fx-border-color: black;" +
                            "-fx-border-width: 3;";
                }

                cellButtons[r][c].setStyle(style);
            }
        }
    }

    private void updateStatus() {

        if (game.getState() == GameState.SETUP) {
            statusLabel.setText("Setup phase. Viewer: " + localViewer);
        } else if (game.getState() == GameState.PLAYING) {
            statusLabel.setText("Playing. Current turn: " + game.getCurrentTurn()
                    + ". Viewer: " + localViewer
                    + ". Mode: " + actionMode);
        } else {
            statusLabel.setText("Game finished. Winner: " + game.getWinner()
                    + ". Draw: " + game.isDraw());
        }

        if (modeLabel != null) {
            modeLabel.setText("Mode: " + actionMode);
        }
    }

    private String getCellBaseStyle(int r, int c) {

        TileType type = game.getBoard().getTile(r, c).type;

        String backgroundColor;

        if (type == TileType.CAMP) {
            backgroundColor = "#BFE3B4";
        } else if (type == TileType.BASE) {
            backgroundColor = "#D8C3A5";
        } else {
            backgroundColor = "#F5E6C8";
        }

        return "-fx-background-color: " + backgroundColor + ";" +
                "-fx-border-color: #8A7A66;" +
                "-fx-border-width: 1;" +
                "-fx-font-weight: bold;";
    }

    private String getPieceTextStyle(int r, int c) {

        Piece piece = game.getBoard().getPiece(r, c);

        if (piece == null) {
            return "-fx-text-fill: #333333;";
        }

        String display = game.getPieceDisplayAt(r, c, localViewer);

        if (display.equals("??")) {
            return "-fx-text-fill: #555555;";
        }

        if (piece.team == Team.RED) {
            return "-fx-text-fill: #B22222;";
        }

        if (piece.team == Team.BLUE) {
            return "-fx-text-fill: #1F4E8C;";
        }

        return "-fx-text-fill: #333333;";
    }



    public static void main(String[] args) {
        launch(args);
    }
}