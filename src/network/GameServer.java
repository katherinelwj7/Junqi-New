package network;

import game.Game;
import game.GameState;
import piece.Team;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    private static final int ROWS = 12;
    private static final int COLS = 5;

    private final int port;

    private Game game;
    private final List<ClientHandler> clients;

    public GameServer(int port) {
        this.port = port;
        this.game = new Game();
        this.game.setupDefaultLayout();
        this.clients = new ArrayList<>();
    }

    public void start() {

        System.out.println("Starting Junqi server on port " + port + "...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server started. Waiting for players...");

            while (clients.size() < 2) {

                Socket socket = serverSocket.accept();

                Team assignedTeam;

                if (clients.size() == 0) {
                    assignedTeam = Team.RED;
                } else {
                    assignedTeam = Team.BLUE;
                }

                ClientHandler client = new ClientHandler(socket, assignedTeam);
                clients.add(client);

                Thread thread = new Thread(client);
                thread.start();

                client.sendLine("TEAM " + assignedTeam);

                sendBoardToClient(client, "Connected as " + assignedTeam + ".");

                System.out.println(assignedTeam + " connected.");

                if (clients.size() == 2) {
                    broadcastBoards("Both players connected. Setup phase may begin.");
                }
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private synchronized void handleCommand(ClientHandler client, String commandLine) {

        String[] parts = commandLine.trim().split("\\s+");

        if (parts.length == 0) {
            return;
        }

        String command = parts[0].toUpperCase();

        if (command.equals("START")) {

            boolean success = game.startGame();

            if (success) {
                broadcastBoards("Game started.");
            } else {
                sendBoardToClient(client, "Could not start game.");
            }

            return;
        }

        if (command.equals("NEW_GAME")) {
            game = new Game();
            game.setupDefaultLayout();
            broadcastBoards("New game created.");
            return;
        }

        if (command.equals("SWAP")) {

            if (parts.length != 5) {
                sendBoardToClient(client, "Invalid SWAP command.");
                return;
            }

            int r1 = Integer.parseInt(parts[1]);
            int c1 = Integer.parseInt(parts[2]);
            int r2 = Integer.parseInt(parts[3]);
            int c2 = Integer.parseInt(parts[4]);

            boolean success = game.swapSetupPieces(client.team, r1, c1, r2, c2);

            if (success) {
                broadcastBoards(client.team + " swapped two setup pieces.");
            } else {
                sendBoardToClient(client, "Swap failed.");
            }

            return;
        }

        if (command.equals("MOVE")) {

            if (parts.length != 5) {
                sendBoardToClient(client, "Invalid MOVE command.");
                return;
            }

            if (game.getState() != GameState.PLAYING) {
                sendBoardToClient(client, "Move failed. Game is not in PLAYING state.");
                return;
            }

            if (game.getCurrentTurn() != client.team) {
                sendBoardToClient(client, "Move failed. It is not your turn.");
                return;
            }

            int r1 = Integer.parseInt(parts[1]);
            int c1 = Integer.parseInt(parts[2]);
            int r2 = Integer.parseInt(parts[3]);
            int c2 = Integer.parseInt(parts[4]);

            boolean success = game.move(r1, c1, r2, c2);

            if (success) {
                broadcastBoards(client.team + " moved.");
            } else {
                sendBoardToClient(client, "Move failed.");
            }

            return;
        }

        if (command.equals("SPLIT")) {

            if (parts.length != 5) {
                sendBoardToClient(client, "Invalid SPLIT command.");
                return;
            }

            if (game.getState() != GameState.PLAYING) {
                sendBoardToClient(client, "Split failed. Game is not in PLAYING state.");
                return;
            }

            if (game.getCurrentTurn() != client.team) {
                sendBoardToClient(client, "Split failed. It is not your turn.");
                return;
            }

            int r1 = Integer.parseInt(parts[1]);
            int c1 = Integer.parseInt(parts[2]);
            int r2 = Integer.parseInt(parts[3]);
            int c2 = Integer.parseInt(parts[4]);

            boolean success = game.split(r1, c1, r2, c2);

            if (success) {
                broadcastBoards(client.team + " split a piece.");
            } else {
                sendBoardToClient(client, "Split failed.");
            }

            return;
        }

        sendBoardToClient(client, "Unknown command: " + command);
    }

    private synchronized void broadcastBoards(String message) {
        for (ClientHandler client : clients) {
            sendBoardToClient(client, message);
        }
    }

    private void sendBoardToClient(ClientHandler client, String message) {

        client.sendLine("MESSAGE " + message);
        client.sendLine(createStateLine());

        client.sendLine("BOARD");

        for (int r = 0; r < ROWS; r++) {

            StringBuilder row = new StringBuilder();

            for (int c = 0; c < COLS; c++) {

                String display = game.getPieceDisplayAt(r, c, client.team);

                if (c > 0) {
                    row.append("|");
                }

                row.append(display);
            }

            client.sendLine(row.toString());
        }

        client.sendLine("END_BOARD");
    }

    private String createStateLine() {

        String currentTurnText = "-";
        String winnerText = "-";

        if (game.getCurrentTurn() != null) {
            currentTurnText = game.getCurrentTurn().toString();
        }

        if (game.getWinner() != null) {
            winnerText = game.getWinner().toString();
        }

        return "STATE " +
                game.getState() + " " +
                currentTurnText + " " +
                winnerText + " " +
                game.isDraw();
    }

    private class ClientHandler implements Runnable {

        private final Socket socket;
        private final Team team;
        private final BufferedReader in;
        private final PrintWriter out;

        public ClientHandler(Socket socket, Team team) throws IOException {
            this.socket = socket;
            this.team = team;
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }

        public void sendLine(String line) {
            out.println(line);
        }

        @Override
        public void run() {

            try {
                String line;

                while ((line = in.readLine()) != null) {
                    handleCommand(this, line);
                }

            } catch (IOException e) {
                System.out.println(team + " disconnected.");
            }
        }
    }

    public static void main(String[] args) {
        GameServer server = new GameServer(5000);
        server.start();
    }
}