package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ExtracurricularController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.EventParticipant;
import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.InputValidator;

import java.time.LocalDate;
import java.util.List;

/**
 * Console view for extracurricular clubs, activities, and school events.
 */
public class ExtracurricularView {

    private final ExtracurricularController extracurricularController = new ExtracurricularController();
    private final StudentController studentController = new StudentController();

    public void displayMenu() {
        while (true) {
            System.out.println("\n╔══════════════════════════════════════════════╗");
            System.out.println("║    4. ACTIVIDADES EXTRACURRICULARES          ║");
            System.out.println("╚══════════════════════════════════════════════╝");
            System.out.println("1. Registrar nuevo club");
            System.out.println("2. Inscribir estudiante en club (con validación de cupo)");
            System.out.println("3. Retirar estudiante de un club");
            System.out.println("4. Catálogo general de clubes y disponibilidad de cupos");
            System.out.println("5. Registrar nuevo evento escolar");
            System.out.println("6. Registrar participante en evento (con premiación/roles)");
            System.out.println("7. Consultar participantes de un evento");
            System.out.println("8. Volver al menú principal");

            int option = InputValidator.readInt("\nSeleccione una opción: ", 1, 8);
            switch (option) {
                case 1 -> registerClub();
                case 2 -> enrollClub();
                case 3 -> withdrawClub();
                case 4 -> listClubs();
                case 5 -> registerEvent();
                case 6 -> registerEventParticipant();
                case 7 -> listEventParticipants();
                case 8 -> { return; }
            }
        }
    }

    private void registerClub() {
        System.out.println("\n--- REGISTRO DE NUEVO CLUB ---");
        String name = InputValidator.readNonEmptyString("Nombre del Club: ");
        String desc = InputValidator.readOptionalString("Descripción del Club: ");
        int teacherId = InputValidator.readPositiveInt("ID del Profesor Encargado: ");
        int maxCap = InputValidator.readInt("Cupo máximo de estudiantes: ", 5, 100);
        String schedule = InputValidator.readNonEmptyString("Horario de reunión (ej: Martes 3:30 - 5:00 PM): ");

        Club club = new Club(0, name, desc, teacherId, maxCap, schedule, true);
        if (extracurricularController.createClub(club)) {
            System.out.printf("✅ Club '%s' creado exitosamente con cupo para %d estudiantes.%n", name, maxCap);
        } else {
            System.out.println("❌ Error al crear el club. Verifique que el nombre sea único.");
        }
        InputValidator.pause();
    }

    private void enrollClub() {
        System.out.println("\n--- INSCRIPCIÓN DE ESTUDIANTE EN CLUB ---");
        int studentId = InputValidator.readPositiveInt("ID del Estudiante: ");
        Student s = studentController.findStudentById(studentId);
        if (s == null) {
            System.out.println("❌ Estudiante no encontrado.");
            InputValidator.pause();
            return;
        }

        System.out.println("\nClubes Disponibles:");
        List<Club> clubs = extracurricularController.getAllClubs();
        clubs.forEach(c -> System.out.printf("  [%d] %-35s (Ocupación: %d/%d)%n",
                c.getId(), c.getClubName(), c.getActiveMembersCount(), c.getMaxCapacity()));

        int clubId = InputValidator.readPositiveInt("\nID del Club a inscribir: ");
        LocalDate joinDate = InputValidator.readDate("Fecha de Ingreso al Club (AAAA-MM-DD): ");

        String resultMsg = extracurricularController.enrollStudentInClub(studentId, clubId, joinDate);
        System.out.println(resultMsg);
        InputValidator.pause();
    }

    private void withdrawClub() {
        System.out.println("\n--- RETIRAR ESTUDIANTE DE UN CLUB ---");
        int studentId = InputValidator.readPositiveInt("ID del Estudiante: ");
        List<ClubMembership> memberships = extracurricularController.getMembershipsForStudent(studentId);
        List<ClubMembership> active = memberships.stream()
                .filter(m -> "Activa".equalsIgnoreCase(m.getStatus()))
                .toList();

        if (active.isEmpty()) {
            System.out.println("❌ El estudiante no tiene membresías activas en ningún club.");
            InputValidator.pause();
            return;
        }

        System.out.println("\nMembresías Activas:");
        active.forEach(m -> System.out.printf("  [%d] Club: %s (Desde: %s)%n", m.getId(), m.getClubName(), m.getJoinDate()));

        int memId = InputValidator.readPositiveInt("\nID de la membresía a retirar: ");
        String reason = InputValidator.readNonEmptyString("Motivo del retiro: ");

        if (extracurricularController.withdrawStudentFromClub(memId, reason)) {
            System.out.println("✅ Retiro procesado correctamente.");
        } else {
            System.out.println("❌ Error al procesar retiro.");
        }
        InputValidator.pause();
    }

    private void listClubs() {
        System.out.println("\n--- CATÁLOGO DE CLUBES EXTRACURRICULARES ---");
        List<Club> list = extracurricularController.getAllClubs();
        if (list.isEmpty()) {
            System.out.println("No hay clubes registrados.");
        } else {
            System.out.printf("%-4s | %-35s | %-10s | %-25s | %-25s%n", "ID", "Nombre del Club", "Cupos", "Horario", "Profesor Encargado");
            System.out.println("------------------------------------------------------------------------------------------------------------------");
            list.forEach(c -> System.out.printf("%-4d | %-35s | %2d/%-7d | %-25s | %-25s%n",
                    c.getId(), c.getClubName(), c.getActiveMembersCount(), c.getMaxCapacity(),
                    c.getMeetingSchedule(), c.getTeacherName() != null ? c.getTeacherName() : "ID " + c.getSupervisorTeacherId()));
            System.out.printf("%nTotal clubes disponibles: %d%n", list.size());
        }
        InputValidator.pause();
    }

    private void registerEvent() {
        System.out.println("\n--- REGISTRO DE EVENTO ESCOLAR ---");
        String name = InputValidator.readNonEmptyString("Nombre del Evento: ");
        String desc = InputValidator.readOptionalString("Descripción del Evento: ");

        System.out.println("Tipos de Evento:");
        System.out.println("1. Académico  2. Cultural  3. Deportivo  4. Social  5. Científico");
        int typeOpt = InputValidator.readInt("Seleccione tipo: ", 1, 5);
        String type = switch (typeOpt) {
            case 1 -> "Académico";
            case 2 -> "Cultural";
            case 3 -> "Deportivo";
            case 4 -> "Social";
            default -> "Científico";
        };

        LocalDate eventDate = InputValidator.readDate("Fecha del Evento (AAAA-MM-DD): ");
        String location = InputValidator.readNonEmptyString("Lugar / Instalación (ej: Coliseo, Auditorio): ");
        boolean hasComp = InputValidator.readBoolean("¿El evento tiene competencia con posiciones/premios?");
        int teacherId = InputValidator.readPositiveInt("ID del Profesor Organizador: ");

        SchoolEvent ev = new SchoolEvent(0, name, desc, type, eventDate, location, hasComp, teacherId, true);
        if (extracurricularController.createEvent(ev)) {
            System.out.printf("✅ Evento '%s' agendado exitosamente para el %s.%n", name, eventDate);
        } else {
            System.out.println("❌ Error al registrar el evento.");
        }
        InputValidator.pause();
    }

    private void registerEventParticipant() {
        System.out.println("\n--- REGISTRO DE PARTICIPANTE EN EVENTO ---");
        System.out.println("Eventos Escolares:");
        List<SchoolEvent> events = extracurricularController.getAllEvents();
        events.forEach(System.out::println);

        int eventId = InputValidator.readPositiveInt("\nID del Evento: ");
        SchoolEvent ev = extracurricularController.findEventById(eventId);
        if (ev == null) {
            System.out.println("❌ Evento no encontrado.");
            InputValidator.pause();
            return;
        }

        int studentId = InputValidator.readPositiveInt("ID del Estudiante: ");
        Student s = studentController.findStudentById(studentId);
        if (s == null) {
            System.out.println("❌ Estudiante no encontrado.");
            InputValidator.pause();
            return;
        }

        System.out.println("Rol de Participación: 1. Participante  2. Competidor  3. Organizador  4. Asistente");
        int roleOpt = InputValidator.readInt("Seleccione rol: ", 1, 4);
        String role = switch (roleOpt) {
            case 1 -> "Participante";
            case 2 -> "Competidor";
            case 3 -> "Organizador";
            default -> "Asistente";
        };

        String position = null;
        if (ev.isHasCompetition()) {
            System.out.println("Posición Obtenida: 1. 1er Lugar  2. 2do Lugar  3. 3er Lugar  4. Participante");
            int posOpt = InputValidator.readInt("Seleccione posición: ", 1, 4);
            position = switch (posOpt) {
                case 1 -> "1er Lugar";
                case 2 -> "2do Lugar";
                case 3 -> "3er Lugar";
                default -> "Participante";
            };
        }

        String remarks = InputValidator.readOptionalString("Observaciones o proyecto presentado: ");

        if (extracurricularController.registerParticipantInEvent(eventId, studentId, role, position, remarks)) {
            System.out.printf("✅ Participación registrada para %s en '%s'.%n", s.getFullName(), ev.getEventName());
        } else {
            System.out.println("❌ Error al registrar participación.");
        }
        InputValidator.pause();
    }

    private void listEventParticipants() {
        int eventId = InputValidator.readPositiveInt("\nIngrese el ID del Evento: ");
        SchoolEvent ev = extracurricularController.findEventById(eventId);
        if (ev == null) {
            System.out.println("❌ Evento no encontrado.");
            InputValidator.pause();
            return;
        }

        System.out.printf("%nParticipantes en '%s' (%s):%n", ev.getEventName(), ev.getEventDate());
        List<EventParticipant> list = extracurricularController.getParticipantsForEvent(eventId);
        if (list.isEmpty()) {
            System.out.println("  (No hay participantes registrados)");
        } else {
            list.forEach(p -> System.out.println("  - " + p));
        }
        InputValidator.pause();
    }
}
