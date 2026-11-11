package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Financial transactions, Concepts, and Invoices.
 */
public class FinanceDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public List<PaymentConcept> findAllConcepts() {
        List<PaymentConcept> list = new ArrayList<>();
        String sql = "SELECT * FROM conceptos_pago WHERE esta_activo = TRUE ORDER BY id_concepto";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapConcept(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching payment concepts", e);
        }
        return list;
    }

    public PaymentConcept findConceptById(int id) {
        String sql = "SELECT * FROM conceptos_pago WHERE id_concepto = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapConcept(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error finding payment concept: " + id, e);
        }
        return null;
    }

    public boolean createConcept(PaymentConcept c) {
        String sql = "INSERT INTO conceptos_pago (codigo_concepto, nombre_concepto, descripcion, monto_base, es_recurrente, esta_activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getConceptCode());
            ps.setString(2, c.getConceptName());
            ps.setString(3, c.getDescription());
            ps.setBigDecimal(4, c.getBaseAmount());
            ps.setBoolean(5, c.isRecurring());
            ps.setBoolean(6, c.isActive());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        c.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error creating payment concept: " + c.getConceptCode(), e);
        }
        return false;
    }

    public boolean recordPayment(Payment p) {
        String sql = "INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, fecha_pago, metodo_pago, numero_recibo, año_academico, periodo_academico, notas) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getStudentId());
            if (p.getGuardianId() != null) {
                ps.setInt(2, p.getGuardianId());
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setInt(3, p.getConceptId());
            ps.setBigDecimal(4, p.getAmountPaid());
            ps.setDate(5, Date.valueOf(p.getPaymentDate()));
            ps.setString(6, p.getPaymentMethod());
            ps.setString(7, p.getReceiptNumber());
            if (p.getAcademicYear() != null) {
                ps.setInt(8, p.getAcademicYear());
            } else {
                ps.setNull(8, java.sql.Types.INTEGER);
            }
            ps.setString(9, p.getAcademicPeriod());
            ps.setString(10, p.getNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        p.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error recording payment for student: " + p.getStudentId(), e);
        }
        return false;
    }

    public Payment findPaymentById(int id) {
        String sql = "SELECT p.*, cp.nombre_concepto, "
                + "CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acud "
                + "FROM pagos p "
                + "JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto "
                + "JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                + "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente "
                + "WHERE p.id_pago = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPayment(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error finding payment by id: " + id, e);
        }
        return null;
    }

    public List<Payment> findPaymentsByStudent(int studentId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, cp.nombre_concepto, "
                + "CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acud "
                + "FROM pagos p "
                + "JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto "
                + "JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                + "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente "
                + "WHERE p.id_estudiante = ? ORDER BY p.fecha_pago DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPayment(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching payments for student: " + studentId, e);
        }
        return list;
    }

    public List<Payment> findPaymentsByGuardian(int guardianId) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT p.*, cp.nombre_concepto, "
                + "CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(a.primer_nombre, ' ', a.primer_apellido) AS nom_acud "
                + "FROM pagos p "
                + "JOIN conceptos_pago cp ON p.id_concepto = cp.id_concepto "
                + "JOIN estudiantes e ON p.id_estudiante = e.id_estudiante "
                + "LEFT JOIN acudientes a ON p.id_acudiente = a.id_acudiente "
                + "WHERE p.id_acudiente = ? ORDER BY p.fecha_pago DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guardianId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPayment(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching payments for guardian: " + guardianId, e);
        }
        return list;
    }

    public List<String> getOverdueStudentsReport() {
        List<String> list = new ArrayList<>();
        // Query active students who have NOT paid pension within the last 30 days
        String sql = "SELECT e.id_estudiante, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "e.numero_documento, MAX(p.fecha_pago) AS ultimo_pago "
                + "FROM estudiantes e "
                + "LEFT JOIN pagos p ON e.id_estudiante = p.id_estudiante AND p.id_concepto = 2 "
                + "WHERE e.estado = 'Activo' "
                + "GROUP BY e.id_estudiante, e.primer_nombre, e.primer_apellido, e.numero_documento "
                + "HAVING ultimo_pago IS NULL OR ultimo_pago < DATE_SUB(CURDATE(), INTERVAL 30 DAY) "
                + "ORDER BY ultimo_pago ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date lastDate = rs.getDate("ultimo_pago");
                String strDate = (lastDate != null) ? lastDate.toString() : "Sin pagos registrados";
                list.add(String.format("Estudiante: %s (Doc: %s) | Último Pago Pensión: %s",
                        rs.getString("nom_est"), rs.getString("numero_documento"), strDate));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error generating overdue report", e);
        }
        return list;
    }

    public boolean createInvoice(Invoice inv) {
        String sql = "INSERT INTO facturas (numero_factura, id_pago, fecha_emision, cufe, nombre_archivo_pdf, ruta_archivo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, inv.getInvoiceNumber());
            ps.setInt(2, inv.getPaymentId());
            ps.setDate(3, Date.valueOf(inv.getIssueDate()));
            ps.setString(4, inv.getCufe());
            ps.setString(5, inv.getPdfFileName());
            ps.setString(6, inv.getFilePath());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        inv.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error inserting invoice: " + inv.getInvoiceNumber(), e);
        }
        return false;
    }

    public String generateNextInvoiceNumber() {
        String sql = "SELECT COUNT(*) FROM facturas";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int nextVal = rs.getInt(1) + 1;
                return String.format("FACT-2026-%04d", nextVal);
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error generating invoice consecutive", e);
        }
        return "FACT-2026-" + System.currentTimeMillis() % 10000;
    }

    public String generateNextReceiptNumber() {
        String sql = "SELECT COUNT(*) FROM pagos";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int nextVal = rs.getInt(1) + 1;
                return String.format("REC-2026-%04d", nextVal);
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error generating receipt consecutive", e);
        }
        return "REC-2026-" + System.currentTimeMillis() % 10000;
    }

    private static PaymentConcept mapConcept(ResultSet rs) throws SQLException {
        PaymentConcept c = new PaymentConcept();
        c.setId(rs.getInt("id_concepto"));
        c.setConceptCode(rs.getString("codigo_concepto"));
        c.setConceptName(rs.getString("nombre_concepto"));
        c.setDescription(rs.getString("descripcion"));
        c.setBaseAmount(rs.getBigDecimal("monto_base"));
        c.setRecurring(rs.getBoolean("es_recurrente"));
        c.setActive(rs.getBoolean("esta_activo"));
        return c;
    }

    private static Payment mapPayment(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id_pago"));
        p.setStudentId(rs.getInt("id_estudiante"));
        int acud = rs.getInt("id_acudiente");
        if (!rs.wasNull()) p.setGuardianId(acud);
        p.setConceptId(rs.getInt("id_concepto"));
        p.setAmountPaid(rs.getBigDecimal("monto_pagado"));
        p.setPaymentDate(rs.getDate("fecha_pago").toLocalDate());
        p.setPaymentMethod(rs.getString("metodo_pago"));
        p.setReceiptNumber(rs.getString("numero_recibo"));
        int yr = rs.getInt("año_academico");
        if (!rs.wasNull()) p.setAcademicYear(yr);
        p.setAcademicPeriod(rs.getString("periodo_academico"));
        p.setNotes(rs.getString("notas"));
        p.setConceptName(rs.getString("nombre_concepto"));
        p.setStudentName(rs.getString("nom_est"));
        p.setGuardianName(rs.getString("nom_acud"));
        return p;
    }
}
