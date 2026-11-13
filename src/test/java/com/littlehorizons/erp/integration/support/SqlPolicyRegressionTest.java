package com.littlehorizons.erp.integration.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class SqlPolicyRegressionTest {
    @ParameterizedTest @ValueSource(strings = {
            "SELECT * FROM estudiantes2.estudiantes", "SELECT * FROM estudiantes2",
            "SELECT * FROM `estudiantes2`.`estudiantes`", "SELECT * FROM estudiantes_archive",
            "UPDATE estudiantes e, erpXlittleYhorizonsZtest.estudiantes x SET x.primer_nombre='X' WHERE 1=0",
            "UPDATE estudiantes e JOIN other.estudiantes x ON e.id_estudiante=x.id_estudiante SET x.primer_nombre='X'",
            "DELETE e FROM estudiantes e JOIN other.estudiantes x ON e.id_estudiante=x.id_estudiante",
            "SELECT * FROM estudiantes, other.estudiantes", "SELECT * FROM erp_little_horizons.estudiantes",
            "SELECT * FROM estudiantes UNION SELECT * FROM other.estudiantes",
            "SELECT * FROM estudiantes WHERE id_estudiante=?; DELETE FROM estudiantes",
            "UPDATE recursos SET estado_recurso='Disponible'", "SELECT /*comment*/ * FROM estudiantes",
            "USE erp_little_horizons_test", "SELECT * FROM estudiantes WHERE id_estudiante=1"})
    void rejectsForeignSchemasMultitableAndUnreviewedForms(String sql) {
        assertThrows(SQLException.class, () -> SqlPolicy.check(sql));
    }

    @Test void acceptsReviewedParameterTemplatesWithFormattingDifferences() {
        assertDoesNotThrow(() -> SqlPolicy.check(" SELECT *\nFROM estudiantes WHERE id_estudiante = ? "));
        assertDoesNotThrow(() -> SqlPolicy.check("UPDATE recursos SET estado_recurso=? WHERE id_recurso=?"));
        assertThrows(SQLException.class, () -> SqlPolicy.check(null));
    }

    @Test void neverErasesWhitespaceInsideLiterals() {
        assertNotEquals(SqlPolicy.key("SELECT 'a b'"), SqlPolicy.key("SELECT 'ab'"));
        assertNotEquals(SqlPolicy.key("SELECT * FROM estudiantes WHERE id_estudiante=?"),
                SqlPolicy.key("SELECT * FROM estudiantesWHERE id_estudiante=?"));
        assertEquals("", SqlPolicy.key("SELECT 'unterminated"));
    }
}
