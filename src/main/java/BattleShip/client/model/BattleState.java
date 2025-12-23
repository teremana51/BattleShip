package BattleShip.client.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс, представляющий состояние боя между игроком и противником.
 * <p>
 * Содержит доски игроков, информацию о текущем ходе, статус игры,
 * победителе и журнал событий (gameLog).
 */
public class BattleState {
    private Board playerBoard;
    private Board opponentBoard;

    private String playerName;
    private String opponentName;

    private boolean playerTurn;

    private boolean gameStarted;
    private boolean gameOver;

    private String winner;

    private List<String> gameLog;

    /**
     * Конструктор состояния боя.
     *
     * @param playerName имя игрока (если null, устанавливается "Игрок")
     */
    public BattleState(String playerName) {
        if (playerName == null) {
            playerName = "Игрок";
        }

        this.playerName = playerName;
        this.playerBoard = new Board();
        this.opponentBoard = new Board();
        this.gameLog = new ArrayList<>();
        this.gameStarted = false;
        this.gameOver = false;
        this.playerTurn = false;
    }

    public Board getPlayerBoard() {
        return playerBoard;
    }

    public Board getOpponentBoard() {
        return opponentBoard;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String name) {
        this.opponentName = name;
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public void setPlayerTurn(boolean turn) {
        this.playerTurn = turn;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean started) {
        this.gameStarted = started;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean over) {
        this.gameOver = over;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    /**
     * Добавляет запись в журнал событий игры.
     * Если количество записей превышает 50, удаляется самая старая.
     *
     * @param message сообщение для добавления
     */
    public void addLog(String message) {
        gameLog.add(message);
        if (gameLog.size() > 50) {
            gameLog.remove(0);
        }
    }

    public List<String> getGameLog() {
        return gameLog;
    }

    /**
     * Считает количество оставшихся кораблей игрока.
     *
     * @return количество непотопленных кораблей игрока
     */
    public int getPlayerShipsLeft() {
        if (playerBoard == null) {
            return 0;
        }

        int shipsLeft = 0;
        for (Ship ship : playerBoard.getShips()) {
            boolean isDestroyed = true;
            for (Cell cell : ship.getCells()) {
                if (cell.getState() != Cell.State.HIT && cell.getState() != Cell.State.SHIP_SUNK) {
                    isDestroyed = false;
                    break;
                }
            }
            if (!isDestroyed) {
                shipsLeft++;
            }
        }
        return shipsLeft;
    }

    /**
     * Считает количество оставшихся кораблей противника.
     * <p>
     * Оценка делается по количеству попаданий по клеткам противника.
     *
     * @return приблизительное количество оставшихся кораблей противника
     */
    public int getOpponentShipsLeft() {
        if (opponentBoard == null) {
            return 10;
        }

        int hitCells = 0;
        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                Cell cell = opponentBoard.getCell(x, y);
                if (cell.getState() == Cell.State.HIT || cell.getState() == Cell.State.SHIP_SUNK) {
                    hitCells++;
                }
            }
        }

        int totalShipCells = 20;
        int remainingCells = totalShipCells - hitCells;
        return Math.max(0, (int) Math.ceil(remainingCells / 2.0)); // Округляем вверх
    }
}