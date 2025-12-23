package BattleShip.client.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShipTest {

    private Ship destroyer;

    @BeforeEach
    void setUp() {
        destroyer = new Ship(ShipType.DESTROYER);
    }

    @Test
    void testInitialization() {
        assertEquals(ShipType.DESTROYER, destroyer.getType());
        assertEquals(1, destroyer.getSize());
        assertEquals(Ship.Orientation.HORIZONTAL, destroyer.getOrientation());
        assertFalse(destroyer.isPlaced());
        assertFalse(destroyer.isDestroyed());
        assertNotNull(destroyer.getCells());
        assertTrue(destroyer.getCells().isEmpty());
    }

    @Test
    void testRotate() {
        destroyer.setOrientation(Ship.Orientation.HORIZONTAL);
        destroyer.rotate();
        assertEquals(Ship.Orientation.VERTICAL, destroyer.getOrientation());
        destroyer.rotate();
        assertEquals(Ship.Orientation.HORIZONTAL, destroyer.getOrientation());
    }

    @Test
    void testSetAndGetCells() {
        Cell cell = new Cell(0, 0, Cell.State.SHIP);
        List<Cell> cells = new ArrayList<>();
        cells.add(cell);

        destroyer.setCells(cells);
        assertEquals(cells, destroyer.getCells());
    }

    @Test
    void testSetAndGetPlaced() {
        assertFalse(destroyer.isPlaced());
        destroyer.setPlaced(true);
        assertTrue(destroyer.isPlaced());
    }

    @Test
    void testIsDestroyed() {
        Ship cruiser = new Ship(ShipType.CRUISER); // размер 2
        Cell c1 = new Cell(0,0, Cell.State.EMPTY);
        Cell c2 = new Cell(1,0, Cell.State.EMPTY);
        List<Cell> cells = new ArrayList<>();
        cells.add(c1);
        cells.add(c2);
        cruiser.setCells(cells);

        assertFalse(cruiser.isDestroyed());

        c1.setState(Cell.State.HIT);
        assertFalse(cruiser.isDestroyed());

        c2.setState(Cell.State.HIT);
        assertTrue(cruiser.isDestroyed());
    }

    @Test
    void testGetTypeGetSize() {
        assertEquals(ShipType.DESTROYER, destroyer.getType(), "getType должен возвращать правильный тип корабля");
        assertEquals(1, destroyer.getSize(), "getSize должен возвращать правильный размер корабля");
    }

    @Test
    void testSetGetOrientation() {
        destroyer.setOrientation(Ship.Orientation.VERTICAL);
        assertEquals(Ship.Orientation.VERTICAL, destroyer.getOrientation(), "setOrientation должен менять ориентацию");
        destroyer.setOrientation(Ship.Orientation.HORIZONTAL);
        assertEquals(Ship.Orientation.HORIZONTAL, destroyer.getOrientation(), "setOrientation должен менять ориентацию обратно");
    }

}
