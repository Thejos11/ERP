package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ReportsController;
import com.littlehorizons.erp.util.InputValidator;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Console view for institutional intelligence, rankings, and statistical reports.
 */
public class ReportsView {

    private final ReportsController reportsController = new ReportsController();

    public void displayMenu() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║        7. REPORTES Y ESTADÍSTICAS DEL ERP    ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Cuadro de Honor / Ranking de mejores estudiantes (Stream API)");
            System.out.println("2. Estadísticas de ocupación de clubes extracurriculares");
            System.out.println("3. Resumen y distribución de inventario de recursos");
            System.out.println("4. Consolidado financiero de recaudos de la institución");
            System.out.println("5. Volver al menú principal");

            int option = InputValidator.readInt("\nSeleccione una opción: ", 1, 5);
            switch (option) {
                case 1 -> viewRanking();
                case 2 -> viewClubOccupancy();
                case 3 -> viewResourceSummary();
                case 4 -> viewFinancialSummary();
                case 5 -> { return; }
            }
        }
    }

    private void viewRanking() {
        System.out.println("\n--- CUADRO DE HONOR Y RANKING ESTUDIANTIL ---");
        int year = InputValidator.readInt("Año Académico: ", 2020, 2035);
        String period = InputValidator.readNonEmptyString("Período Académico (I, II, III, IV): ").toUpperCase();
        int topN = InputValidator.readInt("Número de posiciones a listar (Top N, ej: 10): ", 1, 50);

        List<String> ranking = reportsController.getTopStudentsRanking(year, period, topN);
        if (ranking.isEmpty()) {
            System.out.println("No se registran notas para el año y período seleccionados.");
        } else {
            System.out.printf("%n🏆 TOP %d ESTUDIANTES DESTACADOS (Año %d - Periodo %s):%n", topN, year, period);
            ranking.forEach(System.out::println);
        }
        InputValidator.pause();
    }

    private void viewClubOccupancy() {
        System.out.println("\n--- OCUPACIÓN DE CLUBES EXTRACURRICULARES ---");
        List<String> summary = reportsController.getClubOccupancySummary();
        summary.forEach(System.out::println);
        InputValidator.pause();
    }

    private void viewResourceSummary() {
        System.out.println("\n--- DISTRIBUCIÓN DE INVENTARIO DE RECURSOS ---");
        Map<String, Long> summary = reportsController.getResourceInventorySummary();
        if (summary.isEmpty()) {
            System.out.println("No hay recursos en la base de datos.");
        } else {
            System.out.println("Recursos clasificados por estado operativo:");
            summary.forEach((status, count) -> System.out.printf("  * %-20s : %d unidades%n", status, count));
        }
        InputValidator.pause();
    }

    private void viewFinancialSummary() {
        System.out.println("\n--- CONSOLIDADO FINANCIERO INSTITUCIONAL ---");
        BigDecimal total = reportsController.calculateTotalCollections();
        System.out.println("=================================================");
        System.out.printf("💰 TOTAL HISTÓRICO DE RECAUDO EN CAJA: $ %,.2f COP%n", total.doubleValue());
        System.out.println("=================================================");
        InputValidator.pause();
    }
}
