package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AcademicController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.util.InputValidator;

import java.time.LocalDate;
import java.util.List;

/**
 * Console view for teacher human resources and teaching load assignments.
 */
public class TeacherView {

    private final TeacherController teacherController = new TeacherController();
    private final AcademicController academicController = new AcademicController();

    public void displayMenu() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║        3. MÓDULO DE GESTIÓN DE PROFESORES    ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Registrar nuevo profesor");
            System.out.println("2. Asignar materia y sección a un profesor");
            System.out.println("3. Consultar carga académica de un profesor");
            System.out.println("4. Directorio completo de profesores");
            System.out.println("5. Volver al menú principal");

            int option = InputValidator.readInt("\nSeleccione una opción: ", 1, 5);
            switch (option) {
                case 1 -> registerTeacher();
                case 2 -> assignSubject();
                case 3 -> viewTeacherSchedule();
                case 4 -> listTeachers();
                case 5 -> { return; }
            }
        }
    }

    private void registerTeacher() {
        System.out.println("\n--- REGISTRO DE NUEVO PROFESOR ---");
        String docType = InputValidator.readNonEmptyString("Tipo de Documento (CC, CE, Pasaporte): ");
        String docNum = InputValidator.readNonEmptyString("Número de Documento: ");
        String firstName = InputValidator.readNonEmptyString("Primer Nombre: ");
        String secondName = InputValidator.readOptionalString("Segundo Nombre (opcional): ");
        String lastName = InputValidator.readNonEmptyString("Primer Apellido: ");
        String secondLastName = InputValidator.readOptionalString("Segundo Apellido (opcional): ");
        String email = InputValidator.readNonEmptyString("Correo Electrónico Institucional: ");
        String phone = InputValidator.readOptionalString("Teléfono: ");
        String spec = InputValidator.readNonEmptyString("Área de Especialización (ej: Matemáticas, Ciencias): ");
        LocalDate hireDate = InputValidator.readDate("Fecha de Contratación (AAAA-MM-DD): ");

        Teacher t = new Teacher(0, docType, docNum, firstName, secondName, lastName, secondLastName,
                email, phone, spec, hireDate, "Activo");

        if (teacherController.registerTeacher(t)) {
            System.out.printf("✅ Profesor %s registrado exitosamente con ID %d.%n", t.getFullName(), t.getId());
        } else {
            System.out.println("❌ Error al registrar profesor. Verifique que documento y correo sean únicos.");
        }
        InputValidator.pause();
    }

    private void assignSubject() {
        System.out.println("\n--- ASIGNACIÓN DE MATERIA A PROFESOR ---");
        int teacherId = InputValidator.readPositiveInt("ID del Profesor: ");
        Teacher t = teacherController.findTeacherById(teacherId);
        if (t == null) {
            System.out.println("❌ Profesor no encontrado.");
            InputValidator.pause();
            return;
        }

        System.out.println("\nMaterias Disponibles:");
        List<Subject> subjects = academicController.getAllSubjects();
        subjects.forEach(System.out::println);

        int subjectId = InputValidator.readPositiveInt("\nID de la Materia a dictar: ");
        int year = InputValidator.readInt("Año Académico (ej: 2026): ", 2020, 2035);
        String section = InputValidator.readNonEmptyString("Sección o Grupo (A, B, C): ").toUpperCase();

        if (teacherController.assignSubjectToTeacher(teacherId, subjectId, year, section)) {
            System.out.printf("✅ Asignación completada para el Prof. %s en la sección %s.%n", t.getFullName(), section);
        } else {
            System.out.println("❌ Error en la asignación o ya se encuentra asignado a esa misma materia y sección.");
        }
        InputValidator.pause();
    }

    private void viewTeacherSchedule() {
        System.out.println("\n--- CONSULTAR CARGA ACADÉMICA DE PROFESOR ---");
        int teacherId = InputValidator.readPositiveInt("ID del Profesor: ");
        Teacher t = teacherController.findTeacherById(teacherId);
        if (t == null) {
            System.out.println("❌ Profesor no encontrado.");
            InputValidator.pause();
            return;
        }

        int year = InputValidator.readInt("Año Académico a consultar: ", 2020, 2035);
        List<TeacherAssignment> list = teacherController.getAssignmentsForTeacher(teacherId, year);
        System.out.printf("%nAsignaciones de %s para el año %d:%n", t.getFullName(), year);
        if (list.isEmpty()) {
            System.out.println("  (Sin asignaciones registradas para este año)");
        } else {
            list.forEach(a -> System.out.println("  - " + a));
        }
        InputValidator.pause();
    }

    private void listTeachers() {
        System.out.println("\n--- DIRECTORIO DE PROFESORES ---");
        List<Teacher> list = teacherController.getAllTeachers();
        if (list.isEmpty()) {
            System.out.println("No hay profesores registrados.");
        } else {
            System.out.printf("%-5s | %-12s | %-30s | %-30s | %-15s%n", "ID", "Documento", "Nombre Completo", "Especialización", "Teléfono");
            System.out.println("------------------------------------------------------------------------------------------------------");
            list.forEach(t -> System.out.printf("%-5d | %-12s | %-30s | %-30s | %-15s%n",
                    t.getId(), t.getDocumentNumber(), t.getFullName(), t.getSpecialization(), t.getPhone()));
            System.out.printf("%nTotal profesores: %d%n", list.size());
        }
        InputValidator.pause();
    }
}
