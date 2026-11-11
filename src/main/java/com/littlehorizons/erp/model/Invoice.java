package com.littlehorizons.erp.model;

import java.time.LocalDate;

/**
 * Domain entity representing an electronic invoice record.
 */
public class Invoice {

    private int id;
    private String invoiceNumber;
    private int paymentId;
    private LocalDate issueDate;
    private String cufe;
    private String pdfFileName;
    private String filePath;

    public Invoice() {
    }

    public Invoice(int id, String invoiceNumber, int paymentId, LocalDate issueDate,
                   String cufe, String pdfFileName, String filePath) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.paymentId = paymentId;
        this.issueDate = issueDate;
        this.cufe = cufe;
        this.pdfFileName = pdfFileName;
        this.filePath = filePath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public String getCufe() { return cufe; }
    public void setCufe(String cufe) { this.cufe = cufe; }

    public String getPdfFileName() { return pdfFileName; }
    public void setPdfFileName(String pdfFileName) { this.pdfFileName = pdfFileName; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    @Override
    public String toString() {
        return String.format("[%d] Factura: %s | Pago ID: %d | Emisión: %s | Archivo: %s",
                id, invoiceNumber, paymentId, issueDate, pdfFileName);
    }
}
