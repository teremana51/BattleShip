package BattleShip.client.view;

import BattleShip.client.model.BattleState;
import BattleShip.client.model.Board;
import BattleShip.client.model.Cell;
import BattleShip.client.model.Ship;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Панель игрового экрана для игры "Морской бой".
 * <p>
 * Отображает игровое поле игрока и противника, лог игры, статус текущего хода и кнопки управления.
 * Позволяет обрабатывать клики по клеткам противника и сдачу игрока.
 */
public class BattleScreen extends JPanel {

    private static final int CELL_SIZE = 40;

    private int offsetX;
    private int offsetY;

    private BattleState battleState;

    private JPanel playerBoardPanel;
    private JPanel opponentBoardPanel;
    private JLabel statusLabel;
    private JLabel playerInfoLabel;
    private JLabel opponentInfoLabel;

    private JTextArea gameLogArea;
    private JButton surrenderButton;

    private BoardMouseListener opponentBoardListener;

    /**
     * Интерфейс для обработки действий игрока на игровом поле.
     */
    public interface BattleScreenListener {

        /**
         * Вызывается при клике по клетке противника.
         *
         * @param x координата X клетки
         * @param y координата Y клетки
         */
        void onCellClicked(int x, int y);

        /**
         * Вызывается, когда игрок сдается.
         */
        void onSurrender();
    }

    private BattleScreenListener listener;

    /**
     * Внутренний класс для обработки событий мыши на поле противника.
     */
    private class BoardMouseListener extends MouseAdapter {
        private boolean enabled;

        public BoardMouseListener(boolean enabled) {
            this.enabled = enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (!enabled || listener == null) {
                return;
            }

            int x = (e.getX() - offsetX) / CELL_SIZE;
            int y = (e.getY() - offsetY) / CELL_SIZE;

            if (x >= 0 && x < Board.SIZE && y >= 0 && y < Board.SIZE) {
                Cell cell = battleState.getOpponentBoard().getCell(x, y);

                if (cell.getState() == Cell.State.EMPTY || cell.getState() == Cell.State.SHIP) {
                    listener.onCellClicked(x, y);
                }
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (!enabled) return;

            int x = (e.getX() - offsetX) / CELL_SIZE;
            int y = (e.getY() - offsetY) / CELL_SIZE;

            if (x >= 0 && x < Board.SIZE && y >= 0 && y < Board.SIZE) {
                opponentBoardPanel.repaint();
            }
        }
    }

    /**
     * Конструктор панели.
     *
     * @param battleState состояние текущей игры
     */
    public BattleScreen(BattleState battleState) {
        this.battleState = battleState;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(new Color(240, 248, 255));

        createComponents();

        initializeUIComponents();

        setInitialValues();
    }

    public void setListener(BattleScreenListener listener) {
        this.listener = listener;
    }

    /**
     * Создает компоненты панели: доски, лог, кнопки и панель состояния.
     */
    private void createComponents() {
        JPanel topPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        topPanel.setBackground(new Color(240, 248, 255));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        playerInfoLabel = new JLabel("", SwingConstants.CENTER);
        playerInfoLabel.setFont(new Font("Arial", Font.BOLD, 16));

        statusLabel = new JLabel("Ожидание начала игры", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusLabel.setForeground(new Color(25, 25, 112));

        opponentInfoLabel = new JLabel("", SwingConstants.CENTER);
        opponentInfoLabel.setFont(new Font("Arial", Font.BOLD, 16));

        topPanel.add(playerInfoLabel);
        topPanel.add(statusLabel);
        topPanel.add(opponentInfoLabel);

        playerBoardPanel = createBoardPanel("МОИ КОРАБЛИ", true);
        opponentBoardPanel = createBoardPanel("ПРОТИВНИК", false);

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 10));
        centerPanel.setBackground(new Color(240, 248, 255));
        centerPanel.add(playerBoardPanel);
        centerPanel.add(opponentBoardPanel);

        gameLogArea = new JTextArea();
        gameLogArea.setEditable(false);
        gameLogArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        gameLogArea.setLineWrap(true);
        gameLogArea.setWrapStyleWord(true);

        JScrollPane gameLogScroll = new JScrollPane(gameLogArea);
        gameLogScroll.setPreferredSize(new Dimension(600, 100));
        gameLogScroll.setBorder(BorderFactory.createTitledBorder("Лог игры"));

        surrenderButton = new JButton("Сдаться");
        surrenderButton.setFont(new Font("Arial", Font.BOLD, 14));
        surrenderButton.setBackground(new Color(220, 20, 60));
        surrenderButton.setForeground(Color.WHITE);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        controlPanel.setBackground(new Color(240, 248, 255));
        controlPanel.add(surrenderButton);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(new Color(240, 248, 255));
        bottomPanel.add(gameLogScroll, BorderLayout.CENTER);
        bottomPanel.add(controlPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Инициализация слушателей и взаимодействий.
     */
    private void initializeUIComponents() {
        opponentBoardListener = new BoardMouseListener(false);
        opponentBoardPanel.addMouseListener(opponentBoardListener);
        opponentBoardPanel.addMouseMotionListener(opponentBoardListener);

        surrenderButton.addActionListener(e -> handleSurrender());
    }

    /**
     * Устанавливает начальные значения для компонентов.
     */
    private void setInitialValues() {
        if (battleState != null) {
            playerInfoLabel.setText(battleState.getPlayerName());
            opponentInfoLabel.setText(battleState.getOpponentName() != null ?
                    battleState.getOpponentName() : "Противник");
        }

        statusLabel.setText("Ожидание начала игры");
        gameLogArea.setText("");
    }

    /**
     * Создает панель доски.
     *
     * @param title название панели
     * @param isPlayerBoard true, если это панель игрока, false — противника
     * @return JPanel с доской
     */
    private JPanel createBoardPanel(String title, boolean isPlayerBoard) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int boardPixelSize = CELL_SIZE * Board.SIZE;

                offsetX = (getWidth() - boardPixelSize) / 2;
                offsetY = (getHeight() - boardPixelSize) / 2;

                g2d.translate(offsetX, offsetY);

                drawBoard(g2d, isPlayerBoard);
                drawCoordinates(g2d);

                g2d.dispose();
            }
        };

        panel.setPreferredSize(new Dimension(CELL_SIZE * Board.SIZE, CELL_SIZE * Board.SIZE));
        panel.setBackground(isPlayerBoard ? new Color(240, 255, 240) : new Color(255, 240, 240));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                title
        ));

        return panel;
    }

    /**
     * Отрисовывает игровое поле (доску) игрока или противника.
     * <p>
     * Метод выбирает нужную доску в зависимости от параметра {@code isPlayerBoard},
     * проходит по всем ячейкам поля и отрисовывает каждую из них,
     * а также координатную сетку.
     *
     * @param g2d              графический контекст для рисования
     * @param isPlayerBoard  {@code true} — если требуется отрисовать поле игрока,
     *                       {@code false} — если поле противника
     */
    private void drawBoard(Graphics2D g2d, boolean isPlayerBoard) {
        //g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Board board = isPlayerBoard ? battleState.getPlayerBoard() : battleState.getOpponentBoard();

        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                Cell cell = board.getCell(x, y);
                drawCell(g2d, cell, x, y, isPlayerBoard);
            }
        }

        //drawCoordinates(g2d);
    }

    /**
     * Отрисовывает одну ячейку игрового поля.
     * <p>
     * Цвет ячейки определяется её состоянием (пустая, корабль, попадание,
     * промах, потопленный корабль и т.д.). Для поля противника корабли
     * не отображаются до момента попадания.
     *
     * @param g2d            графический контекст {@link Graphics2D}
     * @param cell           ячейка игрового поля
     * @param x              координата X ячейки
     * @param y              координата Y ячейки
     * @param isPlayerBoard  {@code true}, если ячейка принадлежит полю игрока
     */
    private void drawCell(Graphics2D g2d, Cell cell, int x, int y, boolean isPlayerBoard) {
        int cellX = x * CELL_SIZE;
        int cellY = y * CELL_SIZE;

        Color cellColor = Color.WHITE;
        Color borderColor = Color.LIGHT_GRAY;

        switch (cell.getState()) {
            case EMPTY:
                cellColor = new Color(240, 248, 255);
                break;

            case SHIP:
                if (isPlayerBoard) {
                    Ship ship = cell.getShip();
                    if (ship != null) {
                        cellColor = new Color(153, 17, 153);
                    }
                }
                else {
                    cellColor = new Color(240, 248, 255);
                }
                break;

            case HIT:
                cellColor = new Color(255, 0, 0);
                break;

            case MISS:
                cellColor = new Color(30, 144, 255);
                break;

            case SHIP_SUNK:
                cellColor = new Color(139, 0, 0);
                break;

            case FORBIDDEN:
                cellColor = new Color(255, 182, 193);
                break;
        }

        g2d.setColor(cellColor);
        g2d.fillRect(cellX, cellY, CELL_SIZE, CELL_SIZE);

        g2d.setColor(borderColor);
        g2d.drawRect(cellX, cellY, CELL_SIZE, CELL_SIZE);

        if (cell.getState() == Cell.State.HIT || cell.getState() == Cell.State.SHIP_SUNK) {
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(cellX + 5, cellY + 5, cellX + CELL_SIZE - 5, cellY + CELL_SIZE - 5);
            g2d.drawLine(cellX + CELL_SIZE - 5, cellY + 5, cellX + 5, cellY + CELL_SIZE - 5);
        }

        if (cell.getState() == Cell.State.MISS) {
            g2d.setColor(Color.WHITE);
            g2d.fillOval(cellX + CELL_SIZE/2 - 3, cellY + CELL_SIZE/2 - 3, 6, 6);
        }
    }

    /**
     * Отрисовывает координаты игрового поля.
     * <p>
     * По горизонтали используются буквенные обозначения (А–К),
     * по вертикали — числовые (1–10).
     *
     * @param g2d графический контекст {@link Graphics2D}
     */
    private void drawCoordinates(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        for (int x = 0; x < Board.SIZE; x++) {
            String letter = String.valueOf((char) ('А' + x));
            int letterX = x * CELL_SIZE + CELL_SIZE / 2 - 4;
            int letterY = -5;
            g2d.drawString(letter, letterX, letterY);
        }

        for (int y = 0; y < Board.SIZE; y++) {
            String number = String.valueOf(y + 1);
            int numberX = -15;
            int numberY = y * CELL_SIZE + CELL_SIZE / 2 + 4;
            g2d.drawString(number, numberX, numberY);
        }
    }

    /**
     * Обрабатывает действие «Сдаться» во время боя.
     * <p>
     * Метод запрашивает подтверждение у игрока и, в случае согласия,
     * уведомляет слушателя, обновляет интерфейс и блокирует дальнейшие действия.
     */
    private void handleSurrender() {
        if (listener != null) {
            UIManager.put("OptionPane.yesButtonText", "Да");
            UIManager.put("OptionPane.noButtonText", "Нет");

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Вы уверены, что хотите сдаться?\nЭто приведет к техническому поражению.",
                    "Подтверждение сдачи",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                addGameLog("Вы сдались. Техническое поражение!");

                listener.onSurrender();

                surrenderButton.setEnabled(false);
                opponentBoardListener.setEnabled(false);
                statusLabel.setText("ВЫ СДАЛИСЬ");
                statusLabel.setForeground(new Color(139, 0, 0));
            }
        }
    }

    /**
     * Обновляет UI: доски, статус и лог игры.
     */
    public void updateUI() {
        if (battleState == null) {
            return;
        }

        String playerName = battleState.getPlayerName();
        String opponentName = battleState.getOpponentName();

        if (playerName == null) {
            playerName = "Игрок";
        }

        if (opponentName == null) {
            opponentName = "Противник";
        }

        playerInfoLabel.setText(String.format(
                "<html><center>%s<br>Кораблей: %d</center></html>",
                playerName,
                battleState.getPlayerShipsLeft()
        ));

        opponentInfoLabel.setText(String.format(
                "<html><center>%s<br>Кораблей: %d</center></html>",
                opponentName,
                battleState.getOpponentShipsLeft()
        ));

        if (battleState.isGameOver()) {
            if (battleState.getWinner() != null &&
                    battleState.getWinner().equals(battleState.getPlayerName())) {
                statusLabel.setText("ПОБЕДА!");
                statusLabel.setForeground(new Color(0, 128, 0));
            }
            else {
                statusLabel.setText("ПОРАЖЕНИЕ");
                statusLabel.setForeground(new Color(220, 20, 60));
            }
            surrenderButton.setEnabled(false);
            opponentBoardListener.setEnabled(false);
        }
        else if (battleState.isPlayerTurn()) {
            statusLabel.setText("ВАШ ХОД");
            statusLabel.setForeground(new Color(0, 100, 0));
            opponentBoardListener.setEnabled(true);
        }
        else {
            statusLabel.setText("ХОД ПРОТИВНИКА");
            statusLabel.setForeground(new Color(220, 20, 60));
            opponentBoardListener.setEnabled(false);
        }

        updateGameLog();

        if (playerBoardPanel != null) {
            playerBoardPanel.repaint();
        }

        if (opponentBoardPanel != null) {
            opponentBoardPanel.repaint();
        }
    }

    private void updateGameLog() {
        StringBuilder logText = new StringBuilder();
        List<String> log = battleState.getGameLog();

        for (String entry : log) {
            logText.append(entry).append("\n");
        }

        gameLogArea.setText(logText.toString());

        gameLogArea.setCaretPosition(gameLogArea.getDocument().getLength());
    }

    public void addGameLog(String message) {
        battleState.addLog(message);
        updateGameLog();
    }
}