package com.littlehorizons.erp.view;

import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.InputValidator;

/**
 * Main application console view orchestrating navigation through all ERP modules.
 */
public class ConsoleView {

    private final StudentView studentView = new StudentView();
    private final AcademicView academicView = new AcademicView();
    private final TeacherView teacherView = new TeacherView();
    private final ExtracurricularView extracurricularView = new ExtracurricularView();
    private final FinanceView financeView = new FinanceView();
    private final ResourceView resourceView = new ResourceView();
    private final ReportsView reportsView = new ReportsView();

    public void start() {
        printWelcomeBanner();
        checkDatabaseStatus();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = InputValidator.readInt("Seleccione una opción: ", 1, 9);
            switch (choice) {
                case 1 -> studentView.displayMenu();
                case 2 -> academicView.displayMenu();
                case 3 -> teacherView.displayMenu();
                case 4 -> extracurricularView.displayMenu();
                case 5 -> financeView.displayMenu();
                case 6 -> resourceView.displayMenu();
                case 7 -> reportsView.displayMenu();
                case 8 -> configureDatabase();
                case 9 -> {
                    System.out.println("\n╔══════════════════════════════════════════════╗");
                    System.out.println("║  ¡Gracias por utilizar Little Horizons ERP!  ║");
                    System.out.println("║          Sesión finalizada con éxito.        ║");
                    System.out.println("╚══════════════════════════════════════════════╝\n");
                    running = false;
                }
            }
        }
    }

    private void printWelcomeBanner() {
        System.out.println();
        System.out.println("****************************************************************");
        System.out.println("*            COLEGIO INTERNACIONAL LITTLE HORIZONS             *");
        System.out.println("*     Sistema de Planificación de Recursos Empresariales (ERP) *");
        System.out.println("*                     Versión 2.0 LTS (2026)                   *");
        System.out.println("****************************************************************");
    }

    private void checkDatabaseStatus() {
        if (DatabaseConnection.isInMemoryMode()) {
            System.out.println("🟣 [MODO STANDALONE] Ejecutando 100% En Memoria (Sin SQL requerido).");
            return;
        }
        boolean connected = DatabaseConnection.getInstance().testConnection();
        if (connected) {
            System.out.println("🟢 [ESTADO] Conexión a la base de datos MySQL establecida correctamente.");
        } else {
            System.out.println("🟣 [MODO STANDALONE ACTIVO]");
            System.out.println("   No se detectó servidor MySQL activo. El sistema se ejecutará");
            System.out.println("   automáticamente en Modo En Memoria (Sin SQL). Todos los datos,");
            System.out.println("   validaciones y generación de facturas PDF funcionan al 100%.");
            DatabaseConnection.setInMemoryMode(true);
        }
    }

    private void printMainMenu() {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║   ERP Colegio Little Horizons                ║");
        System.out.println("║   Sistema de Gestión Educativa               ║");
        System.out.println("╚══════════════════════════════════════════════╝\n");
        System.out.println("1. Gestión de Estudiantes");
        System.out.println("2. Gestión Académica");
        System.out.println("3. Gestión de Profesores");
        System.out.println("4. Actividades Extracurriculares");
        System.out.println("5. Gestión Financiera");
        System.out.println("6. Gestión de Recursos");
        System.out.println("7. Reportes y Estadísticas");
        System.out.println("8. Configurar Conexión a Base de Datos");
        System.out.println("9. Salir\n");
    }

    private void configureDatabase() {
        System.out.println("\n--- CONFIGURACIÓN DE CONEXIÓN A BASE DE DATOS ---");
        System.out.println("Configuración actual:");
        System.out.println("URL: " + DatabaseConnection.getDbUrl());
        System.out.println("Usuario: " + DatabaseConnection.getDbUser());
        System.out.println("-------------------------------------------------");

        String url = InputValidator.readNonEmptyString("Nueva URL JDBC (o enter el default actual): ");
        String user = InputValidator.readNonEmptyString("Usuario MySQL: ");
        String password = InputValidator.readOptionalString("Contraseña MySQL (dejar vacío si no tiene): ");
        if (password == null) password = "";

        DatabaseConnection.setCredentials(url, user, password);
        if (DatabaseConnection.getInstance().testConnection()) {
            System.out.println("🟢 ¡Conexión exitosa a la base de datos!");
        } else {
            System.out.println("❌ No se pudo conectar con las nuevas credenciales. Revise que MySQL esté ejecutándose.");
        }
        InputValidator.pause();
    }
}
