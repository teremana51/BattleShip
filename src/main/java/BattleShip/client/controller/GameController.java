package BattleShip.client.controller;

import BattleShip.client.utils.LoggerClient;
import BattleShip.client.view.MainScreen;
import BattleShip.client.view.ShipPlacementScreen;

import javax.swing.*;
import java.awt.*;

/**
 * Главный контроллер клиента.
 * <p>
 * Отвечает за:
 * <ul>
 *     <li>обработку сообщений от сервера</li>
 *     <li>навигацию между экранами</li>
 *     <li>связь между сетевой логикой и GUI</li>
 * </ul>
 */
public class GameController {

    private JFrame frame;

    private MainScreen mainScreen;
    private ShipPlacementScreen shipPlacementScreen;

    private CardLayout cardLayout;

    private JPanel mainPanel;
    private JPanel connectionScreen;

    private NetworkController networkController;
    private BattleController battleController;

    /** Имя текущего пользователя */
    private String username;

    /**
     * Создаёт контроллер игры.
     *
     * @param frame главное окно приложения
     * @param cardLayout менеджер экранов
     * @param mainPanel основной контейнер экранов
     * @param networkController сетевой контроллер
     * @param connectionScreen экран подключения
     * @param shipPlacementScreen экран расстановки кораблей
     * @param mainScreen главный экран меню
     */
    public GameController(JFrame frame, CardLayout cardLayout, JPanel mainPanel,
                          NetworkController networkController, JPanel connectionScreen,
                          ShipPlacementScreen shipPlacementScreen, MainScreen mainScreen) {
        this.frame = frame;
        this.cardLayout = cardLayout;
        this.mainPanel = mainPanel;
        this.networkController = networkController;
        this.connectionScreen = connectionScreen;
        this.shipPlacementScreen = shipPlacementScreen;
        this.mainScreen = mainScreen;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setShipPlacementScreen (ShipPlacementScreen shipPlacementScreen) {
        this.shipPlacementScreen = shipPlacementScreen;
    }

    public void setBattleController(BattleController battleController) {
        this.battleController = battleController;
    }

    public void setMainPanel(JPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    /**
     * Обрабатывает сообщение, полученное от сервера.
     *
     * @param message строка сообщения от сервера
     */
    public void processServerMessage(String message) {
        LoggerClient.info(message);

        int firstColonIndex = message.indexOf(':');

        String command;
        String data;

        if (firstColonIndex == -1) {
            command = message.trim();
            data = null;
        }
        else {
            command = message.substring(0, firstColonIndex).trim();
            data = message.substring(firstColonIndex + 1).trim();
        }

        switch (command) {
            case "CONNECTED":
                networkController.sendMessage("REGISTER:" + username);
                break;

            case "REGISTERED":
                SwingUtilities.invokeLater(() -> {
                    frame.setTitle("Морской бой - " + username);
                    cardLayout.show(mainPanel, "MENU");
                    JOptionPane.showMessageDialog(frame,
                            "Успешная регистрация! Добро пожаловать, " + username + "!",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);
                });
                break;

            case "GAME_LIST":
                mainScreen.updateGameList(data);
                break;

            case "GAME_CREATED":
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame,
                            "Игра '" + data + "' создана!\nПереходите к расстановке кораблей.",
                            "Игра создана", JOptionPane.INFORMATION_MESSAGE);
                    mainScreen.showShipPlacementScreen(data, true);
                });
                break;

            case "CREATOR_LEFT":
                SwingUtilities.invokeLater(() -> {
                    closeAllOptionPanes();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame,
                                "Создатель лобби вышел, перенаправление на список доступных игр",
                                "Создатель вышел", JOptionPane.INFORMATION_MESSAGE);
                        cardLayout.show(mainPanel, "LOBBY");
                    });
                });
                break;

            case "GAME_JOINED":
                String[] joinParts = data.split(":");
                if (joinParts.length >= 2) {
                    String gameName = joinParts[0];
                    String creator = joinParts[1];
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame,
                                "Вы присоединились к игре '" + gameName + "'\nСоздатель: " + creator,
                                "Присоединение к игре", JOptionPane.INFORMATION_MESSAGE);
                        mainScreen.showShipPlacementScreen(gameName, false);
                    });
                }
                break;

            case "PLAYER_JOINED":
                SwingUtilities.invokeLater(() -> {
                    String playerName = data;
                    JOptionPane.showMessageDialog(frame,
                            "Игрок " + playerName + " присоединился к вашей игре!",
                            "Новый игрок", JOptionPane.INFORMATION_MESSAGE);
                });
                break;

            case "SHIPS_ACCEPTED":
                SwingUtilities.invokeLater(() -> {
                    if (shipPlacementScreen != null) {
                        mainScreen.setPlayerShipsData(shipPlacementScreen.getShipsData());
                    }
                });
                break;

            case "OPPONENT_PLACING_SHIPS":
                SwingUtilities.invokeLater(() -> {
                    if (shipPlacementScreen != null) {
                        shipPlacementScreen.showMessage("Соперник уже разместил корабли. Завершите свою расстановку!");
                    }
                });
                break;

            case "WAITING_FOR_OPPONENT":
                SwingUtilities.invokeLater(() -> {
                    mainScreen.showWaitingScreen("Корабли отправлены. Ожидание соперника...");
                });
                break;

            case "ALL_SHIPS_PLACED":
                SwingUtilities.invokeLater(() -> {
                    mainScreen.showWaitingScreenWithReadyButton();
                });
                break;

            case "GAME_START":
                SwingUtilities.invokeLater(() -> {
                    String[] startParts = data.split(":");
                    if (startParts.length >= 2) {
                        String turnInfo = startParts[0];
                        String opponentName = startParts[1];
                        mainScreen.showBattleScreen(opponentName, turnInfo.equals("YOUR_TURN"));
                    }
                });
                break;

            case "TURN":
                SwingUtilities.invokeLater(() -> {
                    String player = data;
                    boolean myTurn = player.equals(username);
                    if (battleController != null) {
                        battleController.getModel().setPlayerTurn(myTurn);

                        if (battleController.getView() != null) {
                            battleController.getView().updateUI();
                        }
                    }
                });
                break;

            case "SHOT_RESULT":
                SwingUtilities.invokeLater(() -> {
                    String[] shotParts = data.split(":");
                    if (shotParts.length >= 4) {
                        String shooter = shotParts[0];
                        String result = shotParts[1];

                        int x = Integer.parseInt(shotParts[2]);
                        int y = Integer.parseInt(shotParts[3]);

                        if (battleController != null) {
                            battleController.processShotResult(shooter, result, x, y);
                        }
                    }
                });
                break;

            case "GAME_OVER":
                SwingUtilities.invokeLater(() -> {
                    String[] msgParts = data.split(",");
                    String winner = msgParts[0];
                    String isSurrend = msgParts[1];
                    if (battleController != null) {
                        battleController.processGameOver(winner, isSurrend);
                    }
                });
                break;

            case "LEFT_GAME":
                cardLayout.show(mainPanel, "MENU");
                break;

            case "SERVER_CLOSED":
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame,
                            "Сервер остановлен, выход из игры",
                            "Сервер остановлен", JOptionPane.INFORMATION_MESSAGE);
                    networkController.disconnect();

                    if (connectionScreen != null) {
                        mainPanel.remove(connectionScreen);
                    }

                    connectionScreen = mainScreen.createConnectionScreen();
                    mainPanel.add(connectionScreen, "CONNECTION");

                    cardLayout.show(mainPanel, "CONNECTION");

                    mainPanel.revalidate();
                    mainPanel.repaint();
                });

            case "ERROR":
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame, data, "Ошибка", JOptionPane.ERROR_MESSAGE);
                });
                break;
        }
    }

    /**
     * Закрывает все открытые диалоговые окна JOptionPane.
     */
    private void closeAllOptionPanes() {
        Window[] windows = Window.getWindows();
        for (Window window : windows) {
            if (window instanceof JDialog) {
                JDialog dialog = (JDialog) window;
                if (dialog.getContentPane().getComponentCount() > 0) {
                    Component c = dialog.getContentPane().getComponent(0);
                    if (c instanceof JOptionPane) {
                        dialog.dispose();
                    }
                }
            }
        }
    }
}