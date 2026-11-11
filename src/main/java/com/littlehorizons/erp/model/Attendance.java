package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain entity representing daily student attendance for a subject.
 */
public class Attendance {

    private int id;
    private int studentId;
    private int subjectId;
    private LocalDate attendanceDate;
    private String status; // 'Presente', 'Ausente', 'Tardanza', 'Excusa Médica'
    private String remarks;
    private Integer registeredByTeacherId;

    private String studentName;
    private String subjectName;

    public Attendance() {
    }

    public Attendance(int id, int studentId, int subjectId, LocalDate attendanceDate,
                      String status, String remarks, Integer registeredByTeacherId) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.remarks = remarks;
        this.registeredByTeacherId = registeredByTeacherId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public Integer getRegisteredByTeacherId() { return registeredByTeacherId; }
    public void setRegisteredByTeacherId(Integer registeredByTeacherId) { this.registeredByTeacherId = registeredByTeacherId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s -> %s %s",
                id, attendanceDate, studentName != null ? studentName : studentId,
                subjectName != null ? subjectName : subjectId, status,
                remarks != null ? "(" + remarks + ")" : "");
    }
}
