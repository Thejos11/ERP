package com.littlehorizons.erp.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for Electronic Invoice PDF generation and DIAN compliance elements.
 */
public class InvoicePdfGeneratorTest {

    @Test
    public void testGenerateInvoicePdf() {
        String invoiceNumber = "FACT-TEST-0001";
        LocalDate date = LocalDate.of(2026, 3, 15);
        String guardianName = "Carolina Rodriguez Sarmiento";
        String guardianDoc = "1014234562";
        String guardianAddress = "Carrera 15 # 85-30, Bogotá";
        String studentName = "Sofia Isabella Gomez Rodriguez";
        String concept = "Pensión Mensual Marzo 2026";
        int qty = 1;
        BigDecimal price = new BigDecimal("380000.00");
        String method = "Transferencia";
        String receipt = "REC-2026-9999";

        String path = InvoicePdfGenerator.generateInvoicePdf(
                invoiceNumber, date, guardianName, guardianDoc, guardianAddress,
                studentName, concept, qty, price, method, receipt
        );

        assertNotNull(path, "Generated path should not be null");
        File file = new File(path);
        assertTrue(file.exists(), "PDF file must exist on disk");
        assertTrue(file.length() > 0, "PDF file size must be greater than zero bytes");

        String cufe = InvoicePdfGenerator.calculateCufe(invoiceNumber, date, price, guardianDoc);
        assertNotNull(cufe, "CUFE should not be null");
        assertTrue(cufe.length() >= 32, "CUFE must be a valid hash string");
    }
}
