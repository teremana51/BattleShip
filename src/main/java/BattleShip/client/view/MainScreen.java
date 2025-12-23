package BattleShip.client.view;

import BattleShip.client.utils.LoggerClient;
import BattleShip.client.controller.BattleController;
import BattleShip.client.controller.GameController;
import BattleShip.client.controller.NetworkController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Основной экран клиента игры "Морской бой".
 * <p>
 * Управляет навигацией между экранами подключения, главного меню, списка игр,
 * расстановки кораблей и игрового поля.
 */
public class MainScreen {

    private CardLayout cardLayout;

    private JFrame frame;

    private JTable gamesTable;

    private JPanel mainPanel;
    private JPanel connectionScreen;
    private JPanel battleScreen;
    private JPanel waitingWithReadyPanel;

    private NetworkController networkController;
    private BattleController battleController;
    private GameController gameController;

    private String username;
    private String playerShipsData;
    private String currentGameName;

    private DefaultTableModel gamesTableModel;

    private ShipPlacementScreen shipPlacementScreen;

    private boolean isGameCreator = false;

    /**
     * Инициализация интерфейса.
     */
    public void init() {
        createAndShowGUI();
    }

    public void setPlayerShipsData(String playerShipsData) {
        this.playerShipsData = playerShipsData;
    }

    /**
     * Рендерер кнопки для таблицы списка игр.
     */
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBorderPainted(true);
            setFocusPainted(false);
            setContentAreaFilled(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (value != null) {
                setText(value.toString());

                String text = value.toString();

                if (text.equals("Присоединиться")) {
                    setBackground(new Color(76, 175, 80)); // Зеленый
                    setForeground(Color.WHITE);
                    setEnabled(true);
                }
                else if (text.equals("В процессе")) {
                    setBackground(Color.ORANGE);
                    setForeground(Color.BLACK);
                    setEnabled(false);
                }
                else {
                    setBackground(Color.LIGHT_GRAY);
                    setForeground(Color.DARK_GRAY);
                    setEnabled(false);
                }
            }
            else {
                setText("");
                setBackground(Color.LIGHT_GRAY);
            }

            return this;
        }
    }

    /**
     * Редактор кнопки для таблицы списка игр.
     */
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int clickedRow;
        private String clickedGame;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            if (value != null) {
                label = value.toString();
                clickedRow = row;
                clickedGame = (String) table.getValueAt(row, 0);
            }
            else {
                label = "";
                clickedGame = null;
            }

            button.setText(label);

            if (label.equals("Присоединиться")) {
                button.setBackground(new Color(86, 185, 90));
                button.setForeground(Color.WHITE);
            }
            else {
                button.setBackground(Color.LIGHT_GRAY);
                button.setForeground(Color.DARK_GRAY);
            }

            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed && clickedGame != null && label.equals("Присоединиться")) {
                networkController.sendMessage("JOIN_GAME:" + clickedGame);
            }
            isPushed = false;
            return label;
        }
    }

    /**
     * Создает и отображает графический интерфейс.
     */
    private void createAndShowGUI() {
        frame = new JFrame("Морской бой");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(new Color(240, 248, 255));

        networkController = new NetworkController();
        connectionScreen = createConnectionScreen();

        mainPanel.add(connectionScreen, "CONNECTION");
        mainPanel.add(createMainMenuScreen(), "MENU");
        mainPanel.add(createGameLobbyScreen(), "LOBBY");

        shipPlacementScreen = new ShipPlacementScreen(networkController);

        mainPanel.add(shipPlacementScreen, "PLACEMENT");

        mainPanel.add(createWaitingScreen(), "WAITING");

        waitingWithReadyPanel = createWaitingWithReadyPanel();
        mainPanel.add(waitingWithReadyPanel, "WAITING_READY");

        frame.add(mainPanel);
        cardLayout.show(mainPanel, "CONNECTION");
        frame.setVisible(true);

        gameController = new GameController(frame, cardLayout, mainPanel,
                networkController, connectionScreen, shipPlacementScreen, this);
    }

    /**
     * Создает экран подключения к серверу.
     *
     * @return панель подключения
     */
    public JPanel createConnectionScreen() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 248, 255));

        JLabel titleLabel = new JLabel("МОРСКОЙ БОЙ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(new Color(25, 25, 112));

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("Ваше имя:"), gbc);

        JTextField usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        centerPanel.add(new JLabel("Адрес сервера:"), gbc);

        JTextField serverAddressField = new JTextField("localhost", 20);
        serverAddressField.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0; gbc.gridy = 3;
        centerPanel.add(serverAddressField, gbc);

        JButton connectButton = new JButton("ПОДКЛЮЧИТЬСЯ");
        connectButton.setFont(new Font("Arial", Font.BOLD, 16));
        connectButton.setBackground(new Color(30, 144, 255));
        connectButton.setForeground(Color.WHITE);
        connectButton.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 4;
        centerPanel.add(connectButton, gbc);

        JLabel statusLabel = new JLabel("Введите данные для подключения", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        connectButton.addActionListener(e -> {
            username = usernameField.getText().trim();
            String serverAddress = serverAddressField.getText().trim();

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Введите имя пользователя!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (serverAddress.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Введите адрес сервера!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            statusLabel.setText("Подключение к " + serverAddress + "...");
            statusLabel.setForeground(Color.BLUE);
            connectButton.setEnabled(false);

            gameController.setUsername(username);

            new Thread(() -> {
                try {
                    networkController.connect(serverAddress, 12345, gameController::processServerMessage);

                }
                catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Ошибка подключения: " + ex.getMessage());
                        statusLabel.setForeground(Color.RED);
                        connectButton.setEnabled(true);
                        JOptionPane.showMessageDialog(panel,
                                "Не удалось подключиться к серверу: " + ex.getMessage(),
                                "Ошибка подключения", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }).start();
        });

        usernameField.addActionListener(e -> connectButton.doClick());
        serverAddressField.addActionListener(e -> connectButton.doClick());

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Создаёт и настраивает главный экран меню приложения.
     * <p>
     * Экран содержит кнопки для создания новой игры, просмотра списка доступных игр,
     * перехода в настройки и выхода из приложения.
     *
     * @return панель {@link JPanel}, представляющая главный экран меню
     */
    private JPanel createMainMenuScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("ГЛАВНОЕ МЕНЮ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(25, 25, 112));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        JButton createGameButton = createMenuButton("СОЗДАТЬ ИГРУ", new Color(30, 144, 255));
        JButton listGamesButton = createMenuButton("СПИСОК ИГР", new Color(50, 205, 50));
        JButton settingsButton = createMenuButton("НАСТРОЙКИ", new Color(255, 140, 0));
        JButton exitButton = createMenuButton("ВЫХОД", new Color(220, 20, 60));

        createGameButton.addActionListener(e -> showCreateGameDialog());
        listGamesButton.addActionListener(e -> {
            networkController.sendMessage("LIST_GAMES");
            cardLayout.show(mainPanel, "LOBBY");
        });
        settingsButton.addActionListener(e -> showSettings());
        exitButton.addActionListener(e -> disconnectAndExit());

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(createGameButton, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(listGamesButton, gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(settingsButton, gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(exitButton, gbc);

        return panel;
    }

    /**
     * Создаёт стилизованную кнопку меню с заданным текстом и цветом.
     * <p>
     * Кнопка автоматически изменяет цвет при наведении курсора мыши.
     *
     * @param text  текст, отображаемый на кнопке
     * @param color основной цвет фона кнопки
     * @return настроенная кнопка {@link JButton}
     */
    private JButton createMenuButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    /**
     * Создаёт экран лобби со списком доступных игровых сессий.
     * <p>
     * Экран содержит таблицу игр, а также кнопки для обновления списка
     * и возврата в главное меню.
     *
     * @return панель {@link JPanel}, представляющая экран лобби
     */
    private JPanel createGameLobbyScreen() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(240, 248, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("ДОСТУПНЫЕ ИГРЫ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 25, 112));

        String[] columnNames = {"Название игры", "Создатель", "Игроков", "Статус", "Действие"};
        gamesTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        gamesTable = new JTable(gamesTableModel);
        gamesTable.setFillsViewportHeight(true);
        gamesTable.setRowHeight(35);
        gamesTable.setFont(new Font("Arial", Font.PLAIN, 12));
        gamesTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        gamesTable.getTableHeader().setBackground(new Color(70, 130, 180));
        gamesTable.getTableHeader().setForeground(Color.WHITE);

        gamesTable.getColumnModel().getColumn(4).setCellRenderer(new MainScreen.ButtonRenderer());
        gamesTable.getColumnModel().getColumn(4).setCellEditor(new MainScreen.ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(gamesTable);
        scrollPane.setPreferredSize(new Dimension(800, 400));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));

        JButton refreshButton = new JButton("Обновить список");
        JButton backButton = new JButton("Назад в меню");

        refreshButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setFont(new Font("Arial", Font.BOLD, 14));

        refreshButton.addActionListener(e -> {
            networkController.sendMessage("LIST_GAMES");
        });

        backButton.addActionListener(e -> cardLayout.show(mainPanel, "MENU"));

        buttonPanel.add(refreshButton);
        buttonPanel.add(backButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        SwingUtilities.invokeLater(() -> {
            networkController.sendMessage("LIST_GAMES");
        });

        return panel;
    }

    /**
     * Создаёт экран ожидания подключения или действий соперника.
     *
     * @return панель {@link JPanel} с индикатором ожидания и кнопкой отмены
     */
    private JPanel createWaitingScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        panel.setBackground(new Color(240, 248, 255));

        JLabel label = new JLabel("Ожидание...", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(new Color(25, 25, 112));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 20));

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> {
            networkController.sendMessage("LEAVE_GAME");
            cardLayout.show(mainPanel, "MENU");
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(240, 248, 255));
        buttonPanel.add(cancelButton);

        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Создаёт экран ожидания с кнопкой подтверждения готовности к началу игры.
     *
     * @return панель {@link JPanel}, отображающая ожидание старта игры
     */
    private JPanel createWaitingWithReadyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        panel.setBackground(new Color(240, 248, 255));

        JLabel label = new JLabel("Оба игрока разместили корабли!", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(new Color(25, 25, 112));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(300, 20));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 248, 255));

        JButton readyButton = new JButton("Готов начать игру");
        readyButton.addActionListener(e -> {
            networkController.sendMessage("READY");
            showWaitingScreen("Ожидание готовности соперника...");
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> {
            networkController.sendMessage("LEAVE_GAME");
            cardLayout.show(mainPanel, "MENU");
        });

        buttonPanel.add(readyButton);
        buttonPanel.add(cancelButton);

        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Отображает экран ожидания с заданным текстовым сообщением.
     *
     * @param message сообщение, отображаемое пользователю
     */
    public void showWaitingScreen(String message) {
        JPanel waitingPanel = (JPanel) mainPanel.getComponent(4);

        for (Component comp : waitingPanel.getComponents()) {
            if (comp instanceof JLabel) {
                JLabel label = (JLabel) comp;
                label.setText(message);
                break;
            }
        }

        cardLayout.show(mainPanel, "WAITING");
    }

    /**
     * Отображает экран ожидания с кнопкой подтверждения готовности.
     */
    public void showWaitingScreenWithReadyButton() {
        cardLayout.show(mainPanel, "WAITING_READY");
    }

    /**
     * Отображает экран размещения кораблей для выбранной игры.
     *
     * @param gameName  название игровой сессии
     * @param isCreator {@code true}, если пользователь является создателем игры
     */
    public void showShipPlacementScreen(String gameName, boolean isCreator) {
        this.currentGameName = gameName;
        this.isGameCreator = isCreator;
        shipPlacementScreen.setGameInfo(gameName, username, isCreator);
        gameController.setShipPlacementScreen(shipPlacementScreen);
        cardLayout.show(mainPanel, "PLACEMENT");
    }

    /**
     * Отображает экран боя и инициализирует контроллер игрового процесса.
     * <p>
     * Метод создаёт новый {@link BattleController}, настраивает обработчики
     * возврата в меню и повторной игры, передаёт данные о сопернике и размещении
     * кораблей игрока, а затем переключает интерфейс на экран боя.
     *
     * @param opponentName имя соперника
     * @param playerTurn   {@code true}, если первый ход принадлежит текущему игроку
     */
    public void showBattleScreen(String opponentName, boolean playerTurn) {
        battleController = new BattleController(networkController, username);

        battleController.setOnPlayAgainCallback(() -> {
            showShipPlacementScreen(currentGameName, isGameCreator);
            networkController.sendMessage("LIST_GAMES");
        });

        battleController.setOnReturnToMenuCallback(() -> {
            networkController.sendMessage("LEAVE_GAME");
        });

        battleController.setOpponentName(opponentName);

        if (playerShipsData != null && !playerShipsData.isEmpty()) {
            battleController.setPlayerBoardFromPlacement(playerShipsData);
        }


        gameController.setBattleController(battleController);

        battleScreen = battleController.createView();

        boolean alreadyAdded = false;
        for (Component comp : mainPanel.getComponents()) {
            if (comp == battleScreen) {
                alreadyAdded = true;
                break;
            }
        }

        if (!alreadyAdded) {
            mainPanel.add(battleScreen, "BATTLE");
            gameController.setMainPanel(mainPanel);
        }

        battleController.processGameStart(playerTurn ? "YOUR_TURN" : "OPPONENT_TURN");

        cardLayout.show(mainPanel, "BATTLE");
    }

    /**
     * Обновляет таблицу доступных игровых сессий в лобби.
     * <p>
     * Метод парсит строку с данными об играх, полученную от сервера,
     * и заполняет таблицу актуальной информацией: названием игры,
     * создателем, количеством игроков, статусом и доступным действием.
     *
     * @param gamesData строка с данными об играх в формате:
     *                  {@code gameName:creator:playerCount:status;...}
     */
    public void updateGameList(String gamesData) {
        SwingUtilities.invokeLater(() -> {
            if (gamesTableModel == null) {
                return;
            }

            gamesTableModel.setRowCount(0);

            if (gamesData == null || gamesData.isEmpty() || gamesData.trim().equals("")) {
                return;
            }

            String[] games = gamesData.split(";");

            for (String game : games) {
                if (game != null && !game.trim().isEmpty()) {

                    String[] gameInfo = game.split(":");

                    if (gameInfo.length == 4) {
                        String gameName = gameInfo[0].trim();
                        String creator = gameInfo[1].trim();
                        String playerCountStr = gameInfo[2].trim();
                        String status = gameInfo[3].trim();

                        int playerCount;
                        try {
                            playerCount = Integer.parseInt(playerCountStr);
                        }
                        catch (NumberFormatException e) {
                            continue;
                        }

                        String statusText;
                        switch (status) {
                            case "WAITING":
                                statusText = "Ожидание игроков";
                                break;

                            case "PLACING_SHIPS":
                                statusText = "Расстановка кораблей";
                                break;

                            case "IN_PROGRESS":
                                statusText = "Игра идет";
                                break;

                            default:
                                statusText = status;
                        }

                        String action;
                        if (playerCount < 2 && !status.equals("IN_PROGRESS")) {
                            action = "Присоединиться";
                        }
                        else if (status.equals("IN_PROGRESS")) {
                            action = "В процессе";
                        }
                        else {
                            action = "Заполнено";
                        }

                        gamesTableModel.addRow(new Object[]{
                                gameName, creator, playerCount + "/2", statusText, action
                        });

                    }
                }
            }

            gamesTable.revalidate();
            gamesTable.repaint();
        });
    }

    /**
     * Отображает диалог создания новой игры.
     * <p>
     * Пользователю предлагается выбрать режим игры:
     * по сети или против компьютера. В зависимости от выбора
     * формируется соответствующий запрос серверу.
     */
    private void showCreateGameDialog() {
        String[] options = {"По сети", "Против компьютера"};
        int choice = JOptionPane.showOptionDialog(frame,
                "Выберите режим игры",
                "Создать игру",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (choice == 0) {
            String gameName = JOptionPane.showInputDialog(frame,
                    "Введите название игры (3-20 символов):",
                    "Создание игры",
                    JOptionPane.QUESTION_MESSAGE);

            if (gameName != null && !gameName.trim().isEmpty()) {
                networkController.sendMessage("CREATE_GAME:" + gameName.trim());
            }
        }
        else if (choice == 1) {
            networkController.sendMessage("CREATE_GAME:" + "AI");
        }
    }

    /**
     * Отображает окно с информацией о настройках игры.
     * <p>
     * В окне показываются параметры игрового поля,
     * список кораблей и доступные режимы игры.
     */
    private void showSettings() {
        JOptionPane.showMessageDialog(frame,
                "Настройки игры:\n\n" +
                        "• Размер поля: 10x10\n" +
                        "• Корабли:\n" +
                        "   - 1 авианосец (4 клетки)\n" +
                        "   - 2 линкора (3 клетки)\n" +
                        "   - 3 крейсера (2 клетки)\n" +
                        "   - 4 эсминца (1 клетка)\n" +
                        "• Режимы игры:\n" +
                        "   - По сети\n"+
                        "   - Против компьютера",
                "Настройки", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Корректно завершает работу клиента.
     * <p>
     * Метод отправляет серверу сообщение о разрыве соединения,
     * останавливает сетевое соединение и логгер, после чего
     * завершает выполнение приложения.
     */
    private void disconnectAndExit() {
        networkController.sendMessage("DISCONNECT");
        networkController.disconnect();
        LoggerClient.shutdown();
        System.exit(0);
    }
}
