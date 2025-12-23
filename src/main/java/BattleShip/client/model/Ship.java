package BattleShip.client.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс, представляющий корабль в игре «Морской бой».
 * <p>
 * Корабль характеризуется:
 * <ul>
 *     <li>типом ({@link ShipType})</li>
 *     <li>размером</li>
 *     <li>ориентацией (горизонтальной или вертикальной)</li>
 *     <li>набором клеток, которые он занимает на поле</li>
 * </ul>
 */
public class Ship {
    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    private ShipType type;

    private int size;

    private Orientation orientation;

    /** Список клеток, занимаемых кораблём */
    private List<Cell> cells;

    /** Флаг, указывающий, размещён ли корабль на поле */
    private boolean placed;

    /** Флаг, указывающий, уничтожен ли корабль (не используется напрямую) */
    private boolean destroyed;

    /**
     * Создаёт новый корабль заданного типа.
     * <p>
     * По умолчанию корабль:
     * <ul>
     *     <li>имеет горизонтальную ориентацию</li>
     *     <li>не размещён на поле</li>
     *     <li>не уничтожен</li>
     * </ul>
     *
     * @param type тип корабля
     */
    public Ship(ShipType type) {
        this.type = type;
        this.size = type.getSize();
        this.orientation = Orientation.HORIZONTAL;
        this.cells = new ArrayList<>();
        this.placed = false;
        this.destroyed = false;
    }

    /**
     * Поворачивает корабль, меняя ориентацию
     * с горизонтальной на вертикальную и наоборот.
     */
    public void rotate() {
        orientation = (orientation == Orientation.HORIZONTAL) ? Orientation.VERTICAL : Orientation.HORIZONTAL;
    }

    public boolean isPlaced() {
        return placed;
    }

    public ShipType getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public void setPlaced(boolean placed) {
        this.placed = placed;
    }

    public void setCells(List<Cell> cells) {
        this.cells = cells;
    }

    /**
     * Проверяет, уничтожен ли корабль.
     * <p>
     * Корабль считается уничтоженным, если:
     * <ul>
     *     <li>он занимает хотя бы одну клетку</li>
     *     <li>все его клетки находятся в состоянии {@link Cell.State#HIT}</li>
     * </ul>
     *
     * @return {@code true}, если корабль полностью уничтожен
     */
    public boolean isDestroyed() {
        if (cells.isEmpty()) return false;
        for (Cell cell : cells) {
            if (cell.getState() != Cell.State.HIT) {
                return false;
            }
        }
        return true;
    }
}