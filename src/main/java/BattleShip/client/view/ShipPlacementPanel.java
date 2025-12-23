package BattleShip.client.view;

import BattleShip.client.model.Board;
import BattleShip.client.model.Cell;
import BattleShip.client.model.Ship;
import BattleShip.client.model.ShipType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Панель для размещения кораблей на игровом поле.
 * <p>
 * Отображает игровое поле, список доступных кораблей, панель управления,
 * позволяет размещать, вращать, случайно расставлять и очищать корабли.
 */
public class ShipPlacementPanel extends JPanel {

    private static final int CELL_SIZE = 40;

    private static final Color[] SHIP_COLORS = {
            new Color(153, 17, 153),
            new Color(153, 17, 153),
            new Color(153, 17, 153),
            new Color(153, 17, 153)
    };

    private int offsetX;
    private int offsetY;

    private Board board;
    private Ship currentShip;
    private Map<ShipType, Integer> shipsToPlace;
    private ShipPlacementListener listener;

    private JPanel boardPanel;
    private JPanel shipsPanel;
    private JLabel statusLabel;

    private JButton rotateButton;
    private JButton readyButton;
    private JButton randomButton;
    private JButton clearButton;

    /**
     * Интерфейс слушателя для уведомления о событиях расстановки кораблей.
     */
    public interface ShipPlacementListener {
        /**
         * Вызывается, когда все корабли размещены и игрок готов.
         */
        void onPlacementReady();

        /**
         * Вызывается, когда игрок отменяет расстановку и выходит назад.
         */
        void onPlacementCancelled();
    }

    /**
     * Конструктор панели расстановки кораблей.
     *
     * @param board игровое поле, на котором размещаются корабли
     */
    public ShipPlacementPanel(Board board) {
        this.board = board;
        this.shipsToPlace = new HashMap<>();
        initializeShipsToPlace();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        createBoardPanel();
        createControlPanel();
        createShipsPanel();

        updateStatus();
    }

    /**
     * Инициализирует количество кораблей каждого типа для размещения.
     */
    private void initializeShipsToPlace() {
        shipsToPlace.put(ShipType.CARRIER, 1);
        shipsToPlace.put(ShipType.BATTLESHIP, 2);
        shipsToPlace.put(ShipType.CRUISER, 3);
        shipsToPlace.put(ShipType.DESTROYER, 4);
    }

    /**
     * Создает панель с игровым полем.
     */
    private void createBoardPanel() {
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int boardPixelSize = CELL_SIZE * Board.SIZE;

                offsetX = (getWidth() - boardPixelSize) / 2;
                offsetY = (getHeight() - boardPixelSize) / 2;

                g2d.translate(offsetX, offsetY);

                drawBoard(g2d);
                drawGrid(g2d);
                drawShips(g2d);
                drawCurrentShipPreview(g2d);
                drawCoordinates(g2d);

                g2d.dispose();
            }
        };

        boardPanel.setPreferredSize(new Dimension(CELL_SIZE * Board.SIZE, CELL_SIZE * Board.SIZE));
        boardPanel.setBackground(new Color(240, 248, 255)); // AliceBlue
        boardPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        boardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = (e.getX() - offsetX) / CELL_SIZE;
                int y = (e.getY() - offsetY) / CELL_SIZE;

                if (x >= 0 && x < Board.SIZE && y >= 0 && y < Board.SIZE) {
                    if (currentShip != null) {
                        placeShip(x, y);
                    }
                }
            }
        });

        boardPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boardPanel.repaint();
            }
        });

        add(boardPanel, BorderLayout.CENTER);
    }

    /**
     * Создает панель управления кораблями и кнопками.
     */
    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

        statusLabel = new JLabel("Разместите ваши корабли");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        rotateButton = new JButton("Повернуть");
        rotateButton.addActionListener(e -> rotateCurrentShip());

        randomButton = new JButton("Случайная расстановка");
        randomButton.addActionListener(e -> placeShipsRandomly());

        clearButton = new JButton("Очистить поле");
        clearButton.addActionListener(e -> clearBoard());

        readyButton = new JButton("Готово");
        readyButton.setEnabled(false);
        readyButton.addActionListener(e -> {
            if (listener != null) {
                listener.onPlacementReady();
            }
        });

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(e -> {
            if (listener != null) {
                listener.onPlacementCancelled();
            }
        });

        controlPanel.add(backButton);
        controlPanel.add(rotateButton);
        controlPanel.add(randomButton);
        controlPanel.add(clearButton);
        controlPanel.add(readyButton);

        add(controlPanel, BorderLayout.SOUTH);
    }

    /**
     * Создает панель со списком кораблей для размещения.
     */
    private void createShipsPanel() {
        shipsPanel = new JPanel();
        shipsPanel.setLayout(new BoxLayout(shipsPanel, BoxLayout.Y_AXIS));
        shipsPanel.setBorder(BorderFactory.createTitledBorder("Корабли для размещения"));

        updateShipsPanel();

        JScrollPane scrollPane = new JScrollPane(shipsPanel);
        scrollPane.setPreferredSize(new Dimension(280, 400));

        add(scrollPane, BorderLayout.EAST);
    }

    /**
     * Обновляет отображение панели со списком оставшихся кораблей.
     */
    private void updateShipsPanel() {
        shipsPanel.removeAll();

        for (ShipType type : ShipType.values()) {
            int count = shipsToPlace.getOrDefault(type, 0);
            if (count > 0) {
                JPanel shipPanel = createShipPanel(type, count);
                shipsPanel.add(shipPanel);
                shipsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }

        shipsPanel.revalidate();
        shipsPanel.repaint();
    }

    /**
     * Создает панель отдельного корабля для выбора.
     *
     * @param type  тип корабля
     * @param count количество оставшихся кораблей этого типа
     * @return панель для отображения корабля
     */
    private JPanel createShipPanel(ShipType type, int count) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawShipPreview(g, type, count > 0);
            }
        };
        previewPanel.setPreferredSize(new Dimension(100, 30));
        previewPanel.setBackground(Color.WHITE);
        previewPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        previewPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (count > 0) {
                    selectShip(type);
                }
            }
        });

        JLabel label = new JLabel(type.getDisplayName() + " (" + count + ")");
        label.setFont(new Font("Arial", Font.PLAIN, 12));

        panel.add(previewPanel, BorderLayout.WEST);
        panel.add(label, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Отрисовывает превью корабля в панели выбора.
     *
     * @param g          графический контекст
     * @param type       тип корабля
     * @param available  признак доступности корабля для размещения
     */
    private void drawShipPreview(Graphics g, ShipType type, boolean available) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color shipColor = SHIP_COLORS[type.ordinal()];
        if (!available) {
            shipColor = shipColor.darker();
        }

        g2d.setColor(shipColor);
        int shipLength = type.getSize();

        for (int i = 0; i < shipLength; i++) {
            g2d.fillRect(5 + i * 25, 5, 20, 20);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(5 + i * 25, 5, 20, 20);
            g2d.setColor(shipColor);
        }
    }

    /**
     * Отрисовывает игровое поле целиком, включая все его клетки.
     *
     * @param g2d графический контекст
     */
    private void drawBoard(Graphics2D g2d) {
        for (int y = 0; y < Board.SIZE; y++) {
            for (int x = 0; x < Board.SIZE; x++) {
                Cell cell = board.getCell(x, y);
                drawCell(g2d, cell, x, y);
            }
        }
    }

    /**
     * Отрисовывает одну клетку игрового поля с учётом её состояния.
     *
     * @param g2d графический контекст
     * @param cell клетка игрового поля
     * @param x координата X клетки
     * @param y координата Y клетки
     */
    private void drawCell(Graphics2D g2d, Cell cell, int x, int y) {
        int cellX = x * CELL_SIZE;
        int cellY = y * CELL_SIZE;

        Color cellColor = Color.WHITE;
        switch (cell.getState()) {
            case SHIP:
                Ship ship = cell.getShip();
                if (ship != null) {
                    cellColor = SHIP_COLORS[ship.getType().ordinal()];
                }
                break;
            case FORBIDDEN:
                cellColor = new Color(255, 200, 200);
                break;
            default:
                cellColor = new Color(240, 248, 255);
        }

        g2d.setColor(cellColor);
        g2d.fillRect(cellX, cellY, CELL_SIZE, CELL_SIZE);

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawRect(cellX, cellY, CELL_SIZE, CELL_SIZE);
    }

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
     * Отрисовывает сетку игрового поля.
     *
     * @param g2d графический контекст
     */
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(Color.DARK_GRAY);

        for (int x = 0; x <= Board.SIZE; x++) {
            g2d.drawLine(x * CELL_SIZE, 0, x * CELL_SIZE, Board.SIZE * CELL_SIZE);
        }

        for (int y = 0; y <= Board.SIZE; y++) {
            g2d.drawLine(0, y * CELL_SIZE, Board.SIZE * CELL_SIZE, y * CELL_SIZE);
        }
    }

    /**
     * Отрисовывает все размещённые на поле корабли.
     *
     * @param g2d графический контекст
     */
    private void drawShips(Graphics2D g2d) {
        for (Ship ship : board.getShips()) {
            drawShip(g2d, ship);
        }
    }

    /**
     * Отрисовывает один корабль на игровом поле.
     *
     * @param g2d графический контекст
     * @param ship корабль
     */
    private void drawShip(Graphics2D g2d, Ship ship) {
        Color shipColor = SHIP_COLORS[ship.getType().ordinal()];
        g2d.setColor(shipColor);

        for (Cell cell : ship.getCells()) {
            int x = cell.getX() * CELL_SIZE;
            int y = cell.getY() * CELL_SIZE;
            g2d.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);

            // Контур корабля
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
            g2d.setColor(shipColor);
        }
    }

    /**
     * Отрисовывает полупрозрачное превью текущего корабля
     * в позиции курсора мыши.
     *
     * @param g2d графический контекст
     */
    private void drawCurrentShipPreview(Graphics2D g2d) {
        if (currentShip == null) {
            return;
        }

        Point mousePos = boardPanel.getMousePosition();
        if (mousePos == null) {
            return;
        }

        int x = (mousePos.x - offsetX) / CELL_SIZE;
        int y = (mousePos.y - offsetY) / CELL_SIZE;

        if (x < 0 || y < 0 || x >= Board.SIZE || y >= Board.SIZE) {
            return;
        }

        boolean canPlace = board.canPlaceShip(currentShip, x, y);

        Color previewColor = SHIP_COLORS[currentShip.getType().ordinal()];
        previewColor = new Color(previewColor.getRed(), previewColor.getGreen(),
                previewColor.getBlue(), 128);

        if (!canPlace) {
            previewColor = new Color(255, 0, 0, 128);
        }

        g2d.setColor(previewColor);

        int size = currentShip.getSize();
        Ship.Orientation orientation = currentShip.getOrientation();

        for (int i = 0; i < size; i++) {
            int shipX = x + (orientation == Ship.Orientation.HORIZONTAL ? i : 0);
            int shipY = y + (orientation == Ship.Orientation.VERTICAL ? i : 0);

            if (shipX < Board.SIZE && shipY < Board.SIZE) {
                int drawX = shipX * CELL_SIZE;
                int drawY = shipY * CELL_SIZE;
                g2d.fillRect(drawX + 1, drawY + 1, CELL_SIZE - 2, CELL_SIZE - 2);

                g2d.setColor(Color.BLACK);
                g2d.drawRect(drawX + 1, drawY + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                g2d.setColor(previewColor);
            }
        }
    }

    /**
     * Выбирает корабль указанного типа для размещения.
     *
     * @param type тип корабля
     */
    private void selectShip(ShipType type) {
        if (shipsToPlace.get(type) > 0) {
            currentShip = new Ship(type);
            statusLabel.setText("Разместите " + type.getDisplayName().toLowerCase());
        }
    }

    /**
     * Поворачивает текущий выбранный корабль.
     */
    private void rotateCurrentShip() {
        if (currentShip != null) {
            currentShip.rotate();
            boardPanel.repaint();
        }
    }

    /**
     * Пытается разместить текущий корабль на поле по заданным координатам.
     *
     * @param x координата X
     * @param y координата Y
     */
    private void placeShip(int x, int y) {
        if (currentShip == null) {
            return;
        }

        ShipType type = currentShip.getType();

        if (board.placeShip(currentShip, x, y)) {
            shipsToPlace.put(type, shipsToPlace.get(type) - 1);

            updateShipsPanel();
            updateStatus();
            boardPanel.repaint();

            currentShip = null;

            if (board.allShipsPlaced()) {
                readyButton.setEnabled(true);
                statusLabel.setText("Все корабли размещены! Нажмите 'Готово'");
            }
        }
        else {
            statusLabel.setText("Невозможно разместить корабль здесь!");
        }
    }

    /**
     * Автоматически размещает все корабли на поле случайным образом.
     */
    private void placeShipsRandomly() {
        clearBoard();

        java.util.List<Ship> ships = new java.util.ArrayList<>();
        for (ShipType type : ShipType.values()) {
            int count = shipsToPlace.get(type);
            for (int i = 0; i < count; i++) {
                ships.add(new Ship(type));
            }
        }

        int attempts = 0;
        int maxAttempts = 1000;

        for (Ship ship : ships) {
            boolean placed = false;

            while (!placed && attempts < maxAttempts) {
                int x = (int) (Math.random() * Board.SIZE);
                int y = (int) (Math.random() * Board.SIZE);

                if (Math.random() > 0.5) {
                    ship.setOrientation(Ship.Orientation.HORIZONTAL);
                } else {
                    ship.setOrientation(Ship.Orientation.VERTICAL);
                }

                if (board.canPlaceShip(ship, x, y)) {
                    board.placeShip(ship, x, y);
                    placed = true;
                }

                attempts++;
            }

            if (attempts >= maxAttempts) {
                JOptionPane.showMessageDialog(this,
                        "Не удалось разместить все корабли случайным образом. Попробуйте вручную.",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
                clearBoard();
                return;
            }
        }

        shipsToPlace.clear();
        updateShipsPanel();
        updateStatus();
        readyButton.setEnabled(true);
        boardPanel.repaint();
        statusLabel.setText("Корабли размещены случайным образом! Нажмите 'Готово'");
    }

    /**
     * Очищает игровое поле и сбрасывает состояние расстановки кораблей.
     */
    private void clearBoard() {
        java.util.List<Ship> ships = new java.util.ArrayList<>(board.getShips());
        for (Ship ship : ships) {
            board.removeShip(ship);
        }

        initializeShipsToPlace();

        currentShip = null;
        readyButton.setEnabled(false);
        updateShipsPanel();
        updateStatus();
        boardPanel.repaint();
    }

    /**
     * Обновляет текстовое сообщение о количестве оставшихся кораблей.
     */
    private void updateStatus() {
        int totalShips = shipsToPlace.values().stream().mapToInt(Integer::intValue).sum();
        statusLabel.setText("Осталось разместить кораблей: " + totalShips);
    }

    public void setListener(ShipPlacementListener listener) {
        this.listener = listener;
    }

    public boolean isReady() {
        return board.allShipsPlaced();
    }

    public String getShipsPlacement() {
        String placement = board.getShipsPlacementString();
        return placement;
    }
}