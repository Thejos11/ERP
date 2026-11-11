package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.FinanceDAO;
import com.littlehorizons.erp.dao.GuardianDAO;
import com.littlehorizons.erp.dao.StudentDAO;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.InvoicePdfGenerator;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller managing school finances, tuition, customized billing,
 * payment history, overdue debt alerts, and automated DIAN-compliant PDF invoice generation.
 */
public class FinanceController {

    private final FinanceDAO financeDAO = new FinanceDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final GuardianDAO guardianDAO = new GuardianDAO();

    public List<PaymentConcept> getAllPaymentConcepts() {
        return financeDAO.findAllConcepts();
    }

    public PaymentConcept getConceptById(int id) {
        return financeDAO.findConceptById(id);
    }

    public boolean createPaymentConcept(PaymentConcept concept) {
        return financeDAO.createConcept(concept);
    }

    /**
     * Registers a payment, creates receipt number, and triggers automated electronic PDF generation.
     *
     * @param payment The payment to register.
     * @param generatePdf Whether to generate and save a PDF invoice.
     * @return Result message containing receipt number and PDF invoice path.
     */
    public String processPayment(Payment payment, boolean generatePdf) {
        if (payment == null || payment.getAmountPaid() == null || payment.getAmountPaid().compareTo(BigDecimal.ZERO) <= 0) {
            return "❌ Monto de pago inválido.";
        }

        if (payment.getReceiptNumber() == null || payment.getReceiptNumber().isBlank()) {
            payment.setReceiptNumber(financeDAO.generateNextReceiptNumber());
        }

        boolean saved = financeDAO.recordPayment(payment);
        if (!saved) {
            return "❌ Error al guardar el pago en la base de datos.";
        }

        StringBuilder response = new StringBuilder();
        response.append(String.format("✅ Pago registrado exitosamente con Recibo No. %s por un valor de $ %,.2f.%n",
                payment.getReceiptNumber(), payment.getAmountPaid().doubleValue()));

        if (generatePdf) {
            // Retrieve full details for PDF
            Student student = studentDAO.findStudentById(payment.getStudentId());
            Guardian guardian = payment.getGuardianId() != null
                    ? guardianDAO.findGuardianById(payment.getGuardianId())
                    : null;
            PaymentConcept concept = financeDAO.findConceptById(payment.getConceptId());

            String studentName = (student != null) ? student.getFullName() : "Estudiante ID " + payment.getStudentId();
            String guardianName = (guardian != null) ? guardian.getFullName() : "Responsable Pago";
            String guardianDoc = (guardian != null) ? guardian.getDocumentNumber() : "222222222222";
            String guardianAddress = (guardian != null) ? guardian.getAddress() : "Bogotá D.C.";
            String conceptDesc = (concept != null) ? concept.getConceptName() : "Servicio Educativo Escolar";

            String invoiceNumber = financeDAO.generateNextInvoiceNumber();
            LocalDate today = (payment.getPaymentDate() != null) ? payment.getPaymentDate() : LocalDate.now();

            String pdfPath = InvoicePdfGenerator.generateInvoicePdf(
                    invoiceNumber,
                    today,
                    guardianName,
                    guardianDoc,
                    guardianAddress,
                    studentName,
                    conceptDesc,
                    1,
                    payment.getAmountPaid(),
                    payment.getPaymentMethod(),
                    payment.getReceiptNumber()
            );

            if (pdfPath != null) {
                File f = new File(pdfPath);
                String cufe = InvoicePdfGenerator.calculateCufe(invoiceNumber, today, payment.getAmountPaid(), guardianDoc);
                Invoice inv = new Invoice(0, invoiceNumber, payment.getId(), today, cufe, f.getName(), pdfPath);
                financeDAO.createInvoice(inv);

                response.append(String.format("📄 Factura Electrónica DIAN generada: %s%n", invoiceNumber));
                response.append(String.format("📁 Ubicación del archivo: %s%n", pdfPath));
            } else {
                response.append("⚠️ El pago se registró pero ocurrió un problema al generar el archivo PDF.");
            }
        }

        return response.toString();
    }

    public List<Payment> getPaymentsForStudent(int studentId) {
        return financeDAO.findPaymentsByStudent(studentId);
    }

    public List<Payment> getPaymentsForGuardian(int guardianId) {
        return financeDAO.findPaymentsByGuardian(guardianId);
    }

    public List<String> getOverdueDebtorsReport() {
        return financeDAO.getOverdueStudentsReport();
    }
}
