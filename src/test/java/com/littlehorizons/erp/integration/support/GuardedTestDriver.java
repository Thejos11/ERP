package com.littlehorizons.erp.integration.support;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/** Registered only inside integration forks; all DAO connections pass through this driver. */
public final class GuardedTestDriver implements Driver {
    private final IntegrationConfig config;
    private final Driver delegate;
    @FunctionalInterface public interface TriggerVerifier { void verify(IntegrationConfig runner) throws SQLException; }
    private final TriggerVerifier triggerVerifier;

    public GuardedTestDriver(IntegrationConfig config, Driver delegate) {
        this(config, delegate, runner -> { throw new SQLException("Falta una identidad verificadora de triggers."); });
    }

    public GuardedTestDriver(IntegrationConfig config, Driver delegate, TriggerVerifier triggerVerifier) {
        this.config = Objects.requireNonNull(config);
        this.delegate = Objects.requireNonNull(delegate);
        this.triggerVerifier = Objects.requireNonNull(triggerVerifier);
    }

    public static void install(IntegrationConfig config, IntegrationConfig provisioning) throws SQLException {
        TestDatabaseSafety.requireWriteApproval(System.getProperty("integration.write.enabled"));
        Driver delegate = new com.mysql.cj.jdbc.Driver();
        for (Driver driver : Collections.list(DriverManager.getDrivers())) DriverManager.deregisterDriver(driver);
        DriverManager.registerDriver(new GuardedTestDriver(config, delegate, new ProvisioningTriggerVerifier(provisioning, delegate)));
    }

    @Override public Connection connect(String url, Properties properties) throws SQLException {
        if (!acceptsURL(url)) return null;
        if (!config.url().equals(url) || !config.user().equals(properties.getProperty("user"))
                || !config.password().equals(properties.getProperty("password"))
                || !Set.of("user", "password").containsAll(properties.stringPropertyNames())) {
            throw new SQLException("Conexión fuera de la configuración de integración.");
        }
        Properties safe = new Properties();
        safe.setProperty("user", config.user()); safe.setProperty("password", config.password());
        safe.setProperty("connectTimeout", "5000"); safe.setProperty("socketTimeout", "10000");
        safe.setProperty("allowMultiQueries", "false"); safe.setProperty("allowLoadLocalInfile", "false");
        Connection connection = null;
        try {
            connection = delegate.connect(url, safe);
            TestDatabaseSafety.checkCatalog(connection);
            TestDatabaseSafety.verifyIdentityAndPermissions(connection, config, false);
            triggerVerifier.verify(config);
            return guard(connection);
        } catch (SQLException failure) {
            if (connection != null) { try { connection.close(); } catch (SQLException ignored) { /* Never expose driver text. */ } }
            throw new SQLException("Conexión de integración rechazada; revise catálogo, cuenta y permisos.");
        }
    }

    public static Connection guard(Connection raw) { return JdbcIsolation.guard(raw); }

    @Override public boolean acceptsURL(String url) { return url != null && url.startsWith("jdbc:mysql:"); }
    @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) { return new DriverPropertyInfo[0]; }
    @Override public int getMajorVersion() { return 1; }
    @Override public int getMinorVersion() { return 0; }
    @Override public boolean jdbcCompliant() { return false; }
    @Override public Logger getParentLogger() { return Logger.getLogger("erp.integration"); }
}
