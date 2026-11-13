package com.littlehorizons.erp.integration;

import com.littlehorizons.erp.dao.GuardianDAO;
import com.littlehorizons.erp.integration.support.DaoIntegrationSupport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GuardianDAOIT extends DaoIntegrationSupport {
    @Test void insertsFindsAndRejectsDuplicateGuardian() throws Exception {
        GuardianDAO dao = new GuardianDAO();
        var guardian = dao.findGuardianById(fixtures.guardian);
        assertNotNull(guardian);
        guardian.setId(0); guardian.setDocumentNumber(fixtures.token + "N");
        boolean saved = dao.createGuardian(guardian);
        if (guardian.getId() > 0) fixtures.track("acudientes", "id_acudiente", guardian.getId());
        assertTrue(saved);
        assertEquals(guardian.getId(), dao.findGuardianByDocument(guardian.getDocumentNumber()).getId());
        assertTrue(dao.findAllGuardians().stream().anyMatch(g -> g.getId() == guardian.getId()));
        assertFalse(dao.createGuardian(guardian));
    }
}
