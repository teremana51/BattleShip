package BattleShip.client.utils;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.logging.*;

/**
 * Утилитарный класс для ведения логов клиента игры.
 * <p>
 * Использует асинхронную запись через ExecutorService, чтобы не блокировать основной поток.
 * Логи сохраняются в файл "clientlogs.log".
 */
public class LoggerClient {

    /** Логгер Java */
    private static final Logger logger = Logger.getLogger("ClientLogger");

    /** Однопоточный исполнитель для асинхронной записи логов */
    private static final ExecutorService logExecutor = Executors.newSingleThreadExecutor();

    static {
        try {
            Handler fileHandler = new FileHandler("clientlogs.log", true);
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
     * Асинхронно пишет информационное сообщение в лог.
     *
     * @param message сообщение для записи
     */
    public static void info(String message) {
        logExecutor.submit(() -> logger.info(message));
    }

    /**
     * Останавливает ExecutorService, используемый для логирования.
     * <p>
     * Необходимо вызывать при завершении работы клиента,
     * чтобы корректно закрыть все потоки и записать все сообщения.
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
