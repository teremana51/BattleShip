package BattleShip.server;

import BattleShip.server.AI.AIGameSession;
import BattleShip.server.utils.LoggerServer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Основной серверный класс для игры "Морской бой".
 * Отвечает за подключение клиентов, управление игровыми комнатами и передачу сообщений.
 */
public class Server {
    private static final int PORT = 12345;

    private ServerSocket serverSocket;

    /** Пул потоков для обработки подключений клиентов */
    private ExecutorService threadPool = Executors.newCachedThreadPool();

    private Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();

    private Map<String, GameSession> gameRooms = new ConcurrentHashMap<>();

    /**
     * Точка входа в сервер.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        new Server().start();
    }

    /**
     * Запуск сервера и ожидание подключений клиентов.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            LoggerServer.info("Сервер запущен на порту " + PORT);
            LoggerServer.info("Ожидание подключений клиентов...");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                LoggerServer.info("\nЗавершение работы сервера...");
                shutdown();
            }));

            while (true) {
                Socket clientSocket = serverSocket.accept();

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                threadPool.execute(clientHandler);
            }
        }
        catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    /**
     * Регистрация нового клиента на сервере.
     *
     * @param client   обработчик клиента
     * @param username имя пользователя
     */
    public synchronized void registerClient(ClientHandler client, String username) {
        connectedClients.put(username, client);

        client.sendMessage("REGISTERED:" + username);

        client.sendGameList();
    }

    /**
     * Создание новой сетевой игры.
     *
     * @param gameName       название игры
     * @param creator        имя создателя
     * @param creatorHandler обработчик создателя
     */
    public synchronized void createGame(String gameName, String creator, ClientHandler creatorHandler) {
        if (gameRooms.containsKey(gameName)) {
            creatorHandler.sendMessage("ERROR:Игра с таким названием уже существует");
            return;
        }

        if (gameName.length() < 3 || gameName.length() > 20) {
            creatorHandler.sendMessage("ERROR:Название игры должно быть от 3 до 20 символов");
            return;
        }

        GameSession game = new GameSession(gameName, creator, creatorHandler, this);
        gameRooms.put(gameName, game);
        creatorHandler.setCurrentGame(game);

        creatorHandler.sendMessage("GAME_CREATED:" + gameName);

        broadcastGameList();
    }

    /**
     * Создание игры против ИИ.
     *
     * @param gameName       название игры
     * @param creator        имя создателя
     * @param creatorHandler обработчик создателя
     */
    public void createAIGame(String gameName, String creator, ClientHandler creatorHandler) {

        AIGameSession AIgame = new AIGameSession(gameName, creator, creatorHandler, this);
        creatorHandler.setCurrentAIGame(AIgame);

        creatorHandler.sendMessage("GAME_CREATED:" + gameName);

    }

    /**
     * Подключение игрока к существующей игре.
     *
     * @param gameName      название игры
     * @param player        имя игрока
     * @param playerHandler обработчик игрока
     * @return true, если игрок успешно подключен, false — иначе
     */
    public synchronized boolean joinGame(String gameName, String player, ClientHandler playerHandler) {
        GameSession game = gameRooms.get(gameName);

        if (game == null) {
            playerHandler.sendMessage("ERROR:Игра не найдена");
            return false;
        }

        if (game.isFull()) {
            playerHandler.sendMessage("ERROR:Игра уже заполнена");
            return false;
        }

        if (game.addPlayer(player, playerHandler)) {
            playerHandler.setCurrentGame(game);

            broadcastGameList();
            return true;
        }

        return false;
    }

    /**
     * Удаление игры с сервера.
     *
     * @param gameName название игры
     */
    public synchronized void removeGame(String gameName) {
        GameSession game = gameRooms.remove(gameName);
        if (game != null) {
            broadcastGameList();
        }
    }

    /**
     * Рассылка списка игр всем подключенным клиентам.
     */
    public void broadcastGameList() {
        String gameList = buildGameList();

        for (ClientHandler client : connectedClients.values()) {
            if (client != null) {
                client.sendMessage("GAME_LIST:" + gameList);
            }
        }
    }

    /**
     * Формирование строки с информацией о всех играх.
     *
     * @return строка со списком игр
     */
    String buildGameList() {
        StringBuilder sb = new StringBuilder();
        for (GameSession game : gameRooms.values()) {
            sb.append(game.getGameName())
                    .append(":")
                    .append(game.getCreator())
                    .append(":")
                    .append(game.getPlayerCount())
                    .append(":")
                    .append(game.getStatus())
                    .append(";");
        }
        String result = sb.toString();
        return result.isEmpty() ? "" : result.substring(0, result.length() - 1);
    }

    /**
     * Удаление клиента с сервера.
     *
     * @param username имя пользователя
     */
    public synchronized void removeClient(String username) {
        connectedClients.remove(username);
    }

    /**
     * Завершение работы сервера: уведомление клиентов, закрытие сокета и завершение потоков.
     */
    private void shutdown() {
        try {
            for (ClientHandler client : connectedClients.values()) {
                client.sendMessage("SERVER_CLOSED");
            }

            LoggerServer.shutdown();

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            threadPool.shutdown();
        }
        catch (IOException e) {
            System.err.println("Ошибка при завершении работы: " + e.getMessage());
        }
    }
}