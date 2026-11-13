package com.littlehorizons.erp.integration.support;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

class JdbcIsolationRegressionTest {
    private final AtomicReference<String> catalog = new AtomicReference<>(IntegrationConfig.DATABASE);
    private final AtomicInteger mutations = new AtomicInteger();
    private Connection raw;
    private PreparedStatement statement;
    private ResultSet rows;
    private DatabaseMetaData metadata;
    private java.sql.Array array;

    private Connection graph() {
        array = (java.sql.Array) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{java.sql.Array.class}, (p, m, a) -> switch(m.getName()) {
            case "getResultSet" -> rows;
            case "getArray" -> new Object[]{rows};
            default -> JdbcDoubles.defaultValue(m.getReturnType());
        });
        rows = (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ResultSet.class}, (p, m, a) -> switch(m.getName()) {
            case "getStatement" -> statement;
            case "getArray", "getObject" -> array;
            case "updateRow", "deleteRow", "insertRow", "updateString" -> { mutations.incrementAndGet(); yield null; }
            default -> JdbcDoubles.defaultValue(m.getReturnType());
        });
        statement = (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class}, (p, m, a) -> switch(m.getName()) {
            case "getConnection" -> raw;
            case "executeQuery", "getResultSet", "getGeneratedKeys" -> rows;
            case "executeUpdate" -> mutations.incrementAndGet();
            default -> JdbcDoubles.defaultValue(m.getReturnType());
        });
        metadata = (DatabaseMetaData) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{DatabaseMetaData.class}, (p, m, a) -> switch(m.getName()) {
            case "getConnection" -> raw;
            case "getTables", "getColumns", "getImportedKeys", "getIndexInfo" -> rows;
            default -> JdbcDoubles.defaultValue(m.getReturnType());
        });
        raw = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (p, m, a) -> switch(m.getName()) {
            case "getCatalog" -> catalog.get();
            case "getMetaData" -> metadata;
            case "prepareStatement", "createStatement" -> statement;
            default -> JdbcDoubles.defaultValue(m.getReturnType());
        });
        return GuardedTestDriver.guard(raw);
    }

    @Test void metadataResultSetChainNeverReturnsRawConnection() throws Exception {
        Connection guarded = graph();
        DatabaseMetaData guardedMetadata = guarded.getMetaData();
        ResultSet guardedRows = guardedMetadata.getTables(IntegrationConfig.DATABASE, null, "%", null);
        assertNotSame(raw, guardedMetadata.getConnection()); assertSame(guarded, guardedMetadata.getConnection());
        assertNotSame(rows, guardedRows); assertNotSame(statement, guardedRows.getStatement());
        assertSame(guarded, guardedRows.getStatement().getConnection());
        assertSame(guardedRows, guardedMetadata.getColumns(IntegrationConfig.DATABASE, null, "%", "%"));
        assertSame(guarded, guardedMetadata.getImportedKeys(IntegrationConfig.DATABASE, null, "estudiantes").getStatement().getConnection());
        assertSame(guarded, guardedMetadata.getIndexInfo(IntegrationConfig.DATABASE, null, "estudiantes", false, false).getStatement().getConnection());
        assertThrows(SQLException.class, () -> guardedMetadata.unwrap(DatabaseMetaData.class));
        assertThrows(SQLException.class, () -> guardedRows.unwrap(ResultSet.class));
        assertThrows(SQLException.class, () -> guardedRows.getStatement().unwrap(Statement.class));
    }

    @Test void queriesGeneratedKeysAndNestedArraysStayInsideSameGraph() throws Exception {
        Connection guarded = graph();
        PreparedStatement prepared = guarded.prepareStatement("SELECT * FROM estudiantes WHERE id_estudiante=?");
        ResultSet result = prepared.executeQuery();
        assertSame(guarded, result.getStatement().getConnection());
        assertSame(result, prepared.getGeneratedKeys()); assertSame(result, prepared.getResultSet());
        java.sql.Array guardedArray = result.getArray(1);
        assertNotSame(array, guardedArray);
        assertSame(guarded, guardedArray.getResultSet().getStatement().getConnection());
        assertSame(result, ((Object[]) guardedArray.getArray())[0]);
        assertSame(guardedArray, result.getObject(1));
    }

    @Test void catalogIsRecheckedThroughEveryMetadataRoute() throws Exception {
        Connection guarded = graph();
        ResultSet result = guarded.getMetaData().getTables(IntegrationConfig.DATABASE, null, "%", null);
        Statement guardedStatement = result.getStatement();
        catalog.set("erp_little_horizons");
        assertThrows(SQLException.class, result::getStatement);
        assertThrows(SQLException.class, guardedStatement::getConnection);
        assertThrows(SQLException.class, () -> guardedStatement.executeUpdate("UPDATE recursos SET estado_recurso=? WHERE id_recurso=?"));
        assertEquals(0, mutations.get());
    }

    @Test void resultSetMutationsAndUpdatableCursorsCannotBypassPolicy() throws Exception {
        Connection guarded = graph();
        ResultSet result = guarded.getMetaData().getTables(IntegrationConfig.DATABASE, null, "%", null);
        assertThrows(SQLException.class, result::deleteRow); assertThrows(SQLException.class, result::updateRow);
        assertThrows(SQLException.class, result::insertRow); assertThrows(SQLException.class, () -> result.updateString(1, "fiction"));
        assertThrows(SQLException.class, () -> guarded.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE));
        assertThrows(SQLException.class, () -> guarded.prepareStatement("SELECT * FROM estudiantes WHERE id_estudiante=?",
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE));
        assertEquals(0, mutations.get());
    }
}
