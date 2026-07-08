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
import javafx.application.Platform;
import network.GameClient;

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
    private Label messageLabel;

    private ActionMode actionMode = ActionMode.MOVE;
    private Label modeLabel;

    private static final int ROWS = 12;
    private static final int COLS = 5;

    private static final double CELL_W = 80;
    private static final double CELL_H = 50;

    private static final double BUTTON_W = 56;
    private static final double BUTTON_H = 34;

    private static final double BOARD_PADDING = 30;

    private ScrollPane sideScrollPane;

    private Button startButton;


    private static final int PORT = 5000;

    private boolean networkMode = false;
    private GameClient networkClient;

    private String[][] networkBoard = new String[12][5];

    private GameState networkState = GameState.SETUP;
    private Team networkCurrentTurn = null;
    private Team networkWinner = null;
    private boolean networkDraw = false;

    private Button viewRedButton;
    private Button viewBlueButton;

    @Override
    public void start(Stage stage) {

        game = new Game();
        game.setupDefaultLayout();
        initializeNetworkBoard();

        localViewer = Team.RED;
        cellButtons = new Button[12][5];

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));

        statusLabel = new Label();
        messageLabel = new Label("Welcome to Junqi.");

        messageLabel.setStyle(
                "-fx-background-color: #FFF3C4;" +
                        "-fx-text-fill: #4A3B00;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8;" +
                        "-fx-border-color: #D6B656;" +
                        "-fx-border-width: 1;"
        );

        Pane boardPane = createBoardPane();

        VBox sidePanel = createSidePanel();

        ScrollPane sideScrollPane = new ScrollPane(sidePanel);
        sideScrollPane.setFitToWidth(true);
        sideScrollPane.setPrefWidth(280);
        sideScrollPane.setMinWidth(230);
        sideScrollPane.setMaxWidth(360);

        VBox topBox = new VBox(6);
        topBox.getChildren().addAll(statusLabel, messageLabel);

        root.setTop(topBox);
        root.setCenter(boardPane);
        root.setRight(sideScrollPane);

        refreshBoard();

        Scene scene = new Scene(root, 800, 600);
        sideScrollPane.prefWidthProperty().bind(
                scene.widthProperty().multiply(0.36)
        );
        scene.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            updateResponsiveStyle(root, newWidth.doubleValue());
        });

        stage.setTitle("Junqi");
        stage.setScene(scene);
        updateResponsiveStyle(root, 800);
        stage.show();
    }

    private void initializeNetworkBoard() {

        for (int r = 0; r < 12; r++) {
            for (int c = 0; c < 5; c++) {
                networkBoard[r][c] = ".";
            }
        }
    }

    private void showMessage(String message, boolean success) {

        messageLabel.setText(message);

        if (success) {
            messageLabel.setStyle(
                    "-fx-background-color: #DFF3DD;" +
                            "-fx-text-fill: #235423;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8;" +
                            "-fx-border-color: #7EB77A;" +
                            "-fx-border-width: 1;"
            );
        } else {
            messageLabel.setStyle(
                    "-fx-background-color: #F8D7DA;" +
                            "-fx-text-fill: #842029;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 8;" +
                            "-fx-border-color: #D28A92;" +
                            "-fx-border-width: 1;"
            );
        }
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

        viewRedButton = new Button("View RED");
        viewBlueButton = new Button("View BLUE");
        startButton = new Button("Start Game");

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

            if (networkMode) {

                if (networkState == GameState.FINISHED) {
                    networkClient.sendNewGame();
                    showMessage("New game request sent.", true);
                } else {
                    networkClient.sendStart();
                    showMessage("Start request sent.", true);
                }

                return;
            }

            if (game.getState() == GameState.FINISHED) {
                resetGame();
                return;
            }

            boolean success = game.startGame();

            if (success) {
                showMessage("Game started.", true);
            } else {
                showMessage("Could not start game.", false);
            }

            refreshBoard();
            updateStatus();
            updateStartButton();
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

        Button connectLocalhostButton = new Button("Connect Localhost");
        connectLocalhostButton.setOnAction(e -> connectToServer("localhost"));

        sidePanel.getChildren().addAll(
                viewRedButton,
                viewBlueButton,
                connectLocalhostButton,
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

    private void connectToServer(String host) {

        if (networkMode) {
            showMessage("Already connected to server.", false);
            return;
        }

        networkClient = new GameClient(new GameClient.Listener() {

            @Override
            public void onTeamAssigned(Team team) {
                Platform.runLater(() -> {
                    localViewer = team;
                    networkMode = true;

                    viewRedButton.setDisable(true);
                    viewBlueButton.setDisable(true);

                    showMessage("Connected as " + team + ".", true);
                    updateStatus();
                });
            }

            @Override
            public void onStateUpdated(GameState state, Team currentTurn, Team winner, boolean draw) {
                Platform.runLater(() -> {
                    networkState = state;
                    networkCurrentTurn = currentTurn;
                    networkWinner = winner;
                    networkDraw = draw;

                    updateStatus();
                    updateStartButton();
                });
            }

            @Override
            public void onMessage(String message) {
                Platform.runLater(() -> {
                    boolean success = !message.toLowerCase().contains("failed")
                            && !message.toLowerCase().contains("could not")
                            && !message.toLowerCase().contains("invalid")
                            && !message.toLowerCase().contains("not your turn");

                    showMessage(message, success);
                });
            }

            @Override
            public void onBoardUpdated(String[][] board) {
                Platform.runLater(() -> {
                    networkBoard = board;
                    refreshBoard();
                });
            }

            @Override
            public void onDisconnected() {
                Platform.runLater(() -> {
                    showMessage("Disconnected from server.", false);
                });
            }
        });

        try {
            networkClient.connect(host, PORT);
            showMessage("Connecting to server...", true);
        } catch (Exception e) {
            showMessage("Connection failed: " + e.getMessage(), false);
        }
    }

    private void updateStartButton() {

        if (startButton == null) {
            return;
        }

        GameState state;

        if (networkMode) {
            state = networkState;
        } else {
            state = game.getState();
        }

        if (state == GameState.SETUP) {
            startButton.setText("Start Game");
            startButton.setDisable(false);
        } else if (state == GameState.PLAYING) {
            startButton.setText("Game Started");
            startButton.setDisable(true);
        } else {
            startButton.setText("New Game");
            startButton.setDisable(false);
        }
    }

    private void resetGame() {

        game = new Game();
        game.setupDefaultLayout();

        localViewer = Team.RED;
        actionMode = ActionMode.MOVE;

        selectedRow = -1;
        selectedCol = -1;

        showMessage("New game created.", true);

        refreshBoard();
        updateStatus();
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

        if (networkMode) {
            handleNetworkAction(fromRow, fromCol, row, col);
            refreshBoard();
            updateStatus();
            return;
        }

        selectedRow = -1;
        selectedCol = -1;

        if (game.getState() == GameState.SETUP) {

            boolean success = game.swapSetupPieces(localViewer, fromRow, fromCol, row, col);

            if (success) {
                showMessage("Swap succeeded.", true);
            } else {
                showMessage("Swap failed.", false);
            }

        } else if (game.getState() == GameState.PLAYING) {

            boolean success;

            if (actionMode == ActionMode.MOVE) {
                success = game.move(fromRow, fromCol, row, col);

                if (success) {
                    showMessage("Move succeeded. Current turn: " + game.getCurrentTurn(), true);
                } else {
                    showMessage("Move failed. Current turn: " + game.getCurrentTurn(), false);
                }

            } else {
                success = game.split(fromRow, fromCol, row, col);

                if (success) {
                    showMessage("Split succeeded. Current turn: " + game.getCurrentTurn(), true);
                } else {
                    showMessage("Split failed. Current turn: " + game.getCurrentTurn(), false);
                }
            }

        } else {
            statusLabel.setText("Game is finished.");
        }

        refreshBoard();
        updateStatus();
    }

    private void handleNetworkAction(int fromRow, int fromCol, int toRow, int toCol) {

        if (networkClient == null) {
            showMessage("Not connected to server.", false);
            return;
        }

        if (networkState == GameState.SETUP) {
            networkClient.sendSwap(fromRow, fromCol, toRow, toCol);
            showMessage("Swap request sent.", true);
            return;
        }

        if (networkState == GameState.PLAYING) {

            if (networkCurrentTurn != localViewer) {
                showMessage("It is not your turn.", false);
                return;
            }

            if (actionMode == ActionMode.MOVE) {
                networkClient.sendMove(fromRow, fromCol, toRow, toCol);
                showMessage("Move request sent.", true);
            } else {
                networkClient.sendSplit(fromRow, fromCol, toRow, toCol);
                showMessage("Split request sent.", true);
            }

            return;
        }

        showMessage("Game is finished.", false);
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

                String display;

                if (networkMode) {
                    display = networkBoard[r][c];
                } else {
                    display = game.getPieceDisplayAt(r, c, localViewer);
                }

                cellButtons[r][c].setText(getButtonText(display));

                String style = getCellBaseStyle(r, c) + getPieceStyle(r, c, display);

                if (r == selectedRow && c == selectedCol) {
                    style += "-fx-border-color: black;" +
                            "-fx-border-width: 3;";
                }

                cellButtons[r][c].setStyle(style);
            }
        }
    }

    private void updateStatus() {

        if (networkMode) {

            if (networkState == GameState.SETUP) {
                statusLabel.setText("Network Setup. You are: " + localViewer);
            } else if (networkState == GameState.PLAYING) {
                statusLabel.setText("Network Game. Current turn: " + networkCurrentTurn
                        + ". You are: " + localViewer
                        + ". Mode: " + actionMode);
            } else {
                statusLabel.setText("Network Game Finished. Winner: " + networkWinner
                        + ". Draw: " + networkDraw);
            }

        } else {

            if (game.getState() == GameState.SETUP) {
                statusLabel.setText("Setup phase. Local viewer: " + localViewer);
            } else if (game.getState() == GameState.PLAYING) {
                statusLabel.setText("Playing. Current turn: " + game.getCurrentTurn()
                        + ". Local viewer: " + localViewer
                        + ". Mode: " + actionMode);
            } else {
                statusLabel.setText("Game finished. Winner: " + game.getWinner()
                        + ". Draw: " + game.isDraw());
            }
        }

        if (modeLabel != null) {
            modeLabel.setText("Mode: " + actionMode);
        }

        updateStartButton();
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

    private String getPieceStyle(int r, int c, String display) {

        if (display == null || display.equals(".")) {
            return "-fx-text-fill: #333333;";
        }

        // 敌方未知棋子：GameServer 仍然发送 "??"
        // UI 显示成棋子背面
        if ("??".equals(display)) {
            return "-fx-background-color: #6F6F6F;" +
                    "-fx-text-fill: transparent;" +
                    "-fx-font-weight: bold;";
        }

        // 敌方军旗被 revealed
        if ("FL".equals(display)) {
            return "-fx-background-color: #8A6F3E;" +
                    "-fx-text-fill: #FFFDF7;" +
                    "-fx-font-weight: bold;";
        }

        Team displayTeam;

        if (networkMode) {
            displayTeam = localViewer;
        } else {
            if (game.getBoard().getPiece(r, c) == null) {
                return "-fx-text-fill: #333333;";
            }

            displayTeam = game.getBoard().getPiece(r, c).team;
        }

        if (displayTeam == Team.RED) {
            return "-fx-background-color: #C86B63;" +
                    "-fx-text-fill: #FFFDF7;" +
                    "-fx-font-weight: bold;";
        }

        if (displayTeam == Team.BLUE) {
            return "-fx-background-color: #5F7FA8;" +
                    "-fx-text-fill: #FFFDF7;" +
                    "-fx-font-weight: bold;";
        }

        return "-fx-text-fill: #333333;";
    }

    private String getButtonText(String display) {

        if (display == null) {
            return ".";
        }

        if ("??".equals(display)) {
            return "";
        }

        return display;
    }

    public static void main(String[] args) {
        launch(args);
    }
}