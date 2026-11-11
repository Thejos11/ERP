package com.littlehorizons.erp;

import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerUtil;

/**
 * Entry point for the Little Horizons ERP System.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   Little Horizons ERP - Educational System       ");
        System.out.println("==================================================");
        
        LoggerUtil.logInfo("System startup initiated.");

        DatabaseConnection dbConn = DatabaseConnection.getInstance();
        System.out.println("Connecting using URL: " + dbConn.getDbUrl());
        System.out.println("Connecting using User: " + dbConn.getDbUser());

        boolean isConnected = dbConn.testConnection();
        if (isConnected) {
            System.out.println("✅ Database connection successful!");
            LoggerUtil.logInfo("Database connection established successfully.");
        } else {
            System.out.println("⚠️ Warning: Could not connect to the database.");
            System.out.println("Please check your db.properties file or make sure MySQL is running.");
            LoggerUtil.logWarning("Initial database connection test failed.");
        }
    }
}
