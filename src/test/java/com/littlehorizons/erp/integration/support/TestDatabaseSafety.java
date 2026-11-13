package com.littlehorizons.erp.integration.support;

import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

/** Fail-closed server-aware checks. No server state is assumed or cached. */
public final class TestDatabaseSafety {
    public static final Set<String> TABLES = Set.of("grados", "estudiantes", "acudientes",
            "estudiante_acudiente", "matriculas", "materias", "profesores", "asignaciones_profesor",
            "inscripciones_materia", "registros_academicos", "asistencias", "clubes", "membresias_club",
            "eventos", "participantes_evento", "conceptos_pago", "pagos", "facturas",
            "categorias_recurso", "recursos", "prestamos_estudiante", "prestamos_profesor");
    private static final Pattern GRANT = Pattern.compile(
            "^GRANT ([A-Z, ]+) ON (`[^`]+`|[a-zA-Z0-9_\\\\]+)\\.\\* TO .+$", Pattern.CASE_INSENSITIVE);
    private TestDatabaseSafety() { }

    public static void requireWriteApproval(String flag) {
        if (!"true".equals(flag)) throw new IllegalStateException("La integración requiere autorización explícita de escritura.");
    }

    public static void checkCatalog(Connection connection) throws SQLException {
        if (connection == null || !IntegrationConfig.DATABASE.equals(connection.getCatalog())) {
            throw new SQLException("Catálogo JDBC no permitido.");
        }
    }

    /** OFF: only escaped underscores denote literals. ON: accept the exact unescaped name only. */
    static boolean exactScope(String scope, Boolean partialRevokes) {
        if (partialRevokes == null) return false;
        if (scope.startsWith("`") && scope.endsWith("`")) scope = scope.substring(1, scope.length() - 1);
        if (partialRevokes) return IntegrationConfig.DATABASE.equals(scope);
        StringBuilder literal = new StringBuilder();
        for (int i = 0; i < scope.length(); i++) {
            char ch = scope.charAt(i);
            if (ch == '\\') {
                if (++i >= scope.length() || scope.charAt(i) != '_') return false;
                literal.append('_');
            } else {
                if (ch == '_' || ch == '%') return false;
                literal.append(ch);
            }
        }
        return IntegrationConfig.DATABASE.contentEquals(literal);
    }

    public static void validateGrants(List<String> grants, boolean provisioner, Boolean partialRevokes) throws SQLException {
        if (partialRevokes == null) throw new SQLException("Semántica de permisos no comprobada.");
        Set<String> allowed = provisioner ? Set.of("CREATE", "SELECT", "INSERT", "TRIGGER")
                : Set.of("SELECT", "INSERT", "UPDATE", "DELETE");
        Set<String> found = new HashSet<>();
        for (String grant : grants) {
            if (grant == null || grant.toUpperCase(Locale.ROOT).contains("WITH GRANT OPTION")) throw new SQLException("Permisos ambiguos.");
            if (grant.matches("(?i)GRANT USAGE ON \\*\\.\\* TO .+")) continue;
            var match = GRANT.matcher(grant);
            if (!match.matches() || !exactScope(match.group(2), partialRevokes)) {
                throw new SQLException("Permisos globales, roles, comodines o alcance no permitido.");
            }
            for (String privilege : match.group(1).toUpperCase(Locale.ROOT).split(",\\s*")) {
                if (!allowed.contains(privilege.trim())) throw new SQLException("Permiso no permitido para esta cuenta.");
                found.add(privilege.trim());
            }
        }
        if (!found.containsAll(allowed)) throw new SQLException("Faltan permisos verificables del entorno de pruebas.");
    }

    public static void verifyIdentityAndPermissions(Connection connection, IntegrationConfig config,
                                                     boolean provisioner) throws SQLException {
        Boolean partialRevokes;
        try (Statement s = connection.createStatement(); ResultSet r = s.executeQuery(
                "SELECT CURRENT_USER(), CURRENT_ROLE(), @@GLOBAL.partial_revokes")) {
            if (!r.next() || r.getString(1) == null || !r.getString(1).startsWith(config.user() + "@")
                    || !"NONE".equals(r.getString(2))) throw new SQLException("Identidad o roles no permitidos.");
            String value = r.getString(3);
            if (!"0".equals(value) && !"1".equals(value)) throw new SQLException("partial_revokes no verificable.");
            partialRevokes = "1".equals(value);
            if (r.next()) throw new SQLException("Respuesta de identidad ambigua.");
        }
        List<String> grants = new ArrayList<>();
        try (Statement s = connection.createStatement(); ResultSet r = s.executeQuery("SHOW GRANTS")) {
            while (r.next()) grants.add(r.getString(1));
        }
        validateGrants(grants, provisioner, partialRevokes);
    }

    /** Never infer absence from a runner's filtered metadata. Verify visibility first. */
    public static void verifyTriggerAbsence(Connection provisioningConnection, IntegrationConfig provisioner) throws SQLException {
        verifyIdentityAndPermissions(provisioningConnection, provisioner, true);
        try (Statement s = provisioningConnection.createStatement(); ResultSet r = s.executeQuery(
                "SELECT COUNT(*) FROM information_schema.TRIGGERS WHERE TRIGGER_SCHEMA='erp_little_horizons_test'")) {
            if (!r.next()) throw new SQLException("Verificación de triggers inconclusa.");
            String count = r.getString(1);
            if (!"0".equals(count) || r.next()) throw new SQLException("Triggers presentes o resultado inconcluso.");
        }
    }

    public static void checkSql(String sql) throws SQLException { SqlPolicy.check(sql); }
}