package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain entity representing an inventory resource (book, lab equipment, etc.).
 */
public class ResourceItem {

    private int id;
    private String resourceCode;
    private String resourceName;
    private int categoryId;
    private String description;
    private LocalDate acquisitionDate;
    private String physicalLocation;
    private String status; // 'Disponible', 'Prestado', 'En Mantenimiento', 'Dado de Baja'

    private String categoryName;

    public ResourceItem() {
    }

    public ResourceItem(int id, String resourceCode, String resourceName, int categoryId,
                        String description, LocalDate acquisitionDate, String physicalLocation, String status) {
        this.id = id;
        this.resourceCode = resourceCode;
        this.resourceName = resourceName;
        this.categoryId = categoryId;
        this.description = description;
        this.acquisitionDate = acquisitionDate;
        this.physicalLocation = physicalLocation;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getResourceCode() { return resourceCode; }
    public void setResourceCode(String resourceCode) { this.resourceCode = resourceCode; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getAcquisitionDate() { return acquisitionDate; }
    public void setAcquisitionDate(LocalDate acquisitionDate) { this.acquisitionDate = acquisitionDate; }

    public String getPhysicalLocation() { return physicalLocation; }
    public void setPhysicalLocation(String physicalLocation) { this.physicalLocation = physicalLocation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) | %s | Ubic: %s | Estado: %s",
                id, resourceName, resourceCode,
                categoryName != null ? categoryName : "Cat. " + categoryId,
                physicalLocation, status);
    }
}
