package com.littlehorizons.erp.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseConfigTest {
    @TempDir Path directory;
    private static final String URL = "jdbc:mysql://localhost/erp_little_horizons";
    private static final Map<String, String> ENV = Map.of(
            "DB_URL", URL, "DB_USER", "erp_test", "DB_PASSWORD", "synthetic-secret");

    private ClassLoader loader(String content) {
        return new ClassLoader(null) {
            @Override public InputStream getResourceAsStream(String name) {
                return content == null ? null : new ByteArrayInputStream(
                        content.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1));
            }
        };
    }

    @Test void loadsExternalFileBeforeClasspathAndEnvironment() throws Exception {
        Path file = directory.resolve("db.properties");
        Files.writeString(file, "db.url=" + URL + "\ndb.user=local_user\ndb.password=local-secret\n");
        DatabaseConfig config = DatabaseConfig.load(file, loader("db.user=classpath_user"), ENV);
        assertEquals("local_user", config.user());
        assertEquals("local-secret", config.password());
        assertEquals(URL, config.url());
    }

    @Test void loadsEnvironmentWithoutFiles() {
        DatabaseConfig config = DatabaseConfig.load(directory.resolve("missing"), loader(null), ENV);
        assertEquals("erp_test", config.user());
        assertEquals("synthetic-secret", config.password());
    }

    @Test void retainsExternalClasspathCompatibility() {
        DatabaseConfig config = DatabaseConfig.load(directory.resolve("missing"),
                loader("db.url=" + URL + "\ndb.user=classpath_user\ndb.password=classpath-secret"), Map.of());
        assertEquals("classpath_user", config.user());
    }

    @Test void usesEnvironmentForMissingIndividualProperties() throws Exception {
        Path file = directory.resolve("db.properties");
        Files.writeString(file, "db.user=local_user");
        DatabaseConfig config = DatabaseConfig.load(file, loader(null), ENV);
        assertEquals("local_user", config.user());
        assertEquals("synthetic-secret", config.password());
    }

    @Test void rejectsMissingConfigurationWithoutImplicitRoot() {
        assertThrows(IllegalArgumentException.class, () ->
                DatabaseConfig.load(directory.resolve("missing"), loader(null), Map.of()));
    }

    @Test void rejectsMissingPassword() {
        assertThrows(IllegalArgumentException.class, () -> new DatabaseConfig(URL, "erp_test", null));
    }

    @Test void acceptsExplicitEmptyPasswordForCompatibility() {
        assertEquals("", new DatabaseConfig(URL, "erp_test", "").password());
    }

    @Test void rejectsBlankUserAndInvalidUrlWithoutEchoingValues() {
        assertThrows(IllegalArgumentException.class, () -> new DatabaseConfig(URL, " ", "secret"));
        Exception error = assertThrows(IllegalArgumentException.class, () ->
                new DatabaseConfig("invalid-sensitive-url", "erp_test", "secret"));
        assertFalse(error.getMessage().contains("invalid-sensitive-url"));
    }

    @Test void unreadableExternalFileDoesNotFallBack() {
        assertThrows(IllegalArgumentException.class, () -> DatabaseConfig.load(directory, loader(null), ENV));
    }

    @Test void malformedPropertiesDoNotExposeContent() throws Exception {
        Path file = directory.resolve("db.properties");
        Files.writeString(file, "db.password=\\uZZZZ-sensitive");
        Exception error = assertThrows(IllegalArgumentException.class, () -> DatabaseConfig.load(file, loader(null), ENV));
        assertNull(error.getCause());
        assertFalse(error.getMessage().contains("sensitive"));
    }

    @Test void stringRepresentationDoesNotExposeConfiguration() {
        assertEquals("DatabaseConfig[REDACTED]", new DatabaseConfig(URL, "erp_test", "secret").toString());
    }
}
