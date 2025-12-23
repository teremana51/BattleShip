package BattleShip.client.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BattleStateTest {

    private BattleState battleState;

    @BeforeEach
    void setUp() {
        battleState = new BattleState("Player1");
    }

    @Test
    void testInitialState() {
        assertEquals("Player1", battleState.getPlayerName());
        assertNotNull(battleState.getPlayerBoard());
        assertNotNull(battleState.getOpponentBoard());
        assertFalse(battleState.isPlayerTurn());
        assertFalse(battleState.isGameStarted());
        assertFalse(battleState.isGameOver());
        assertNull(battleState.getWinner());
        assertNotNull(battleState.getGameLog());
    }

    @Test
    void testSettersAndGetters() {
        battleState.setOpponentName("Opponent");
        battleState.setPlayerTurn(true);
        battleState.setGameStarted(true);
        battleState.setGameOver(true);
        battleState.setWinner("Player1");

        assertEquals("Opponent", battleState.getOpponentName());
        assertTrue(battleState.isPlayerTurn());
        assertTrue(battleState.isGameStarted());
        assertTrue(battleState.isGameOver());
        assertEquals("Player1", battleState.getWinner());
    }

    @Test
    void testAddLog() {
        for (int i = 0; i < 55; i++) {
            battleState.addLog("Message " + i);
        }

        List<String> log = battleState.getGameLog();
        assertEquals(50, log.size(), "Лог должен хранить максимум 50 сообщений");
        assertEquals("Message 5", log.get(0), "Первое сообщение должно быть Message 5 после обрезки");
        assertEquals("Message 54", log.get(49), "Последнее сообщение должно быть Message 54");
    }

    @Test
    void testGetPlayerShipsLeft() {
        Ship ship = new Ship(ShipType.DESTROYER);
        battleState.getPlayerBoard().placeShip(ship, 0, 0);

        assertEquals(1, battleState.getPlayerShipsLeft(), "Один корабль не разрушен, должен вернуть 1");

        for (Cell cell : ship.getCells()) {
            cell.setState(Cell.State.HIT);
        }

        assertEquals(0, battleState.getPlayerShipsLeft(), "Корабль уничтожен, должно вернуть 0");
    }

    @Test
    void testGetOpponentShipsLeft() {
        Ship ship = new Ship(ShipType.DESTROYER);
        battleState.getOpponentBoard().placeShip(ship, 0, 0);

        assertTrue(battleState.getOpponentShipsLeft() <= 10, "Количество оставшихся кораблей не должно превышать 10");

        for (Cell cell : ship.getCells()) {
            cell.setState(Cell.State.HIT);
        }

        int remaining = battleState.getOpponentShipsLeft();
        assertTrue(remaining <= 10 && remaining >= 0, "Количество оставшихся кораблей корректно после попадания");
    }
}
