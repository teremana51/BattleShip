package BattleShip.client.model;

/**
 * Клетка игрового поля.
 * <p>
 * Представляет одну ячейку на игровом поле с координатами,
 * текущим состоянием и ссылкой на корабль (если он находится в клетке).
 */
public class Cell {

    /**
     * Возможные состояния клетки игрового поля.
     */
    public enum State {
        EMPTY, SHIP, HIT, MISS, FORBIDDEN, SHIP_SUNK
    }

    private int x;
    private int y;

    private State state;

    /** Корабль, находящийся в клетке (если есть) */
    private Ship ship;

    /**
     * Создаёт новую клетку игрового поля.
     *
     * @param x координата X
     * @param y координата Y
     * @param state начальное состояние клетки
     */
    public Cell(int x, int y, State state) {
        this.x = x;
        this.y = y;
        this.state = State.EMPTY;
        this.ship = null;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public State getState() {
        return state;
    }

    public Ship getShip() {
        return ship;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public boolean isEmpty() {
        return state == State.EMPTY;
    }

    public boolean hasShip() {
        return state == State.SHIP;
    }

    public boolean isHit() {
        return state == State.HIT;
    }

    public boolean isMiss() {
        return state == State.MISS;
    }
}