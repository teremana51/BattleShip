package BattleShip.client.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CellTest {

    private Cell cell;

    @BeforeEach
    void setUp() {
        cell = new Cell(2, 3, Cell.State.EMPTY);
    }

    @Test
    void testInitialCoordinates() {
        assertEquals(2, cell.getX());
        assertEquals(3, cell.getY());
    }

    @Test
    void testInitialState() {
        assertEquals(Cell.State.EMPTY, cell.getState());
        assertTrue(cell.isEmpty());
        assertFalse(cell.hasShip());
        assertFalse(cell.isHit());
        assertFalse(cell.isMiss());
    }

    @Test
    void testSetState() {
        cell.setState(Cell.State.SHIP);
        assertEquals(Cell.State.SHIP, cell.getState());
        assertTrue(cell.hasShip());
        assertFalse(cell.isEmpty());

        cell.setState(Cell.State.HIT);
        assertEquals(Cell.State.HIT, cell.getState());
        assertTrue(cell.isHit());

        cell.setState(Cell.State.MISS);
        assertEquals(Cell.State.MISS, cell.getState());
        assertTrue(cell.isMiss());
    }

    @Test
    void testShipReference() {
        Ship ship = new Ship(ShipType.DESTROYER);
        cell.setShip(ship);
        assertEquals(ship, cell.getShip());
        assertTrue(cell.isEmpty());
    }
}

