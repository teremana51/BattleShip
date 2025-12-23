package BattleShip.client.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerBoardTest {

    private PlayerBoard playerBoard;

    @BeforeEach
    void setUp() {
        playerBoard = new PlayerBoard();
    }

    @Test
    void testInitialState() {
        assertNotNull(playerBoard.getGrid(), "Доска должна быть инициализирована");
        assertEquals(0, playerBoard.getShips().size(), "Список кораблей должен быть пустым");
        assertFalse(playerBoard.isReady(), "Флаг ready должен быть false по умолчанию");

        for (int x = 0; x < PlayerBoard.SIZE; x++) {
            for (int y = 0; y < PlayerBoard.SIZE; y++) {
                assertEquals(Cell.State.EMPTY, playerBoard.getCell(x, y).getState());
            }
        }
    }

    @Test
    void testSetReady() {
        playerBoard.setReady(true);
        assertTrue(playerBoard.isReady(), "Флаг ready должен быть true после установки");
    }

    @Test
    void testReset() {
        Ship ship = new Ship(ShipType.DESTROYER);
        playerBoard.placeShip(ship, 0, 0);
        playerBoard.setReady(true);

        assertEquals(1, playerBoard.getShips().size());
        assertTrue(playerBoard.isReady());

        playerBoard.reset();

        assertEquals(0, playerBoard.getShips().size(), "Список кораблей должен быть очищен после reset");
        assertFalse(playerBoard.isReady(), "Флаг ready должен быть сброшен после reset");

        for (int x = 0; x < PlayerBoard.SIZE; x++) {
            for (int y = 0; y < PlayerBoard.SIZE; y++) {
                assertEquals(Cell.State.EMPTY, playerBoard.getCell(x, y).getState());
                assertNull(playerBoard.getCell(x, y).getShip());
            }
        }
    }

    @Test
    void testPlaceShipValidation() {
        Ship ship = new Ship(ShipType.DESTROYER);

        assertTrue(playerBoard.placeShip(ship, 0, 0), "Корабль должен размещаться корректно");
        assertEquals(1, playerBoard.getShips().size());

        Ship ship2 = new Ship(ShipType.DESTROYER);
        assertFalse(playerBoard.placeShip(ship2, 0, 0), "Корабль не должен размещаться на занятой клетке");
        assertEquals(1, playerBoard.getShips().size());
    }
}

