package com.littlehorizons.erp.model;

import java.math.BigDecimal;

/**
 * Domain entity representing a billing payment concept (e.g. Tuition, Uniform, Transport).
 */
public class PaymentConcept {

    private int id;
    private String conceptCode;
    private String conceptName;
    private String description;
    private BigDecimal baseAmount;
    private boolean isRecurring;
    private boolean isActive;

    public PaymentConcept() {
    }

    public PaymentConcept(int id, String conceptCode, String conceptName, String description,
                          BigDecimal baseAmount, boolean isRecurring, boolean isActive) {
        this.id = id;
        this.conceptCode = conceptCode;
        this.conceptName = conceptName;
        this.description = description;
        this.baseAmount = baseAmount;
        this.isRecurring = isRecurring;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getConceptCode() { return conceptCode; }
    public void setConceptCode(String conceptCode) { this.conceptCode = conceptCode; }

    public String getConceptName() { return conceptName; }
    public void setConceptName(String conceptName) { this.conceptName = conceptName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Base: $ %,.2f %s",
                id, conceptName, conceptCode, baseAmount != null ? baseAmount.doubleValue() : 0.0,
                isRecurring ? "[Recurrente]" : "");
    }
}
