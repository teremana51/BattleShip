package BattleShip.client.controller;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

/**
 * Контроллер сетевого взаимодействия клиента с сервером.
 * <p>
 * Отвечает за:
 * <ul>
 *     <li>подключение к серверу</li>
 *     <li>отправку сообщений</li>
 *     <li>приём сообщений в отдельном потоке</li>
 * </ul>
 * Все входящие сообщения передаются в обработчик {@link Consumer}.
 */
public class NetworkController {
    private Socket socket;

    private PrintWriter out;
    private BufferedReader in;

    /** Обработчик входящих сообщений от сервера */
    private Consumer<String> messageHandler;

    /** Флаг состояния подключения */
    private boolean connected = false;

    /**
     * Устанавливает соединение с сервером и запускает поток приёма сообщений.
     *
     * @param serverAddress адрес сервера
     * @param port порт сервера
     * @param messageHandler обработчик входящих сообщений
     * @throws IOException если не удалось подключиться к серверу
     */
    public void connect(String serverAddress, int port, Consumer<String> messageHandler) throws IOException {
        this.messageHandler = messageHandler;
        socket = new Socket(serverAddress, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        connected = true;

        new Thread(this::receiveMessages).start();
    }

    /**
     * Основной цикл приёма сообщений от сервера.
     * Работает в отдельном потоке до разрыва соединения.
     */
    private void receiveMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                if (messageHandler != null) {
                    messageHandler.accept(message);
                }
            }
        }
        catch (IOException e) {
            if (connected) {
                connected = false;
            }
        }
    }

    /**
     * Отправляет сообщение серверу.
     *
     * @param message сообщение для отправки
     */
    public void sendMessage(String message) {
        if (out != null && connected) {
            out.println(message);
        }
    }

    /**
     * Корректно закрывает соединение с сервером и освобождает ресурсы.
     */
    public void disconnect() {
        connected = false;
        try {
            if (socket != null) {
                socket.close();
            }

            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}