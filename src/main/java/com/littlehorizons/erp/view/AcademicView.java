package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AcademicController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.util.InputValidator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Console view for academic management, subjects, grades, averages, and attendance.
 */
public class AcademicView {

    private final AcademicController academicController = new AcademicController();
    private final StudentController studentController = new StudentController();

    public void displayMenu() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║        2. MÓDULO DE GESTIÓN ACADÉMICA        ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Registrar nueva materia");
            System.out.println("2. Inscribir estudiante en materia");
            System.out.println("3. Registrar calificación de estudiante (0.0 - 5.0)");
            System.out.println("4. Consultar historial académico de notas de estudiante");
            System.out.println("5. Calcular promedios automáticos (Materia, Período, Curso)");
            System.out.println("6. Registrar asistencia diaria de estudiante");
            System.out.println("7. Reporte de alertas por inasistencias (>3 ausencias)");
            System.out.println("8. Volver al menú principal");

            int option = InputValidator.readInt("\nSeleccione una opción: ", 1, 8);
            switch (option) {
                case 1 -> registerSubject();
                case 2 -> enrollSubject();
                case 3 -> recordGrade();
                case 4 -> viewAcademicHistory();
                case 5 -> viewAverages();
                case 6 -> markAttendance();
                case 7 -> viewAbsenceAlerts();
                case 8 -> { return; }
            }
        }
    }

    private void registerSubject() {
        System.out.println("\n--- REGISTRO DE NUEVA MATERIA ---");
        String code = InputValidator.readNonEmptyString("Código de la materia (ej: MAT-7, ESP-3): ");
        String name = InputValidator.readNonEmptyString("Nombre de la materia: ");
        String desc = InputValidator.readOptionalString("Descripción de la materia: ");

        System.out.println("\nGrados Disponibles:");
        List<GradeLevel> grades = studentController.getAllGradeLevels();
        grades.forEach(System.out::println);
        int gradeId = InputValidator.readPositiveInt("ID del Grado correspondiente: ");
        int hours = InputValidator.readInt("Horas semanales asignadas: ", 1, 20);

        Subject sub = new Subject(0, code, name, desc, gradeId, hours, true);
        if (academicController.registerSubject(sub)) {
            System.out.printf("✅ Materia '%s' (%s) creada exitosamente con ID %d.%n", name, code, sub.getId());
        } else {
            System.out.println("❌ Error al crear la materia. Verifique que el código no exista.");
        }
        InputValidator.pause();
    }

    private void enrollSubject() {
        System.out.println("\n--- INSCRIBIR ESTUDIANTE EN MATERIA ---");
        int studentId = InputValidator.readPositiveInt("ID del Estudiante: ");
        Student s = studentController.findStudentById(studentId);
        if (s == null) {
            System.out.println("❌ Estudiante no encontrado.");
            InputValidator.pause();
            return;
        }

        System.out.println("\nMaterias Disponibles:");
        List<Subject> subjects = academicController.getAllSubjects();
        subjects.forEach(System.out::println);

        int subjectId = InputValidator.readPositiveInt("\nID de la Materia: ");
        int year = InputValidator.readInt("Año Académico (ej: 2026): ", 2020, 2035);
        String period = InputValidator.readNonEmptyString("Período Académico (I, II, III, IV): ").toUpperCase();
        String section = InputValidator.readNonEmptyString("Sección / Grupo (ej: A, B): ").toUpperCase();

        if (academicController.enrollStudentInSubject(studentId, subjectId, year, period, section)) {
            System.out.printf("✅ Estudiante %s inscrito exitosamente en la materia.%n", s.getFullName());
        } else {
            System.out.println("❌ Error en la inscripción o ya se encuentra inscrito en ese período.");
        }
        InputValidator.pause();
    }

    private void recordGrade() {
        System.out.println("\n--- REGISTRO DE CALIFICACIONES ---");
        int studentId = InputValidator.readPositiveInt("ID del Estudiante: ");
        Student s = studentController.findStudentById(studentId);
        if (s == null) {
            System.out.println("❌ Estudiante no encontrado.");
            InputValidator.pause();
            return;
        }

        int subjectId = InputValidator.readPositiveInt("ID de la Materia: ");
        int teacherId = InputValidator.readPositiveInt("ID del Profesor Calificador: ");
        int year = InputValidator.readInt("Año Académico (ej: 2026): ", 2020, 2035);
        String period = InputValidator.readNonEmptyString("Período Académico (I, II, III, IV, Final): ");
        BigDecimal grade = InputValidator.readAcademicGrade("Nota numérica (0.0 a 5.0 con 1 decimal): ");
        String obs = InputValidator.readOptionalString("Observaciones cualitativas de la nota: ");

        AcademicRecord record = new AcademicRecord(0, studentId, subjectId, teacherId, year, period, grade, obs, LocalDate.now());
        if (academicController.recordGrade(record)) {
            System.out.printf("✅ Calificación de %.1f registrada correctamente para %s.%n", grade.doubleValue(), s.getFullName());
        } else {
            System.out.println("❌ Error al guardar la nota.");
        }
        InputValidator.pause();
    }

    private void viewAcademicHistory() {
        System.out.println("\n--- HISTORIAL ACADÉMICO DEL ESTUDIANTE ---");
        int studentId = InputValidator.readPositiveInt("ID del Estudiante: ");
        Student s = studentController.findStudentById(studentId);
        if (s == null) {
            System.out.println("❌ Estudiante no encontrado.");
            InputValidator.pause();
            return;
        }

        System.out.printf("Historial Completo de: %s (Doc: %s)%n", s.getFullName(), s.getDocumentNumber());
        List<AcademicRecord> history = academicController.getStudentAcademicHistory(studentId);
        if (history.isEmpty()) {
            System.out.println("  (No registra calificaciones en el sistema)");
        } else {
            System.out.println("---------------------------------------------------------------------------------------------------");
            history.forEach(System.out::println);
            System.out.println("---------------------------------------------------------------------------------------------------");

            // Display subject averages summary using Streams
            Map<String, Double> subjectAvg = academicController.getSubjectAveragesSummary(studentId);
            System.out.println("\nPromedios Acumulados por Asignatura:");
            subjectAvg.forEach((sub, avg) -> System.out.printf("  * %-30s : %.1f%n", sub, avg));
        }
        InputValidator.pause();
    }

    private void viewAverages() {
        System.out.println("\n--- CÁLCULO AUTOMÁTICO DE PROMEDIOS ---");
        System.out.println("1. Promedio de un estudiante en una materia específica");
        System.out.println("2. Promedio general de un estudiante en un período");
        System.out.println("3. Promedio general del curso en una materia");

        int type = InputValidator.readInt("Seleccione cálculo: ", 1, 3);
        int year = InputValidator.readInt("Año Académico: ", 2020, 2035);

        switch (type) {
            case 1 -> {
                int studentId = InputValidator.readPositiveInt("ID del Estudiante: ");
                int subjectId = InputValidator.readPositiveInt("ID de la Materia: ");
                double avg = academicController.calculateSubjectAverage(studentId, subjectId, year);
                System.out.printf("📊 Promedio acumulado del estudiante en la materia: %.1f%n", avg);
            }
            case 2 -> {
                int studentId = InputValidator.readPositiveInt("ID del Estudiante: ");
                String period = InputValidator.readNonEmptyString("Período (I, II, III, IV): ").toUpperCase();
                double avg = academicController.calculateStudentPeriodAverage(studentId, year, period);
                System.out.printf("📊 Promedio general del estudiante en el Periodo %s: %.1f%n", period, avg);
            }
            case 3 -> {
                int subjectId = InputValidator.readPositiveInt("ID de la Materia: ");
                String period = InputValidator.readNonEmptyString("Período (I, II, III, IV): ").toUpperCase();
                double avg = academicController.calculateCourseAverageBySubject(subjectId, year, period);
                System.out.printf("📊 Promedio general del curso en la materia (Periodo %s): %.1f%n", period, avg);
            }
        }
        InputValidator.pause();
    }

    private void markAttendance() {
        System.out.println("\n--- REGISTRO DE ASISTENCIA DIARIA ---");
        int studentId = InputValidator.readPositiveInt("ID del Estudiante: ");
        int subjectId = InputValidator.readPositiveInt("ID de la Materia: ");
        LocalDate date = InputValidator.readDate("Fecha de Asistencia (AAAA-MM-DD): ");

        System.out.println("Estados de Asistencia:");
        System.out.println("1. Presente");
        System.out.println("2. Ausente");
        System.out.println("3. Tardanza");
        System.out.println("4. Excusa Médica");
        int statusOpt = InputValidator.readInt("Seleccione estado: ", 1, 4);
        String status = switch (statusOpt) {
            case 1 -> "Presente";
            case 2 -> "Ausente";
            case 3 -> "Tardanza";
            default -> "Excusa Médica";
        };

        String obs = InputValidator.readOptionalString("Observaciones: ");
        int teacherId = InputValidator.readPositiveInt("ID del Profesor que registra: ");

        Attendance att = new Attendance(0, studentId, subjectId, date, status, obs, teacherId);
        if (academicController.recordAttendance(att)) {
            System.out.printf("✅ Asistencia marcada como '%s' para el %s.%n", status, date);
        } else {
            System.out.println("❌ Error al registrar asistencia.");
        }
        InputValidator.pause();
    }

    private void viewAbsenceAlerts() {
        System.out.println("\n--- REPORTE DE PATRONES DE INASISTENCIA CRÍTICA ---");
        LocalDate start = InputValidator.readDate("Fecha inicial del rango (AAAA-MM-DD): ");
        LocalDate end = InputValidator.readDate("Fecha final del rango (AAAA-MM-DD): ");
        int threshold = InputValidator.readInt("Número mínimo de ausencias para alertar (ej: 3): ", 1, 50);

        List<String> report = academicController.getAbsencePatternReport(start, end, threshold);
        if (report.isEmpty()) {
            System.out.println("✅ No se detectaron estudiantes con inasistencias superiores al límite.");
        } else {
            System.out.println("\n⚠️ ESTUDIANTES CON INASISTENCIAS PREOCUPANTES:");
            report.forEach(r -> System.out.println("  " + r));
        }
        InputValidator.pause();
    }
}
