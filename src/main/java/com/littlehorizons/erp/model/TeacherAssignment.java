package com.littlehorizons.erp.model;

/**
 * Domain entity representing an assignment of a subject and group section to a teacher.
 */
public class TeacherAssignment {

    private int id;
    private int teacherId;
    private int subjectId;
    private int academicYear;
    private String section;

    private String teacherName;
    private String subjectName;
    private String subjectCode;

    public TeacherAssignment() {
    }

    public TeacherAssignment(int id, int teacherId, int subjectId, int academicYear, String section) {
        this.id = id;
        this.teacherId = teacherId;
        this.subjectId = subjectId;
        this.academicYear = academicYear;
        this.section = section;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getAcademicYear() { return academicYear; }
    public void setAcademicYear(int academicYear) { this.academicYear = academicYear; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    @Override
    public String toString() {
        return String.format("[%d] %s -> %s (%s) - Grupo: %s | Año: %d",
                id, teacherName != null ? teacherName : "ID " + teacherId,
                subjectName != null ? subjectName : "ID " + subjectId,
                subjectCode != null ? subjectCode : "", section, academicYear);
    }
}
