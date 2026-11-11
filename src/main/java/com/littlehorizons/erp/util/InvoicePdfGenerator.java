package com.littlehorizons.erp.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Utility class to generate official Colombian DIAN-compliant Electronic Invoices in PDF format.
 * Utilizes OpenPDF for PDF creation and ZXing for QR code generation.
 */
public class InvoicePdfGenerator {

    private static final String INVOICE_DIR = "invoices";
    private static final String SCHOOL_NAME = "Colegio Internacional Little Horizons S.A.S.";
    private static final String SCHOOL_NIT = "901.458.732-4";
    private static final String SCHOOL_ADDRESS = "Avenida Suba # 128-45, Bogotá D.C., Colombia";
    private static final String SCHOOL_PHONE = "(+57) 601 745 8920 / (+57) 310 456 7890";
    private static final String SCHOOL_EMAIL = "facturacion@littlehorizons.edu.co";
    private static final String DIAN_RESOLUTION = "Resolución DIAN No. 18764000001234 de 2024-01-15 | Prefijo: FACT | Rango: 0001 al 10000 | Vigencia: 24 meses";

    private InvoicePdfGenerator() {
        // Utility class
    }

    /**
     * Calculates a simulated Colombian CUFE (Código Único de Factura Electrónica) using SHA-256.
     *
     * @param invoiceNumber Invoice number.
     * @param date Issue date.
     * @param amount Total amount.
     * @param customerDoc Customer identity document.
     * @return Hexadecimal SHA-256 CUFE string.
     */
    public static String calculateCufe(String invoiceNumber, LocalDate date, BigDecimal amount, String customerDoc) {
        try {
            String raw = invoiceNumber + SCHOOL_NIT + date.toString() + amount.toPlainString() + customerDoc + "LH_SECRET_KEY_2026";
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            LoggerUtil.logError("Error generating CUFE", e);
            return "cufe-simulado-" + System.currentTimeMillis();
        }
    }

    /**
     * Generates a 2D QR Code image as bytes.
     *
     * @param content Content payload to encode.
     * @param width Width in pixels.
     * @param height Height in pixels.
     * @return PNG image byte array.
     */
    public static byte[] generateQrCodeImage(String content, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            LoggerUtil.logError("Failed to generate QR Code", e);
            return new byte[0];
        }
    }

    /**
     * Generates an electronic invoice PDF document and saves it in the invoices folder.
     *
     * @param invoiceNumber Consecutive invoice number (e.g. FACT-2026-0001).
     * @param issueDate Date of issue.
     * @param guardianName Full name of parent/guardian.
     * @param guardianDoc Identity document of parent/guardian.
     * @param guardianAddress Address of parent/guardian.
     * @param studentName Full name of enrolled student.
     * @param conceptDescription Name/description of payment concept.
     * @param quantity Quantity of items.
     * @param unitPrice Unit price in COP.
     * @param paymentMethod Method used (Efectivo, Transferencia, etc.).
     * @param receiptNumber Receipt number.
     * @return File path of the generated PDF invoice.
     */
    public static String generateInvoicePdf(
            String invoiceNumber,
            LocalDate issueDate,
            String guardianName,
            String guardianDoc,
            String guardianAddress,
            String studentName,
            String conceptDescription,
            int quantity,
            BigDecimal unitPrice,
            String paymentMethod,
            String receiptNumber) {

        File directory = new File(INVOICE_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String safeFileName = invoiceNumber.replaceAll("[^a-zA-Z0-9_-]", "_") + ".pdf";
        File pdfFile = new File(directory, safeFileName);

        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal iva = BigDecimal.ZERO; // Colombian educational services are VAT exempt (Art. 476 E.T.)
        BigDecimal total = subtotal.add(iva);

        String cufe = calculateCufe(invoiceNumber, issueDate, total, guardianDoc);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CO"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat currencyFormat = new DecimalFormat("$ #,##0.00", symbols);

        Document document = new Document();
        document.setPageSize(new Rectangle(595, 842)); // Standard A4
        document.setMargins(36, 36, 36, 36);

        try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
            PdfWriter.getInstance(document, fos);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(17, 34, 78));
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font sectionTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
            Font cufeFont = FontFactory.getFont(FontFactory.COURIER, 7, Color.GRAY);

            // Header Table
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{65, 35});

            // School Details
            PdfPCell schoolCell = new PdfPCell();
            schoolCell.setBorder(Rectangle.NO_BORDER);
            schoolCell.addElement(new Paragraph(SCHOOL_NAME, titleFont));
            schoolCell.addElement(new Paragraph("NIT: " + SCHOOL_NIT + " | Régimen Común", subtitleFont));
            schoolCell.addElement(new Paragraph(SCHOOL_ADDRESS, subtitleFont));
            schoolCell.addElement(new Paragraph("Tel: " + SCHOOL_PHONE + " | " + SCHOOL_EMAIL, subtitleFont));
            headerTable.addCell(schoolCell);

            // Invoice Box
            PdfPCell invoiceBoxCell = new PdfPCell();
            invoiceBoxCell.setBackgroundColor(new Color(242, 245, 250));
            invoiceBoxCell.setBorderColor(new Color(20, 60, 120));
            invoiceBoxCell.setBorderWidth(1.2f);
            invoiceBoxCell.setPadding(8);
            Paragraph invTitle = new Paragraph("FACTURA ELECTRÓNICA DE VENTA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(20, 60, 120)));
            invTitle.setAlignment(Element.ALIGN_CENTER);
            Paragraph invNumber = new Paragraph("No. " + invoiceNumber, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.RED));
            invNumber.setAlignment(Element.ALIGN_CENTER);
            Paragraph invDate = new Paragraph("Fecha Emisión: " + issueDate.toString(), subtitleFont);
            invDate.setAlignment(Element.ALIGN_CENTER);
            invoiceBoxCell.addElement(invTitle);
            invoiceBoxCell.addElement(invNumber);
            invoiceBoxCell.addElement(invDate);
            headerTable.addCell(invoiceBoxCell);

            document.add(headerTable);
            document.add(new Paragraph(" "));

            // DIAN Authorization Bar
            PdfPTable dianBar = new PdfPTable(1);
            dianBar.setWidthPercentage(100);
            PdfPCell dianCell = new PdfPCell(new Phrase(DIAN_RESOLUTION, FontFactory.getFont(FontFactory.HELVETICA, 7, Color.DARK_GRAY)));
            dianCell.setBackgroundColor(new Color(240, 240, 240));
            dianCell.setBorder(Rectangle.BOX);
            dianCell.setBorderColor(Color.LIGHT_GRAY);
            dianCell.setPadding(4);
            dianCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            dianBar.addCell(dianCell);
            document.add(dianBar);
            document.add(new Paragraph(" "));

            // Customer & Student Info Table
            PdfPTable clientTable = new PdfPTable(2);
            clientTable.setWidthPercentage(100);
            clientTable.setWidths(new float[]{50, 50});

            PdfPCell leftClient = new PdfPCell();
            leftClient.setPadding(6);
            leftClient.setBorderColor(Color.LIGHT_GRAY);
            leftClient.addElement(new Paragraph("DATOS DEL CLIENTE / ACUDIENTE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(20, 60, 120))));
            leftClient.addElement(new Paragraph("Nombre: " + (guardianName != null ? guardianName : "N/A"), regularFont));
            leftClient.addElement(new Paragraph("Documento: " + (guardianDoc != null ? guardianDoc : "N/A"), regularFont));
            leftClient.addElement(new Paragraph("Dirección: " + (guardianAddress != null ? guardianAddress : "N/A"), regularFont));
            clientTable.addCell(leftClient);

            PdfPCell rightClient = new PdfPCell();
            rightClient.setPadding(6);
            rightClient.setBorderColor(Color.LIGHT_GRAY);
            rightClient.addElement(new Paragraph("INFORMACIÓN ACADÉMICA / PAGO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(20, 60, 120))));
            rightClient.addElement(new Paragraph("Estudiante: " + (studentName != null ? studentName : "N/A"), regularFont));
            rightClient.addElement(new Paragraph("Método de Pago: " + (paymentMethod != null ? paymentMethod : "N/A"), regularFont));
            rightClient.addElement(new Paragraph("Recibo Caja No: " + (receiptNumber != null ? receiptNumber : "N/A"), regularFont));
            clientTable.addCell(rightClient);

            document.add(clientTable);
            document.add(new Paragraph(" "));

            // Itemized Concept Table
            PdfPTable itemsTable = new PdfPTable(4);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{50, 15, 17, 18});

            // Table Header
            String[] headers = {"Descripción del Concepto", "Cantidad", "Valor Unitario", "Subtotal"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, sectionTitle));
                cell.setBackgroundColor(new Color(20, 60, 120));
                cell.setPadding(5);
                cell.setHorizontalAlignment(h.equals("Descripción del Concepto") ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
                itemsTable.addCell(cell);
            }

            // Table Row
            PdfPCell descCell = new PdfPCell(new Phrase(conceptDescription, regularFont));
            descCell.setPadding(5);
            itemsTable.addCell(descCell);

            PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(quantity), regularFont));
            qtyCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            qtyCell.setPadding(5);
            itemsTable.addCell(qtyCell);

            PdfPCell unitCell = new PdfPCell(new Phrase(currencyFormat.format(unitPrice), regularFont));
            unitCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            unitCell.setPadding(5);
            itemsTable.addCell(unitCell);

            PdfPCell subCell = new PdfPCell(new Phrase(currencyFormat.format(subtotal), regularFont));
            subCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subCell.setPadding(5);
            itemsTable.addCell(subCell);

            document.add(itemsTable);

            // Totals Table
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(40);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalsTable.setWidths(new float[]{50, 50});

            addTotalRow(totalsTable, "Subtotal:", currencyFormat.format(subtotal), regularFont);
            addTotalRow(totalsTable, "IVA (0% Exento):", currencyFormat.format(iva), regularFont);
            addTotalRow(totalsTable, "TOTAL A PAGAR:", currencyFormat.format(total), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(20, 60, 120)));

            document.add(totalsTable);
            document.add(new Paragraph(" "));

            // DIAN QR Code and CUFE Table
            PdfPTable dianFooterTable = new PdfPTable(2);
            dianFooterTable.setWidthPercentage(100);
            dianFooterTable.setWidths(new float[]{25, 75});

            // Generate QR Code Image
            String qrPayload = "NumFac: " + invoiceNumber + "\n"
                    + "FecFac: " + issueDate + "\n"
                    + "NitFac: " + SCHOOL_NIT + "\n"
                    + "DocAdq: " + guardianDoc + "\n"
                    + "ValFac: " + total.toPlainString() + "\n"
                    + "CUFE: " + cufe;

            byte[] qrBytes = generateQrCodeImage(qrPayload, 120, 120);
            if (qrBytes.length > 0) {
                Image qrImage = Image.getInstance(qrBytes);
                qrImage.scaleToFit(90, 90);
                PdfPCell qrCell = new PdfPCell(qrImage);
                qrCell.setBorder(Rectangle.BOX);
                qrCell.setBorderColor(Color.LIGHT_GRAY);
                qrCell.setPadding(5);
                qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                dianFooterTable.addCell(qrCell);
            } else {
                PdfPCell emptyQr = new PdfPCell(new Phrase("[Código QR Simulado]", regularFont));
                emptyQr.setBorder(Rectangle.BOX);
                dianFooterTable.addCell(emptyQr);
            }

            // CUFE and Legal text
            PdfPCell cufeCell = new PdfPCell();
            cufeCell.setBorder(Rectangle.BOX);
            cufeCell.setBorderColor(Color.LIGHT_GRAY);
            cufeCell.setPadding(6);
            cufeCell.addElement(new Paragraph("CUFE (Código Único de Factura Electrónica):", boldFont));
            cufeCell.addElement(new Paragraph(cufe, cufeFont));
            cufeCell.addElement(new Paragraph(" "));
            cufeCell.addElement(new Paragraph("Esta factura electrónica es un documento legalmente simulado con fines pedagógicos conforme al Anexo Técnico de la DIAN.", subtitleFont));
            dianFooterTable.addCell(cufeCell);

            document.add(dianFooterTable);

            document.close();
            LoggerUtil.logInfo("Electronic Invoice generated successfully at: " + pdfFile.getAbsolutePath());
            return pdfFile.getAbsolutePath();

        } catch (Exception e) {
            LoggerUtil.logError("Failed to generate PDF for invoice: " + invoiceNumber, e);
            return null;
        }
    }

    private static void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(3);
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(3);
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }
}
