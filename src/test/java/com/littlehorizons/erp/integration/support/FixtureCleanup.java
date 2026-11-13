package com.littlehorizons.erp.integration.support;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;

/** Retryable ownership ledger: remove an entry only after confirmed commit. */
public final class FixtureCleanup implements AutoCloseable {
    @FunctionalInterface public interface Connections { Connection open() throws SQLException; }
    private record Row(String table, String key, int id) {
        @Override public String toString() { return table + "." + key + "=" + id; }
    }
    private final Deque<Row> pending = new ArrayDeque<>();
    private final Connections connections;
    private final Consumer<String> log;

    public FixtureCleanup(Connections connections, Consumer<String> log) {
        this.connections = Objects.requireNonNull(connections); this.log = Objects.requireNonNull(log);
    }

    public void track(String table, String key, int id) {
        String expected = SqlPolicy.PRIMARY_KEYS.get(table);
        if (expected == null || !expected.equals(key) || id < 1) throw new IllegalArgumentException("Clave de limpieza no permitida.");
        Row row = new Row(table, key, id);
        if (!pending.contains(row)) pending.push(row);
    }

    public List<String> pendingRows() { return pending.stream().map(Row::toString).toList(); }

    @Override public void close() throws SQLException {
        for (Row row : List.copyOf(pending)) {
            try (Connection c = connections.open()) {
                TestDatabaseSafety.checkCatalog(c);
                if (!c.getAutoCommit()) throw new SQLException("La limpieza exige una conexión independiente sin transacción previa.");
                c.setAutoCommit(false);
                try {
                    String sql = "DELETE FROM " + row.table + " WHERE " + row.key + " = ?";
                    SqlPolicy.check(sql);
                    int affected;
                    try (PreparedStatement s = c.prepareStatement(sql)) { s.setInt(1, row.id); affected = s.executeUpdate(); }
                    if (affected == 0) {
                        String check = "SELECT COUNT(*) FROM " + row.table + " WHERE " + row.key + " = ?";
                        SqlPolicy.check(check);
                        try (PreparedStatement s = c.prepareStatement(check)) {
                            s.setInt(1, row.id);
                            try (ResultSet r = s.executeQuery()) {
                                if (!r.next() || r.getLong(1) != 0 || r.wasNull() || r.next()) throw new SQLException("Ausencia no comprobada.");
                            }
                        }
                    } else if (affected != 1) throw new SQLException("Cantidad de filas afectadas inesperada.");
                    TestDatabaseSafety.checkCatalog(c);
                    c.commit();
                    pending.remove(row);
                } catch (SQLException e) {
                    try { c.rollback(); } catch (SQLException ignored) { /* Keep the ledger; do not expose driver messages. */ }
                    throw e;
                }
            } catch (SQLException e) {
                log.accept((pending.contains(row) ? "Limpieza pendiente: " : "Eliminación confirmada, cierre de conexión fallido: ")
                        + row + ". Revise exclusivamente la base de pruebas.");
            }
        }
        if (!pending.isEmpty()) throw new SQLException("Registros ficticios pendientes de limpieza: " + String.join(", ", pendingRows()));
    }
}
