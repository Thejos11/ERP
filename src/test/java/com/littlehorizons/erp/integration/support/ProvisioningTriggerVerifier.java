package com.littlehorizons.erp.integration.support;

import java.sql.*;
import java.util.Properties;

/** Separate privileged identity, used for read-only visibility checks before runner access. */
public final class ProvisioningTriggerVerifier implements GuardedTestDriver.TriggerVerifier {
    private final IntegrationConfig provisioning;
    private final Driver delegate;

    public ProvisioningTriggerVerifier(IntegrationConfig provisioning, Driver delegate) {
        this.provisioning = provisioning; this.delegate = delegate;
    }

    @Override public void verify(IntegrationConfig runner) throws SQLException {
        if (!runner.url().equals(provisioning.url()) || runner.user().equalsIgnoreCase(provisioning.user())) {
            throw new SQLException("La verificación exige otra identidad y el mismo destino exacto.");
        }
        Properties props = new Properties();
        props.setProperty("user", provisioning.user()); props.setProperty("password", provisioning.password());
        props.setProperty("connectTimeout", "5000"); props.setProperty("socketTimeout", "10000");
        props.setProperty("allowMultiQueries", "false"); props.setProperty("allowLoadLocalInfile", "false");
        try (Connection connection = delegate.connect(provisioning.url(), props)) {
            TestDatabaseSafety.checkCatalog(connection);
            connection.setReadOnly(true);
            TestDatabaseSafety.verifyTriggerAbsence(connection, provisioning);
        } catch (SQLException e) {
            throw new SQLException("No se pudo demostrar visibilidad y ausencia de triggers con la identidad de aprovisionamiento.");
        }
    }
}
