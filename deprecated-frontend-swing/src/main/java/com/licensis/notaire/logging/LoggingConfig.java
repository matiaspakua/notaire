package com.licensis.notaire.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuración centralizada de logging para el frontend Swing.
 * 
 * Configura todos los loggers de la aplicación para emitir logs JSON
 * estructurados a stdout (INFO y menores) y stderr (WARNING y superiores).
 * 
 * <p>
 * Debe invocarse una sola vez al inicio de la aplicación, antes de
 * cualquier otro log, típicamente en el {@code main()} de {@code Login}.
 * </p>
 * 
 * <h3>Ejemplo de uso:</h3>
 * 
 * <pre>
 * public static void main(String[] args) {
 *     LoggingConfig.configurar();
 *     // ... resto de la aplicación
 * }
 * </pre>
 *
 * @author matias
 */
public final class LoggingConfig {

    private LoggingConfig() {
        // Utility class, no instances
    }

    /**
     * Configura el logging global de la aplicación.
     * <ul>
     * <li>Remueve los handlers por defecto del root logger.</li>
     * <li>Añade un handler para stdout (niveles ALL hasta INFO inclusive).</li>
     * <li>Añade un handler para stderr (niveles WARNING y SEVERE).</li>
     * <li>Ambos handlers usan {@link JsonLogFormatter} para formato JSON.</li>
     * </ul>
     */
    public static void configurar() {
        Logger rootLogger = Logger.getLogger("");

        // Remover handlers previos para evitar duplicación
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }

        JsonLogFormatter jsonFormatter = new JsonLogFormatter();

        // Handler para stdout: INFO y niveles inferiores (FINE, FINER, FINEST, CONFIG)
        ConsoleHandler stdoutHandler = new ConsoleHandler() {
            {
                // ConsoleHandler por defecto usa System.err, redirigir a System.out
                setOutputStream(System.out);
            }
        };
        stdoutHandler.setFormatter(jsonFormatter);
        stdoutHandler.setLevel(Level.ALL);
        stdoutHandler.setFilter(record -> record.getLevel().intValue() < Level.WARNING.intValue());

        // Handler para stderr: WARNING y SEVERE
        ConsoleHandler stderrHandler = new ConsoleHandler();
        stderrHandler.setFormatter(jsonFormatter);
        stderrHandler.setLevel(Level.WARNING);

        rootLogger.addHandler(stdoutHandler);
        rootLogger.addHandler(stderrHandler);
        rootLogger.setLevel(Level.INFO);

        // Configurar nivel para el paquete de la aplicación
        Logger appLogger = Logger.getLogger("com.licensis.notaire");
        appLogger.setLevel(Level.ALL);

        // Silenciar loggers muy verbosos
        Logger.getLogger("javax.swing").setLevel(Level.WARNING);
        Logger.getLogger("java.awt").setLevel(Level.WARNING);
        Logger.getLogger("sun").setLevel(Level.WARNING);

        // Log de confirmación de configuración
        Logger log = Logger.getLogger(LoggingConfig.class.getName());
        log.info("Logging JSON estructurado configurado correctamente - stdout (INFO-), stderr (WARNING+)");
    }
}
