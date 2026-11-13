package com.littlehorizons.erp.util;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseSecurityTest {
    @Test void redactsUrlsAndSensitiveAssignments() {
        String safe = LoggerUtil.sanitize("jdbc:mysql://host/db?password=url-secret&token=url-token "
                + "db.password=plain-secret token='token with spaces' api_key=key-secret\r\nforged");
        for (String secret : new String[]{"url-secret", "url-token", "plain-secret", "token with spaces", "key-secret"}) {
            assertFalse(safe.contains(secret));
        }
        assertFalse(safe.contains("\n"));
        assertTrue(safe.contains("[REDACTED]"));
    }

    @Test void allLogLevelsAndNestedExceptionsProtectSecrets() throws Exception {
        Path log = Path.of("error_log.txt");
        long offset = Files.exists(log) ? Files.size(log) : 0;
        SQLException error = new SQLException("unlabelled-driver-secret");
        error.initCause(new RuntimeException("cause-secret"));
        error.addSuppressed(new RuntimeException("suppressed-secret"));
        error.setNextException(new SQLException("next-secret"));
        LoggerUtil.logInfo("password=info-secret");
        LoggerUtil.logWarning("token=warning-secret");
        LoggerUtil.logError("jdbc:mysql://host/db?custom=custom-secret", error);
        byte[] bytes = Files.readAllBytes(log);
        String appended = new String(java.util.Arrays.copyOfRange(bytes, (int) offset, bytes.length),
                java.nio.charset.StandardCharsets.UTF_8);
        for (String secret : new String[]{"info-secret", "warning-secret", "custom-secret",
                "unlabelled-driver-secret", "cause-secret", "suppressed-secret", "next-secret"}) {
            assertFalse(appended.contains(secret));
        }
        assertTrue(appended.contains("java.sql.SQLException"));
        assertTrue(appended.contains("DatabaseSecurityTest"));
    }

    @Test void driverFailuresAreSafeForCallersAndDoNotNeedMysql() throws Exception {
        Driver driver = new Driver() {
            public boolean acceptsURL(String url) { return url.startsWith("jdbc:mysql://security-test.invalid/"); }
            public Connection connect(String url, Properties props) throws SQLException {
                if (!acceptsURL(url)) return null;
                throw new SQLException("driver-secret " + url + " " + props.getProperty("password"));
            }
            public DriverPropertyInfo[] getPropertyInfo(String url, Properties props) { return new DriverPropertyInfo[0]; }
            public int getMajorVersion() { return 1; }
            public int getMinorVersion() { return 0; }
            public boolean jdbcCompliant() { return false; }
            public Logger getParentLogger() { return Logger.getGlobal(); }
        };
        // Replace registered drivers temporarily so the real MySQL driver never attempts a network connection.
        var originalDrivers = java.util.Collections.list(DriverManager.getDrivers());
        DatabaseConnection db = DatabaseConnection.getInstance();
        try {
            for (Driver original : originalDrivers) DriverManager.deregisterDriver(original);
            DriverManager.registerDriver(driver);
            db.updateCredentials("jdbc:mysql://security-test.invalid/db?token=url-secret", "erp_test", "password-secret");
            SQLException error = assertThrows(SQLException.class, db::getConnection);
            assertFalse(error.getMessage().contains("secret"));
            assertNull(error.getCause());
            assertFalse(db.testConnection());
        } finally {
            DriverManager.deregisterDriver(driver);
            for (Driver original : originalDrivers) DriverManager.registerDriver(original);
            db.loadProperties();
        }
    }

    @Test void invalidRuntimeConfigurationCannotReusePreviousCredentials() {
        DatabaseConnection db = DatabaseConnection.getInstance();
        try {
            db.updateCredentials("jdbc:mysql://localhost/test", "erp_test", "explicit");
            db.updateCredentials(null, null, null);
            assertEquals("", DatabaseConnection.getDbUser());
            assertThrows(SQLException.class, db::getConnection);
            assertFalse(db.testConnection());
        } finally {
            db.loadProperties();
        }
    }
}
