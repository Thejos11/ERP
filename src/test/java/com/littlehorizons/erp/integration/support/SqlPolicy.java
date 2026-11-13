package com.littlehorizons.erp.integration.support;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;

/** Exact reviewed templates, not a prefix-based SQL parser. Bound values remain parameters. */
public final class SqlPolicy {
    public static final Map<String, String> PRIMARY_KEYS = Map.ofEntries(
            Map.entry("grados", "id_grado"), Map.entry("estudiantes", "id_estudiante"),
            Map.entry("acudientes", "id_acudiente"), Map.entry("matriculas", "id_matricula"),
            Map.entry("materias", "id_materia"), Map.entry("profesores", "id_profesor"),
            Map.entry("asignaciones_profesor", "id_asignacion"), Map.entry("inscripciones_materia", "id_inscripcion"),
            Map.entry("registros_academicos", "id_registro"), Map.entry("asistencias", "id_asistencia"),
            Map.entry("clubes", "id_club"), Map.entry("membresias_club", "id_membresia"),
            Map.entry("eventos", "id_evento"), Map.entry("participantes_evento", "id_participacion"),
            Map.entry("conceptos_pago", "id_concepto"), Map.entry("pagos", "id_pago"),
            Map.entry("facturas", "id_factura"), Map.entry("categorias_recurso", "id_categoria"),
            Map.entry("recursos", "id_recurso"), Map.entry("prestamos_estudiante", "id_prestamo"),
            Map.entry("prestamos_profesor", "id_prestamo"));
    private static final Set<String> ALLOWED = load();
    private SqlPolicy() { }

    private static Set<String> load() {
        Set<String> allowed = new HashSet<>();
        try (InputStream input = SqlPolicy.class.getResourceAsStream("/integration/allowed-sql.txt")) {
            if (input == null) throw new IOException("Missing reviewed SQL policy");
            for (String line : new String(input.readAllBytes(), StandardCharsets.UTF_8).split("\\R")) {
                if (!line.isBlank()) allowed.add(key(line));
            }
            PRIMARY_KEYS.forEach((table, column) -> {
                allowed.add(key("DELETE FROM " + table + " WHERE " + column + " = ?"));
                allowed.add(key("SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?"));
            });
        } catch (IOException e) { throw new ExceptionInInitializerError("No se pudo cargar la política SQL revisada."); }
        return Set.copyOf(allowed);
    }

    static String key(String sql) {
        if (sql == null) return "";
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < sql.length();) {
            char ch = sql.charAt(i);
            if (Character.isWhitespace(ch)) { i++; continue; }
            int start = i++;
            if (ch == '\'' || ch == '"' || ch == '`') {
                boolean closed = false;
                while (i < sql.length()) {
                    char current = sql.charAt(i++);
                    if (current == '\\') { if (i >= sql.length()) return ""; i++; }
                    else if (current == ch) {
                        if (i < sql.length() && sql.charAt(i) == ch) i++;
                        else { closed = true; break; }
                    }
                }
                if (!closed) return "";
            } else if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '$') {
                while (i < sql.length() && (Character.isLetterOrDigit(sql.charAt(i))
                        || sql.charAt(i) == '_' || sql.charAt(i) == '$')) i++;
            }
            String token = sql.substring(start, i);
            key.append(token.length()).append(':').append(token).append('|');
        }
        return key.toString();
    }

    public static void check(String sql) throws SQLException {
        if (!ALLOWED.contains(key(sql))) throw new SQLException("SQL no incluido en las plantillas revisadas de integración.");
    }
}
