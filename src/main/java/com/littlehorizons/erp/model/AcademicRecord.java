package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Domain entity representing an academic grade entry (Calificación).
 * Supports standard Colombian numeric grades from 0.0 to 5.0 with 1 decimal.
 */
public class AcademicRecord {

    private int id;
    private int studentId;
    private int subjectId;
    private int teacherId;
    private int academicYear;
    private String academicPeriod; // 'I', 'II', 'III', 'IV', 'Final'
    private BigDecimal gradeValue;
    private String remarks;
    private LocalDate recordDate;

    // View helper fields
    private String studentName;
    private String subjectName;
    private String teacherName;

    public AcademicRecord() {
    }

    public AcademicRecord(int id, int studentId, int subjectId, int teacherId, int academicYear,
                          String academicPeriod, BigDecimal gradeValue, String remarks, LocalDate recordDate) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.teacherId = teacherId;
        this.academicYear = academicYear;
        this.academicPeriod = academicPeriod;
        this.gradeValue = gradeValue;
        this.remarks = remarks;
        this.recordDate = recordDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public String getAcademicPeriod() { return academicPeriod; }
    public void setAcademicPeriod(String academicPeriod) { this.academicPeriod = academicPeriod; }

    public BigDecimal getGradeValue() { return gradeValue; }
    public void setGradeValue(BigDecimal gradeValue) { this.gradeValue = gradeValue; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | Periodo %s: %.1f (%s) - Prof: %s",
                id, studentName != null ? studentName : "Est. " + studentId,
                subjectName != null ? subjectName : "Mat. " + subjectId,
                academicPeriod, gradeValue != null ? gradeValue.doubleValue() : 0.0,
                remarks != null ? remarks : "",
                teacherName != null ? teacherName : "ID " + teacherId);
    }
}
