package com.littlehorizons.erp.integration.support;

import com.littlehorizons.erp.util.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Fictitious, per-test records. Cleanup targets only generated primary keys. */
public final class TestFixtures implements AutoCloseable {
    private final FixtureCleanup cleanup = new FixtureCleanup(
            () -> DatabaseConnection.getInstance().getConnection(), com.littlehorizons.erp.util.LoggerUtil::logWarning);
    public final String token = "IT" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    public int teacher, guardian, subject, student, club, event, resource;
    public int grade, category, concept;
    public final LocalDate date = LocalDate.now();

    public void create() throws SQLException {
        grade = lookup("SELECT id_grado FROM grados WHERE nivel_numerico=0");
        category = lookup("SELECT id_categoria FROM categorias_recurso WHERE nombre_categoria='Libro'");
        concept = lookup("SELECT id_concepto FROM conceptos_pago WHERE codigo_concepto='PENSION'");
        teacher = insert("profesores", "id_profesor", "numero_documento,primer_nombre,primer_apellido,correo_electronico,fecha_contratacion",
                token + "T", "Docente", "Ficticio", token + "@example.invalid", date);
        guardian = insert("acudientes", "id_acudiente", "numero_documento,primer_nombre,primer_apellido,parentesco,telefono_principal",
                token + "G", "Acudiente", "Ficticio", "Tutor Legal", "0000000000");
        subject = insert("materias", "id_materia", "codigo_materia,nombre_materia,id_grado", token, "Materia ficticia", grade);
        student = insert("estudiantes", "id_estudiante", "numero_documento,primer_nombre,primer_apellido,fecha_nacimiento,genero,fecha_ingreso",
                token + "S", "Estudiante", "Ficticio", LocalDate.of(2015, 1, 1), "Otro", date);
        if (!new com.littlehorizons.erp.dao.StudentDAO().linkGuardianToStudent(student, guardian, false, false)) {
            throw new SQLException("No se pudo asociar el acudiente ficticio.");
        }
        club = insert("clubes", "id_club", "nombre_club,id_profesor_supervisor,capacidad_maxima", token, teacher, 2);
        event = insert("eventos", "id_evento", "nombre_evento,tipo_evento,fecha_evento,id_organizador", token, "Cultural", date, teacher);
        resource = insert("recursos", "id_recurso", "codigo_recurso,nombre_recurso,id_categoria,estado_recurso", token, "Recurso ficticio", category, "Disponible");
    }

    private int lookup(String sql) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            if (!r.next()) throw new SQLException("Faltan los datos iniciales oficiales.");
            return r.getInt(1);
        }
    }

    public int insert(String table, String key, String columns, Object... values) throws SQLException {
        if (!TestDatabaseSafety.TABLES.contains(table) || !columns.matches("[a-z_,]+") || !key.matches("[a-z_]+")) {
            throw new IllegalArgumentException("Invalid fixture identifiers");
        }
        String markers = String.join(",", Collections.nCopies(values.length, "?"));
        try (Connection c = DatabaseConnection.getInstance().getConnection(); PreparedStatement s = c.prepareStatement(
                "INSERT INTO " + table + " (" + columns + ") VALUES (" + markers + ")", Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < values.length; i++) s.setObject(i + 1, values[i]);
            s.executeUpdate();
            try (ResultSet r = s.getGeneratedKeys()) {
                if (!r.next()) throw new SQLException("Missing fixture generated key");
                int id = r.getInt(1); track(table, key, id); return id;
            }
        }
    }

    public void track(String table, String key, int id) {
        cleanup.track(table, key, id);
    }

    @Override public void close() throws SQLException {
        cleanup.close();
    }

    public List<String> pendingCleanup() { return cleanup.pendingRows(); }
}
