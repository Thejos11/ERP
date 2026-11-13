package com.littlehorizons.erp.integration;

import com.littlehorizons.erp.dao.AcademicDAO;
import com.littlehorizons.erp.integration.support.DaoIntegrationSupport;
import com.littlehorizons.erp.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AcademicDAOIT extends DaoIntegrationSupport {
    @Test void createsSubjectAndEnrollsStudent() throws Exception {
        AcademicDAO dao = new AcademicDAO();
        var subject = dao.findSubjectById(fixtures.subject);
        assertNotNull(subject);
        subject.setId(0); subject.setSubjectCode(fixtures.token + "N");
        boolean saved = dao.createSubject(subject);
        if (subject.getId() > 0) fixtures.track("materias", "id_materia", subject.getId());
        assertTrue(saved);
        assertTrue(dao.enrollStudentInSubject(fixtures.student, subject.getId(), fixtures.date.getYear(), "I", "A"));
        assertEquals(1, dao.getStudentSubjectEnrollments(fixtures.student, fixtures.date.getYear(), "I").size());
        assertFalse(dao.enrollStudentInSubject(fixtures.student, subject.getId(), fixtures.date.getYear(), "I", "A"));
    }

    @Test void insertsAndUpdatesGradesAndAttendance() throws Exception {
        AcademicDAO dao = new AcademicDAO();
        AcademicRecord record = grade("0.0");
        assertTrue(dao.recordGrade(record));
        record.setGradeValue(new BigDecimal("5.0"));
        assertTrue(dao.recordGrade(record));
        var history = dao.getGradesForStudent(fixtures.student);
        assertEquals(1, history.size());
        assertEquals(new BigDecimal("5.0"), history.getFirst().getGradeValue());
        Attendance attendance = new Attendance(0, fixtures.student, fixtures.subject, fixtures.date, "Presente", null, fixtures.teacher);
        assertTrue(dao.recordAttendance(attendance));
        attendance.setStatus("Ausente");
        assertTrue(dao.recordAttendance(attendance));
        assertEquals("Ausente", dao.getAttendanceByStudent(fixtures.student).getFirst().getStatus());
    }

    @ParameterizedTest @ValueSource(strings = {"-0.1", "5.1"})
    void databaseRejectsOutOfRangeGrades(String value) throws Exception {
        assertFalse(new AcademicDAO().recordGrade(grade(value)));
        assertEquals(0, count("SELECT COUNT(*) FROM registros_academicos WHERE id_estudiante=?", fixtures.student));
    }

    private AcademicRecord grade(String value) {
        return new AcademicRecord(0, fixtures.student, fixtures.subject, fixtures.teacher, fixtures.date.getYear(),
                "I", new BigDecimal(value), "Fictitious grade", fixtures.date);
    }
}
