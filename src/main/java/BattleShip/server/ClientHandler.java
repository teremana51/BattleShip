package BattleShip.server;

import BattleShip.server.AI.AIGameSession;
import BattleShip.server.utils.LoggerServer;

import java.io.*;
import java.net.*;
import java.util.UUID;

/**
 * Класс, обрабатывающий подключение одного клиента к серверу.
 * Слушает команды клиента, управляет игровой сессией и взаимодействием с сервером.
 */
public class ClientHandler implements Runnable {
    private Socket socket;
    private Server server;

    private BufferedReader in;
    private PrintWriter out;

    private String clientId;
    private String username;

    private GameSession currentGame;
    private AIGameSession currentAIGame;

    /**
     * Конструктор обработчика клиента.
     *
     * @param socket сокет подключения клиента
     * @param server ссылка на сервер
     */
    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.clientId = UUID.randomUUID().toString().substring(0, 8);

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }
        catch (IOException e) {
            System.err.println("Ошибка создания потоков ввода/вывода: " + e.getMessage());
        }
    }

    public void setCurrentGame(GameSession game) {
        this.currentGame = game;
    }

    public void setCurrentAIGame(AIGameSession AIGame) {
        this.currentAIGame = AIGame;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Главный метод обработчика клиента.
     * Слушает входящие сообщения и передает их на обработку.
     */
    @Override
    public void run() {
        try {
            sendMessage("CONNECTED:" + clientId);

            String message;
            while ((message = in.readLine()) != null) {
                processClientMessage(message);
            }

        }
        catch (IOException e) {
            LoggerServer.info("CLIENT" + clientId + " DISCONNECTED");
        }
        finally {
            disconnect();
        }
    }

    /**
     * Обработка сообщения от клиента.
     *
     * @param message текст сообщения
     */
    private void processClientMessage(String message) {
        LoggerServer.info(message);

        String[] parts = message.split(":");
        String command = parts[0];

        switch (command) {
            case "REGISTER":
                if (parts.length >= 2) {
                    username = parts[1];
                    server.registerClient(this, username);
                }
                break;

            case "CREATE_GAME":
                if (parts.length >= 2 && username != null) {
                    String gameName = parts[1];
                    if (gameName.equals("AI")) {
                        server.createAIGame(gameName, username, this);
                    }
                    else {
                        server.createGame(gameName, username, this);
                    }
                }
                break;

            case "JOIN_GAME":
                if (parts.length >= 2 && username != null) {
                    String gameName = parts[1];
                    server.joinGame(gameName, username, this);
                }
                break;

            case "LIST_GAMES":
                sendGameList();
                break;

            case "LEAVE_GAME":
                if (currentGame != null) {
                    currentGame.removePlayer(username);
                    currentGame = null;
                }
                if (currentAIGame != null) {
                    sendMessage("LEFT_GAME");
                    currentAIGame.resetGameState();
                    currentAIGame = null;
                }
                break;

            case "SHIPS_PLACED":
                if (currentGame != null && parts.length >= 2) {
                    String shipsData = parts[1];
                    currentGame.processShipsPlaced(this, shipsData);
                }
                if (currentAIGame != null && parts.length >= 2) {
                    String shipsData = parts[1];
                    currentAIGame.processShipsPlaced(this, shipsData);
                }
                break;

            case "READY":
                if (currentGame != null) {
                    currentGame.playerReady(this);
                }
                if (currentAIGame != null) {
                    currentAIGame.startGameAI();
                }
                break;

            case "SHOT":
                if (currentGame != null && parts.length == 3) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    currentGame.processShot(this, x, y);
                }
                if (currentAIGame != null && parts.length == 3) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    currentAIGame.processShotAI(this, x, y);
                }
                break;

            case "SURRENDER":
                String winner = parts[1];
                if (currentGame != null) {
                    currentGame.setWinner(winner);
                    currentGame.setSurrend(true);
                    currentGame.endGame();
                }
                if (currentAIGame != null) {
                    currentAIGame.setWinner(winner);
                    currentAIGame.setSurrend(true);
                    currentAIGame.endGame();
                }
                break;

            case "DISCONNECT":
                disconnect();
                break;
        }
    }

    /**
     * Отправка сообщения клиенту.
     *
     * @param message текст сообщения
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    /** Отправка списка доступных игр клиенту */
    public void sendGameList() {
        if (server != null) {
            String gameList = server.buildGameList();
            sendMessage("GAME_LIST:" + gameList);
        }
    }

    /** Отключение клиента и очистка ресурсов */
    public void disconnect() {
        try {
            if (currentGame != null) {
                currentGame.removePlayer(username);
            }

            if (username != null) {
                server.removeClient(username);
            }

            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

        }
        catch (IOException e) {
            System.err.println("Ошибка при отключении клиента: " + e.getMessage());
        }
    }
}