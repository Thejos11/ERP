package com.littlehorizons.erp.integration.support;

import java.util.*;
import java.util.regex.Pattern;

/** Produces only qualified test-schema DDL and official seed inserts. */
public final class InitializationSql {
    private InitializationSql() { }

    public static String generate(String official) {
        List<String> statements = new ArrayList<>();
        var ddl = Pattern.compile("(?ms)^CREATE TABLE ([a-z_]+) \\(.*?^\\) ENGINE=InnoDB[^;]*;").matcher(official);
        Set<String> names = new HashSet<>();
        while (ddl.find()) {
            names.add(ddl.group(1));
            String statement = ddl.group().replaceFirst("CREATE TABLE ", "CREATE TABLE " + IntegrationConfig.DATABASE + ".");
            statement = statement.replaceAll("REFERENCES ([a-z_]+)\\(", "REFERENCES " + IntegrationConfig.DATABASE + ".$1(");
            statements.add(statement);
        }
        if (!names.equals(TestDatabaseSafety.TABLES)) throw new IllegalArgumentException("Esquema oficial inesperado.");
        var seeds = Pattern.compile("(?ms)^INSERT INTO (grados|categorias_recurso|conceptos_pago) \\(.*?;").matcher(official);
        int count = 0;
        while (seeds.find()) {
            statements.add(seeds.group().replaceFirst("INSERT INTO ", "INSERT INTO " + IntegrationConfig.DATABASE + "."));
            count++;
        }
        if (count != 3) throw new IllegalArgumentException("Datos iniciales oficiales inesperados.");
        return "-- Test schema only. Requires an empty, separately provisioned test database.\n"
                + "-- Generated from the official SQL; no DROP, USE, GRANT or database creation.\n\n"
                + String.join("\n\n", statements) + "\n";
    }

    public static void validate(String script, String official) {
        if (!script.replace("\r\n", "\n").equals(generate(official).replace("\r\n", "\n"))) {
            throw new IllegalArgumentException("El script de pruebas no coincide con la derivación oficial segura.");
        }
    }
}
