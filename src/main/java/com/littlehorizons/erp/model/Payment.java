package com.littlehorizons.erp.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Domain entity representing a payment transaction record.
 */
public class Payment {

    private int id;
    private int studentId;
    private Integer guardianId;
    private int conceptId;
    private BigDecimal amountPaid;
    private LocalDate paymentDate;
    private String paymentMethod; // 'Efectivo', 'Transferencia', 'Tarjeta Débito', 'Tarjeta Crédito', 'Cheque'
    private String receiptNumber;
    private Integer academicYear;
    private String academicPeriod; // 'I', 'II', 'III', 'IV', 'Anual'
    private String notes;

    // View helper fields
    private String studentName;
    private String guardianName;
    private String conceptName;

    public Payment() {
    }

    public Payment(int id, int studentId, Integer guardianId, int conceptId, BigDecimal amountPaid,
                   LocalDate paymentDate, String paymentMethod, String receiptNumber,
                   Integer academicYear, String academicPeriod, String notes) {
        this.id = id;
        this.studentId = studentId;
        this.guardianId = guardianId;
        this.conceptId = conceptId;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.receiptNumber = receiptNumber;
        this.academicYear = academicYear;
        this.academicPeriod = academicPeriod;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public Integer getGuardianId() { return guardianId; }
    public void setGuardianId(Integer guardianId) { this.guardianId = guardianId; }

    public int getConceptId() { return conceptId; }
    public void setConceptId(int conceptId) { this.conceptId = conceptId; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public Integer getAcademicYear() { return academicYear; }
    public void setAcademicYear(Integer academicYear) { this.academicYear = academicYear; }

    public String getAcademicPeriod() { return academicPeriod; }
    public void setAcademicPeriod(String academicPeriod) { this.academicPeriod = academicPeriod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }

    public String getConceptName() { return conceptName; }
    public void setConceptName(String conceptName) { this.conceptName = conceptName; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | $ %,.2f via %s (Recibo: %s) - %s",
                id, paymentDate, studentName != null ? studentName : studentId,
                amountPaid != null ? amountPaid.doubleValue() : 0.0,
                paymentMethod, receiptNumber != null ? receiptNumber : "N/A",
                conceptName != null ? conceptName : conceptId);
    }
}
