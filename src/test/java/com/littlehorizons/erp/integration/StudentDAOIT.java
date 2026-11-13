package com.littlehorizons.erp.integration;

import com.littlehorizons.erp.dao.StudentDAO;
import com.littlehorizons.erp.integration.support.DaoIntegrationSupport;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StudentDAOIT extends DaoIntegrationSupport {
    @Test void insertsFindsAndRejectsDuplicateDocument() throws Exception {
        StudentDAO dao = new StudentDAO();
        var student = dao.findStudentById(fixtures.student);
        assertNotNull(student);
        student.setId(0); student.setDocumentNumber(fixtures.token + "N");
        boolean saved = dao.createStudent(student);
        if (student.getId() > 0) fixtures.track("estudiantes", "id_estudiante", student.getId());
        assertTrue(saved); assertTrue(student.getId() > 0);
        assertEquals(student.getId(), dao.findStudentByDocument(student.getDocumentNumber()).getId());
        assertFalse(dao.createStudent(student));
        assertEquals(1, count("SELECT COUNT(*) FROM estudiantes WHERE numero_documento=?", student.getDocumentNumber()));
    }

    @Test void linksGuardianAndEnrollsInGrade() throws Exception {
        StudentDAO dao = new StudentDAO();
        assertTrue(dao.linkGuardianToStudent(fixtures.student, fixtures.guardian, true, true));
        assertTrue(dao.getGuardiansForStudent(fixtures.student).stream().anyMatch(g -> g.getId() == fixtures.guardian));
        assertTrue(dao.getStudentsForGuardian(fixtures.guardian).stream().anyMatch(s -> s.getId() == fixtures.student));
        assertTrue(dao.enrollStudentInGrade(fixtures.student, fixtures.grade, fixtures.date.getYear(), "Fictitious enrollment"));
        assertEquals(1, dao.getEnrollmentsForStudent(fixtures.student).size());
        assertFalse(dao.enrollStudentInGrade(fixtures.student, fixtures.grade, fixtures.date.getYear(), "Duplicate"));
        assertFalse(dao.linkGuardianToStudent(fixtures.student, -1, true, true));
    }
}
