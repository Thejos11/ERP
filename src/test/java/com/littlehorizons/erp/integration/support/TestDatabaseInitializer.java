package com.littlehorizons.erp.integration.support;

import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

/** Explicit future provisioning entry point. Never invoked by Maven test lifecycle. */
public final class TestDatabaseInitializer {
    private TestDatabaseInitializer() { }

    public static void main(String[] args) {
        try {
            if (args.length != 4 || !"--apply-test-only".equals(args[0])) {
                throw new IllegalArgumentException("Se requiere autorización explícita de inicialización.");
            }
            IntegrationConfig config = IntegrationConfig.load(Path.of(args[1]));
            String script = Files.readString(Path.of(args[2]));
            InitializationSql.validate(script, Files.readString(Path.of(args[3])));
            Properties properties = new Properties();
            properties.setProperty("user", config.user()); properties.setProperty("password", config.password());
            properties.setProperty("connectTimeout", "5000"); properties.setProperty("socketTimeout", "10000");
            properties.setProperty("allowMultiQueries", "false"); properties.setProperty("allowLoadLocalInfile", "false");
            Driver driver = new com.mysql.cj.jdbc.Driver();
            String serverUrl = config.url().substring(0, config.url().lastIndexOf('/') + 1);
            try (Connection connection = driver.connect(serverUrl, properties)) {
                TestDatabaseSafety.verifyTriggerAbsence(connection, config);
                try (PreparedStatement check = connection.prepareStatement(
                        "SELECT COUNT(*) FROM information_schema.SCHEMATA WHERE SCHEMA_NAME = ?")) {
                    check.setString(1, IntegrationConfig.DATABASE);
                    try (ResultSet result = check.executeQuery()) {
                        result.next();
                        if (result.getInt(1) != 0) throw new IllegalStateException("La base de pruebas ya existe; no se modificará.");
                    }
                }
                try (Statement statement = connection.createStatement()) {
                    statement.execute("CREATE DATABASE `erp_little_horizons_test` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
                    connection.setCatalog(IntegrationConfig.DATABASE);
                    TestDatabaseSafety.checkCatalog(connection);
                    // The exact generated script is validated above, before the first write.
                    for (String sql : script.replaceAll("(?m)^--[^\\r\\n]*", "").split(";")) {
                        if (!sql.isBlank()) statement.execute(sql.trim());
                    }
                }
                TestDatabaseSafety.verifyTriggerAbsence(connection, config);
            }
            System.out.println("Base de pruebas inicializada. La base principal no se utilizó.");
        } catch (Exception failure) {
            System.err.println("Inicialización rechazada o fallida. No se reintentará ni borrará ninguna base. Revise configuración, permisos y estado parcial.");
            System.exit(1);
        }
    }
}
