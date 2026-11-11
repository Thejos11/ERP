package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain entity representing a student's enrollment in a subject for a specific period.
 */
public class SubjectEnrollment {

    private int id;
    private int studentId;
    private int subjectId;
    private int academicYear;
    private String academicPeriod; // 'I', 'II', 'III', 'IV'
    private String section;
    private LocalDate enrollmentDate;
    private String status;

    private String studentName;
    private String subjectName;

    public SubjectEnrollment() {
    }

    public SubjectEnrollment(int id, int studentId, int subjectId, int academicYear,
                             String academicPeriod, String section, LocalDate enrollmentDate, String status) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.academicYear = academicYear;
        this.academicPeriod = academicPeriod;
        this.section = section;
        this.enrollmentDate = enrollmentDate;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public String getAcademicPeriod() { return academicPeriod; }
    public void setAcademicPeriod(String academicPeriod) { this.academicPeriod = academicPeriod; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    @Override
    public String toString() {
        return String.format("[%d] %s -> %s - Periodo %s %d (Sección %s) - %s",
                id, studentName != null ? studentName : studentId,
                subjectName != null ? subjectName : subjectId,
                academicPeriod, academicYear, section, status);
    }
}
