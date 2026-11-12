package com.littlehorizons.erp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager for MySQL.
 * Loads configuration dynamically from 'db.properties' in the working directory
 * or from the classpath, avoiding hardcoded database credentials.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/erp_little_horizons?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private static boolean inMemoryMode = false;

    public static boolean isInMemoryMode() {
        return inMemoryMode;
    }

    public static void setInMemoryMode(boolean mode) {
        inMemoryMode = mode;
        if (mode) {
            LoggerUtil.logInfo("System switched to In-Memory Standalone Mode (No SQL required).");
        }
    }

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LoggerUtil.logError("MySQL JDBC Driver not found in classpath", e);
        }
        loadProperties();
    }

    /**
     * Loads database credentials from db.properties (file in working directory first, then classpath).
     */
    public synchronized void loadProperties() {
        Properties props = new Properties();
        boolean loaded = false;

        // 1. Check working directory for external db.properties
        File externalFile = new File("db.properties");
        if (externalFile.exists() && externalFile.isFile()) {
            try (FileInputStream fis = new FileInputStream(externalFile)) {
                props.load(fis);
                loaded = true;
                LoggerUtil.logInfo("Loaded database configuration from external file: " + externalFile.getAbsolutePath());
            } catch (IOException e) {
                LoggerUtil.logWarning("Could not read external db.properties: " + e.getMessage());
            }
        }

        // 2. Fallback to classpath db.properties
        if (!loaded) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
                if (is != null) {
                    props.load(is);
                    loaded = true;
                    LoggerUtil.logInfo("Loaded database configuration from classpath: db.properties");
                }
            } catch (IOException e) {
                LoggerUtil.logWarning("Could not read classpath db.properties: " + e.getMessage());
            }
        }

        // 3. Populate credentials with environment variable or default fallbacks
        this.dbUrl = props.getProperty("db.url", 
                System.getenv("DB_URL") != null ? System.getenv("DB_URL") : DEFAULT_URL);
        this.dbUser = props.getProperty("db.user", 
                System.getenv("DB_USER") != null ? System.getenv("DB_USER") : DEFAULT_USER);
        this.dbPassword = props.getProperty("db.password", 
                System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : DEFAULT_PASSWORD);
    }

    /**
     * Retrieves the singleton instance of DatabaseConnection.
     *
     * @return The singleton DatabaseConnection instance.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Creates and returns a new active Connection using the loaded credentials.
     *
     * @return A live JDBC Connection.
     * @throws SQLException If a connection error occurs.
     */
    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            LoggerUtil.logError("Failed to obtain database connection: " + dbUrl + " [user=" + dbUser + "]", e);
            throw e;
        }
    }

    /**
     * Tests the current database connection.
     *
     * @return true if connection succeeds, false otherwise.
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Static helper to allow dynamic runtime update of database credentials.
     *
     * @param url Database JDBC URL.
     * @param user Database user.
     * @param password Database password.
     */
    public static void setCredentials(String url, String user, String password) {
        getInstance().updateCredentials(url, user, password);
    }

    public synchronized void updateCredentials(String url, String user, String password) {
        this.dbUrl = url;
        this.dbUser = user;
        this.dbPassword = password;
        LoggerUtil.logInfo("Database credentials updated manually: " + url + " | user=" + user);
    }

    public static String getDbUrl() {
        return getInstance().dbUrl;
    }

    public static String getDbUser() {
        return getInstance().dbUser;
    }
}
