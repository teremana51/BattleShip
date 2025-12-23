package BattleShip.client.model;

/**
 * Перечисление типов кораблей в игре «Морской бой».
 * <p>
 * Каждый тип корабля имеет:
 * <ul>
 *     <li>размер (количество занимаемых клеток)</li>
 *     <li>отображаемое имя для пользовательского интерфейса</li>
 * </ul>
 */
public enum ShipType {
    CARRIER(4, "Авианосец (4 клетки)"),
    BATTLESHIP(3, "Линкор (3 клетки)"),
    CRUISER(2, "Крейсер (2 клетки)"),
    DESTROYER(1, "Эсминец (1 клетка)");

    private final int size;

    /** Название корабля для отображения в интерфейсе */
    private final String displayName;

    /**
     * Конструктор типа корабля.
     *
     * @param size количество клеток, занимаемых кораблём
     * @param displayName отображаемое название корабля
     */
    ShipType(int size, String displayName) {
        this.size = size;
        this.displayName = displayName;
    }

    public int getSize() {
        return size;
    }

    public String getDisplayName() {
        return displayName;
    }
}