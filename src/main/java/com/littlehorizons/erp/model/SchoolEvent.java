package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain entity representing a school event (e.g. Science Fair, Sports Day).
 */
public class SchoolEvent {

    private int id;
    private String eventName;
    private String description;
    private String eventType; // 'Académico', 'Cultural', 'Deportivo', 'Social', 'Científico'
    private LocalDate eventDate;
    private String location;
    private boolean hasCompetition;
    private Integer organizerTeacherId;
    private boolean isActive;

    private String organizerName;

    public SchoolEvent() {
    }

    public SchoolEvent(int id, String eventName, String description, String eventType,
                       LocalDate eventDate, String location, boolean hasCompetition,
                       Integer organizerTeacherId, boolean isActive) {
        this.id = id;
        this.eventName = eventName;
        this.description = description;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.location = location;
        this.hasCompetition = hasCompetition;
        this.organizerTeacherId = organizerTeacherId;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isHasCompetition() { return hasCompetition; }
    public void setHasCompetition(boolean hasCompetition) { this.hasCompetition = hasCompetition; }

    public Integer getOrganizerTeacherId() { return organizerTeacherId; }
    public void setOrganizerTeacherId(Integer organizerTeacherId) { this.organizerTeacherId = organizerTeacherId; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Fecha: %s - Lugar: %s %s",
                id, eventName, eventType, eventDate, location,
                hasCompetition ? "[Con Competencia]" : "");
    }
}
