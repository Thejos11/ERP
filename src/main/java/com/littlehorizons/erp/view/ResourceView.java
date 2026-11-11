package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ResourceController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.ResourceItem;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.StudentLoan;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherLoan;
import com.littlehorizons.erp.util.InputValidator;

import java.time.LocalDate;
import java.util.List;

/**
 * Console view for school resource inventory (Library, Labs, Tech, Sports)
 * and borrowing lifecycle management.
 */
public class ResourceView {

    private final ResourceController resourceController = new ResourceController();
    private final StudentController studentController = new StudentController();
    private final TeacherController teacherController = new TeacherController();

    public void displayMenu() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║        6. MÓDULO DE GESTIÓN DE RECURSOS      ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Registrar nuevo recurso en inventario");
            System.out.println("2. Consultar inventario completo de recursos");
            System.out.println("3. Prestar recurso a un estudiante");
            System.out.println("4. Registrar devolución de préstamo de estudiante");
            System.out.println("5. Prestar recurso a un profesor");
            System.out.println("6. Registrar devolución de préstamo de profesor");
            System.out.println("7. Reporte de préstamos vencidos (pendientes de devolución)");
            System.out.println("8. Volver al menú principal");

            int option = InputValidator.readInt("\nSeleccione una opción: ", 1, 8);
            switch (option) {
                case 1 -> registerResource();
                case 2 -> listInventory();
                case 3 -> loanToStudent();
                case 4 -> returnStudentLoan();
                case 5 -> loanToTeacher();
                case 6 -> returnTeacherLoan();
                case 7 -> viewOverdueLoans();
                case 8 -> { return; }
            }
        }
    }

    private void registerResource() {
        System.out.println("\n--- REGISTRO DE RECURSO INSTITUCIONAL ---");
        String code = InputValidator.readNonEmptyString("Código de inventario único (ej: LIB-020, LAB-015): ");
        String name = InputValidator.readNonEmptyString("Nombre del recurso: ");

        System.out.println("\nCategorías de Recursos:");
        List<ResourceCategory> categories = resourceController.getAllCategories();
        categories.forEach(System.out::println);

        int catId = InputValidator.readPositiveInt("\nID de la Categoría: ");
        String desc = InputValidator.readOptionalString("Descripción del recurso: ");
        LocalDate acq = InputValidator.readDate("Fecha de adquisición (AAAA-MM-DD): ");
        String location = InputValidator.readNonEmptyString("Ubicación física (ej: Biblioteca - Estante A1, Lab 2): ");

        ResourceItem r = new ResourceItem(0, code, name, catId, desc, acq, location, "Disponible");
        if (resourceController.createResource(r)) {
            System.out.printf("✅ Recurso '%s' registrado exitosamente con ID %d y estado 'Disponible'.%n", name, r.getId());
        } else {
            System.out.println("❌ Error al registrar recurso. Verifique que el código no exista.");
        }
        InputValidator.pause();
    }

    private void listInventory() {
        System.out.println("\n--- INVENTARIO DE RECURSOS DEL COLEGIO ---");
        List<ResourceItem> list = resourceController.getAllResources();
        if (list.isEmpty()) {
            System.out.println("No hay recursos registrados.");
        } else {
            System.out.printf("%-5s | %-10s | %-40s | %-22s | %-15s | %-12s%n",
                    "ID", "Código", "Nombre del Recurso", "Categoría", "Ubicación", "Estado");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
            list.forEach(r -> System.out.printf("%-5d | %-10s | %-40s | %-22s | %-15s | %-12s%n",
                    r.getId(), r.getResourceCode(), r.getResourceName(), r.getCategoryName(), r.getPhysicalLocation(), r.getStatus()));
            System.out.printf("%nTotal recursos en inventario: %d%n", list.size());
        }
        InputValidator.pause();
    }

    private void loanToStudent() {
        System.out.println("\n--- PRESTAR RECURSO A ESTUDIANTE ---");
        int resId = InputValidator.readPositiveInt("ID del Recurso a prestar: ");
        int studentId = InputValidator.readPositiveInt("ID del Estudiante: ");
        Student s = studentController.findStudentById(studentId);
        if (s == null) {
            System.out.println("❌ Estudiante no encontrado.");
            InputValidator.pause();
            return;
        }

        LocalDate loanDate = InputValidator.readDate("Fecha de Préstamo (AAAA-MM-DD): ");
        LocalDate returnExpected = InputValidator.readDate("Fecha esperada de devolución (AAAA-MM-DD): ");
        String obs = InputValidator.readOptionalString("Observaciones del préstamo: ");

        String msg = resourceController.loanToStudent(resId, studentId, loanDate, returnExpected, obs);
        System.out.println(msg);
        InputValidator.pause();
    }

    private void returnStudentLoan() {
        System.out.println("\n--- DEVOLUCIÓN DE PRÉSTAMO DE ESTUDIANTE ---");
        List<StudentLoan> activeLoans = resourceController.getActiveStudentLoans();
        if (activeLoans.isEmpty()) {
            System.out.println("No hay préstamos activos de estudiantes.");
            InputValidator.pause();
            return;
        }

        System.out.println("Préstamos activos de estudiantes:");
        activeLoans.forEach(System.out::println);

        int loanId = InputValidator.readPositiveInt("\nID del Préstamo a devolver: ");
        StudentLoan sel = activeLoans.stream().filter(l -> l.getId() == loanId).findFirst().orElse(null);
        if (sel == null) {
            System.out.println("❌ ID de préstamo no encontrado entre los activos.");
            InputValidator.pause();
            return;
        }

        LocalDate retDate = InputValidator.readDate("Fecha real de devolución (AAAA-MM-DD): ");
        String obs = InputValidator.readOptionalString("Estado del recurso al devolver: ");

        String msg = resourceController.returnStudentLoan(loanId, sel.getResourceId(), retDate, obs);
        System.out.println(msg);
        InputValidator.pause();
    }

    private void loanToTeacher() {
        System.out.println("\n--- PRESTAR RECURSO A PROFESOR ---");
        int resId = InputValidator.readPositiveInt("ID del Recurso a prestar: ");
        int teacherId = InputValidator.readPositiveInt("ID del Profesor: ");
        Teacher t = teacherController.findTeacherById(teacherId);
        if (t == null) {
            System.out.println("❌ Profesor no encontrado.");
            InputValidator.pause();
            return;
        }

        LocalDate loanDate = InputValidator.readDate("Fecha de Préstamo (AAAA-MM-DD): ");
        LocalDate returnExpected = InputValidator.readDate("Fecha esperada de devolución (AAAA-MM-DD): ");
        String obs = InputValidator.readOptionalString("Observaciones del préstamo: ");

        String msg = resourceController.loanToTeacher(resId, teacherId, loanDate, returnExpected, obs);
        System.out.println(msg);
        InputValidator.pause();
    }

    private void returnTeacherLoan() {
        System.out.println("\n--- DEVOLUCIÓN DE PRÉSTAMO DE PROFESOR ---");
        List<TeacherLoan> activeLoans = resourceController.getActiveTeacherLoans();
        if (activeLoans.isEmpty()) {
            System.out.println("No hay préstamos activos de profesores.");
            InputValidator.pause();
            return;
        }

        System.out.println("Préstamos activos de profesores:");
        activeLoans.forEach(System.out::println);

        int loanId = InputValidator.readPositiveInt("\nID del Préstamo a devolver: ");
        TeacherLoan sel = activeLoans.stream().filter(l -> l.getId() == loanId).findFirst().orElse(null);
        if (sel == null) {
            System.out.println("❌ ID de préstamo no encontrado entre los activos.");
            InputValidator.pause();
            return;
        }

        LocalDate retDate = InputValidator.readDate("Fecha real de devolución (AAAA-MM-DD): ");
        String obs = InputValidator.readOptionalString("Estado del recurso al devolver: ");

        String msg = resourceController.returnTeacherLoan(loanId, sel.getResourceId(), retDate, obs);
        System.out.println(msg);
        InputValidator.pause();
    }

    private void viewOverdueLoans() {
        System.out.println("\n--- REPORTE DE PRÉSTAMOS VENCIDOS ---");
        List<StudentLoan> overdueStudents = resourceController.getOverdueStudentLoans();
        List<TeacherLoan> overdueTeachers = resourceController.getOverdueTeacherLoans();

        System.out.println("Préstamos Vencidos - Estudiantes:");
        if (overdueStudents.isEmpty()) {
            System.out.println("  (Ninguno)");
        } else {
            overdueStudents.forEach(l -> System.out.println("  ⚠️ " + l));
        }

        System.out.println("\nPréstamos Vencidos - Profesores:");
        if (overdueTeachers.isEmpty()) {
            System.out.println("  (Ninguno)");
        } else {
            overdueTeachers.forEach(l -> System.out.println("  ⚠️ " + l));
        }
        InputValidator.pause();
    }
}
