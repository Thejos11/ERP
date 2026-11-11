package com.littlehorizons.erp.model;

/**
 * Domain entity representing the relationship between a student and their guardian.
 */
public class StudentGuardian {

    private int studentId;
    private int guardianId;
    private boolean isPrimary;
    private boolean isEmergencyContact;

    public StudentGuardian() {
    }

    public StudentGuardian(int studentId, int guardianId, boolean isPrimary, boolean isEmergencyContact) {
        this.studentId = studentId;
        this.guardianId = guardianId;
        this.isPrimary = isPrimary;
        this.isEmergencyContact = isEmergencyContact;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getGuardianId() { return guardianId; }
    public void setGuardianId(int guardianId) { this.guardianId = guardianId; }

    public boolean isPrimary() { return isPrimary; }
    public void setPrimary(boolean primary) { isPrimary = primary; }

    public boolean isEmergencyContact() { return isEmergencyContact; }
    public void setEmergencyContact(boolean emergencyContact) { isEmergencyContact = emergencyContact; }
}
