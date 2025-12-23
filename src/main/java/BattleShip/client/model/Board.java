package BattleShip.client.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс, представляющий игровое поле (доску) для размещения кораблей.
 * <p>
 * Хранит клетки (Cell) и список всех кораблей (Ship) на доске.
 * Обеспечивает логику проверки возможности размещения корабля, его установки,
 * удаления и работы с автоматическим размещением кораблей для компьютера.
 */
public class Board {
    /** Размер доски (10x10) */
    public static final int SIZE = 10;

    /** Сетка клеток */
    private Cell[][] grid;

    /** Список кораблей на доске */
    private List<Ship> ships;

    /**
     * Создаёт пустое игровое поле и инициализирует клетки.
     */
    public Board() {
        grid = new Cell[SIZE][SIZE];
        ships = new ArrayList<>();
        initializeGrid();
    }

    /**
     * Инициализирует все клетки поля пустыми клетками.
     */
    private void initializeGrid() {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                grid[x][y] = new Cell(x, y, Cell.State.EMPTY);
            }
        }
    }

    public Cell getCell(int x, int y) {
        if (x >= 0 && x < SIZE && y >= 0 && y < SIZE) {
            return grid[x][y];
        }
        return null;
    }

    public List<Ship> getShips() {
        return ships;
    }

    public Cell[][] getGrid() {
        return grid;
    }

    /**
     * Проверяет, можно ли разместить корабль в указанной позиции.
     *
     * @param ship корабль
     * @param startX начальная координата X
     * @param startY начальная координата Y
     * @return {@code true}, если корабль можно разместить
     */
    public boolean canPlaceShip(Ship ship, int startX, int startY) {
        int size = ship.getSize();
        Ship.Orientation orientation = ship.getOrientation();

        if (orientation == Ship.Orientation.HORIZONTAL) {
            if (startX + size > SIZE) return false;
        } else {
            if (startY + size > SIZE) return false;
        }

        for (int i = 0; i < size; i++) {
            int x = startX + (orientation == Ship.Orientation.HORIZONTAL ? i : 0);
            int y = startY + (orientation == Ship.Orientation.VERTICAL ? i : 0);

            if (!grid[x][y].isEmpty()) {
                return false;
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int nx = x + dx;
                    int ny = y + dy;
                    if (nx >= 0 && nx < SIZE && ny >= 0 && ny < SIZE) {
                        if (grid[nx][ny].hasShip()) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Размещает корабль на доске в указанной позиции.
     *
     * @param ship корабль
     * @param startX начальная координата X
     * @param startY начальная координата Y
     * @return {@code true}, если размещение прошло успешно
     */
    public boolean placeShip(Ship ship, int startX, int startY) {
        if (!canPlaceShip(ship, startX, startY)) {
            return false;
        }

        int size = ship.getSize();
        Ship.Orientation orientation = ship.getOrientation();
        List<Cell> shipCells = new ArrayList<>();


        for (int i = 0; i < size; i++) {
            int x = startX + (orientation == Ship.Orientation.HORIZONTAL ? i : 0);
            int y = startY + (orientation == Ship.Orientation.VERTICAL ? i : 0);

            Cell cell = grid[x][y];
            cell.setState(Cell.State.SHIP);
            cell.setShip(ship);
            shipCells.add(cell);

        }

        ship.setCells(shipCells);
        ship.setPlaced(true);
        ships.add(ship);

        return true;
    }

    /**
     * Размещает корабли компьютера случайным образом на доске.
     *
     * @return строка с информацией о расположении кораблей
     */
    public String placeComputerShips() {

        clearBoard();

        List<Ship> shipsToPlace = new ArrayList<>();

        for (int i = 0; i < 1; i++) {
            shipsToPlace.add(new Ship(ShipType.CARRIER));
        }

        for (int i = 0; i < 2; i++) {
            shipsToPlace.add(new Ship(ShipType.BATTLESHIP));
        }

        for (int i = 0; i < 3; i++) {
            shipsToPlace.add(new Ship(ShipType.CRUISER));
        }

        for (int i = 0; i < 4; i++) {
            shipsToPlace.add(new Ship(ShipType.DESTROYER));
        }

        List<String> placedShipsInfo = new ArrayList<>();

        for (Ship ship : shipsToPlace) {
            boolean placed = false;
            int attempts = 0;

            while (!placed && attempts < 1000) {
                int x = (int) (Math.random() * Board.SIZE);
                int y = (int) (Math.random() * Board.SIZE);

                if (Math.random() > 0.5) {
                    ship.setOrientation(Ship.Orientation.HORIZONTAL);
                }
                else {
                    ship.setOrientation(Ship.Orientation.VERTICAL);
                }

                if (canPlaceShip(ship, x, y)) {
                    placeShip(ship, x, y);
                    placed = true;

                    int orientationCode = ship.getOrientation() == Ship.Orientation.HORIZONTAL ? 0 : 1;
                    placedShipsInfo.add(ship.getType().ordinal() + "," + x + "," + y + "," + orientationCode);
                }
                attempts++;
            }

        }

        return getShipsPlacementString();
    }

    /**
     * Проверяет, расставлены ли все корабли на доске.
     *
     * @return {@code true}, если все корабли размещены
     */
    public boolean allShipsPlaced() {
        long carrierCount = ships.stream().filter(s -> s.getType() == ShipType.CARRIER).count();
        long battleshipCount = ships.stream().filter(s -> s.getType() == ShipType.BATTLESHIP).count();
        long cruiserCount = ships.stream().filter(s -> s.getType() == ShipType.CRUISER).count();
        long destroyerCount = ships.stream().filter(s -> s.getType() == ShipType.DESTROYER).count();

        return carrierCount >= 1 && battleshipCount >= 2 && cruiserCount >= 3 && destroyerCount >= 4;
    }

    /**
     * Возвращает строку с информацией о расположении всех кораблей.
     *
     * @return строка с координатами и ориентацией кораблей
     */
    public String getShipsPlacementString() {
        StringBuilder sb = new StringBuilder();
        for (Ship ship : ships) {
            if (!ship.getCells().isEmpty()) {
                Cell firstCell = ship.getCells().get(0);
                sb.append(ship.getType().ordinal())
                        .append(",")
                        .append(firstCell.getX())
                        .append(",")
                        .append(firstCell.getY())
                        .append(",")
                        .append(ship.getOrientation() == Ship.Orientation.HORIZONTAL ? 0 : 1)
                        .append(";");
            }
        }
        return sb.toString();
    }

    /**
     * Очищает доску и удаляет все корабли.
     */
    public void clearBoard() {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                grid[y][x].setState(Cell.State.EMPTY);
                grid[y][x].setShip(null);
            }
        }
        ships.clear();
    }

    /**
     * Удаляет конкретный корабль с доски.
     *
     * @param ship корабль для удаления
     */
    public void removeShip(Ship ship) {
        for (Cell cell : ship.getCells()) {
            cell.setState(Cell.State.EMPTY);
            cell.setShip(null);
        }
        ship.setCells(new ArrayList<>());
        ship.setPlaced(false);
        ships.remove(ship);
    }
}