package com.littlehorizons.erp;

import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.view.ConsoleView;

/**
 * Main application entry point for the Little Horizons ERP educational system.
 * Starts the console interface and ensures uncaught exceptions are caught and logged.
 */
public class Main {

    public static void main(String[] args) {
        LoggerUtil.logInfo("Starting Little Horizons ERP Application...");
        try {
            ConsoleView consoleView = new ConsoleView();
            consoleView.start();
        } catch (Exception e) {
            LoggerUtil.logError("Fatal unhandled exception in main execution loop", e);
            System.err.println("❌ Ha ocurrido un error inesperado. Los detalles han sido registrados en error_log.txt.");
        } finally {
            LoggerUtil.logInfo("Little Horizons ERP Application stopped.");
        }
    }
}
