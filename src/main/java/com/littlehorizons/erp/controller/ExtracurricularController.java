package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ExtracurricularDAO;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.EventParticipant;
import com.littlehorizons.erp.model.SchoolEvent;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller managing extracurricular clubs, memberships with capacity validation,
 * school events, and student event participation.
 */
public class ExtracurricularController {

    private final ExtracurricularDAO extracurricularDAO = new ExtracurricularDAO();

    public boolean createClub(Club club) {
        if (club == null || club.getClubName() == null || club.getClubName().isBlank()) {
            return false;
        }
        return extracurricularDAO.createClub(club);
    }

    public Club findClubById(int id) {
        return extracurricularDAO.findClubById(id);
    }

    public List<Club> getAllClubs() {
        return extracurricularDAO.findAllClubs();
    }

    /**
     * Enrolls a student in a club verifying that capacity has not been exceeded.
     *
     * @param studentId Student identifier.
     * @param clubId Club identifier.
     * @param joinDate Date joined.
     * @return Result message indicating success or capacity rejection.
     */
    public String enrollStudentInClub(int studentId, int clubId, LocalDate joinDate) {
        Club club = extracurricularDAO.findClubById(clubId);
        if (club == null) {
            return "❌ El club especificado no existe.";
        }
        if (!club.isActive()) {
            return "❌ El club seleccionado no está activo actualmente.";
        }

        int activeMembers = extracurricularDAO.getActiveMembersCount(clubId);
        if (activeMembers >= club.getMaxCapacity()) {
            return String.format("❌ Cupo máximo alcanzado (%d/%d estudiantes). No es posible inscribir más estudiantes.",
                    activeMembers, club.getMaxCapacity());
        }

        boolean success = extracurricularDAO.registerMembership(studentId, clubId, joinDate);
        if (success) {
            return String.format("✅ Estudiante inscrito exitosamente en '%s'. Cupos ocupados: %d/%d.",
                    club.getClubName(), activeMembers + 1, club.getMaxCapacity());
        } else {
            return "❌ Error al registrar la membresía en la base de datos.";
        }
    }

    public boolean withdrawStudentFromClub(int membershipId, String reason) {
        return extracurricularDAO.withdrawMembership(membershipId, reason);
    }

    public List<ClubMembership> getMembershipsForStudent(int studentId) {
        return extracurricularDAO.getMembershipsForStudent(studentId);
    }

    public List<ClubMembership> getMembershipsForClub(int clubId) {
        return extracurricularDAO.getMembershipsForClub(clubId);
    }

    public boolean createEvent(SchoolEvent event) {
        if (event == null || event.getEventName() == null || event.getEventName().isBlank()) {
            return false;
        }
        return extracurricularDAO.createEvent(event);
    }

    public SchoolEvent findEventById(int id) {
        return extracurricularDAO.findEventById(id);
    }

    public List<SchoolEvent> getAllEvents() {
        return extracurricularDAO.findAllEvents();
    }

    public boolean registerParticipantInEvent(int eventId, int studentId, String role, String positionWon, String remarks) {
        return extracurricularDAO.registerParticipant(eventId, studentId, role, positionWon, remarks);
    }

    public List<EventParticipant> getParticipantsForEvent(int eventId) {
        return extracurricularDAO.getParticipantsForEvent(eventId);
    }
}
