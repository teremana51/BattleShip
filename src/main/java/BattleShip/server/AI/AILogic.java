package BattleShip.server.AI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Класс, реализующий логику ИИ для игры "Морской бой".
 * ИИ выбирает клетки для выстрелов случайным образом и хранит список приоритетных целей.
 */
public class AILogic {

    /** Массив, отмечающий, какие клетки уже были использованы для выстрелов */
    private final boolean[][] used = new boolean[10][10];

    /** Генератор случайных чисел для выбора клеток */
    private final Random rnd = new Random();

    /** Список приоритетных целей для ИИ (например, соседние клетки после попадания) */
    private final List<int[]> targets = new ArrayList<>();

    /**
     * Получение следующего выстрела ИИ.
     * Если есть приоритетные цели, выбирается одна из них, иначе — случайная клетка.
     *
     * @return массив из двух элементов {x, y} с координатами выстрела
     */
    public int[] nextShot() {
        if (!targets.isEmpty()) {
            return popTarget();
        }
        return randomShot();
    }

    /**
     * Сброс состояния ИИ.
     * Очищает список приоритетных целей и помечает все клетки как неиспользованные.
     */
    public void reset() {
        targets.clear();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                used[x][y] = false;
            }
        }
    }

    /**
     * Выбор случайной клетки для выстрела.
     * Проверяется, что клетка ещё не использовалась.
     *
     * @return массив из двух элементов {x, y} с координатами выстрела
     */
    private int[] randomShot() {
        int x, y;
        do {
            x = rnd.nextInt(10);
            y = rnd.nextInt(10);
        } while (used[x][y]);

        used[x][y] = true;
        return new int[]{x, y};
    }

    /**
     * Получение следующей цели из списка приоритетных целей.
     * Клетка помечается как использованная и удаляется из списка targets.
     *
     * @return массив из двух элементов {x, y} с координатами выстрела
     */
    private int[] popTarget() {
        int[] p = targets.remove(0);
        used[p[0]][p[1]] = true;
        return p;
    }
}
