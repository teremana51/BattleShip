package BattleShip.server;

import BattleShip.client.model.Cell;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Класс, представляющий игровую сессию между двумя игроками.
 * Управляет состоянием игры, ходами, проверкой попаданий и победы.
 */
public class GameSession {
    private static final AtomicInteger sessionCounter = new AtomicInteger(1);

    private int player1Hits = 0;
    private int player2Hits = 0;

    private String winner;

    private boolean isSurrend = false;

    private final int sessionId;
    private final String gameName;
    private String creator;
    private final Server server;

    private ClientHandler player1;
    private ClientHandler player2;

    private String player1Username;
    private String player2Username;

    private String player1Ships = "";
    private String player2Ships = "";

    private boolean player1Ready = false;
    private boolean player2Ready = false;

    private boolean gameStarted = false;
    private ClientHandler currentTurn;


    private boolean player1ShipsPlaced = false;
    private boolean player2ShipsPlaced = false;

    public enum GameStatus {
        WAITING, PLACING_SHIPS, IN_PROGRESS, FINISHED,
    }

    private GameStatus status;

    /**
     * Конструктор игровой сессии.
     *
     * @param gameName       название игры
     * @param creator        имя создателя
     * @param creatorHandler обработчик создателя
     * @param server         ссылка на сервер
     */
    public GameSession(String gameName, String creator, ClientHandler creatorHandler, Server server) {
        this.sessionId = sessionCounter.getAndIncrement();
        this.gameName = gameName;
        this.creator = creator;
        this.server = server;

        this.player1 = creatorHandler;
        this.player1Username = creator;

        status = GameStatus.PLACING_SHIPS;
    }

    /**
     * Вложенный класс для хранения информации о корабле.
     * Хранит координаты клеток, тип корабля и состояние попаданий.
     */
    private static class ShipInfo {
        int type;
        int x;
        int y;
        int orientation;
        private List<Cell> cells; // Добавляем поле для хранения клеток

        ShipInfo(int type, int x, int y, int orientation) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.orientation = orientation;
            this.cells = new ArrayList<>();
            initializeCells(); // Инициализируем клетки при создании
        }

        private void initializeCells() {
            int size = getSize();
            for (int i = 0; i < size; i++) {
                int cellX = x + (orientation == 0 ? i : 0);
                int cellY = y + (orientation == 1 ? i : 0);
                if (cellX >= 0 && cellX < 10 && cellY >= 0 && cellY < 10) {
                    cells.add(new Cell(cellX, cellY, Cell.State.EMPTY));
                }
            }
        }

        List<Cell> getCells() {
            return cells;
        }

        public boolean hitCell(int x, int y) {
            for (Cell cell : cells) {
                if (cell.getX() == x && cell.getY() == y) {
                    cell.setState(Cell.State.HIT);
                    return true;
                }
            }
            return false;
        }

        public boolean isSunk() {
            for (Cell cell : cells) {
                if (cell.getState() != Cell.State.HIT) {
                    return false;
                }
            }
            return true;
        }

        private int getSize() {
            switch (type) {
                case 0: return 4;
                case 1: return 3;
                case 2: return 2;
                case 3: return 1;
                default: return 0;
            }
        }
    }

    public boolean isFull() {
        return player2 != null;
    }

    public String getGameName() {
        return gameName;
    }

    public String getCreator() {
        return creator;
    }

    public int getPlayerCount() {
        int count = 0;
        if (player1 != null) count++;
        if (player2 != null) count++;
        return count;
    }

    public String getStatus() {
        if (gameStarted) return "IN_PROGRESS";
        if (player2 != null) return "PLACING_SHIPS";
        return "WAITING";
    }

    public void setSurrend (boolean isSurrend) {
        this.isSurrend = isSurrend;
    }

    /**
     * Добавление второго игрока в игру.
     *
     * @param player        имя игрока
     * @param playerHandler обработчик игрока
     * @return true, если игрок успешно добавлен
     */
    public synchronized boolean addPlayer(String player, ClientHandler playerHandler) {
        if (player2 != null) {
            return false;
        }

        player2 = playerHandler;
        player2Username = player;

        player1.sendMessage("PLAYER_JOINED:" + player);
        player2.sendMessage("GAME_JOINED:" + gameName + ":" + creator);
        status = GameStatus.PLACING_SHIPS;

        return true;
    }

    /**
     * Удаление игрока из игры.
     *
     * @param username имя игрока
     */
    public synchronized void removePlayer(String username) {
        resetGameState();

        if (username.equals(player1Username)) {
            player1.sendMessage("LEFT_GAME");
            if (player2 != null) {
                player2.sendMessage("CREATOR_LEFT");
            }
            player1 = null;
            player1Username = null;
        }
        else if (username.equals(player2Username)) {
            player2.sendMessage("LEFT_GAME");
            player2 = null;
            player2Username = null;
        }

        if (player1 == null) {
            server.removeGame(gameName);
        }
        else {
            status = GameStatus.WAITING;
            player1.sendMessage("GAME_STATUS:WAITING");
            server.broadcastGameList();
        }
    }

    /**
     * Обработка размещения кораблей игроком.
     *
     * @param player    игрок
     * @param shipsData строковое представление кораблей
     */
    public synchronized void processShipsPlaced(ClientHandler player, String shipsData) {

        if (player == player1) {
            player1Ships = shipsData;
            player1ShipsPlaced = true;
            player.sendMessage("SHIPS_ACCEPTED");

            if (player2 != null) {
                player2.sendMessage("OPPONENT_PLACING_SHIPS");
            }
        }
        else if (player == player2) {
            player2Ships = shipsData;
            player2ShipsPlaced = true;
            player.sendMessage("SHIPS_ACCEPTED");

            if (player1 != null) {
                player1.sendMessage("OPPONENT_PLACING_SHIPS");
            }
        }

        if (player1ShipsPlaced && player2ShipsPlaced) {
            player1.sendMessage("ALL_SHIPS_PLACED");
            player2.sendMessage("ALL_SHIPS_PLACED");
        }
        else {
            if (player1ShipsPlaced && !player2ShipsPlaced) {
                player1.sendMessage("WAITING_FOR_OPPONENT");
            }
            else if (!player1ShipsPlaced && player2ShipsPlaced) {
                player2.sendMessage("WAITING_FOR_OPPONENT");
            }
        }
    }

    /** Пометка игрока как готового к игре */
    public synchronized void playerReady(ClientHandler player) {
        if (player == player1) {
            player1Ready = true;
        }
        else if (player == player2) {
            player2Ready = true;
        }

        if (player1Ready && player2Ready) {
            startGame();
        }
    }

    /** Запуск игры */
    private void startGame() {
        gameStarted = true;
        status = GameStatus.IN_PROGRESS;
        currentTurn = player1;

        player1Hits = 0;
        player2Hits = 0;
        winner = null;

        player1.sendMessage("GAME_START:YOUR_TURN:" + player2Username);
        player2.sendMessage("GAME_START:OPPONENT_TURN:" + player1Username);
    }

    /**
     * Обработка выстрела игрока.
     *
     * @param shooter игрок, который делает выстрел
     * @param x       координата X
     * @param y       координата Y
     */
    public synchronized void processShot(ClientHandler shooter, int x, int y) {

        if (!gameStarted || shooter != currentTurn) {
            return;
        }

        String targetShips = (shooter == player1) ? player2Ships : player1Ships;

        boolean hit = checkHit(targetShips, x, y);
        String result = "MISS";

        if (hit) {
            if (shooter == player1) {
                player2Hits++;
                updateShipCell(player2Ships, x, y);
            }
            else {
                player1Hits++;
                updateShipCell(player1Ships, x, y);
            }

            boolean shipSunk = isShipSunk(targetShips, x, y);
            result = shipSunk ? "SUNK" : "HIT";
        }

        player1.sendMessage("SHOT_RESULT:" + shooter.getUsername() + ":" + result + ":" + x + ":" + y);
        player2.sendMessage("SHOT_RESULT:" + shooter.getUsername() + ":" + result + ":" + x + ":" + y);

        if (!hit) {
            currentTurn = (currentTurn == player1) ? player2 : player1;
            player1.sendMessage("TURN:" + currentTurn.getUsername());
            player2.sendMessage("TURN:" + currentTurn.getUsername());
        }

        checkGameOver();
    }

    /** Обновление состояния клетки корабля после попадания */
    private void updateShipCell(String shipsData, int x, int y) {
        List<ShipInfo> ships = parseShips(shipsData);
        for (ShipInfo ship : ships) {
            if (ship.hitCell(x, y)) {
                break;
            }
        }
    }

    private boolean isShipSunk(String shipsData, int x, int y) {
        List<ShipInfo> ships = parseShips(shipsData);
        for (ShipInfo ship : ships) {
            for (Cell cell : ship.getCells()) {
                if (cell.getX() == x && cell.getY() == y) {
                    return ship.isSunk();
                }
            }
        }
        return false;
    }

    private boolean checkHit(String shipsData, int x, int y) {
        List<ShipInfo> ships = parseShips(shipsData);

        for (ShipInfo ship : ships) {
            for (Cell cell : ship.getCells()) {
                if (cell.getX() == x && cell.getY() == y) {
                    cell.setState(Cell.State.HIT);
                    return true;
                }
            }
        }

        return false;
    }

    /** Парсинг строки с кораблями в список объектов ShipInfo */
    private List<ShipInfo> parseShips(String shipsData) {
        List<ShipInfo> ships = new ArrayList<>();
        if (shipsData == null || shipsData.isEmpty()) {
            return ships;
        }

        String[] shipStrings = shipsData.split(";");

        for (String shipStr : shipStrings) {
            shipStr = shipStr.trim();
            if (shipStr.isEmpty()) continue;

            String[] parts = shipStr.split(",");
            if (parts.length == 4) {
                try {
                    int type = Integer.parseInt(parts[0]);
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int orientation = Integer.parseInt(parts[3]);

                    if (x < 0 || x >= 10 || y < 0 || y >= 10) {
                        continue;
                    }

                    ShipInfo ship = new ShipInfo(type, x, y, orientation);
                    ships.add(ship);

                }
                catch (NumberFormatException e) {
                    System.err.println("❌ Ошибка парсинга корабля: " + shipStr);
                }
            }
        }

        return ships;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    private void checkGameOver() {
        if (player1Hits == 20) {
            setWinner(player2Username);
            endGame();
        } else if (player2Hits == 20) {
            setWinner(player1Username);
            endGame();
        }
    }

    /** Завершение игры и уведомление игроков */
    public void endGame() {
        if (gameStarted) {
            gameStarted = false;
            status = GameStatus.FINISHED;

            String winnerMessage = "GAME_OVER:" + winner + "," + isSurrend;

            if (player1 != null) {
                player1.sendMessage(winnerMessage);
            }
            if (player2 != null) {
                player2.sendMessage(winnerMessage);
            }
        }

        resetGameState();
    }

    /** Сброс состояния игры */
    public void resetGameState() {
        player1Ships = "";
        player2Ships = "";
        player1Ready = false;
        player2Ready = false;
        gameStarted = false;
        currentTurn = null;
        player1ShipsPlaced = false;
        player2ShipsPlaced = false;
        player1Hits = 0;
        player2Hits = 0;
        winner = null;
        isSurrend = false;
        status = GameStatus.WAITING;
    }
}