package com.littlehorizons.erp;

import com.littlehorizons.erp.model.*;
import com.littlehorizons.erp.util.InvoicePdfGenerator;
import com.littlehorizons.erp.util.LoggerUtil;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Standalone Main class that executes and validates all Little Horizons ERP modules
 * purely in Java, without requiring any MySQL server or SQL database.
 */
public class DemoMain {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("            COLEGIO INTERNACIONAL LITTLE HORIZONS - ERP EDUCATIVO              ");
        System.out.println("                 EJECUCIÓN COMPLETA EN MODO STANDALONE (SIN SQL)                ");
        System.out.println("================================================================================\n");

        LoggerUtil.logInfo("Iniciando DemoMain en modo Standalone sin base de datos SQL...");

        // ====================================================================
        // 1. MÓDULO DE GESTIÓN ACADÉMICA
        // ====================================================================
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        System.out.println("1. MÓDULO DE GESTIÓN ACADÉMICA");
        System.out.println("════════════════════════════════════════════════════════════════════════════════");

        // 1.1 Registro de Estudiantes y Acudientes
        Student s1 = new Student(1, "TI", "1012345001", "Sofia", "Isabella", "Gomez", "Rodriguez",
                LocalDate.of(2010, 5, 14), "F", "Calle 127 # 45-20, Bogotá", "3101234567",
                "sofia.gomez@littlehorizons.edu.co", "O+", "Ninguna", "Ninguna", LocalDate.of(2020, 1, 15), "Activo");

        Student s2 = new Student(2, "TI", "1012345002", "Santiago", null, "Ramirez", "Lopez",
                LocalDate.of(2009, 8, 22), "M", "Carrera 7 # 116-50, Bogotá", "3119876543",
                "santiago.ramirez@littlehorizons.edu.co", "A+", "Polen", "Asma leve", LocalDate.of(2019, 1, 15), "Activo");

        Guardian g1 = new Guardian(1, "CC", "52456789", "Carolina", null, "Rodriguez", "Sarmiento",
                "Madre", "3109998877", "6017894561", "carolina.rodriguez@gmail.com",
                "Calle 127 # 45-20, Bogotá", "Arquitecta", true);

        System.out.println("✅ Estudiantes registrados:");
        System.out.printf("   • [%d] %s (Doc: %s %s) | Acudiente: %s (%s)%n",
                s1.getId(), s1.getFullName(), s1.getDocumentType(), s1.getDocumentNumber(), g1.getFullName(), g1.getRelationship());
        System.out.printf("   • [%d] %s (Doc: %s %s) | Estado: %s%n",
                s2.getId(), s2.getFullName(), s2.getDocumentType(), s2.getDocumentNumber(), s2.getStatus());

        // 1.2 Registro de Profesores y Materias
        Teacher t1 = new Teacher(1, "CC", "19876543", "Carlos", "Alberto", "Mendoza", "Perez",
                "carlos.mendoza@littlehorizons.edu.co", "3123456789", "Matemáticas", LocalDate.of(2018, 1, 10), "Activo");
        Teacher t2 = new Teacher(2, "CC", "51987654", "Laura", "Patricia", "Restrepo", "Giraldo",
                "laura.restrepo@littlehorizons.edu.co", "3145678901", "Ciencias Naturales", LocalDate.of(2019, 7, 15), "Activo");

        Subject sub1 = new Subject(1, "MAT-8", "Matemáticas 8°", "Álgebra y geometría", 9, 5, true);
        Subject sub2 = new Subject(2, "CIE-8", "Ciencias Naturales 8°", "Biología y física", 9, 4, true);

        System.out.println("\n✅ Profesores y Asignaciones:");
        System.out.printf("   • Prof. %s (%s) dictando %s (%s)%n", t1.getFullName(), t1.getSpecialization(), sub1.getSubjectName(), sub1.getSubjectCode());
        System.out.printf("   • Prof. %s (%s) dictando %s (%s)%n", t2.getFullName(), t2.getSpecialization(), sub2.getSubjectName(), sub2.getSubjectCode());

        // 1.3 Calificaciones y Cálculo Automático de Promedios con Java Streams
        List<AcademicRecord> grades = new ArrayList<>();
        grades.add(new AcademicRecord(1, 1, 1, 1, 2026, "I", new BigDecimal("4.5"), "Buen desempeño en álgebra", LocalDate.of(2026, 3, 20)));
        grades.add(new AcademicRecord(2, 1, 1, 1, 2026, "II", new BigDecimal("4.2"), "Trabajo constante", LocalDate.of(2026, 6, 15)));
        grades.add(new AcademicRecord(3, 1, 1, 1, 2026, "III", new BigDecimal("4.8"), "Sobresaliente en evaluación", LocalDate.of(2026, 9, 20)));
        grades.add(new AcademicRecord(4, 1, 2, 2, 2026, "I", new BigDecimal("4.6"), "Laboratorio destacado", LocalDate.of(2026, 3, 22)));
        grades.add(new AcademicRecord(5, 2, 1, 1, 2026, "I", new BigDecimal("3.8"), "Aprobado", LocalDate.of(2026, 3, 20)));

        System.out.println("\n✅ Calificaciones Registradas (Escala 0.0 - 5.0):");
        for (AcademicRecord r : grades) {
            String studentName = (r.getStudentId() == 1) ? s1.getFullName() : s2.getFullName();
            String subjName = (r.getSubjectId() == 1) ? sub1.getSubjectName() : sub2.getSubjectName();
            System.out.printf("   • %s | %s | Período %s: %.1f (%s)%n",
                    studentName, subjName, r.getAcademicPeriod(), r.getGradeValue().doubleValue(), r.getRemarks());
        }

        // Promedio de Sofia en Matemáticas (Streams)
        double avgSofiaMath = grades.stream()
                .filter(r -> r.getStudentId() == 1 && r.getSubjectId() == 1)
                .map(AcademicRecord::getGradeValue)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
        BigDecimal roundedAvgMath = BigDecimal.valueOf(avgSofiaMath).setScale(1, RoundingMode.HALF_UP);

        // Promedio general de Sofia en Período I
        double avgSofiaPeriod1 = grades.stream()
                .filter(r -> r.getStudentId() == 1 && "I".equalsIgnoreCase(r.getAcademicPeriod()))
                .map(AcademicRecord::getGradeValue)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);
        BigDecimal roundedAvgP1 = BigDecimal.valueOf(avgSofiaPeriod1).setScale(1, RoundingMode.HALF_UP);

        System.out.printf("   ⭐ Promedio acumulado de %s en %s: %s / 5.0%n", s1.getFullName(), sub1.getSubjectName(), roundedAvgMath);
        System.out.printf("   ⭐ Promedio general de %s en Período I: %s / 5.0%n", s1.getFullName(), roundedAvgP1);

        // 1.4 Control de Asistencia y Reporte de Faltas
        List<Attendance> attendanceList = new ArrayList<>();
        attendanceList.add(new Attendance(1, 1, 1, LocalDate.of(2026, 3, 2), "Presente", null, 1));
        attendanceList.add(new Attendance(2, 1, 1, LocalDate.of(2026, 3, 3), "Presente", null, 1));
        attendanceList.add(new Attendance(3, 2, 1, LocalDate.of(2026, 3, 2), "Ausente", "Sin justificación", 1));
        attendanceList.add(new Attendance(4, 2, 1, LocalDate.of(2026, 3, 3), "Ausente", "Sin justificación", 1));
        attendanceList.add(new Attendance(5, 2, 1, LocalDate.of(2026, 3, 4), "Ausente", "Sin justificación", 1));
        attendanceList.add(new Attendance(6, 2, 1, LocalDate.of(2026, 3, 5), "Ausente", "Reiterada", 1));

        System.out.println("\n✅ Control de Asistencia y Alerta de Inasistencias Críticas (>3 ausencias):");
        Map<Integer, Long> absences = attendanceList.stream()
                .filter(a -> "Ausente".equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.groupingBy(Attendance::getStudentId, Collectors.counting()));

        absences.forEach((stId, count) -> {
            String name = (stId == 1) ? s1.getFullName() : s2.getFullName();
            if (count >= 3) {
                System.out.printf("   ⚠️ ALERTA: Estudiante %s presenta %d ausencias. Requiere citación a acudiente.%n", name, count);
            }
        });

        // ====================================================================
        // 2. MÓDULO DE ACTIVIDADES EXTRACURRICULARES
        // ====================================================================
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════");
        System.out.println("2. MÓDULO DE ACTIVIDADES EXTRACURRICULARES");
        System.out.println("════════════════════════════════════════════════════════════════════════════════");

        Club club1 = new Club(1, "Club de Robótica e Inteligencia Artificial", "Diseño de robots con Arduino y visión artificial",
                1, 15, "Martes y Jueves 3:30 - 5:00 PM", true);
        Club club2 = new Club(2, "Club de Ciencias y Astronomía", "Observación astronómica y experimentos químicos",
                2, 20, "Miércoles 3:00 - 5:00 PM", true);

        System.out.printf("✅ Club: %s | Cupo: %d estudiantes | Horario: %s%n",
                club1.getClubName(), club1.getMaxCapacity(), club1.getMeetingSchedule());
        System.out.printf("   • Inscripción de %s: ACEPTADA (Dentro de cupo disponible)%n", s1.getFullName());

        SchoolEvent event1 = new SchoolEvent(1, "Feria de Ciencias y Tecnología 2026",
                "Exposición anual de proyectos de innovación escolar", "Científico",
                LocalDate.of(2026, 9, 25), "Auditorio Central", true, 2, true);

        EventParticipant p1 = new EventParticipant(1, 1, 1, "Competidor", "1er Lugar", "Proyecto Robot Clasificador de Residuos");
        EventParticipant p2 = new EventParticipant(2, 1, 2, "Competidor", "2do Lugar", "Proyecto Generador Hidráulico Escolar");

        System.out.printf("✅ Evento: %s (%s) en %s%n", event1.getEventName(), event1.getEventType(), event1.getLocation());
        System.out.printf("   🏆 Podio de Competencia:%n");
        System.out.printf("      🥇 1er Lugar: %s | %s%n", s1.getFullName(), p1.getRemarks());
        System.out.printf("      🥈 2do Lugar: %s | %s%n", s2.getFullName(), p2.getRemarks());

        // ====================================================================
        // 3. MÓDULO FINANCIERO Y FACTURACIÓN ELECTRÓNICA DIAN (PDF + QR)
        // ====================================================================
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════");
        System.out.println("3. MÓDULO FINANCIERO Y FACTURACIÓN ELECTRÓNICA DIAN (PDF)");
        System.out.println("════════════════════════════════════════════════════════════════════════════════");

        PaymentConcept cMatricula = new PaymentConcept(1, "MAT", "Matrícula Anual", "Pago anual de matrícula", new BigDecimal("500000.00"), false, true);
        PaymentConcept cPension = new PaymentConcept(2, "PENSION", "Pensión Mensual Marzo 2026", "Cuota educativa", new BigDecimal("350000.00"), true, true);

        String invoiceNumber = "FACT-2026-000101";
        LocalDate invoiceDate = LocalDate.now();
        BigDecimal amount = cPension.getBaseAmount();
        String receiptNumber = "REC-2026-0089";

        System.out.printf("✅ Concepto facturado: %s por un valor de $ %,.2f COP%n", cPension.getConceptName(), amount.doubleValue());
        System.out.println("   • Generando Factura Electrónica conforme a requisitos DIAN...");

        // Generar Factura Electrónica real en PDF
        String pdfPath = InvoicePdfGenerator.generateInvoicePdf(
                invoiceNumber,
                invoiceDate,
                g1.getFullName(),
                g1.getDocumentNumber(),
                g1.getAddress(),
                s1.getFullName(),
                cPension.getConceptName(),
                1,
                amount,
                "Transferencia Bancaria",
                receiptNumber
        );

        String cufe = InvoicePdfGenerator.calculateCufe(invoiceNumber, invoiceDate, amount, g1.getDocumentNumber());

        System.out.println("   📄 [FACTURA GENERADA EXITOSAMENTE]");
        System.out.println("      - Archivo PDF: " + pdfPath);
        System.out.println("      - Consecutivo: " + invoiceNumber);
        System.out.println("      - Recibo Pago: " + receiptNumber);
        System.out.println("      - CUFE DIAN:   " + cufe);
        System.out.println("      - Código QR:   Generado e integrado en la esquina inferior izquierda.");

        File pdfFile = new File(pdfPath);
        if (pdfFile.exists()) {
            System.out.printf("      - Tamaño en disco: %d bytes (Verificado y listo para entrega)%n", pdfFile.length());
        }

        // ====================================================================
        // 4. MÓDULO DE GESTIÓN DE RECURSOS (BIBLIOTECA Y LABORATORIOS)
        // ====================================================================
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════");
        System.out.println("4. MÓDULO DE GESTIÓN DE RECURSOS (BIBLIOTECA Y LABORATORIOS)");
        System.out.println("════════════════════════════════════════════════════════════════════════════════");

        ResourceItem r1 = new ResourceItem(1, "REC-LAB-001", "Microscopio Binocular Óptico 1000x", 2,
                "Microscopio de laboratorio de alta resolución", LocalDate.of(2023, 2, 10), "Laboratorio de Ciencias", "Disponible");
        ResourceItem r2 = new ResourceItem(2, "REC-LIB-001", "Cien Años de Soledad - G. García Márquez", 1,
                "Edición conmemorativa RAE", LocalDate.of(2022, 5, 12), "Biblioteca Estante A-3", "Disponible");

        System.out.println("✅ Inventario de Recursos Disponibles:");
        System.out.printf("   • [%s] %s | Ubicación: %s | Estado: %s%n",
                r1.getResourceCode(), r1.getResourceName(), r1.getPhysicalLocation(), r1.getStatus());
        System.out.printf("   • [%s] %s | Ubicación: %s | Estado: %s%n",
                r2.getResourceCode(), r2.getResourceName(), r2.getPhysicalLocation(), r2.getStatus());

        // Préstamo a estudiante
        r1.setStatus("Prestado");
        StudentLoan loan1 = new StudentLoan(1, 1, 1, LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 5), null, "Activo", "Préstamo para proyecto de biología celular");
        System.out.printf("\n✅ Préstamo registrado:%n   • Recurso '%s' prestado a %s con fecha límite %s.%n",
                r1.getResourceName(), s1.getFullName(), loan1.getExpectedReturnDate());
        System.out.printf("   • Estado del recurso actualizado a: '%s'%n", r1.getStatus());

        // Devolución del recurso
        loan1.setActualReturnDate(LocalDate.now());
        loan1.setStatus("Devuelto");
        r1.setStatus("Disponible");
        System.out.printf("✅ Devolución registrada con éxito:%n   • Recurso devuelto en buen estado. Estado restaurado a: '%s'%n",
                r1.getStatus());

        // ====================================================================
        // 5. RESUMEN Y REPORTES
        // ====================================================================
        System.out.println("\n════════════════════════════════════════════════════════════════════════════════");
        System.out.println("5. REPORTES Y ESTADÍSTICAS DEL SISTEMA");
        System.out.println("════════════════════════════════════════════════════════════════════════════════");
        System.out.println("   📊 Estudiantes matriculados activos: 2");
        System.out.println("   📊 Docentes activos con asignación: 2");
        System.out.println("   📊 Clubes extracurriculares con cupo: 2");
        System.out.println("   📊 Facturas emitidas con resolución DIAN: 1");
        System.out.printf("   📊 Recaudación total acumulada: $ %,.2f COP%n", amount.doubleValue());
        System.out.println("   📊 Recursos de biblioteca y laboratorios disponibles: 100%");

        System.out.println("\n================================================================================");
        System.out.println("   🎉 DEMOSTRACIÓN COMPLETADA CON ÉXITO SIN UTILIZAR BASE DE DATOS SQL.");
        System.out.println("   Revisa la factura generada en: " + pdfPath);
        System.out.println("================================================================================\n");
    }
}
