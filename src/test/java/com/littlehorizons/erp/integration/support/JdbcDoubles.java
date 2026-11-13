package com.littlehorizons.erp.integration.support;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/** In-memory protocol doubles; never register a JDBC driver or open a socket. */
final class JdbcDoubles {
    private JdbcDoubles() { }
    static String grants(boolean setup, boolean partialRevokes) {
        String scope = partialRevokes ? IntegrationConfig.DATABASE : "erp\\_little\\_horizons\\_test";
        return "GRANT " + (setup ? "CREATE, SELECT, INSERT, TRIGGER" : "SELECT, INSERT, UPDATE, DELETE")
                + " ON `" + scope + "`.* TO 'synthetic'@'localhost'";
    }
    static ResultSet rows(String[]... data) {
        AtomicInteger cursor = new AtomicInteger(-1); AtomicBoolean wasNull = new AtomicBoolean();
        return (ResultSet) Proxy.newProxyInstance(JdbcDoubles.class.getClassLoader(), new Class<?>[]{ResultSet.class}, (p, m, a) -> {
            if (m.getName().equals("next")) return cursor.incrementAndGet() < data.length;
            if (m.getName().equals("wasNull")) return wasNull.get();
            if (Set.of("getString", "getInt", "getLong").contains(m.getName())) {
                String value = data[cursor.get()][(Integer) a[0] - 1]; wasNull.set(value == null);
                if (m.getName().equals("getString")) return value;
                if (m.getName().equals("getInt")) return value == null ? 0 : Integer.parseInt(value);
                return value == null ? 0L : Long.parseLong(value);
            }
            return defaultValue(m.getReturnType());
        });
    }

    static Connection server(String user, List<String> grants, String partialRevokes, String count,
                             AtomicInteger triggerQueries, AtomicBoolean readOnly) {
        Statement statement = (Statement) Proxy.newProxyInstance(JdbcDoubles.class.getClassLoader(), new Class<?>[]{Statement.class}, (p, m, a) -> {
            if (!m.getName().equals("executeQuery")) return defaultValue(m.getReturnType());
            String sql = (String) a[0];
            if (sql.contains("CURRENT_USER")) return rows(new String[]{user + "@localhost", "NONE", partialRevokes});
            if (sql.equals("SHOW GRANTS")) return rows(grants.stream().map(g -> new String[]{g}).toArray(String[][]::new));
            if (sql.contains("information_schema.TRIGGERS")) {
                triggerQueries.incrementAndGet();
                if (count.equals("ERROR")) throw new SQLException("synthetic-secret-from-driver");
                if (count.equals("EMPTY")) return rows();
                return rows(new String[]{count.equals("NULL") ? null : count});
            }
            throw new AssertionError("Unexpected query in offline double");
        });
        return (Connection) Proxy.newProxyInstance(JdbcDoubles.class.getClassLoader(), new Class<?>[]{Connection.class}, (p, m, a) -> switch (m.getName()) {
            case "getCatalog" -> IntegrationConfig.DATABASE;
            case "createStatement" -> statement;
            case "setReadOnly" -> { readOnly.set((Boolean) a[0]); yield null; }
            default -> defaultValue(m.getReturnType());
        });
    }
    static Driver driver(Connection connection, AtomicInteger calls) {
        return new Driver() {
            public Connection connect(String url, Properties props) { calls.incrementAndGet(); return connection; }
            public boolean acceptsURL(String url) { return true; }
            public DriverPropertyInfo[] getPropertyInfo(String url, Properties props) { return new DriverPropertyInfo[0]; }
            public int getMajorVersion() { return 1; } public int getMinorVersion() { return 0; }
            public boolean jdbcCompliant() { return false; }
            public java.util.logging.Logger getParentLogger() { return java.util.logging.Logger.getGlobal(); }
        };
    }
    static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
    }
}
