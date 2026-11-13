package com.littlehorizons.erp.integration;

import com.littlehorizons.erp.dao.ExtracurricularDAO;
import com.littlehorizons.erp.integration.support.DaoIntegrationSupport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExtracurricularDAOIT extends DaoIntegrationSupport {
    @Test void createsClubAndWithdrawsMembership() throws Exception {
        ExtracurricularDAO dao = new ExtracurricularDAO();
        var club = dao.findClubById(fixtures.club);
        assertNotNull(club);
        club.setId(0); club.setClubName(fixtures.token + "N");
        boolean saved = dao.createClub(club);
        if (club.getId() > 0) fixtures.track("clubes", "id_club", club.getId());
        assertTrue(saved);
        assertTrue(dao.registerMembership(fixtures.student, club.getId(), fixtures.date));
        assertEquals(1, dao.getActiveMembersCount(club.getId()));
        var membership = dao.getMembershipsForStudent(fixtures.student).getFirst();
        assertTrue(dao.withdrawMembership(membership.getId(), "Fictitious withdrawal"));
        assertEquals(0, dao.getActiveMembersCount(club.getId()));
    }

    @Test void createsEventAndUpdatesParticipation() throws Exception {
        ExtracurricularDAO dao = new ExtracurricularDAO();
        var event = dao.findEventById(fixtures.event);
        assertNotNull(event);
        event.setId(0); event.setEventName(fixtures.token + "N");
        boolean saved = dao.createEvent(event);
        if (event.getId() > 0) fixtures.track("eventos", "id_evento", event.getId());
        assertTrue(saved);
        assertTrue(dao.registerParticipant(event.getId(), fixtures.student, "Participante", null, "Fictitious participant"));
        assertTrue(dao.registerParticipant(event.getId(), fixtures.student, "Competidor", "1er Lugar", "Updated"));
        assertEquals(1, dao.getParticipantsForEvent(event.getId()).size());
        assertEquals(1, count("SELECT COUNT(*) FROM participantes_evento WHERE id_evento=? AND rol_participacion='Competidor'", event.getId()));
    }
}
