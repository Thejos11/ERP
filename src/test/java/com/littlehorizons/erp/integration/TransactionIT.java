package com.littlehorizons.erp.integration;

import com.littlehorizons.erp.integration.support.DaoIntegrationSupport;
import org.junit.jupiter.api.Test;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class TransactionIT extends DaoIntegrationSupport {
    @Test void rollsBackInsertAfterForeignKeyFailure() throws Exception {
        String code = fixtures.token + "TX";
        try (Connection c = connection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement s = c.prepareStatement("INSERT INTO recursos (codigo_recurso,nombre_recurso,id_categoria) VALUES (?,?,?)")) {
                    s.setString(1, code); s.setString(2, "Transactional fixture"); s.setInt(3, fixtures.category);
                    assertEquals(1, s.executeUpdate());
                }
                try (PreparedStatement s = c.prepareStatement("INSERT INTO recursos (codigo_recurso,nombre_recurso,id_categoria) VALUES (?,?,?)")) {
                    s.setString(1, code + "X"); s.setString(2, "Invalid FK"); s.setInt(3, -1);
                    assertThrows(SQLException.class, s::executeUpdate);
                }
            } finally { c.rollback(); }
        }
        assertEquals(0, count("SELECT COUNT(*) FROM recursos WHERE codigo_recurso=?", code));
    }

    @Test void commitsInsertVisibleFromAnotherConnection() throws Exception {
        String code = fixtures.token + "TX";
        try (Connection c = connection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement s = c.prepareStatement("INSERT INTO recursos (codigo_recurso,nombre_recurso,id_categoria) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    s.setString(1, code); s.setString(2, "Committed fixture"); s.setInt(3, fixtures.category);
                    assertEquals(1, s.executeUpdate());
                    try (ResultSet keys = s.getGeneratedKeys()) { assertTrue(keys.next()); fixtures.track("recursos", "id_recurso", keys.getInt(1)); }
                }
                c.commit();
            } finally { c.rollback(); }
        }
        assertEquals(1, count("SELECT COUNT(*) FROM recursos WHERE codigo_recurso=?", code));
    }
}
