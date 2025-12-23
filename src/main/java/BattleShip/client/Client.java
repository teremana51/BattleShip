package BattleShip.client;

import BattleShip.client.view.MainScreen;

import javax.swing.*;

/**
 * Главный класс клиента игры "Морской бой".
 * <p>
 * Запускает графический интерфейс пользователя и инициализирует основной экран.
 */
public class Client {
    /**
     * Главный метод приложения.
     * <p>
     * Использует SwingUtilities.invokeLater для безопасного запуска GUI в Event Dispatch Thread (EDT).
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainScreen mainScreen = new MainScreen();
            mainScreen.init();
        });
    }
}