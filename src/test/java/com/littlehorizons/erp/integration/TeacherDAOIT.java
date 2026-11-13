package com.littlehorizons.erp.integration;

import com.littlehorizons.erp.dao.TeacherDAO;
import com.littlehorizons.erp.integration.support.DaoIntegrationSupport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TeacherDAOIT extends DaoIntegrationSupport {
    @Test void insertsFindsAndAssignsTeacher() throws Exception {
        TeacherDAO dao = new TeacherDAO();
        var teacher = dao.findTeacherById(fixtures.teacher);
        assertNotNull(teacher);
        teacher.setId(0); teacher.setDocumentNumber(fixtures.token + "N");
        teacher.setEmail(fixtures.token + "new@example.invalid");
        boolean saved = dao.createTeacher(teacher);
        if (teacher.getId() > 0) fixtures.track("profesores", "id_profesor", teacher.getId());
        assertTrue(saved);
        assertEquals(teacher.getId(), dao.findTeacherByDocument(teacher.getDocumentNumber()).getId());
        assertTrue(dao.assignTeacherToSubject(teacher.getId(), fixtures.subject, fixtures.date.getYear(), "A"));
        assertEquals(1, dao.getAssignmentsForTeacher(teacher.getId(), fixtures.date.getYear()).size());
        assertFalse(dao.createTeacher(teacher));
    }
}
