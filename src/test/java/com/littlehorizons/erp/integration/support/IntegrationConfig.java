package com.littlehorizons.erp.integration.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.regex.Pattern;

/** Test-only configuration: deliberately independent of development configuration. */
public final class IntegrationConfig {
    public static final String DATABASE = "erp_little_horizons_test";
    private static final Pattern URL = Pattern.compile(
            "jdbc:mysql://(?:127\\.0\\.0\\.1|localhost):([0-9]{1,5})/" + DATABASE);
    private final String url;
    private final String user;
    private final String password;

    public IntegrationConfig(String url, String user, String password) {
        var match = URL.matcher(url == null ? "" : url);
        if (!match.matches() || Integer.parseInt(match.group(1)) < 1
                || Integer.parseInt(match.group(1)) > 65535
                || user == null || !user.matches("[a-zA-Z][a-zA-Z0-9_]{0,31}")
                || user.equalsIgnoreCase("root") || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Configuración de integración insegura o incompleta.");
        }
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static IntegrationConfig load(Path file) {
        Properties props = new Properties();
        try (var stream = Files.newInputStream(file)) {
            props.load(stream);
        } catch (IOException | IllegalArgumentException e) {
            throw new IllegalArgumentException("No se pudo leer la configuración de integración.");
        }
        if (!props.stringPropertyNames().equals(java.util.Set.of("db.url", "db.user", "db.password"))) {
            throw new IllegalArgumentException("Solo se admiten las tres propiedades de integración.");
        }
        return new IntegrationConfig(props.getProperty("db.url"), props.getProperty("db.user"),
                props.getProperty("db.password"));
    }

    public String url() { return url; }
    public String user() { return user; }
    public String password() { return password; }
    @Override public String toString() { return "IntegrationConfig[REDACTED]"; }
}
