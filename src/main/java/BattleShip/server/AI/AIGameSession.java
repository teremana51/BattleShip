package BattleShip.server.AI;

import BattleShip.client.model.Board;
import BattleShip.client.model.Cell;
import BattleShip.server.ClientHandler;
import BattleShip.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Класс, представляющий игровую сессию с ИИ для игры "Морской бой".
 * Сессия управляет ходом игры между игроком и компьютером, обработкой выстрелов,
 * проверкой попаданий и окончания игры.
 */
public class AIGameSession {

    /** Планировщик для действий ИИ с задержкой */
    private final ScheduledExecutorService aiExecutor = Executors.newSingleThreadScheduledExecutor();

    /** Логика ИИ для генерации выстрелов */
    private final AILogic aiLogic = new AILogic();

    private int playerHits = 0;
    private int computerHits = 0;

    private String winner;

    private boolean isSurrend = false;

    private final String gameName;
    private String creator;

    private final Server server;

    private ClientHandler player;
    private String playerUsername;

    private String playerShips = "";
    private String computerShips = "";

    private boolean gameStarted = false;

    /** Текущий ходящий игрок */
    private ClientHandler currentTurn;

    /** Поле компьютера */
    private Board aiBoard = new Board();

    /**
     * Конструктор сессии игры с ИИ.
     *
     * @param gameName      название игры
     * @param creator       имя создателя игры
     * @param creatorHandler обработчик клиента создателя
     * @param server        ссылка на сервер
     */
    public AIGameSession(String gameName, String creator, ClientHandler creatorHandler, Server server) {
        this.gameName = gameName;
        this.creator = creator;
        this.server = server;

        this.player = creatorHandler;
        this.playerUsername = creator;

    }

    /**
     * Внутренний класс для хранения информации о корабле.
     */
    private static class ShipInfo {
        int type;
        int x;
        int y;
        int orientation;
        private List<Cell> cells; // Добавляем поле для хранения клеток

        /**
         * Конструктор информации о корабле.
         *
         * @param type        тип корабля (0 - авианосец, 1 - линкор, 2 - крейсер, 3 - эсминец)
         * @param x           координата X
         * @param y           координата Y
         * @param orientation ориентация (0 - горизонтально, 1 - вертикально)
         */
        ShipInfo(int type, int x, int y, int orientation) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.orientation = orientation;
            this.cells = new ArrayList<>();
            initializeCells();
        }

        /** Инициализация клеток корабля на доске */
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

        /**
         * Отметить попадание по клетке корабля.
         *
         * @param x координата X
         * @param y координата Y
         * @return true, если попадание по этому кораблю, иначе false
         */
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

    public void setSurrend (boolean isSurrend) {
        this.isSurrend = isSurrend;
    }

    /**
     * Обработка расстановки кораблей игрока.
     *
     * @param player    игрок
     * @param shipsData данные кораблей
     */
    public void processShipsPlaced(ClientHandler player, String shipsData) {
        playerShips = shipsData;

        player.sendMessage("SHIPS_ACCEPTED");

        computerShips = aiBoard.placeComputerShips();

        player.sendMessage("ALL_SHIPS_PLACED");
    }

    /** Запуск игры с ИИ */
    public void startGameAI() {
        gameStarted = true;
        currentTurn = player;

        playerHits = 0;
        computerHits = 0;
        winner = null;

        player.sendMessage("GAME_START:YOUR_TURN:" + "COMPUTER");
    }

    /**
     * Обработка выстрела в игре с ИИ.
     *
     * @param shooter игрок, который стреляет (null, если ИИ)
     * @param x       координата X
     * @param y       координата Y
     */
    public void processShotAI(ClientHandler shooter, int x, int y) {

        boolean isComputer = (shooter == null);

        if (!gameStarted) {
            return;
        }

        if (!isComputer && shooter != currentTurn) {
            return;
        }

        if (isComputer && currentTurn != null) {
            return;
        }

        String targetShips = isComputer ? playerShips : computerShips;

        boolean hit = checkHit(targetShips, x, y);
        String result = "MISS";

        if (hit) {
            if (isComputer) {
                playerHits++;
                updateShipCell(playerShips, x, y);
            }
            else {
                computerHits++;
                updateShipCell(computerShips, x, y);
            }

            boolean shipSunk = isShipSunk(targetShips, x, y);
            result = shipSunk ? "SUNK" : "HIT";
        }

        String shooterName = isComputer ? "COMPUTER" : shooter.getUsername();
        player.sendMessage("SHOT_RESULT:" + shooterName + ":" + result + ":" + x + ":" + y);

        if (isComputer) {
            if (result.equals("HIT") || result.equals("SUNK")) {
                makeComputerShot();
            }
            else {
                currentTurn = player;
                player.sendMessage("TURN:" + playerUsername);
            }
        }
        else {
            if (result.equals("MISS")) {
                currentTurn = null;
                player.sendMessage("TURN:COMPUTER");
                makeComputerShot();
            }
        }

        checkGameOver();
    }

    /** Выполнить выстрел компьютера с задержкой */
    private void makeComputerShot() {
        if (!gameStarted || currentTurn != null) {
            return;
        }

        aiExecutor.schedule(() -> {
            int[] shot = aiLogic.nextShot();
            int x = shot[0];
            int y = shot[1];

            processShotAI(null, x, y);

        }, 1000, TimeUnit.MILLISECONDS); // 600 мс задержка
    }

    /** Обновление состояния клетки корабля после выстрела */
    private void updateShipCell(String shipsData, int x, int y) {
        List<ShipInfo> ships = parseShips(shipsData);
        for (ShipInfo ship : ships) {
            if (ship.hitCell(x, y)) {
                break;
            }
        }
    }

    /** Проверка, потоплен ли корабль после выстрела */
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

    /** Проверка попадания по кораблям */
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

    /**
     * Парсинг строки данных кораблей в список объектов ShipInfo.
     *
     * @param shipsData строка с данными кораблей
     * @return список объектов ShipInfo
     */
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
                    System.err.println("Ошибка парсинга корабля: " + shipStr);
                }
            }
        }

        return ships;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    /** Проверка завершения игры */
    private void checkGameOver() {
        if (playerHits == 20) {
            setWinner("COMPUTER");
            endGame();
        }
        else if (computerHits == 20) {
            setWinner(playerUsername);
            endGame();
        }
    }

    /** Завершение игры и уведомление игрока */
    public void endGame() {
        if (gameStarted) {
            gameStarted = false;

            String winnerMessage = "GAME_OVER:" + winner + "," + isSurrend;

            if (player != null) {
                player.sendMessage(winnerMessage);
            }
        }

        resetGameState();
    }

    /** Сброс состояния игры для новой сессии */
    public void resetGameState() {
        playerShips = "";
        computerShips = "";
        gameStarted = false;
        currentTurn = null;
        playerHits = 0;
        computerHits = 0;
        winner = null;
        aiLogic.reset();
    }
}