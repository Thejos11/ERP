package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AcademicDAO;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.util.GradeValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller handling academic curriculum, subject enrollments, grading,
 * automated average calculations using Java Streams, and attendance monitoring.
 */
public class AcademicController {

    private final AcademicDAO academicDAO;

    public AcademicController() {
        this(new AcademicDAO());
    }

    public AcademicController(AcademicDAO academicDAO) {
        this.academicDAO = java.util.Objects.requireNonNull(academicDAO);
    }

    public boolean registerSubject(Subject subject) {
        if (subject == null || subject.getSubjectCode() == null || subject.getSubjectCode().isBlank()) {
            return false;
        }
        return academicDAO.createSubject(subject);
    }

    public List<Subject> getSubjectsByGrade(int gradeId) {
        return academicDAO.findSubjectsByGrade(gradeId);
    }

    public List<Subject> getAllSubjects() {
        return academicDAO.findAllSubjects();
    }

    public Subject findSubjectById(int id) {
        return academicDAO.findSubjectById(id);
    }

    public boolean enrollStudentInSubject(int studentId, int subjectId, int year, String period, String section) {
        return academicDAO.enrollStudentInSubject(studentId, subjectId, year, period, section);
    }

    public List<SubjectEnrollment> getStudentSubjectEnrollments(int studentId, int year, String period) {
        return academicDAO.getStudentSubjectEnrollments(studentId, year, period);
    }

    public boolean recordGrade(AcademicRecord record) {
        if (record == null || record.getGradeValue() == null) {
            return false;
        }
        try {
            GradeValidator.validate(record.getGradeValue());
        } catch (IllegalArgumentException e) {
            return false;
        }
        return academicDAO.recordGrade(record);
    }

    public List<AcademicRecord> getStudentAcademicHistory(int studentId) {
        return academicDAO.getGradesForStudent(studentId);
    }

    /**
     * Calculates the cumulative average of a student for a specific subject across all periods.
     * Implements Java Streams and Lambdas.
     *
     * @param studentId Student identifier.
     * @param subjectId Subject identifier.
     * @param year Academic year.
     * @return Double average rounded to 1 decimal place.
     */
    public double calculateSubjectAverage(int studentId, int subjectId, int year) {
        List<AcademicRecord> records = academicDAO.getGradesForStudent(studentId);
        double avg = records.stream()
                .filter(r -> r.getSubjectId() == subjectId && r.getAcademicYear() == year)
                .map(AcademicRecord::getGradeValue)
                .filter(val -> val != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calculates the overall average of a student in an academic period.
     * Implements Java Streams and Lambdas.
     *
     * @param studentId Student identifier.
     * @param year Academic year.
     * @param period Academic period ('I', 'II', 'III', 'IV').
     * @return Double general average rounded to 1 decimal place.
     */
    public double calculateStudentPeriodAverage(int studentId, int year, String period) {
        List<AcademicRecord> records = academicDAO.getGradesForStudent(studentId);
        double avg = records.stream()
                .filter(r -> r.getAcademicYear() == year && r.getAcademicPeriod().equalsIgnoreCase(period))
                .map(AcademicRecord::getGradeValue)
                .filter(val -> val != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calculates the general course average of all students in a given subject and period.
     * Implements Java Streams and Lambdas.
     *
     * @param subjectId Subject identifier.
     * @param year Academic year.
     * @param period Academic period.
     * @return Double course average rounded to 1 decimal place.
     */
    public double calculateCourseAverageBySubject(int subjectId, int year, String period) {
        List<AcademicRecord> records = academicDAO.getGradesBySubjectAndPeriod(subjectId, year, period);
        double avg = records.stream()
                .map(AcademicRecord::getGradeValue)
                .filter(val -> val != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        return BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Groups a student's grades by subject and calculates each subject's average using Streams.
     *
     * @param studentId Student identifier.
     * @return Map linking subject name to average score.
     */
    public Map<String, Double> getSubjectAveragesSummary(int studentId) {
        List<AcademicRecord> records = academicDAO.getGradesForStudent(studentId);
        return records.stream()
                .collect(Collectors.groupingBy(
                        AcademicRecord::getSubjectName,
                        Collectors.averagingDouble(r -> r.getGradeValue().doubleValue())
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> BigDecimal.valueOf(e.getValue()).setScale(1, RoundingMode.HALF_UP).doubleValue()
                ));
    }

    public boolean recordAttendance(Attendance attendance) {
        if (attendance == null || attendance.getStatus() == null || attendance.getAttendanceDate() == null) {
            return false;
        }
        return academicDAO.recordAttendance(attendance);
    }

    public List<Attendance> getAttendanceForStudent(int studentId) {
        return academicDAO.getAttendanceByStudent(studentId);
    }

    public List<String> getAbsencePatternReport(LocalDate start, LocalDate end, int minAbsences) {
        return academicDAO.getAbsencePatternReport(start, end, minAbsences);
    }
}
