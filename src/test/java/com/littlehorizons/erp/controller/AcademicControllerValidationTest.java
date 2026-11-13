package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AcademicDAO;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AcademicControllerValidationTest {
    private static class RecordingDAO extends AcademicDAO {
        int gradeCalls;
        int attendanceCalls;
        AcademicRecord saved;
        @Override public boolean recordGrade(AcademicRecord record) {
            gradeCalls++;
            saved = record;
            return true;
        }
        @Override public boolean recordAttendance(Attendance attendance) {
            attendanceCalls++;
            return true;
        }
    }

    private AcademicRecord record(String grade) {
        return new AcademicRecord(0, 1, 1, 1, 2026, "I", new BigDecimal(grade), null, LocalDate.of(2026, 1, 1));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.04", "5.04", "-0.1", "5.1", "4.55", "4.54", "10"})
    void rejectsInvalidOriginalGradeWithoutCallingDao(String grade) {
        RecordingDAO dao = new RecordingDAO();
        AcademicRecord record = record(grade);
        assertFalse(new AcademicController(dao).recordGrade(record));
        assertEquals(0, dao.gradeCalls);
        assertEquals(new BigDecimal(grade), record.getGradeValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.0", "5", "5.0", "4.5", "4.50"})
    void acceptsExactValuesWithoutChangingThem(String grade) {
        RecordingDAO dao = new RecordingDAO();
        AcademicRecord record = record(grade);
        assertTrue(new AcademicController(dao).recordGrade(record));
        assertEquals(1, dao.gradeCalls);
        assertSame(record, dao.saved);
        assertEquals(new BigDecimal(grade), dao.saved.getGradeValue());
    }

    @Test void rejectsNullRecordAndNullGradeBeforePersistence() {
        RecordingDAO dao = new RecordingDAO();
        AcademicController controller = new AcademicController(dao);
        assertFalse(controller.recordGrade(null));
        assertFalse(controller.recordGrade(new AcademicRecord()));
        assertEquals(0, dao.gradeCalls);
    }

    @Test void rejectsMissingAttendanceDateBeforePersistence() {
        RecordingDAO dao = new RecordingDAO();
        AcademicController controller = new AcademicController(dao);
        Attendance attendance = new Attendance(0, 1, 1, null, "Presente", null, 1);
        assertFalse(controller.recordAttendance(attendance));
        assertEquals(0, dao.attendanceCalls);
        attendance.setAttendanceDate(LocalDate.of(2024, 2, 29));
        assertTrue(controller.recordAttendance(attendance));
        assertEquals(1, dao.attendanceCalls);
    }
}
