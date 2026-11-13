package com.littlehorizons.erp.integration.support;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

class FixtureCleanupTest {
    private static class Server {
        final Set<Integer> rows = new HashSet<>(Set.of(1, 2));
        final Set<Integer> fail = new HashSet<>();
        final Map<Integer, Integer> counts = new HashMap<>();
        final AtomicInteger updates = new AtomicInteger(), rollbacks = new AtomicInteger();
        String catalog = IntegrationConfig.DATABASE;
        boolean loseCommitAck;
        boolean autoCommit = true;
        Connection connection() {
            Set<Integer> staged = new HashSet<>();
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{Connection.class}, (p, m, a) -> {
                switch (m.getName()) {
                    case "getCatalog": return catalog;
                    case "getAutoCommit": return autoCommit;
                    case "prepareStatement":
                        AtomicInteger id = new AtomicInteger();
                        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class}, (sp, sm, sa) -> {
                            if (sm.getName().equals("setInt")) { id.set((Integer) sa[1]); return null; }
                            if (sm.getName().equals("executeUpdate")) {
                                updates.incrementAndGet();
                                if (fail.contains(id.get())) throw new SQLException("password=synthetic-secret token=hidden-driver-detail");
                                int count = counts.getOrDefault(id.get(), rows.contains(id.get()) ? 1 : 0);
                                if (count == 1) staged.add(id.get()); return count;
                            }
                            if (sm.getName().equals("executeQuery")) return JdbcDoubles.rows(new String[]{rows.contains(id.get()) ? "1" : "0"});
                            return JdbcDoubles.defaultValue(sm.getReturnType());
                        });
                    case "commit":
                        rows.removeAll(staged); staged.clear();
                        if (loseCommitAck) throw new SQLException("synthetic-secret acknowledgement lost");
                        return null;
                    case "rollback": rollbacks.incrementAndGet(); staged.clear(); return null;
                    default: return JdbcDoubles.defaultValue(m.getReturnType());
                }
            });
        }
    }

    @Test void partialFailureKeepsExactPendingKeysAndCanRetry() throws Exception {
        Server server = new Server(); server.fail.add(2); List<String> messages = new ArrayList<>();
        FixtureCleanup cleanup = new FixtureCleanup(server::connection, messages::add);
        cleanup.track("recursos", "id_recurso", 1); cleanup.track("recursos", "id_recurso", 2);
        SQLException error = assertThrows(SQLException.class, cleanup::close);
        assertEquals(List.of("recursos.id_recurso=2"), cleanup.pendingRows());
        assertEquals(Set.of(2), server.rows);
        assertTrue(error.getMessage().contains("recursos.id_recurso=2"));
        assertFalse(error.getMessage().contains("synthetic-secret"));
        assertFalse(String.join("", messages).contains("hidden-driver-detail"));
        assertNull(error.getCause());
        server.fail.clear(); cleanup.close();
        assertTrue(cleanup.pendingRows().isEmpty()); assertTrue(server.rows.isEmpty());
    }

    @Test void unexpectedRowCountRollsBackAndRetainsOwnership() {
        Server server = new Server(); server.counts.put(1, 2);
        FixtureCleanup cleanup = new FixtureCleanup(server::connection, s -> { }); cleanup.track("recursos", "id_recurso", 1);
        assertThrows(SQLException.class, cleanup::close);
        assertEquals(1, cleanup.pendingRows().size()); assertTrue(server.rows.contains(1));
        assertEquals(1, server.rollbacks.get());
    }

    @Test void zeroAffectedNeedsProofOfAbsence() throws Exception {
        Server server = new Server(); server.counts.put(1, 0);
        FixtureCleanup cleanup = new FixtureCleanup(server::connection, s -> { }); cleanup.track("recursos", "id_recurso", 1);
        assertThrows(SQLException.class, cleanup::close); assertEquals(1, cleanup.pendingRows().size());
        server.rows.remove(1); cleanup.close(); assertTrue(cleanup.pendingRows().isEmpty());
    }

    @Test void unknownCommitOutcomeIsRetainedUntilAbsenceConfirmed() throws Exception {
        Server server = new Server(); server.loseCommitAck = true;
        FixtureCleanup cleanup = new FixtureCleanup(server::connection, s -> { }); cleanup.track("recursos", "id_recurso", 1);
        assertThrows(SQLException.class, cleanup::close);
        assertEquals(1, cleanup.pendingRows().size()); assertFalse(server.rows.contains(1));
        server.loseCommitAck = false; cleanup.close(); assertTrue(cleanup.pendingRows().isEmpty());
    }

    @Test void neverPreparesDeletionAgainstMainCatalog() {
        Server server = new Server(); server.catalog = "erp_little_horizons";
        FixtureCleanup cleanup = new FixtureCleanup(server::connection, s -> { }); cleanup.track("recursos", "id_recurso", 1);
        assertThrows(SQLException.class, cleanup::close);
        assertEquals(0, server.updates.get()); assertEquals(1, cleanup.pendingRows().size());
    }

    @Test void onlyActualPrimaryKeysCanBeTracked() {
        FixtureCleanup cleanup = new FixtureCleanup(() -> { throw new AssertionError("No connection expected"); }, s -> { });
        assertThrows(IllegalArgumentException.class, () -> cleanup.track("clubes", "capacidad_maxima", 1));
        assertThrows(IllegalArgumentException.class, () -> cleanup.track("other.recursos", "id_recurso", 1));
        assertThrows(IllegalArgumentException.class, () -> cleanup.track("recursos", null, 1));
        assertThrows(IllegalArgumentException.class, () -> cleanup.track("recursos", "id_recurso", -1));
    }

    @Test void cannotCommitAnUnrelatedExistingTransaction() {
        Server server = new Server(); server.autoCommit = false;
        FixtureCleanup cleanup = new FixtureCleanup(server::connection, s -> { }); cleanup.track("recursos", "id_recurso", 1);
        assertThrows(SQLException.class, cleanup::close);
        assertEquals(0, server.updates.get()); assertEquals(1, cleanup.pendingRows().size());
    }
}
