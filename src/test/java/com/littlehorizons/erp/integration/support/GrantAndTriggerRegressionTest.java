package com.littlehorizons.erp.integration.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

class GrantAndTriggerRegressionTest {
    private final IntegrationConfig runner = config("erp_it_runner");
    private static IntegrationConfig config(String user) {
        return new IntegrationConfig("jdbc:mysql://127.0.0.1:3306/erp_little_horizons_test", user, "synthetic");
    }

    @Test void exactGrantDependsOnVerifiedServerSemantics() {
        assertDoesNotThrow(() -> TestDatabaseSafety.validateGrants(List.of(JdbcDoubles.grants(false, false)), false, false));
        assertDoesNotThrow(() -> TestDatabaseSafety.validateGrants(List.of(JdbcDoubles.grants(false, true)), false, true));
        assertThrows(SQLException.class, () -> TestDatabaseSafety.validateGrants(List.of(JdbcDoubles.grants(false, true)), false, false));
        assertThrows(SQLException.class, () -> TestDatabaseSafety.validateGrants(List.of(JdbcDoubles.grants(false, false)), false, true));
        assertThrows(SQLException.class, () -> TestDatabaseSafety.validateGrants(List.of(JdbcDoubles.grants(false, false)), false, null));
    }

    @ParameterizedTest @ValueSource(strings = {"erp_little_horizons_test", "erp\\_little_horizons_test", "erp%", "erp\\_little\\_horizons\\_test%",
            "erp_little_horizons", "ERP\\_LITTLE\\_HORIZONS\\_TEST", "erp\\_little\\_horizons\\_test2", "erp\\qlittle\\_horizons\\_test"})
    void rejectsNonliteralOrDifferentGrantScopes(String scope) {
        String grant = "GRANT SELECT, INSERT, UPDATE, DELETE ON `" + scope + "`.* TO 'synthetic'@'localhost'";
        assertThrows(SQLException.class, () -> TestDatabaseSafety.validateGrants(List.of(grant), false, false));
    }

    @Test void runnerCanNeverReceiveTriggerPrivilege() {
        String grant = JdbcDoubles.grants(false, false).replace("SELECT,", "TRIGGER, SELECT,");
        assertThrows(SQLException.class, () -> TestDatabaseSafety.validateGrants(List.of(grant), false, false));
    }

    @Test void filteredZeroWithoutTriggerVisibilityCannotPass() {
        AtomicInteger triggerQueries = new AtomicInteger();
        Connection filtered = JdbcDoubles.server("erp_it_setup", List.of(
                JdbcDoubles.grants(true, false).replace(", TRIGGER", "")), "0", "0", triggerQueries, new AtomicBoolean());
        assertThrows(SQLException.class, () -> TestDatabaseSafety.verifyTriggerAbsence(filtered, config("erp_it_setup")));
        assertEquals(0, triggerQueries.get(), "Never interpret a filtered zero as proof of absence");
    }

    @ParameterizedTest @ValueSource(strings = {"1", "NULL", "EMPTY", "ERROR", "-1"})
    void blocksTriggersAndInconclusiveMetadata(String count) {
        Connection server = JdbcDoubles.server("erp_it_setup", List.of(JdbcDoubles.grants(true, false)), "0", count,
                new AtomicInteger(), new AtomicBoolean());
        assertThrows(SQLException.class, () -> TestDatabaseSafety.verifyTriggerAbsence(server, config("erp_it_setup")));
    }

    @ParameterizedTest @ValueSource(strings = {"UNKNOWN", "", "ON"})
    void cannotGuessPartialRevokes(String value) {
        Connection server = JdbcDoubles.server("erp_it_setup", List.of(JdbcDoubles.grants(true, false)), value, "0",
                new AtomicInteger(), new AtomicBoolean());
        assertThrows(SQLException.class, () -> TestDatabaseSafety.verifyTriggerAbsence(server, config("erp_it_setup")));
    }

    @Test void separateProvisioningIdentityChecksReadOnlyOnEveryCall() throws Exception {
        AtomicInteger queries = new AtomicInteger(), connects = new AtomicInteger(); AtomicBoolean readOnly = new AtomicBoolean();
        Connection server = JdbcDoubles.server("erp_it_setup", List.of(JdbcDoubles.grants(true, false)), "0", "0", queries, readOnly);
        var verifier = new ProvisioningTriggerVerifier(config("erp_it_setup"), JdbcDoubles.driver(server, connects));
        verifier.verify(runner); verifier.verify(runner);
        assertTrue(readOnly.get()); assertEquals(2, queries.get()); assertEquals(2, connects.get());
    }

    @Test void rejectsSameIdentityAndDifferentEndpointBeforeConnecting() {
        AtomicInteger calls = new AtomicInteger(); Driver fake = JdbcDoubles.driver(null, calls);
        assertThrows(SQLException.class, () -> new ProvisioningTriggerVerifier(runner, fake).verify(runner));
        IntegrationConfig different = new IntegrationConfig("jdbc:mysql://127.0.0.1:3307/erp_little_horizons_test", "erp_it_setup", "synthetic");
        assertThrows(SQLException.class, () -> new ProvisioningTriggerVerifier(different, fake).verify(runner));
        assertEquals(0, calls.get());
    }

    @Test void missingVerifierFailsClosedAndNeverExposesDriverSecrets() {
        Connection server = JdbcDoubles.server("erp_it_runner", List.of(JdbcDoubles.grants(false, false)), "0", "0",
                new AtomicInteger(), new AtomicBoolean());
        Properties props = new Properties(); props.setProperty("user", runner.user()); props.setProperty("password", runner.password());
        var guard = new GuardedTestDriver(runner, JdbcDoubles.driver(server, new AtomicInteger()));
        assertThrows(SQLException.class, () -> guard.connect(runner.url(), props));
        Connection failed = JdbcDoubles.server("erp_it_setup", List.of(JdbcDoubles.grants(true, false)), "0", "ERROR",
                new AtomicInteger(), new AtomicBoolean());
        SQLException error = assertThrows(SQLException.class, () -> new ProvisioningTriggerVerifier(config("erp_it_setup"),
                JdbcDoubles.driver(failed, new AtomicInteger())).verify(runner));
        assertFalse(error.getMessage().contains("synthetic")); assertNull(error.getCause());
    }
}
