package com.littlehorizons.erp.model;

/**
 * Domain entity representing an extracurricular club.
 */
public class Club {

    private int id;
    private String clubName;
    private String description;
    private Integer supervisorTeacherId;
    private int maxCapacity;
    private String meetingSchedule;
    private boolean isActive;

    private String teacherName;
    private int activeMembersCount;

    public Club() {
    }

    public Club(int id, String clubName, String description, Integer supervisorTeacherId,
                int maxCapacity, String meetingSchedule, boolean isActive) {
        this.id = id;
        this.clubName = clubName;
        this.description = description;
        this.supervisorTeacherId = supervisorTeacherId;
        this.maxCapacity = maxCapacity;
        this.meetingSchedule = meetingSchedule;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClubName() { return clubName; }
    public void setClubName(String clubName) { this.clubName = clubName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getSupervisorTeacherId() { return supervisorTeacherId; }
    public void setSupervisorTeacherId(Integer supervisorTeacherId) { this.supervisorTeacherId = supervisorTeacherId; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public String getMeetingSchedule() { return meetingSchedule; }
    public void setMeetingSchedule(String meetingSchedule) { this.meetingSchedule = meetingSchedule; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public int getActiveMembersCount() { return activeMembersCount; }
    public void setActiveMembersCount(int activeMembersCount) { this.activeMembersCount = activeMembersCount; }

    @Override
    public String toString() {
        return String.format("[%d] %s (Cupos: %d/%d) - Horario: %s - Encargado: %s",
                id, clubName, activeMembersCount, maxCapacity, meetingSchedule,
                teacherName != null ? teacherName : "ID " + supervisorTeacherId);
    }
}
