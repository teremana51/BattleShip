package BattleShip.server.utils;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * Утилитарный класс для логирования событий сервера "Морской бой".
 * Все сообщения логируются асинхронно в файл serverlogs.log.
 */
public class LoggerServer {
    /** Логгер сервера */
    private static final Logger logger = Logger.getLogger("ServerLogger");

    /** Потоковый исполнитель для асинхронного логирования */
    private static final ExecutorService logExecutor = Executors.newSingleThreadExecutor();

    static {
        try {
            Handler fileHandler = new FileHandler("serverlogs.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.INFO);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Асинхронная запись информационного сообщения в лог.
     *
     * @param message сообщение для записи
     */
    public static void info(String message) {
        logExecutor.submit(() -> logger.info(message));
    }

    /**
     * Завершение работы логгера и освобождение ресурсов.
     * Ждет завершения всех задач в течение 5 секунд, затем принудительно завершает поток.
     */
    public static void shutdown() {
        logExecutor.shutdown();
        try {
            if (!logExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                logExecutor.shutdownNow();
            }
        }
        catch (InterruptedException e) {
            logExecutor.shutdownNow();
        }
    }
}
