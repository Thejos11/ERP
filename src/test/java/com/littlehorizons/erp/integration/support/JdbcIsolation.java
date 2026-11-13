package com.littlehorizons.erp.integration.support;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

/** Recursively wraps the JDBC object graph with stable proxy identities. */
final class JdbcIsolation {
    private final Connection raw;
    private final Map<Object, Object> proxies = new IdentityHashMap<>();
    private JdbcIsolation(Connection raw) { this.raw = Objects.requireNonNull(raw); }

    static Connection guard(Connection raw) { return (Connection) new JdbcIsolation(raw).wrap(raw); }

    private Object wrap(Object value) {
        if (value == null) return null;
        Object cached = proxies.get(value);
        if (cached != null) return cached;
        if (value instanceof Object[] array) {
            Object[] copy = array.clone(); for (int i = 0; i < copy.length; i++) copy[i] = wrap(copy[i]); return copy;
        }
        Class<?> type = null;
        for (Class<?> candidate : List.of(Connection.class, CallableStatement.class, PreparedStatement.class,
                Statement.class, ResultSet.class, DatabaseMetaData.class, ResultSetMetaData.class,
                ParameterMetaData.class, java.sql.Array.class, Struct.class, Ref.class, NClob.class,
                Clob.class, Blob.class, SQLXML.class, Savepoint.class)) {
            if (candidate.isInstance(value)) { type = candidate; break; }
        }
        if (type == null) return value;
        Class<?> jdbcType = type;
        Object proxy = Proxy.newProxyInstance(JdbcIsolation.class.getClassLoader(), new Class<?>[]{type},
                (p, method, args) -> call(p, value, jdbcType, method, args));
        proxies.put(value, proxy);
        return proxy;
    }

    private Object call(Object proxy, Object target, Class<?> type, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        if (name.equals("toString")) return "GuardedIntegration" + type.getSimpleName();
        if (name.equals("equals")) return proxy == args[0];
        if (name.equals("hashCode")) return System.identityHashCode(proxy);
        if (name.equals("unwrap") || name.equals("prepareCall")) throw new SQLException("Acceso JDBC directo bloqueado.");
        if (name.equals("isWrapperFor")) return false;
        if (!Set.of("close", "isClosed", "abort", "free").contains(name)) TestDatabaseSafety.checkCatalog(raw);
        if (name.equals("getConnection")) return wrap(raw);
        if (target instanceof Connection) {
            if ((name.equals("setCatalog") || name.equals("setSchema")) && !IntegrationConfig.DATABASE.equals(args[0])) {
                throw new SQLException("Cambio de catálogo bloqueado.");
            }
            if (name.equals("prepareStatement") || name.equals("nativeSQL")) SqlPolicy.check((String) args[0]);
            if ((name.equals("createStatement") && args != null && args.length > 1 && Objects.equals(args[1], ResultSet.CONCUR_UPDATABLE))
                    || (name.equals("prepareStatement") && args.length > 2 && Objects.equals(args[2], ResultSet.CONCUR_UPDATABLE))) {
                throw new SQLException("ResultSet modificable no permitido.");
            }
        }
        if (target instanceof Statement && (name.startsWith("execute") || name.equals("addBatch"))
                && args != null && args.length > 0 && args[0] instanceof String sql) SqlPolicy.check(sql);
        if (target instanceof ResultSet && (name.startsWith("update") || Set.of("insertRow", "deleteRow", "moveToInsertRow").contains(name))) {
            throw new SQLException("Escrituras mediante ResultSet bloqueadas.");
        }
        try {
            Object result = method.invoke(target, args);
            if (result instanceof Connection connection && connection != raw) throw new SQLException("Conexión alternativa no permitida.");
            return wrap(result);
        } catch (InvocationTargetException e) { throw e.getCause(); }
    }
}
