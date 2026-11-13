package com.littlehorizons.erp.integration.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.lang.reflect.Proxy;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

/** Offline tests only: no MySQL connection, no driver installation, no schema execution. */
class IntegrationSafetyTest {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/erp_little_horizons_test";
    @TempDir Path directory;

    @ParameterizedTest
    @ValueSource(strings = {
            "jdbc:mysql://127.0.0.1:3306/erp_little_horizons",
            "jdbc:mysql://127.0.0.1:3306/erp_little_horizons_test_extra",
            "jdbc:mysql://localhost/erp_little_horizons_test",
            "jdbc:mysql://localhost:0/erp_little_horizons_test",
            "jdbc:mysql://localhost:65536/erp_little_horizons_test",
            "jdbc:mysql://localhost:3306/erp_little_horizons_test?user=root",
            "jdbc:mysql://localhost:3306/erp_little_horizons_test?allowMultiQueries=true",
            "jdbc:mysql://localhost:3306/erp_little_horizons_test?sessionVariables=sql_mode=''",
            "jdbc:mysql://user:secret@localhost:3306/erp_little_horizons_test",
            "jdbc:mysql://localhost:3306,other:3306/erp_little_horizons_test",
            "jdbc:mysql:loadbalance://localhost:3306/erp_little_horizons_test",
            "jdbc:mysql://remote.example:3306/erp_little_horizons_test",
            "jdbc:mysql://localhost:3306/erp_little_horizons_test/",
            "jdbc:mysql://localhost:3306/erp_little_horizons%5ftest", ""})
    void rejectsUnsafeDestinations(String url) {
        assertThrows(IllegalArgumentException.class, () -> new IntegrationConfig(url, "erp_it_runner", "synthetic"));
    }

    @ParameterizedTest @ValueSource(strings = {"root", "ROOT", "Root", "root@localhost", "", " "})
    void rejectsRootOrInvalidUser(String user) {
        assertThrows(IllegalArgumentException.class, () -> new IntegrationConfig(URL, user, "synthetic"));
    }

    @Test void requiresCompletePrivateConfigurationWithoutFallback() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> IntegrationConfig.load(directory.resolve("missing")));
        Path file = directory.resolve("integration.properties");
        Files.writeString(file, "db.url=" + URL + "\ndb.user=erp_it_runner\n");
        assertThrows(IllegalArgumentException.class, () -> IntegrationConfig.load(file));
        assertThrows(IllegalArgumentException.class, () -> new IntegrationConfig(URL, "erp_it_runner", ""));
        assertThrows(IllegalArgumentException.class, () -> new IntegrationConfig(null, "erp_it_runner", "synthetic"));
        assertThrows(IllegalArgumentException.class, () -> new IntegrationConfig(URL, null, "synthetic"));
        assertThrows(IllegalArgumentException.class, () -> new IntegrationConfig(URL, "erp_it_runner", null));
    }

    @Test void loadsOnlyThreeKeysAndDoesNotExposeSecrets() throws Exception {
        Path file = directory.resolve("integration.properties");
        String text = "db.url=" + URL + "\ndb.user=erp_it_runner\ndb.password=synthetic-secret\n";
        Files.writeString(file, text);
        IntegrationConfig config = IntegrationConfig.load(file);
        assertEquals(URL, config.url());
        assertEquals("IntegrationConfig[REDACTED]", config.toString());
        Files.writeString(file, text + "db.catalog=erp_little_horizons\n");
        assertThrows(IllegalArgumentException.class, () -> IntegrationConfig.load(file));
    }

    @ParameterizedTest @ValueSource(strings = {"false", "TRUE", "", "yes"})
    void refusesImplicitWriteApproval(String flag) {
        assertThrows(IllegalStateException.class, () -> TestDatabaseSafety.requireWriteApproval(flag));
    }

    @Test void explicitApprovalIsRequired() {
        assertThrows(IllegalStateException.class, () -> TestDatabaseSafety.requireWriteApproval(null));
        assertDoesNotThrow(() -> TestDatabaseSafety.requireWriteApproval("true"));
    }

    @ParameterizedTest @ValueSource(strings = {
            "GRANT ALL PRIVILEGES ON *.* TO 'runner'@'localhost'",
            "GRANT SELECT, INSERT, UPDATE, DELETE ON erp_little_horizons.* TO 'runner'@'localhost'",
            "GRANT SELECT, INSERT, UPDATE, DELETE, CREATE ON erp_little_horizons_test.* TO 'runner'@'localhost'",
            "GRANT SELECT, INSERT, UPDATE, DELETE ON erp_little_horizons_test.* TO 'runner'@'localhost' WITH GRANT OPTION",
            "GRANT 'admin_role'@'%' TO 'runner'@'localhost'",
            "GRANT SELECT ON erp_little_horizons_test.* TO 'runner'@'localhost'"})
    void rejectsBroadInheritedOrInsufficientPermissions(String grant) {
        assertThrows(SQLException.class, () -> TestDatabaseSafety.validateGrants(List.of(grant), false, false));
    }

    @Test void acceptsOnlySchemaScopedLeastPrivileges() {
        assertDoesNotThrow(() -> TestDatabaseSafety.validateGrants(List.of(
                "GRANT USAGE ON *.* TO 'runner'@'localhost'",
                "GRANT SELECT, INSERT, UPDATE, DELETE ON `erp\\_little\\_horizons\\_test`.* TO 'runner'@'localhost'"), false, false));
        assertDoesNotThrow(() -> TestDatabaseSafety.validateGrants(List.of(
                "GRANT CREATE, SELECT, INSERT, TRIGGER ON `erp\\_little\\_horizons\\_test`.* TO 'setup'@'localhost'"), true, false));
    }

    @ParameterizedTest @ValueSource(strings = {
            "USE erp_little_horizons", "DROP DATABASE erp_little_horizons_test",
            "ALTER TABLE estudiantes ADD x INT", "TRUNCATE estudiantes",
            "SELECT * FROM erp_little_horizons.estudiantes",
            "DELETE FROM other.estudiantes", "SELECT * FROM mysql.user",
            "SELECT 1; DELETE FROM estudiantes", "SELECT 1 INTO OUTFILE '/tmp/x'",
            "SELECT /* hidden */ 1", "CALL unsafe()", "SELECT * FROM unknown_table"})
    void blocksUnsafeSqlBeforeDelegation(String sql) {
        assertThrows(SQLException.class, () -> TestDatabaseSafety.checkSql(sql));
    }

    @Test void acceptsCurrentDaoSqlWithoutExecutingIt() throws Exception {
        int checked = 0;
        try (var files = Files.list(Path.of("src/main/java/com/littlehorizons/erp/dao"))) {
            for (Path file : files.toList()) {
                String source = Files.readString(file);
                var assignments = java.util.regex.Pattern.compile("String\\s+\\w+\\s*=\\s*((?:\"[^\"]*\"\\s*(?:\\+\\s*)?)+);", java.util.regex.Pattern.DOTALL).matcher(source);
                while (assignments.find()) {
                    StringBuilder sql = new StringBuilder();
                    var strings = java.util.regex.Pattern.compile("\"([^\"]*)\"").matcher(assignments.group(1));
                    while (strings.find()) sql.append(strings.group(1));
                    if (sql.toString().matches("(?s)^(SELECT|INSERT|UPDATE|DELETE).*")) {
                        assertDoesNotThrow(() -> TestDatabaseSafety.checkSql(sql.toString()), file.getFileName().toString()); checked++;
                    }
                }
            }
        }
        assertEquals(74, checked);
    }

    @Test void wrongDestinationNeverCallsUnderlyingDriver() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        Driver fake = new Driver() {
            public Connection connect(String url, Properties p) { calls.incrementAndGet(); throw new AssertionError("Must not connect"); }
            public boolean acceptsURL(String url) { return true; }
            public DriverPropertyInfo[] getPropertyInfo(String url, Properties p) { return new DriverPropertyInfo[0]; }
            public int getMajorVersion() { return 1; } public int getMinorVersion() { return 0; }
            public boolean jdbcCompliant() { return false; }
            public java.util.logging.Logger getParentLogger() { return java.util.logging.Logger.getGlobal(); }
        };
        GuardedTestDriver guard = new GuardedTestDriver(new IntegrationConfig(URL, "erp_it_runner", "synthetic"), fake);
        Properties props = new Properties(); props.setProperty("user", "erp_it_runner"); props.setProperty("password", "synthetic");
        assertThrows(SQLException.class, () -> guard.connect(URL.replace("_test", ""), props));
        props.setProperty("user", "root");
        assertThrows(SQLException.class, () -> guard.connect(URL, props));
        assertEquals(0, calls.get());
    }

    @Test void connectionAndStatementsCannotSwitchCatalogOrUnwrap() throws Exception {
        AtomicReference<String> catalog = new AtomicReference<>(IntegrationConfig.DATABASE);
        AtomicInteger executions = new AtomicInteger();
        PreparedStatement rawStatement = (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class},
                (p, m, a) -> { if (m.getName().equals("executeUpdate")) return executions.incrementAndGet(); return null; });
        Connection raw = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class},
                (p, m, a) -> switch (m.getName()) {
                    case "getCatalog" -> catalog.get();
                    case "prepareStatement", "createStatement" -> rawStatement;
                    default -> null;
                });
        Connection guard = GuardedTestDriver.guard(raw);
        assertThrows(SQLException.class, () -> guard.setCatalog("erp_little_horizons"));
        assertThrows(SQLException.class, () -> guard.setSchema("other"));
        assertThrows(SQLException.class, () -> guard.unwrap(Connection.class));
        assertThrows(SQLException.class, () -> guard.prepareStatement("DELETE FROM erp_little_horizons.estudiantes"));
        PreparedStatement statement = guard.prepareStatement("UPDATE recursos SET estado_recurso=? WHERE id_recurso=?");
        assertSame(guard, statement.getConnection());
        catalog.set("erp_little_horizons");
        assertThrows(SQLException.class, statement::executeUpdate);
        assertEquals(0, executions.get());
    }

    @Test void validatesTheActualServerCatalogIdentityRolesAndGrantsWithoutNetwork() throws Exception {
        IntegrationConfig config = new IntegrationConfig(URL, "erp_it_runner", "synthetic");
        Properties props = new Properties(); props.setProperty("user", config.user()); props.setProperty("password", config.password());
        for (String invalid : List.of("catalog", "identity", "role", "grants", "trigger")) {
            AtomicBoolean closed = new AtomicBoolean();
            Driver delegate = serverDriver(invalid, closed);
            GuardedTestDriver guard = new GuardedTestDriver(config, delegate, target -> { if (invalid.equals("trigger")) throw new SQLException("Trigger verification failed"); });
            SQLException error = assertThrows(SQLException.class, () -> guard.connect(URL, props));
            assertTrue(closed.get());
            assertNull(error.getCause());
            assertFalse(error.getMessage().contains("synthetic"));
        }
        AtomicBoolean closed = new AtomicBoolean();
        try (Connection connection = new GuardedTestDriver(config, serverDriver("valid", closed), target -> { }).connect(URL, props)) {
            assertEquals(IntegrationConfig.DATABASE, connection.getCatalog());
        }
        assertTrue(closed.get());
    }

    private Driver serverDriver(String mode, AtomicBoolean closed) {
        Statement statement = (Statement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Statement.class}, (p, m, a) -> {
            if (!m.getName().equals("executeQuery")) return null;
            String sql = (String) a[0];
            List<String[]> data;
            if (sql.contains("CURRENT_USER")) data = Collections.singletonList(new String[]{
                    mode.equals("identity") ? "root@localhost" : "erp_it_runner@localhost", mode.equals("role") ? "admin" : "NONE", "0"});
            else if (sql.equals("SHOW GRANTS")) data = Collections.singletonList(new String[]{
                    mode.equals("grants") ? "GRANT ALL PRIVILEGES ON *.* TO 'runner'@'localhost'"
                            : "GRANT SELECT, INSERT, UPDATE, DELETE ON `erp\\_little\\_horizons\\_test`.* TO 'runner'@'localhost'"});
            else data = Collections.singletonList(new String[]{mode.equals("trigger") ? "1" : "0"});
            AtomicInteger cursor = new AtomicInteger(-1);
            return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{ResultSet.class}, (rp, rm, ra) -> switch (rm.getName()) {
                case "next" -> cursor.incrementAndGet() < data.size();
                case "getString" -> data.get(cursor.get())[(Integer) ra[0] - 1];
                case "getInt" -> Integer.parseInt(data.get(cursor.get())[(Integer) ra[0] - 1]);
                default -> null;
            });
        });
        Connection connection = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (p, m, a) -> {
            if (m.getName().equals("close")) { closed.set(true); return null; }
            return switch (m.getName()) {
                case "getCatalog" -> mode.equals("catalog") ? "erp_little_horizons" : IntegrationConfig.DATABASE;
                case "createStatement" -> statement;
                default -> null;
            };
        });
        return new Driver() {
            public Connection connect(String url, Properties props) { return connection; }
            public boolean acceptsURL(String url) { return true; }
            public DriverPropertyInfo[] getPropertyInfo(String url, Properties props) { return new DriverPropertyInfo[0]; }
            public int getMajorVersion() { return 1; } public int getMinorVersion() { return 0; }
            public boolean jdbcCompliant() { return false; }
            public java.util.logging.Logger getParentLogger() { return java.util.logging.Logger.getGlobal(); }
        };
    }

    @Test void generatedScriptMatchesOfficialAndIsAlwaysQualified() throws Exception {
        String official = Files.readString(Path.of("script-bd-pry-erp-colegio-little-horizon.sql"));
        String script = Files.readString(Path.of("scripts/integration/initialize-test.sql"));
        assertDoesNotThrow(() -> InitializationSql.validate(script, official));
        assertThrows(IllegalArgumentException.class, () -> InitializationSql.validate(script.replace("erp_little_horizons_test", "erp_little_horizons"), official));
        String sql = script.replaceAll("(?m)^--[^\\r\\n]*", "");
        assertFalse(java.util.regex.Pattern.compile("\\berp_little_horizons\\b").matcher(sql).find());
        assertFalse(sql.matches("(?is).*\\b(DROP|TRUNCATE|USE|GRANT|CREATE DATABASE)\\b.*"));
        assertEquals(22, sql.split("CREATE TABLE erp_little_horizons_test\\.", -1).length - 1);
        assertEquals(3, sql.split("INSERT INTO erp_little_horizons_test\\.", -1).length - 1);
    }

    @Test void mavenKeepsIntegrationOptInAndScriptsRequireExplicitApply() throws Exception {
        var factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        var document = factory.newDocumentBuilder().parse(Path.of("pom.xml").toFile());
        var plugins = document.getElementsByTagName("plugin"); boolean surefire = false, failsafe = false;
        for (int i = 0; i < plugins.getLength(); i++) {
            var element = (org.w3c.dom.Element) plugins.item(i);
            String artifact = element.getElementsByTagName("artifactId").item(0).getTextContent();
            if (artifact.equals("maven-surefire-plugin")) {
                assertEquals("integration", element.getElementsByTagName("excludedGroups").item(0).getTextContent());
                assertEquals("**/*IT.java", element.getElementsByTagName("exclude").item(0).getTextContent()); surefire = true;
            }
            if (artifact.equals("maven-failsafe-plugin")) {
                assertEquals("profile", element.getParentNode().getParentNode().getParentNode().getNodeName());
                assertTrue(element.getTextContent().contains("integration-test")); failsafe = true;
            }
        }
        assertTrue(surefire && failsafe);
        String wrapper = Files.readString(Path.of("scripts/integration/prepare-test.ps1"));
        assertTrue(wrapper.indexOf("if (-not $Apply) { return }") < wrapper.indexOf("& $java"));
    }
}
