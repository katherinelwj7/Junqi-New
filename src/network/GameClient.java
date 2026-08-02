package network;

import game.GameState;
import piece.Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameClient {

    private static final int ROWS = 12;
    private static final int COLS = 5;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private final Listener listener;

    public interface Listener {
        void onTeamAssigned(Team team);

        void onStateUpdated(GameState state, Team currentTurn, Team winner, boolean draw, boolean redReady, boolean blueReady);

        void onLastActionUpdated(int fromRow, int fromCol, int toRow, int toCol);

        void onLastActionPathUpdated(List<int[]> path);

        void onMessage(String message);

        void onBoardUpdated(String[][] board);

        void onDrawOfferReceived(Team fromTeam);

        void onNewGameRequestReceived(Team fromTeam);

        void onDisconnected();
    }

    public GameClient(Listener listener) {
        this.listener = listener;
    }

    public void connect(String host, int port) throws IOException {

        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        Thread readerThread = new Thread(this::readLoop);
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void readLoop() {

        try {
            String line;

            while ((line = in.readLine()) != null) {

                if (line.startsWith("TEAM ")) {
                    handleTeamLine(line);
                } else if (line.startsWith("MESSAGE ")) {
                    handleMessageLine(line);
                } else if (line.startsWith("STATE ")) {
                    handleStateLine(line);
                }  else if (line.startsWith("LAST_ACTION ")) {
                    handleLastActionLine(line);
                } else if (line.startsWith("LAST_ACTION_PATH ")) {
                    handleLastActionPathLine(line);
                } else if (line.startsWith("DRAW_OFFER")) {
                    handleDrawOfferLine(line);
                } else if (line.startsWith("NEW_GAME_REQUEST")) {
                    handleNewGameRequestLine(line);
                } else if (line.equals("BOARD")) {
                    handleBoardBlock();
                }
            }

        } catch (IOException e) {
            listener.onDisconnected();
        }
    }

    private void handleLastActionLine(String line) {

        String[] parts = line.split("\\s+");

        int fromRow = Integer.parseInt(parts[1]);
        int fromCol = Integer.parseInt(parts[2]);
        int toRow = Integer.parseInt(parts[3]);
        int toCol = Integer.parseInt(parts[4]);

        listener.onLastActionUpdated(fromRow, fromCol, toRow, toCol);
    }

    private void handleLastActionPathLine(String line) {

        String rest = line.substring("LAST_ACTION_PATH ".length()).trim();

        List<int[]> path = new ArrayList<>();

        if (rest.equals("NONE")) {
            listener.onLastActionPathUpdated(path);
            return;
        }

        String[] points = rest.split(";");

        for (String point : points) {

            String[] parts = point.split(",");

            if (parts.length != 2) {
                continue;
            }

            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);

            path.add(new int[]{row, col});
        }

        listener.onLastActionPathUpdated(path);
    }

    private void handleTeamLine(String line) {
        String teamText = line.substring("TEAM ".length()).trim();
        Team team = Team.valueOf(teamText);
        listener.onTeamAssigned(team);
    }

    private void handleMessageLine(String line) {
        String message = line.substring("MESSAGE ".length());
        listener.onMessage(message);
    }

    private void handleStateLine(String line) {

        String[] parts = line.split("\\s+");

        GameState state = GameState.valueOf(parts[1]);

        Team currentTurn = null;
        Team winner = null;

        if (!parts[2].equals("-")) {
            currentTurn = Team.valueOf(parts[2]);
        }

        if (!parts[3].equals("-")) {
            winner = Team.valueOf(parts[3]);
        }

        boolean draw = Boolean.parseBoolean(parts[4]);

        boolean redReady = false;
        boolean blueReady = false;

        if (parts.length >= 7) {
            redReady = Boolean.parseBoolean(parts[5]);
            blueReady = Boolean.parseBoolean(parts[6]);
        }

        listener.onStateUpdated(state, currentTurn, winner, draw, redReady, blueReady);
    }

    private void handleBoardBlock() throws IOException {

        String[][] board = new String[ROWS][COLS];

        for (int r = 0; r < ROWS; r++) {

            String rowLine = in.readLine();
            String[] cells = rowLine.split("\\|", -1);

            for (int c = 0; c < COLS; c++) {
                board[r][c] = cells[c];
            }
        }

        String endLine = in.readLine();

        if (!endLine.equals("END_BOARD")) {
            throw new IOException("Invalid board block.");
        }

        listener.onBoardUpdated(board);
    }

    private void handleDrawOfferLine(String line) {

        String[] parts = line.split("\\s+");

        if (parts.length < 2) {
            return;
        }

        Team fromTeam = Team.valueOf(parts[1]);
        listener.onDrawOfferReceived(fromTeam);
    }

    private void handleNewGameRequestLine(String line) {

        String[] parts = line.split("\\s+");

        if (parts.length < 2) {
            return;
        }

        Team fromTeam = Team.valueOf(parts[1]);
        listener.onNewGameRequestReceived(fromTeam);
    }

    public void sendStart() {
        sendLine("START");
    }

    public void sendNewGame() {
        sendLine("NEW_GAME");
    }

    public void sendSwap(int r1, int c1, int r2, int c2) {
        sendLine("SWAP " + r1 + " " + c1 + " " + r2 + " " + c2);
    }

    public void sendMove(int r1, int c1, int r2, int c2) {
        sendLine("MOVE " + r1 + " " + c1 + " " + r2 + " " + c2);
    }

    public void sendSplit(int r1, int c1, int r2, int c2) {
        sendLine("SPLIT " + r1 + " " + c1 + " " + r2 + " " + c2);
    }

    private void sendLine(String line) {
        if (out != null) {
            out.println(line);
        }
    }

    public void sendDraw() {
        sendLine("DRAW");
    }

    public void sendDrawOffer() {
        sendLine("DRAW_OFFER");
    }

    public void sendDrawResponse(boolean accepted) {
        if (accepted) {
            sendLine("DRAW_RESPONSE YES");
        } else {
            sendLine("DRAW_RESPONSE NO");
        }
    }

    public void sendNewGameRequest() {
        sendLine("NEW_GAME_REQUEST");
    }

    public void sendNewGameResponse(boolean accepted) {
        if (accepted) {
            sendLine("NEW_GAME_RESPONSE YES");
        } else {
            sendLine("NEW_GAME_RESPONSE NO");
        }
    }
}