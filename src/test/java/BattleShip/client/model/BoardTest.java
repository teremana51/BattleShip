package BattleShip.client.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board();
    }

    @Test
    void testCanPlaceShipValidPlacement() {
        Ship destroyer = new Ship(ShipType.DESTROYER);
        assertTrue(board.canPlaceShip(destroyer, 0, 0), "Корабль можно разместить в верхнем левом углу");
    }

    @Test
    void testCanPlaceShipOutOfBounds() {
        Ship carrier = new Ship(ShipType.CARRIER);
        carrier.setOrientation(Ship.Orientation.HORIZONTAL);
        assertFalse(board.canPlaceShip(carrier, 7, 0), "Горизонтальный авианосец не помещается на правом краю");
        carrier.setOrientation(Ship.Orientation.VERTICAL);
        assertFalse(board.canPlaceShip(carrier, 0, 7), "Вертикальный авианосец не помещается на нижнем краю");
    }

    @Test
    void testPlaceShipSuccessfully() {
        Ship carrier = new Ship(ShipType.CARRIER);
        carrier.setOrientation(Ship.Orientation.VERTICAL);
        boolean placed = board.placeShip(carrier, 5, 5);
        assertTrue(placed, "Корабль должен быть размещен");
        assertFalse(board.getShips().isEmpty(), "Список кораблей должен содержать корабль после размещения");
        assertEquals(Cell.State.SHIP, board.getCell(5,5).getState());
        assertEquals(Cell.State.SHIP, board.getCell(5,6).getState());
        assertEquals(Cell.State.SHIP, board.getCell(5,7).getState());
        assertEquals(Cell.State.SHIP, board.getCell(5,8).getState());
        assertEquals(Cell.State.EMPTY, board.getCell(6,5).getState());
    }

    @Test
    void testPlaceShipFailsIfOccupied() {
        Ship destroyer1 = new Ship(ShipType.DESTROYER);
        board.placeShip(destroyer1, 0, 0);
        Ship destroyer2 = new Ship(ShipType.DESTROYER);
        assertFalse(board.placeShip(destroyer2, 0, 0), "Нельзя разместить корабль на занятой клетке");
    }

    @Test
    void testClearBoard() {
        Ship destroyer = new Ship(ShipType.DESTROYER);
        board.placeShip(destroyer, 0, 0);
        board.clearBoard();
        assertTrue(board.getShips().isEmpty(), "Список кораблей должен быть пуст после очистки");
        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                assertEquals(Cell.State.EMPTY, board.getCell(x,y).getState(), "Все клетки должны быть пустыми после очистки");
                assertNull(board.getCell(x,y).getShip());
            }
        }
    }

    @Test
    void testGetShipsPlacementString() {
        Ship destroyer = new Ship(ShipType.DESTROYER);
        board.placeShip(destroyer, 0, 0);
        String placementString = board.getShipsPlacementString();
        assertTrue(placementString.startsWith("3,0,0,"), "Строка размещения должна содержать правильный тип и координаты");
        assertTrue(placementString.endsWith(";"));
    }

    @Test
    void testPlaceComputerShipsAllShipsPlaced() {
        String shipsData = board.placeComputerShips();
        assertNotNull(shipsData, "Строка с кораблями не должна быть null");
        assertTrue(board.allShipsPlaced(), "Все корабли должны быть размещены");
    }

    @Test
    void testRemoveShip() {
        Ship destroyer = new Ship(ShipType.DESTROYER);
        board.placeShip(destroyer, 0, 0);

        board.removeShip(destroyer);

        assertFalse(board.getShips().contains(destroyer), "Корабль должен быть удален из списка");

        for (Cell cell : destroyer.getCells()) {
            assertEquals(Cell.State.EMPTY, cell.getState(), "Клетки корабля должны быть пустыми после удаления");
            assertNull(cell.getShip(), "Клетка не должна ссылаться на корабль после удаления");
        }

        assertTrue(destroyer.getCells().isEmpty(), "Список клеток корабля должен быть пуст после удаления");
    }

    @Test
    void testGetCell() {
        Cell cell = board.getCell(0, 0);
        assertNotNull(cell, "Клетка в пределах доски не должна быть null");
        assertEquals(0, cell.getX());
        assertEquals(0, cell.getY());

        assertNull(board.getCell(-1, 0), "Клетка с отрицательной координатой должна быть null");
        assertNull(board.getCell(0, 10), "Клетка за пределами доски должна быть null");
        assertNull(board.getCell(10, 10), "Клетка за пределами доски должна быть null");
    }

    @Test
    void testGridInitializedEmpty() {
        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                Cell cell = board.getCell(x, y);
                assertNotNull(cell, "Клетка должна быть инициализирована");
                assertEquals(Cell.State.EMPTY, cell.getState(), "Все клетки должны быть пустыми после инициализации");
                assertNull(cell.getShip(), "Клетка не должна ссылаться на корабль после инициализации");
            }
        }
    }
}
