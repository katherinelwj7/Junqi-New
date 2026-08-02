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
import java.util.ArrayList;
import java.util.List;
import piece.Piece;
import utils.MoveValidator;

public class GameServer {

    private static final int ROWS = 12;
    private static final int COLS = 5;

    private final int port;

    private Game game;
    private final List<ClientHandler> clients;

    private boolean redReady = false;
    private boolean blueReady = false;

    private boolean redDrawReady = false;
    private boolean blueDrawReady = false;

    private int successfulActionCount = 0;

    private int redLastDrawOfferAction = -1000;
    private int blueLastDrawOfferAction = -1000;

    private Team pendingDrawOfferTeam = null;
    private Team pendingNewGameOfferTeam = null;

    private static final int DRAW_OFFER_COOLDOWN = 5;

    private boolean redNewGameReady = false;
    private boolean blueNewGameReady = false;

    private int lastFromRow = -1;
    private int lastFromCol = -1;
    private int lastToRow = -1;
    private int lastToCol = -1;

    private List<int[]> lastActionPath = new ArrayList<>();

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

            successfulActionCount = 0;
            redLastDrawOfferAction = -1000;
            blueLastDrawOfferAction = -1000;
            pendingDrawOfferTeam = null;
            pendingNewGameOfferTeam = null;

            clearLastAction();

            broadcastMessage("Both players agreed. New game created.");
            broadcastUpdates();
            return;
        }

        if (command.equals("NEW_GAME_REQUEST")) {

            if (game.getState() != GameState.FINISHED) {
                sendMessageToClient(client, "New game can only be requested after the game is finished.");
                sendUpdateToClient(client);
                return;
            }

            ClientHandler opponent = findOpponent(client);

            if (opponent == null) {
                sendMessageToClient(client, "No opponent is connected.");
                sendUpdateToClient(client);
                return;
            }

            pendingNewGameOfferTeam = client.team;

            sendMessageToClient(client, "New game request sent. Waiting for opponent.");
            opponent.sendLine("NEW_GAME_REQUEST " + client.team);

            sendUpdateToClient(client);
            sendUpdateToClient(opponent);

            return;
        }

        if (command.equals("NEW_GAME_RESPONSE")) {

            if (parts.length != 2) {
                sendMessageToClient(client, "Invalid new game response.");
                sendUpdateToClient(client);
                return;
            }

            if (pendingNewGameOfferTeam == null) {
                sendMessageToClient(client, "There is no pending new game request.");
                sendUpdateToClient(client);
                return;
            }

            if (client.team == pendingNewGameOfferTeam) {
                sendMessageToClient(client, "You cannot respond to your own new game request.");
                sendUpdateToClient(client);
                return;
            }

            boolean accepted = parts[1].equalsIgnoreCase("YES");

            ClientHandler requester = findClientByTeam(pendingNewGameOfferTeam);

            if (!accepted) {
                if (requester != null) {
                    sendMessageToClient(requester, client.team + " declined the new game request.");
                    sendUpdateToClient(requester);
                }

                sendMessageToClient(client, "You declined the new game request.");
                sendUpdateToClient(client);

                pendingNewGameOfferTeam = null;
                return;
            }

            game = new Game();
            game.setupDefaultLayout();

            redReady = false;
            blueReady = false;
            redNewGameReady = false;
            blueNewGameReady = false;

            successfulActionCount = 0;
            redLastDrawOfferAction = -1000;
            blueLastDrawOfferAction = -1000;
            pendingDrawOfferTeam = null;
            pendingNewGameOfferTeam = null;

            clearLastAction();

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
                // Setup swaps are private, do not remember publicly
                //clearLastAction();

                sendMessageToClient(client, getGameMessageOr("Swap succeeded."));

                // Board still updates for both players, but opponent only see hidden backs
                broadcastUpdates();
            } else {

                // Swap failures are private, do not broadcast
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

            Piece movingPiece = game.getBoard().getPiece(r1, c1);

            List<int[]> path = MoveValidator.findMovePath(
                    game.getBoard(),
                    movingPiece,
                    r1, c1, r2, c2
            );

            boolean success = game.move(r1, c1, r2, c2);

            if (success) {
                rememberLastActionPath(path);

                successfulActionCount++;
                pendingDrawOfferTeam = null;

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

            Piece splittingPiece = game.getBoard().getPiece(r1, c1);

            List<int[]> path = MoveValidator.findMovePath(
                    game.getBoard(),
                    splittingPiece,
                    r1, c1,
                    r2, c2,
                    true
            );

            boolean success = game.split(r1, c1, r2, c2);

            if (success) {
                rememberLastActionPath(path);

                successfulActionCount++;
                pendingDrawOfferTeam = null;

                sendMessageToClient(client, getGameMessageOr("Split succeeded."));
                broadcastUpdates();
            } else {
                sendMessageToClient(client, getGameMessageOr("Split failed."));
                sendUpdateToClient(client);
            }

            return;
        }

        if (command.equals("DRAW_OFFER")) {

            if (game.getState() != GameState.PLAYING) {
                sendMessageToClient(client, "Draw can only be offered during the game.");
                sendUpdateToClient(client);
                return;
            }

            int remaining = getRemainingDrawCooldown(client.team);

            if (remaining > 0) {
                sendMessageToClient(client,
                        "Please do not offer draws too frequently. Wait "
                                + remaining + " more successful turn(s).");
                sendUpdateToClient(client);
                return;
            }

            ClientHandler opponent = findOpponent(client);

            if (opponent == null) {
                sendMessageToClient(client, "No opponent is connected.");
                sendUpdateToClient(client);
                return;
            }

            pendingDrawOfferTeam = client.team;
            setLastDrawOfferAction(client.team, successfulActionCount);

            sendMessageToClient(client, "Draw offer sent. Waiting for opponent.");
            opponent.sendLine("DRAW_OFFER " + client.team);

            sendUpdateToClient(client);
            sendUpdateToClient(opponent);

            return;
        }

        if (command.equals("DRAW_RESPONSE")) {

            if (parts.length != 2) {
                sendMessageToClient(client, "Invalid draw response.");
                sendUpdateToClient(client);
                return;
            }

            if (pendingDrawOfferTeam == null) {
                sendMessageToClient(client, "There is no pending draw offer.");
                sendUpdateToClient(client);
                return;
            }

            if (client.team == pendingDrawOfferTeam) {
                sendMessageToClient(client, "You cannot respond to your own draw offer.");
                sendUpdateToClient(client);
                return;
            }

            boolean accepted = parts[1].equalsIgnoreCase("YES");

            ClientHandler offerer = findClientByTeam(pendingDrawOfferTeam);

            if (!accepted) {
                if (offerer != null) {
                    sendMessageToClient(offerer, client.team + " declined the draw offer.");
                    sendUpdateToClient(offerer);
                }

                sendMessageToClient(client, "You declined the draw offer.");
                sendUpdateToClient(client);

                pendingDrawOfferTeam = null;
                return;
            }

            boolean success = game.agreeDraw();

            pendingDrawOfferTeam = null;

            if (success) {
                broadcastMessage("Both players agreed to a draw.");
                broadcastUpdates();
            } else {
                sendMessageToClient(client, getGameMessageOr("Could not agree to draw."));
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

    private void setDrawReady(Team team, boolean ready) {
        if (team == Team.RED) {
            redDrawReady = ready;
        } else if (team == Team.BLUE) {
            blueDrawReady = ready;
        }
    }

    private boolean bothPlayersWantDraw() {
        return redDrawReady && blueDrawReady;
    }

    private int getLastDrawOfferAction(Team team) {
        if (team == Team.RED) {
            return redLastDrawOfferAction;
        }

        return blueLastDrawOfferAction;
    }

    private void setLastDrawOfferAction(Team team, int value) {
        if (team == Team.RED) {
            redLastDrawOfferAction = value;
        } else {
            blueLastDrawOfferAction = value;
        }
    }

    private int getRemainingDrawCooldown(Team team) {
        int lastOffer = getLastDrawOfferAction(team);
        int passed = successfulActionCount - lastOffer;
        int remaining = DRAW_OFFER_COOLDOWN - passed;

        if (remaining < 0) {
            return 0;
        }

        return remaining;
    }

    private ClientHandler findClientByTeam(Team team) {

        for (ClientHandler client : clients) {
            if (client.team == team) {
                return client;
            }
        }

        return null;
    }

    private void rememberLastAction(int fromRow, int fromCol, int toRow, int toCol) {
        List<int[]> path = new ArrayList<>();
        path.add(new int[]{fromRow, fromCol});
        path.add(new int[]{toRow, toCol});

        rememberLastActionPath(path);
    }

    private void rememberLastActionPath(List<int[]> path) {

        lastActionPath.clear();

        if (path == null || path.size() < 2) {
            clearLastAction();
            return;
        }

        for (int[] point : path) {
            lastActionPath.add(new int[]{point[0], point[1]});
        }

        int[] first = lastActionPath.get(0);
        int[] last = lastActionPath.get(lastActionPath.size() - 1);

        lastFromRow = first[0];
        lastFromCol = first[1];
        lastToRow = last[0];
        lastToCol = last[1];
    }

    private void clearLastAction() {
        lastFromRow = -1;
        lastFromCol = -1;
        lastToRow = -1;
        lastToCol = -1;

        lastActionPath.clear();
    }

    private String createLastActionLine() {
        return "LAST_ACTION " +
                lastFromRow + " " +
                lastFromCol + " " +
                lastToRow + " " +
                lastToCol;
    }

    private String createLastActionPathLine() {

        if (lastActionPath == null || lastActionPath.size() < 2) {
            return "LAST_ACTION_PATH NONE";
        }

        StringBuilder builder = new StringBuilder("LAST_ACTION_PATH ");

        for (int i = 0; i < lastActionPath.size(); i++) {

            int[] point = lastActionPath.get(i);

            if (i > 0) {
                builder.append(";");
            }

            builder.append(point[0])
                    .append(",")
                    .append(point[1]);
        }

        return builder.toString();
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
        client.sendLine(createLastActionLine());
        client.sendLine(createLastActionPathLine());
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