package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain entity representing a loan of an institutional resource to a teacher.
 */
public class TeacherLoan {

    private int id;
    private int resourceId;
    private int teacherId;
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private String status; // 'Activo', 'Devuelto', 'Vencido'
    private String remarks;

    private String resourceName;
    private String resourceCode;
    private String teacherName;

    public TeacherLoan() {
    }

    public TeacherLoan(int id, int resourceId, int teacherId, LocalDate loanDate,
                       LocalDate expectedReturnDate, LocalDate actualReturnDate, String status, String remarks) {
        this.id = id;
        this.resourceId = resourceId;
        this.teacherId = teacherId;
        this.loanDate = loanDate;
        this.expectedReturnDate = expectedReturnDate;
        this.actualReturnDate = actualReturnDate;
        this.status = status;
        this.remarks = remarks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public LocalDate getLoanDate() { return loanDate; }
    public void setLoanDate(LocalDate loanDate) { this.loanDate = loanDate; }

    public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDate expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }

    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getResourceCode() { return resourceCode; }
    public void setResourceCode(String resourceCode) { this.resourceCode = resourceCode; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) prestado a Profesor: %s | Fec. Préstamo: %s | Dev. Esperada: %s | Estado: %s",
                id, resourceName != null ? resourceName : "Recurso " + resourceId,
                resourceCode != null ? resourceCode : "",
                teacherName != null ? teacherName : "Profesor " + teacherId,
                loanDate, expectedReturnDate, status);
    }
}
