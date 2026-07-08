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

    private boolean redReady = false;
    private boolean blueReady = false;

    private boolean redNewGameReady = false;
    private boolean blueNewGameReady = false;

    public GameServer(int port) {
        this.port = port;
        this.game = new Game();
        this.game.setupDefaultLayout();
        this.clients = new ArrayList<>();
    }

    private void setReady(Team team, boolean ready) {
        if (team == Team.RED) {
            redReady = ready;
        } else if (team == Team.BLUE) {
            blueReady = ready;
        }
    }

    private void setNewGameReady(Team team, boolean ready) {
        if (team == Team.RED) {
            redNewGameReady = ready;
        } else if (team == Team.BLUE) {
            blueNewGameReady = ready;
        }
    }

    private boolean bothPlayersWantNewGame() {
        return redNewGameReady && blueNewGameReady;
    }

    private boolean bothPlayersReady() {
        return redReady && blueReady;
    }

    private String getReadyMessage(ClientHandler client) {
        Team otherTeam = client.team == Team.RED ? Team.BLUE : Team.RED;

        return client.team + " is ready. Waiting for " + otherTeam + ".";
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

            if (game.getState() != GameState.SETUP) {
                sendMessageToClient(client, "Game cannot be started now.");
                sendUpdateToClient(client);
                return;
            }

            setReady(client.team, true);

            if (clients.size() < 2) {
                sendMessageToClient(client, "Ready. Waiting for another player to connect.");
                broadcastUpdates();
                return;
            }

            if (!bothPlayersReady()) {
                sendMessageToClient(client, getReadyMessage(client));
                broadcastUpdates();
                return;
            }

            boolean success = game.startGame();

            if (success) {
                broadcastMessage("Both players are ready. Game started.");
                broadcastUpdates();
            } else {
                sendMessageToClient(client, getGameMessageOr("Could not start game."));
                sendUpdateToClient(client);
            }

            return;
        }

        if (command.equals("NEW_GAME")) {

            if (game.getState() != GameState.FINISHED) {
                sendMessageToClient(client, "New game is only available after the game finishes.");
                sendUpdateToClient(client);
                return;
            }

            setNewGameReady(client.team, true);

            if (!bothPlayersWantNewGame()) {
                sendMessageToClient(client, "New game request sent. Waiting for opponent.");
                sendUpdateToClient(client);

                ClientHandler opponent = findOpponent(client);

                if (opponent != null) {
                    sendMessageToClient(opponent, client.team + " wants a new game.");
                    sendUpdateToClient(opponent);
                }

                return;
            }

            game = new Game();
            game.setupDefaultLayout();

            redReady = false;
            blueReady = false;
            redNewGameReady = false;
            blueNewGameReady = false;

            broadcastMessage("Both players agreed. New game created.");
            broadcastUpdates();
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
                sendMessageToClient(client, getGameMessageOr("Swap succeeded."));
                broadcastUpdates();
            } else {
                sendMessageToClient(client, getGameMessageOr("Swap failed."));
                sendUpdateToClient(client);
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
                sendMessageToClient(client, getGameMessageOr("Move succeeded."));
                broadcastUpdates();
            } else {
                sendMessageToClient(client, getGameMessageOr("Move failed."));
                sendUpdateToClient(client);
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
                sendMessageToClient(client, getGameMessageOr("Split succeeded."));
                broadcastUpdates();
            } else {
                sendMessageToClient(client, getGameMessageOr("Split failed."));
                sendUpdateToClient(client);
            }

            return;
        }

        sendBoardToClient(client, "Unknown command: " + command);
    }

    private ClientHandler findOpponent(ClientHandler client) {

        for (ClientHandler other : clients) {
            if (other != client) {
                return other;
            }
        }

        return null;
    }

    private void sendMessageToClient(ClientHandler client, String message) {
        client.sendLine("MESSAGE " + message);
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            sendMessageToClient(client, message);
        }
    }

    private synchronized void broadcastUpdates() {
        for (ClientHandler client : clients) {
            sendUpdateToClient(client);
        }
    }

    private void sendUpdateToClient(ClientHandler client) {

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

    private String getGameMessageOr(String defaultMessage) {

        String message = game.getLastMessage();

        if (message == null || message.isEmpty()) {
            return defaultMessage;
        }

        return message;
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
                game.isDraw() + " " +
                redReady + " " +
                blueReady;
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