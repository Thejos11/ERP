package com.littlehorizons.erp.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/** Loads configuration without connecting to MySQL or exposing its values. */
final class DatabaseConfig {
    private final String url;
    private final String user;
    private final String password;

    DatabaseConfig(String url, String user, String password) {
        if (url == null || !url.startsWith("jdbc:mysql:") || url.isBlank()
                || user == null || user.isBlank() || password == null) {
            throw new IllegalArgumentException("Configuración MySQL incompleta o URL JDBC inválida.");
        }
        // An empty password is accepted only when explicitly supplied by the operator.
        this.url = url;
        this.user = user;
        this.password = password;
    }

    static DatabaseConfig load(Path file, ClassLoader loader, Map<String, String> environment) {
        Properties props = new Properties();
        try {
            if (Files.exists(file)) {
                try (InputStream input = Files.newInputStream(file)) {
                    props.load(input);
                }
            } else {
                // Compatibility for externally supplied classpath configuration; never bundled by Maven.
                try (InputStream input = loader.getResourceAsStream("db.properties")) {
                    if (input != null) props.load(input);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            // Do not attach the original error: it can contain configuration text.
            throw new IllegalArgumentException("No se pudo leer la configuración MySQL.");
        }
        return new DatabaseConfig(props.getProperty("db.url", environment.get("DB_URL")),
                props.getProperty("db.user", environment.get("DB_USER")),
                props.getProperty("db.password", environment.get("DB_PASSWORD")));
    }

    String url() { return url; }
    String user() { return user; }
    String password() { return password; }

    @Override
    public String toString() { return "DatabaseConfig[REDACTED]"; }
}
