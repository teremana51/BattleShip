package BattleShip.client.model;

/**
 * Игровое поле игрока.
 * <p>
 * Наследуется от {@link Board} и дополнительно содержит состояние готовности
 * игрока к началу боя (после расстановки кораблей).
 */
public class PlayerBoard extends Board {

    /** Флаг, указывающий, готов ли игрок к началу игры */
    private boolean ready;

    /**
     * Создаёт новое игровое поле игрока.
     * <p>
     * По умолчанию поле пустое, а игрок не готов к началу игры.
     */
    public PlayerBoard() {
        super();
        this.ready = false;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    /**
     * Сбрасывает игровое поле к начальному состоянию.
     * <p>
     * В процессе сброса:
     * <ul>
     *     <li>очищаются все клетки поля</li>
     *     <li>удаляются все размещённые корабли</li>
     *     <li>сбрасывается флаг готовности игрока</li>
     * </ul>
     */
    public void reset() {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                getCell(x, y).setState(Cell.State.EMPTY);
                getCell(x, y).setShip(null);
            }
        }

        getShips().clear();

        ready = false;
    }
}