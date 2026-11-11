package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.InputValidator;

import java.time.LocalDate;
import java.util.List;

/**
 * Console view for student admissions, guardian management, and grade enrollments.
 */
public class StudentView {

    private final StudentController studentController = new StudentController();

    public void displayMenu() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║        1. MÓDULO DE GESTIÓN DE ESTUDIANTES   ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Registrar nuevo estudiante");
            System.out.println("2. Registrar acudiente");
            System.out.println("3. Asociar acudiente a estudiante");
            System.out.println("4. Matricular estudiante en un grado escolar");
            System.out.println("5. Consultar estudiante por documento");
            System.out.println("6. Listar todos los estudiantes");
            System.out.println("7. Consultar núcleo familiar y hermanos");
            System.out.println("8. Volver al menú principal");

            int option = InputValidator.readInt("\nSeleccione una opción: ", 1, 8);
            switch (option) {
                case 1 -> registerStudent();
                case 2 -> registerGuardian();
                case 3 -> linkGuardian();
                case 4 -> enrollInGrade();
                case 5 -> searchStudent();
                case 6 -> listStudents();
                case 7 -> viewFamilyGroup();
                case 8 -> { return; }
            }
        }
    }

    private void registerStudent() {
        System.out.println("\n--- REGISTRO DE NUEVO ESTUDIANTE ---");
        String docType = InputValidator.readNonEmptyString("Tipo de Documento (TI, CC, CE, Pasaporte): ");
        String docNum = InputValidator.readNonEmptyString("Número de Documento: ");
        String firstName = InputValidator.readNonEmptyString("Primer Nombre: ");
        String secondName = InputValidator.readOptionalString("Segundo Nombre (opcional): ");
        String lastName = InputValidator.readNonEmptyString("Primer Apellido: ");
        String secondLastName = InputValidator.readOptionalString("Segundo Apellido (opcional): ");
        LocalDate birthDate = InputValidator.readDate("Fecha de Nacimiento (AAAA-MM-DD): ");
        String gender = InputValidator.readNonEmptyString("Género (M, F, Otro): ");
        String address = InputValidator.readOptionalString("Dirección de Residencia: ");
        String phone = InputValidator.readOptionalString("Teléfono de Contacto: ");
        String email = InputValidator.readOptionalString("Correo Electrónico: ");
        String bloodType = InputValidator.readOptionalString("Tipo de Sangre (ej: O+, A+): ");
        String allergies = InputValidator.readOptionalString("Alergias (o enter para ninguna): ");
        String medical = InputValidator.readOptionalString("Condiciones Médicas (o enter para ninguna): ");
        LocalDate entryDate = InputValidator.readDate("Fecha de Ingreso al Colegio (AAAA-MM-DD): ");

        Student student = new Student(0, docType, docNum, firstName, secondName, lastName, secondLastName,
                birthDate, gender, address, phone, email, bloodType, allergies, medical, entryDate, "Activo");

        if (studentController.registerStudent(student)) {
            System.out.printf("✅ Estudiante %s registrado exitosamente con ID %d.%n", student.getFullName(), student.getId());
        } else {
            System.out.println("❌ Error al registrar el estudiante. Verifique que el documento no esté duplicado.");
        }
        InputValidator.pause();
    }

    private void registerGuardian() {
        System.out.println("\n--- REGISTRO DE ACUDIENTE / TUTOR LEGAL ---");
        String docType = InputValidator.readNonEmptyString("Tipo de Documento (CC, CE, Pasaporte): ");
        String docNum = InputValidator.readNonEmptyString("Número de Documento: ");
        String firstName = InputValidator.readNonEmptyString("Primer Nombre: ");
        String secondName = InputValidator.readOptionalString("Segundo Nombre (opcional): ");
        String lastName = InputValidator.readNonEmptyString("Primer Apellido: ");
        String secondLastName = InputValidator.readOptionalString("Segundo Apellido (opcional): ");
        String relationship = InputValidator.readNonEmptyString("Parentesco (Padre, Madre, Abuelo, Tutor Legal): ");
        String mainPhone = InputValidator.readNonEmptyString("Teléfono Principal: ");
        String secPhone = InputValidator.readOptionalString("Teléfono Secundario (opcional): ");
        String email = InputValidator.readOptionalString("Correo Electrónico: ");
        String address = InputValidator.readOptionalString("Dirección: ");
        String occupation = InputValidator.readOptionalString("Ocupación / Profesión: ");
        boolean isFinancial = InputValidator.readBoolean("¿Es el responsable financiero de los pagos?");

        Guardian guardian = new Guardian(0, docType, docNum, firstName, secondName, lastName, secondLastName,
                relationship, mainPhone, secPhone, email, address, occupation, isFinancial);

        if (studentController.registerGuardian(guardian)) {
            System.out.printf("✅ Acudiente %s registrado exitosamente con ID %d.%n", guardian.getFullName(), guardian.getId());
        } else {
            System.out.println("❌ Error al registrar acudiente. Verifique que el documento no exista.");
        }
        InputValidator.pause();
    }

    private void linkGuardian() {
        System.out.println("\n--- ASOCIAR ACUDIENTE A ESTUDIANTE ---");
        int studentId = InputValidator.readPositiveInt("Ingrese el ID del Estudiante: ");
        Student s = studentController.findStudentById(studentId);
        if (s == null) {
            System.out.println("❌ Estudiante no encontrado.");
            InputValidator.pause();
            return;
        }

        int guardianId = InputValidator.readPositiveInt("Ingrese el ID del Acudiente: ");
        boolean isPrimary = InputValidator.readBoolean("¿Es el acudiente principal para citaciones?");
        boolean isEmergency = InputValidator.readBoolean("¿Es contacto de emergencia?");

        if (studentController.linkGuardianToStudent(studentId, guardianId, isPrimary, isEmergency)) {
            System.out.println("✅ Acudiente asociado exitosamente al estudiante.");
        } else {
            System.out.println("❌ Error al asociar acudiente. Verifique los IDs ingresados.");
        }
        InputValidator.pause();
    }

    private void enrollInGrade() {
        System.out.println("\n--- MATRICULAR ESTUDIANTE EN GRADO ESCOLAR ---");
        int studentId = InputValidator.readPositiveInt("ID del Estudiante: ");
        Student s = studentController.findStudentById(studentId);
        if (s == null) {
            System.out.println("❌ Estudiante no encontrado.");
            InputValidator.pause();
            return;
        }

        System.out.println("\nGrados Académicos Disponibles:");
        List<GradeLevel> grades = studentController.getAllGradeLevels();
        grades.forEach(System.out::println);

        int gradeId = InputValidator.readPositiveInt("\nID del Grado a matricular: ");
        int year = InputValidator.readInt("Año Académico (ej: 2026): ", 2020, 2035);
        String remarks = InputValidator.readOptionalString("Observaciones de matrícula: ");

        if (studentController.enrollStudentInGrade(studentId, gradeId, year, remarks)) {
            System.out.printf("✅ Matrícula exitosa para %s en el año %d.%n", s.getFullName(), year);
        } else {
            System.out.println("❌ No se pudo completar la matrícula. El estudiante ya puede estar matriculado en este año.");
        }
        InputValidator.pause();
    }

    private void searchStudent() {
        String doc = InputValidator.readNonEmptyString("\nIngrese el número de documento del estudiante: ");
        Student s = studentController.findStudentByDocument(doc);
        if (s != null) {
            System.out.println("\n" + s);
            System.out.println("Dirección: " + s.getAddress() + " | Teléfono: " + s.getPhone());
            System.out.println("Correo: " + s.getEmail() + " | RH: " + s.getBloodType());
            System.out.println("Alergias: " + (s.getAllergies() != null ? s.getAllergies() : "Ninguna"));

            System.out.println("\nHistorial de Matrículas:");
            List<Enrollment> enrollments = studentController.getStudentEnrollments(s.getId());
            if (enrollments.isEmpty()) {
                System.out.println("  (Sin matrículas registradas)");
            } else {
                enrollments.forEach(en -> System.out.println("  - " + en));
            }
        } else {
            System.out.println("❌ No se encontró ningún estudiante con el documento: " + doc);
        }
        InputValidator.pause();
    }

    private void listStudents() {
        System.out.println("\n--- LISTADO GENERAL DE ESTUDIANTES ---");
        List<Student> list = studentController.getAllStudents();
        if (list.isEmpty()) {
            System.out.println("No hay estudiantes registrados.");
        } else {
            System.out.printf("%-5s | %-15s | %-30s | %-10s%n", "ID", "Documento", "Nombre Completo", "Estado");
            System.out.println("-------------------------------------------------------------------");
            list.forEach(st -> System.out.printf("%-5d | %-4s %-10s | %-30s | %-10s%n",
                    st.getId(), st.getDocumentType(), st.getDocumentNumber(), st.getFullName(), st.getStatus()));
            System.out.printf("%nTotal estudiantes registrados: %d%n", list.size());
        }
        InputValidator.pause();
    }

    private void viewFamilyGroup() {
        System.out.println("\n--- CONSULTA DE NÚCLEO FAMILIAR Y HERMANOS ---");
        int studentId = InputValidator.readPositiveInt("Ingrese el ID del Estudiante: ");
        Student s = studentController.findStudentById(studentId);
        if (s == null) {
            System.out.println("❌ Estudiante no encontrado.");
            InputValidator.pause();
            return;
        }

        System.out.printf("\nEstudiante: %s (Doc: %s)%n", s.getFullName(), s.getDocumentNumber());
        List<Guardian> guardians = studentController.getGuardiansForStudent(studentId);
        if (guardians.isEmpty()) {
            System.out.println("  (No tiene acudientes asociados en el sistema)");
        } else {
            System.out.println("\nAcudientes Asociados:");
            for (Guardian g : guardians) {
                System.out.printf("  - %s (%s, Tel: %s) [Responsable Financiero: %s]%n",
                        g.getFullName(), g.getRelationship(), g.getMainPhone(), g.isFinancialResponsible() ? "Sí" : "No");

                // Check other children of this guardian (siblings)
                List<Student> siblings = studentController.getStudentsForGuardian(g.getId());
                List<Student> otherSiblings = siblings.stream()
                        .filter(sib -> sib.getId() != studentId)
                        .toList();
                if (!otherSiblings.isEmpty()) {
                    System.out.println("    Hermano(s) en la institución:");
                    otherSiblings.forEach(os -> System.out.printf("      * %s (Doc: %s, ID: %d)%n",
                            os.getFullName(), os.getDocumentNumber(), os.getId()));
                }
            }
        }
        InputValidator.pause();
    }
}
