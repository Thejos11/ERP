package com.littlehorizons.erp.integration.support;

import com.littlehorizons.erp.util.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
public abstract class DaoIntegrationSupport {
    protected TestFixtures fixtures;

    @BeforeAll protected static void enableGuardedConnections() throws Exception {
        TestDatabaseSafety.requireWriteApproval(System.getProperty("integration.write.enabled"));
        String path = System.getProperty("integration.config");
        if (path == null || !Path.of(path).isAbsolute()) throw new IllegalStateException("Falta la ruta absoluta de configuración de pruebas.");
        IntegrationConfig config = IntegrationConfig.load(Path.of(path));
        String verifierPath = System.getProperty("integration.verifier.config");
        if (verifierPath == null || !Path.of(verifierPath).isAbsolute()) throw new IllegalStateException("Falta configuración privada de visibilidad.");
        IntegrationConfig verifier = IntegrationConfig.load(Path.of(verifierPath));
        GuardedTestDriver.install(config, verifier);
        DatabaseConnection.setCredentials(config.url(), config.user(), config.password());
        try (Connection connection = connection()) {
            var tables = new HashSet<String>();
            try (ResultSet rows = connection.getMetaData().getTables(IntegrationConfig.DATABASE, null, "%", new String[]{"TABLE"})) {
                while (rows.next()) tables.add(rows.getString("TABLE_NAME"));
            }
            assertEquals(TestDatabaseSafety.TABLES, tables, "Initialize the isolated test schema first");
        }
    }

    @BeforeEach protected void createRelatedFixtures() throws Exception {
        fixtures = new TestFixtures();
        fixtures.create();
    }

    @AfterEach protected void cleanOwnedFixtures() throws Exception {
        if (fixtures != null) fixtures.close();
    }

    protected static Connection connection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    protected long count(String sql, Object... values) throws SQLException {
        try (Connection connection = connection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
            try (ResultSet rows = statement.executeQuery()) { assertTrue(rows.next()); return rows.getLong(1); }
        }
    }
}
