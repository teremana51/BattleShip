package BattleShip.client.controller;

import BattleShip.client.model.*;
import BattleShip.client.view.BattleScreen;
import javax.swing.*;

/**
 * Контроллер логики боя.
 * <p>
 * Связывает модель боя ({@link BattleState}),
 * сетевой уровень и графический интерфейс.
 */
public class BattleController {

    private BattleState model;
    private BattleScreen view;

    private NetworkController networkController;

    private Runnable onPlayAgainCallback;
    private Runnable onReturnToMenuCallback;

    /**
     * Создаёт контроллер боя.
     *
     * @param networkController сетевой контроллер
     * @param playerName имя текущего игрока
     */
    public BattleController(NetworkController networkController, String playerName) {
        this.networkController = networkController;
        this.model = new BattleState(playerName);
        this.view = null;

    }

    public BattleState getModel() {
        return model;
    }

    public BattleScreen getView() {
        return view;
    }

    public void setOpponentName(String name) {
        model.setOpponentName(name);
    }

    /**
     * Устанавливает обработчик кнопки "Играть ещё".
     *
     * @param callback действие при выборе
     */
    public void setOnPlayAgainCallback(Runnable callback) {
        this.onPlayAgainCallback = callback;
    }

    /**
     * Устанавливает обработчик возврата в меню.
     *
     * @param callback действие при выборе
     */
    public void setOnReturnToMenuCallback(Runnable callback) {
        this.onReturnToMenuCallback = callback;
    }

    /**
     * Преобразует координаты клетки с числовыми значениями в формат, используемый на игровом поле.
     * @param x индекс колонки на игровом поле (0–9)
     * @param y индекс строки на игровом поле (0–9)
     * @return строковое представление координат в формате "БукваЧисло" (например, "А1")
     */
    private String convertToCoordinates(int x, int y) {
        char letter = (char) ('А' + x);
        return String.format("%c%d", letter, y + 1);
    }

    /**
     * Обрабатывает сообщение о начале игры.
     * Устанавливает состояние начала игры и определяет,
     * чей ход будет первым, а также обновляет интерфейс.
     *
     * @param turnInfo информация о ходе ("YOUR_TURN" — ход игрока,
     *                 любое другое значение — ход противника)
     */
    public void processGameStart(String turnInfo) {

        model.setGameStarted(true);
        model.setPlayerTurn(turnInfo.equals("YOUR_TURN"));

        if (view != null) {
            view.addGameLog("Игра началась!");
            view.addGameLog(turnInfo.equals("YOUR_TURN") ? "Ваш ход первый!" : "Первым ходит противник!");
            view.updateUI();
        }
    }

    /**
     * Восстанавливает игровое поле игрока на основе данных о расстановке кораблей,
     * полученных с сервера.
     * <p>
     * Данные о кораблях передаются в виде строки, содержащей информацию
     * о типе корабля, координатах и ориентации.
     *
     * @param shipsData строка с описанием расстановки кораблей
     */
    public void setPlayerBoardFromPlacement(String shipsData) {

        if (shipsData == null || shipsData.isEmpty()) {
            return;
        }

        model.getPlayerBoard().getShips().clear();

        String[] ships = shipsData.split(";");

        for (String shipStr : ships) {
            shipStr = shipStr.trim();

            if (shipStr.isEmpty()) {
                continue;
            }

            try {
                String[] parts = shipStr.split(",");
                if (parts.length == 4) {
                    int typeIndex = Integer.parseInt(parts[0]);
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int orientationInt = Integer.parseInt(parts[3]);

                    ShipType type = ShipType.values()[typeIndex];
                    Ship ship = new Ship(type);
                    ship.setOrientation(orientationInt == 0 ? Ship.Orientation.HORIZONTAL : Ship.Orientation.VERTICAL);

                    model.getPlayerBoard().placeShip(ship, x, y);
                }
            }
            catch (Exception e) {
                System.err.println("Ошибка парсинга корабля: " + shipStr + ", ошибка: " + e.getMessage());
            }
        }
    }

    /**
     * Обрабатывает результат выстрела (попадание, промах или потопление корабля).
     * Обновляет состояние соответствующей клетки, лог игры и очередность хода.
     *
     * @param shooter имя игрока, совершившего выстрел
     * @param result  результат выстрела ("HIT", "MISS" или "SUNK")
     * @param x       координата X клетки
     * @param y       координата Y клетки
     */
    public void processShotResult(String shooter, String result, int x, int y) {

        boolean isPlayerShot = shooter.equals(model.getPlayerName());

        Cell cell;
        Board board;

        if (isPlayerShot) {
            board = model.getOpponentBoard();
        }
        else {
            board = model.getPlayerBoard();
        }

        cell = board.getCell(x, y);
        if (cell == null) {
            return;
        }

        if (result.equals("HIT") || result.equals("SUNK")) {
            cell.setState(Cell.State.HIT);

            if (view != null) {
                if (isPlayerShot) {
                    view.addGameLog("Попадание по координатам: " + convertToCoordinates(x, y));
                } else {
                    view.addGameLog("Противник попал по вашим координатам: " + convertToCoordinates(x, y));
                }
            }

            if (result.equals("SUNK")) {
                cell.setState(Cell.State.SHIP_SUNK);
                if (view != null) {
                    if (isPlayerShot) {
                        view.addGameLog("Потоплен корабль противника!");
                    }
                    else {
                        view.addGameLog("Ваш корабль потоплен!");
                    }
                }
            }
        }
        else if (result.equals("MISS")) {
            cell.setState(Cell.State.MISS);

            if (view != null) {
                if (isPlayerShot) {
                    view.addGameLog("Промах по координатам: " + convertToCoordinates(x, y));
                }
                else {
                    view.addGameLog("Противник промахнулся по координатам: " + convertToCoordinates(x, y));
                }
            }

            model.setPlayerTurn(!isPlayerShot);
        }


        if (view != null) {
            view.updateUI();
        }

    }

    /**
     * Создаёт и возвращает экран боя.
     * <p>
     * Если экран уже был создан, возвращает существующий экземпляр.
     * Также настраивает обработчики действий пользователя
     * (выстрел и сдача).
     *
     * @return экран боя {@link BattleScreen}
     */
    public BattleScreen createView() {
        if (view == null) {
            view = new BattleScreen(model);

            view.setListener(new BattleScreen.BattleScreenListener() {

                @Override
                public void onCellClicked(int x, int y) {
                    networkController.sendMessage("SHOT:" + x + ":" + y);
                    if (view != null) {
                        view.addGameLog("Выстрел по координатам: " + convertToCoordinates(x, y));
                    }
                }

                @Override
                public void onSurrender() {
                    networkController.sendMessage("SURRENDER:" + model.getOpponentName());
                    if (view != null) {
                        view.addGameLog("Вы сдались!");
                    }
                }
            });
        }

        return view;
    }

    /**
     * Обрабатывает окончание игры.
     *
     * @param winner имя победителя
     * @param isSurrend признак сдачи
     */
    public void processGameOver(String winner, String isSurrend) {
        model.setGameOver(true);
        model.setWinner(winner);

        boolean surrend = isSurrend.equals("true");

        if (view != null) {
            if (winner.equals(model.getPlayerName())) {
                view.addGameLog("Поздравляем! Вы победили!");
            }
            else {
                view.addGameLog("К сожалению, вы проиграли.");
            }
            view.updateUI();

            showGameOverDialog(winner.equals(model.getPlayerName()), surrend);
        }
    }

    /**
     * Показывает диалог завершения игры.
     *
     * @param isWinner победил ли игрок
     * @param isSurrend была ли сдача
     */
    private void showGameOverDialog(boolean isWinner, boolean isSurrend) {
        SwingUtilities.invokeLater(() -> {
            String message;

            if (isSurrend) {
                message = isWinner ? "Противник сдался!" : "Вы сдались!";
                message += "\nХотите сыграть еще?";
            }
            else {
                message = isWinner ? "Поздравляем! Вы победили!" : "К сожалению, вы проиграли.";
                message += "\nХотите сыграть еще?";
            }

            String title = isWinner ? "Победа!" : "Поражение";

            Object[] options = {"Играть еще", "Выйти в меню"};
            int choice = JOptionPane.showOptionDialog(
                    view,
                    message,
                    title,
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == 0) {
                if (onPlayAgainCallback != null) {
                    onPlayAgainCallback.run();
                }
            }
            else {
                if (onReturnToMenuCallback != null) {
                    onReturnToMenuCallback.run();
                }
            }
        });
    }
}