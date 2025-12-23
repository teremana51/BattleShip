package BattleShip.client.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShipTypeTest {

    @Test
    void testCarrierProperties() {
        ShipType type = ShipType.CARRIER;
        assertEquals(4, type.getSize());
        assertEquals("Авианосец (4 клетки)", type.getDisplayName());
    }

    @Test
    void testBattleshipProperties() {
        ShipType type = ShipType.BATTLESHIP;
        assertEquals(3, type.getSize());
        assertEquals("Линкор (3 клетки)", type.getDisplayName());
    }

    @Test
    void testCruiserProperties() {
        ShipType type = ShipType.CRUISER;
        assertEquals(2, type.getSize());
        assertEquals("Крейсер (2 клетки)", type.getDisplayName());
    }

    @Test
    void testDestroyerProperties() {
        ShipType type = ShipType.DESTROYER;
        assertEquals(1, type.getSize());
        assertEquals("Эсминец (1 клетка)", type.getDisplayName());
    }
}

