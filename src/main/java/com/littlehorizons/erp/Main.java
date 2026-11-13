package com.littlehorizons.erp;

import com.littlehorizons.erp.util.InputValidator;
import com.littlehorizons.erp.util.EndOfInputException;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.view.ConsoleView;

/**
 * Main application entry point for the Little Horizons ERP educational system.
 * Allows running in Standalone Demo Mode (without SQL) or Interactive Mode (with MySQL).
 */
public class Main {

    public static void main(String[] args) {
        try {
            run(args);
        } catch (EndOfInputException e) {
            System.out.println("\nEntrada de consola finalizada. Sesión cerrada.");
        }
    }

    private static void run(String[] args) {
        LoggerUtil.logInfo("Starting Little Horizons ERP Application...");
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║            COLEGIO INTERNACIONAL LITTLE HORIZONS             ║");
        System.out.println("║        Sistema de Gestión Educativa y Recursos (ERP)         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        System.out.println("Seleccione el modo de ejecución:");
        System.out.println("1. Ejecutar Demostración Completa (Sin SQL / Standalone)");
        System.out.println("2. Iniciar Sistema Interactivo con Base de Datos MySQL");
        System.out.println("3. Salir\n");

        int option = InputValidator.readInt("Seleccione una opción (1-3): ", 1, 3);

        switch (option) {
            case 1 -> {
                DemoMain.main(args);
                InputValidator.pause();
            }
            case 2 -> {
                try {
                    ConsoleView consoleView = new ConsoleView();
                    consoleView.start();
                } catch (EndOfInputException e) {
                    throw e;
                } catch (Exception e) {
                    LoggerUtil.logError("Fatal unhandled exception in console view", e);
                    System.err.println("❌ Ha ocurrido un error inesperado. Los detalles han sido registrados en error_log.txt.");
                }
            }
            case 3 -> System.out.println("Sesión finalizada.");
        }
    }
}
