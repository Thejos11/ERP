package com.littlehorizons.erp.util;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Singleton JDBC factory with explicit configuration and safe connection errors. */
public class DatabaseConnection {
    private static DatabaseConnection instance;
    private volatile DatabaseConfig config;
    private static volatile boolean inMemoryMode;

    private DatabaseConnection() { loadProperties(); }

    public static boolean isInMemoryMode() { return inMemoryMode; }
    public static void setInMemoryMode(boolean mode) {
        inMemoryMode = mode;
        if (mode) LoggerUtil.logInfo("System switched to In-Memory Standalone Mode (No SQL required).");
    }

    public synchronized void loadProperties() {
        config = null;
        try {
            config = DatabaseConfig.load(Path.of("db.properties"), getClass().getClassLoader(), System.getenv());
            LoggerUtil.logInfo("Database configuration loaded.");
        } catch (IllegalArgumentException e) {
            LoggerUtil.logWarning("Configuracion MySQL ausente, incompleta o ilegible. Complete db.properties o DB_URL, DB_USER y DB_PASSWORD.");
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) instance = new DatabaseConnection();
        return instance;
    }

    public Connection getConnection() throws SQLException {
        DatabaseConfig current = config;
        if (current == null) {
            throw new SQLException("Configuracion MySQL no disponible. Complete db.properties o las variables DB_*.", "08001");
        }
        try {
            return DriverManager.getConnection(current.url(), current.user(), current.password());
        } catch (SQLException e) {
            LoggerUtil.logError("No se pudo establecer la conexion MySQL.", e);
            // Never propagate driver text: it can echo credentials or URL parameters.
            throw new SQLException("No se pudo establecer la conexion MySQL. Revise la configuracion y el servidor.", "08001");
        }
    }

    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static void setCredentials(String url, String user, String password) {
        getInstance().updateCredentials(url, user, password);
    }

    public synchronized void updateCredentials(String url, String user, String password) {
        try {
            config = new DatabaseConfig(url, user, password);
            LoggerUtil.logInfo("Database credentials updated manually.");
        } catch (IllegalArgumentException e) {
            config = null;
            LoggerUtil.logWarning("Configuracion MySQL manual incompleta o invalida.");
        }
    }

    public static String getDbUrl() {
        DatabaseConfig current = getInstance().config;
        return current == null ? "" : current.url();
    }

    public static String getDbUser() {
        DatabaseConfig current = getInstance().config;
        return current == null ? "" : current.user();
    }
}
