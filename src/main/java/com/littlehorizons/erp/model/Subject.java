package com.littlehorizons.erp.model;

/**
 * Domain entity representing an academic subject/course.
 */
public class Subject {

    private int id;
    private String subjectCode;
    private String subjectName;
    private String description;
    private int gradeId;
    private int weeklyHours;
    private boolean isActive;

    private String gradeName;

    public Subject() {
    }

    public Subject(int id, String subjectCode, String subjectName, String description, int gradeId,
                   int weeklyHours, boolean isActive) {
        this.id = id;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.description = description;
        this.gradeId = gradeId;
        this.weeklyHours = weeklyHours;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getGradeId() { return gradeId; }
    public void setGradeId(int gradeId) { this.gradeId = gradeId; }

    public int getWeeklyHours() { return weeklyHours; }
    public void setWeeklyHours(int weeklyHours) { this.weeklyHours = weeklyHours; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getGradeName() { return gradeName; }
    public void setGradeName(String gradeName) { this.gradeName = gradeName; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Grado: %s - %d hrs/sem",
                id, subjectName, subjectCode, gradeName != null ? gradeName : gradeId, weeklyHours);
    }
}
