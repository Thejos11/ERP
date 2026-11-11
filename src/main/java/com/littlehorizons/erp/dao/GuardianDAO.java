package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Guardian operations.
 */
public class GuardianDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean createGuardian(Guardian g) {
        String sql = "INSERT INTO acudientes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                + "primer_apellido, segundo_apellido, parentesco, telefono_principal, telefono_secundario, "
                + "correo_electronico, direccion, ocupacion, es_responsable_financiero) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, g.getDocumentType());
            ps.setString(2, g.getDocumentNumber());
            ps.setString(3, g.getFirstName());
            ps.setString(4, g.getSecondName());
            ps.setString(5, g.getLastName());
            ps.setString(6, g.getSecondLastName());
            ps.setString(7, g.getRelationship());
            ps.setString(8, g.getMainPhone());
            ps.setString(9, g.getSecondaryPhone());
            ps.setString(10, g.getEmail());
            ps.setString(11, g.getAddress());
            ps.setString(12, g.getOccupation());
            ps.setBoolean(13, g.isFinancialResponsible());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        g.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error inserting guardian: " + g.getDocumentNumber(), e);
        }
        return false;
    }

    public Guardian findGuardianById(int id) {
        String sql = "SELECT * FROM acudientes WHERE id_acudiente = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapGuardian(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error finding guardian by id: " + id, e);
        }
        return null;
    }

    public Guardian findGuardianByDocument(String docNumber) {
        String sql = "SELECT * FROM acudientes WHERE numero_documento = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, docNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapGuardian(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error finding guardian by doc: " + docNumber, e);
        }
        return null;
    }

    public List<Guardian> findAllGuardians() {
        List<Guardian> list = new ArrayList<>();
        String sql = "SELECT * FROM acudientes ORDER BY primer_apellido, primer_nombre";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapGuardian(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching all guardians", e);
        }
        return list;
    }

    public static Guardian mapGuardian(ResultSet rs) throws SQLException {
        Guardian g = new Guardian();
        g.setId(rs.getInt("id_acudiente"));
        g.setDocumentType(rs.getString("tipo_documento"));
        g.setDocumentNumber(rs.getString("numero_documento"));
        g.setFirstName(rs.getString("primer_nombre"));
        g.setSecondName(rs.getString("segundo_nombre"));
        g.setLastName(rs.getString("primer_apellido"));
        g.setSecondLastName(rs.getString("segundo_apellido"));
        g.setRelationship(rs.getString("parentesco"));
        g.setMainPhone(rs.getString("telefono_principal"));
        g.setSecondaryPhone(rs.getString("telefono_secundario"));
        g.setEmail(rs.getString("correo_electronico"));
        g.setAddress(rs.getString("direccion"));
        g.setOccupation(rs.getString("ocupacion"));
        g.setFinancialResponsible(rs.getBoolean("es_responsable_financiero"));
        return g;
    }
}
