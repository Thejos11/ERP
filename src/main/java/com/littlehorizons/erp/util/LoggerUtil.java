package com.littlehorizons.erp.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Thread-safe utility class for system logging.
 * Logs informational messages, warnings, and errors with stack traces to 'error_log.txt'.
 */
public class LoggerUtil {

    private static final String LOG_FILE_PATH = "error_log.txt";
    private static final Pattern JDBC_URL = Pattern.compile("(?i)jdbc:[^\\s]+");
    private static final Pattern SECRET = Pattern.compile(
            "(?i)([\\w.-]*(?:password|passwd|pwd|token|secret|credential|api[_-]?key)[\\w.-]*\\s*[=:]\\s*)(?:\"[^\"]*\"|'[^']*'|[^\\s,;&]+)");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LoggerUtil() {
        // Prevent instantiation
    }

    /**
     * Logs an informational message.
     *
     * @param message Informational text.
     */
    public static synchronized void logInfo(String message) {
        writeLog("INFO", message, null);
    }

    /**
     * Logs a warning message.
     *
     * @param message Warning text.
     */
    public static synchronized void logWarning(String message) {
        writeLog("WARNING", message, null);
    }

    /**
     * Logs an error message and its associated exception stack trace.
     *
     * @param message Error description.
     * @param throwable Exception thrown (can be null).
     */
    public static synchronized void logError(String message, Throwable throwable) {
        writeLog("ERROR", message, throwable);
    }

    private static void writeLog(String level, String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        try (FileWriter fw = new FileWriter(LOG_FILE_PATH, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.printf("[%s] [%s] %s%n", timestamp, level, sanitize(message));
            if (throwable != null) {
                // Driver messages, causes and suppressed exceptions can contain unlabelled secrets.
                pw.println(throwable.getClass().getName());
                for (StackTraceElement frame : throwable.getStackTrace()) {
                    pw.println("\tat " + frame);
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo escribir en el archivo de logs.");
        }
    }

    static String sanitize(String message) {
        if (message == null) return "";
        String safe = JDBC_URL.matcher(message).replaceAll("[JDBC REDACTED]");
        return SECRET.matcher(safe).replaceAll("$1[REDACTED]")
                .replace('\r', ' ').replace('\n', ' ');
    }
}
