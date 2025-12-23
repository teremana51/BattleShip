package BattleShip.client.view;

import BattleShip.client.controller.NetworkController;
import BattleShip.client.model.PlayerBoard;

import javax.swing.*;
import java.awt.*;

/**
 * Панель для расстановки кораблей игрока.
 * <p>
 * Отвечает за отображение игрового поля, контроль за готовностью расстановки,
 * а также взаимодействие с сервером через NetworkController.
 */
public class ShipPlacementScreen extends JPanel {

    private ShipPlacementPanel shipPlacementPanel;

    private NetworkController networkController;

    private PlayerBoard playerBoard;

    private String currentGameName;

    private String shipsData = "";

    private JLabel gameInfoLabel;

    private JButton readyButton;
    private JButton cancelButton;

    /**
     * Конструктор панели расстановки кораблей.
     *
     * @param networkController контроллер сети для отправки сообщений на сервер
     */
    public ShipPlacementScreen(NetworkController networkController) {
        this.networkController = networkController;
        this.playerBoard = new PlayerBoard();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(240, 248, 255));

        initializeUI();
    }

    public String getShipsData() {
        return shipsData;
    }

    /**
     * Инициализация пользовательского интерфейса панели.
     * <p>
     * Создает информационную панель, игровое поле и панель управления.
     */
    private void initializeUI() {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        infoPanel.setBackground(new Color(240, 248, 255));

        gameInfoLabel = new JLabel("Расстановка кораблей", SwingConstants.CENTER);
        gameInfoLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gameInfoLabel.setForeground(new Color(25, 25, 112));

        infoPanel.add(gameInfoLabel);

        shipPlacementPanel = new ShipPlacementPanel(playerBoard);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        controlPanel.setBackground(new Color(240, 248, 255));

        readyButton = new JButton("Отправить расстановку");
        readyButton.setFont(new Font("Arial", Font.BOLD, 14));
        readyButton.setBackground(new Color(76, 175, 80));
        readyButton.setForeground(Color.WHITE);
        readyButton.setEnabled(false);

        cancelButton = new JButton("Отмена");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setBackground(new Color(244, 67, 54));
        cancelButton.setForeground(Color.WHITE);

        shipPlacementPanel.setListener(new ShipPlacementPanel.ShipPlacementListener() {

            @Override
            public void onPlacementReady() {
                if (shipPlacementPanel.isReady()) {
                    readyButton.setEnabled(true);
                    readyButton.setBackground(new Color(56, 142, 60));
                }
            }

            @Override
            public void onPlacementCancelled() {
                networkController.sendMessage("LEAVE_GAME");
            }
        });

        readyButton.addActionListener(e -> {
            if (shipPlacementPanel.isReady()) {
                shipsData = shipPlacementPanel.getShipsPlacement();

                networkController.sendMessage("SHIPS_PLACED:" + shipsData);

                readyButton.setEnabled(false);
                readyButton.setText("Отправлено на сервер...");
                gameInfoLabel.setText("Корабли отправлены. Завершите расстановку и нажмите 'Готов'");
            }
        });

        cancelButton.addActionListener(e -> {
            networkController.sendMessage("LEAVE_GAME");
        });

        controlPanel.add(readyButton);
        controlPanel.add(cancelButton);

        add(infoPanel, BorderLayout.NORTH);
        add(shipPlacementPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Устанавливает информацию о текущей игре и сбрасывает поле игрока.
     *
     * @param gameName   название игры
     * @param creator    имя создателя игры
     * @param isCreator  true, если текущий игрок является создателем
     */
    public void setGameInfo(String gameName, String creator, boolean isCreator) {
        this.currentGameName = gameName;
        String role = isCreator ? "Создатель" : "Участник";
        gameInfoLabel.setText("Игра: " + gameName + " | " + role + " | Расстановка кораблей");

        playerBoard.reset();
        shipPlacementPanel = new ShipPlacementPanel(playerBoard);
        removeAll();
        initializeUI();
        revalidate();
        repaint();
    }

    /**
     * Отображает временное сообщение пользователю на панели.
     *
     * @param message сообщение для отображения
     */
    public void showMessage(String message) {
        gameInfoLabel.setText(message);
        gameInfoLabel.setForeground(Color.RED);

        Timer timer = new Timer(3000, e -> {
            gameInfoLabel.setForeground(new Color(25, 25, 112));
        });
        timer.setRepeats(false);
        timer.start();
    }
}