package network;

import game.GameState;
import piece.Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClient {

    private static final int ROWS = 12;
    private static final int COLS = 5;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private final Listener listener;

    public interface Listener {
        void onTeamAssigned(Team team);

        void onStateUpdated(GameState state, Team currentTurn, Team winner, boolean draw);

        void onMessage(String message);

        void onBoardUpdated(String[][] board);

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
                } else if (line.equals("BOARD")) {
                    handleBoardBlock();
                }
            }

        } catch (IOException e) {
            listener.onDisconnected();
        }
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

        listener.onStateUpdated(state, currentTurn, winner, draw);
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
}