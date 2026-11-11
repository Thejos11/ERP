package com.littlehorizons.erp.model;

/**
 * Domain entity representing a student's participation in an event.
 */
public class EventParticipant {

    private int id;
    private int eventId;
    private int studentId;
    private String role; // 'Participante', 'Competidor', 'Organizador', 'Asistente'
    private String positionWon; // '1er Lugar', '2do Lugar', '3er Lugar', 'Participante'
    private String remarks;

    private String studentName;
    private String eventName;

    public EventParticipant() {
    }

    public EventParticipant(int id, int eventId, int studentId, String role, String positionWon, String remarks) {
        this.id = id;
        this.eventId = eventId;
        this.studentId = studentId;
        this.role = role;
        this.positionWon = positionWon;
        this.remarks = remarks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPositionWon() { return positionWon; }
    public void setPositionWon(String positionWon) { this.positionWon = positionWon; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    @Override
    public String toString() {
        return String.format("[%d] %s en %s | Rol: %s | Puesto: %s %s",
                id, studentName != null ? studentName : studentId,
                eventName != null ? eventName : eventId,
                role, positionWon != null ? positionWon : "N/A",
                remarks != null ? "(" + remarks + ")" : "");
    }
}
