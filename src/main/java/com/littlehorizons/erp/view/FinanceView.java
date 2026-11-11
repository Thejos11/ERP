package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.FinanceController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.InputValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Console view for financial billing, payments, overdue debt tracking,
 * and automated electronic PDF invoice generation.
 */
public class FinanceView {

    private final FinanceController financeController = new FinanceController();
    private final StudentController studentController = new StudentController();

    public void displayMenu() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║        5. MÓDULO DE GESTIÓN FINANCIERA       ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Catálogo de conceptos de pago (Matrícula, Pensión, etc.)");
            System.out.println("2. Registrar nuevo concepto de pago");
            System.out.println("3. Registrar pago y generar Factura Electrónica PDF (DIAN)");
            System.out.println("4. Consultar historial de pagos por estudiante");
            System.out.println("5. Consultar historial de pagos por acudiente");
            System.out.println("6. Reporte de estudiantes con pagos vencidos / morosos (>30 días)");
            System.out.println("7. Volver al menú principal");

            int option = InputValidator.readInt("\nSeleccione una opción: ", 1, 7);
            switch (option) {
                case 1 -> listConcepts();
                case 2 -> createConcept();
                case 3 -> recordPayment();
                case 4 -> paymentsByStudent();
                case 5 -> paymentsByGuardian();
                case 6 -> overdueDebtorsReport();
                case 7 -> { return; }
            }
        }
    }

    private void listConcepts() {
        System.out.println("\n--- CATÁLOGO DE CONCEPTOS DE PAGO ---");
        List<PaymentConcept> concepts = financeController.getAllPaymentConcepts();
        concepts.forEach(System.out::println);
        InputValidator.pause();
    }

    private void createConcept() {
        System.out.println("\n--- REGISTRO DE NUEVO CONCEPTO DE PAGO ---");
        String code = InputValidator.readNonEmptyString("Código del concepto (ej: SEGURO, GRADUACION): ");
        String name = InputValidator.readNonEmptyString("Nombre del concepto: ");
        String desc = InputValidator.readOptionalString("Descripción detallada: ");
        BigDecimal baseAmount = InputValidator.readPositiveBigDecimal("Monto base en pesos colombianos ($): ");
        boolean isRecurr = InputValidator.readBoolean("¿Es un concepto de cobro mensual recurrente?");

        PaymentConcept c = new PaymentConcept(0, code, name, desc, baseAmount, isRecurr, true);
        if (financeController.createPaymentConcept(c)) {
            System.out.printf("✅ Concepto '%s' creado exitosamente con ID %d.%n", name, c.getId());
        } else {
            System.out.println("❌ Error al crear concepto. Verifique que el código no exista.");
        }
        InputValidator.pause();
    }

    private void recordPayment() {
        System.out.println("\n--- REGISTRO DE PAGO Y FACTURA ELECTRÓNICA ---");
        int studentId = InputValidator.readPositiveInt("ID del Estudiante: ");
        Student s = studentController.findStudentById(studentId);
        if (s == null) {
            System.out.println("❌ Estudiante no encontrado.");
            InputValidator.pause();
            return;
        }

        // Show guardians
        List<Guardian> guardians = studentController.getGuardiansForStudent(studentId);
        Integer guardianId = null;
        if (!guardians.isEmpty()) {
            System.out.println("\nAcudientes disponibles del estudiante:");
            guardians.forEach(g -> System.out.printf("  [%d] %s (%s, Tel: %s)%n", g.getId(), g.getFullName(), g.getRelationship(), g.getMainPhone()));
            guardianId = InputValidator.readPositiveInt("ID del Acudiente responsable del pago: ");
        }

        System.out.println("\nConceptos de Pago:");
        List<PaymentConcept> concepts = financeController.getAllPaymentConcepts();
        concepts.forEach(System.out::println);

        int conceptId = InputValidator.readPositiveInt("\nID del Concepto a pagar: ");
        PaymentConcept selConcept = financeController.getConceptById(conceptId);

        BigDecimal base = (selConcept != null) ? selConcept.getBaseAmount() : BigDecimal.ZERO;
        System.out.printf("Monto base de catálogo: $ %,.2f%n", base.doubleValue());

        boolean customAmount = InputValidator.readBoolean("¿Desea personalizar el monto (ej: descuento de hermanos / beca)?");
        BigDecimal finalAmount;
        if (customAmount) {
            finalAmount = InputValidator.readPositiveBigDecimal("Ingrese el monto acordado a pagar ($): ");
        } else {
            finalAmount = base;
        }

        LocalDate payDate = InputValidator.readDate("Fecha de Pago (AAAA-MM-DD): ");

        System.out.println("Método de Pago:");
        System.out.println("1. Efectivo  2. Transferencia  3. Tarjeta Débito  4. Tarjeta Crédito  5. Cheque");
        int methOpt = InputValidator.readInt("Seleccione método: ", 1, 5);
        String method = switch (methOpt) {
            case 1 -> "Efectivo";
            case 2 -> "Transferencia";
            case 3 -> "Tarjeta Débito";
            case 4 -> "Tarjeta Crédito";
            default -> "Cheque";
        };

        int year = InputValidator.readInt("Año Académico al que aplica: ", 2020, 2035);
        String period = InputValidator.readNonEmptyString("Período (I, II, III, IV, Anual): ");
        String notes = InputValidator.readOptionalString("Notas adicionales: ");

        boolean genPdf = InputValidator.readBoolean("¿Desea generar la Factura Electrónica oficial en PDF con CUFE y QR?");

        Payment p = new Payment(0, studentId, guardianId, conceptId, finalAmount, payDate, method, null, year, period, notes);
        String result = financeController.processPayment(p, genPdf);
        System.out.println("\n" + result);
        InputValidator.pause();
    }

    private void paymentsByStudent() {
        int studentId = InputValidator.readPositiveInt("\nIngrese el ID del Estudiante: ");
        Student s = studentController.findStudentById(studentId);
        if (s == null) {
            System.out.println("❌ Estudiante no encontrado.");
            InputValidator.pause();
            return;
        }

        System.out.printf("%nHistorial de Pagos de: %s (Doc: %s)%n", s.getFullName(), s.getDocumentNumber());
        List<Payment> list = financeController.getPaymentsForStudent(studentId);
        if (list.isEmpty()) {
            System.out.println("  (No hay pagos registrados)");
        } else {
            list.forEach(p -> System.out.println("  - " + p));
        }
        InputValidator.pause();
    }

    private void paymentsByGuardian() {
        int guardianId = InputValidator.readPositiveInt("\nIngrese el ID del Acudiente: ");
        List<Payment> list = financeController.getPaymentsForGuardian(guardianId);
        System.out.printf("%nHistorial consolidado de pagos del Acudiente ID %d:%n", guardianId);
        if (list.isEmpty()) {
            System.out.println("  (No hay pagos registrados para este acudiente)");
        } else {
            list.forEach(p -> System.out.println("  - " + p));
        }
        InputValidator.pause();
    }

    private void overdueDebtorsReport() {
        System.out.println("\n--- REPORTE DE ESTUDIANTES MOROSOS EN PENSIÓN (>30 DÍAS) ---");
        List<String> overdueList = financeController.getOverdueDebtorsReport();
        if (overdueList.isEmpty()) {
            System.out.println("✅ Felicitaciones: No se registran estudiantes morosos de pensión.");
        } else {
            System.out.println("⚠️ Estudiantes con cuotas pendientes por más de un mes:");
            overdueList.forEach(m -> System.out.println("  - " + m));
        }
        InputValidator.pause();
    }
}
