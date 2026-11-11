package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;
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
 * Data Access Object for Teacher and Teacher Assignment operations.
 */
public class TeacherDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean createTeacher(Teacher t) {
        String sql = "INSERT INTO profesores (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                + "primer_apellido, segundo_apellido, correo_electronico, telefono, especializacion, fecha_contratacion, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getDocumentType());
            ps.setString(2, t.getDocumentNumber());
            ps.setString(3, t.getFirstName());
            ps.setString(4, t.getSecondName());
            ps.setString(5, t.getLastName());
            ps.setString(6, t.getSecondLastName());
            ps.setString(7, t.getEmail());
            ps.setString(8, t.getPhone());
            ps.setString(9, t.getSpecialization());
            ps.setDate(10, Date.valueOf(t.getHireDate()));
            ps.setString(11, t.getStatus() != null ? t.getStatus() : "Activo");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        t.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error inserting teacher: " + t.getDocumentNumber(), e);
        }
        return false;
    }

    public Teacher findTeacherById(int id) {
        String sql = "SELECT * FROM profesores WHERE id_profesor = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTeacher(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error finding teacher by id: " + id, e);
        }
        return null;
    }

    public Teacher findTeacherByDocument(String docNumber) {
        String sql = "SELECT * FROM profesores WHERE numero_documento = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, docNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTeacher(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error finding teacher by doc: " + docNumber, e);
        }
        return null;
    }

    public List<Teacher> findAllTeachers() {
        List<Teacher> list = new ArrayList<>();
        String sql = "SELECT * FROM profesores ORDER BY primer_apellido, primer_nombre";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapTeacher(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching all teachers", e);
        }
        return list;
    }

    public boolean assignTeacherToSubject(int teacherId, int subjectId, int year, String section) {
        String sql = "INSERT INTO asignaciones_profesor (id_profesor, id_materia, año_academico, seccion) VALUES (?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, subjectId);
            ps.setInt(3, year);
            ps.setString(4, section);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.logError("Error assigning teacher " + teacherId + " to subject " + subjectId, e);
        }
        return false;
    }

    public List<TeacherAssignment> getAssignmentsForTeacher(int teacherId, int year) {
        List<TeacherAssignment> list = new ArrayList<>();
        String sql = "SELECT ap.*, m.nombre_materia, m.codigo_materia, "
                + "CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM asignaciones_profesor ap "
                + "JOIN materias m ON ap.id_materia = m.id_materia "
                + "JOIN profesores p ON ap.id_profesor = p.id_profesor "
                + "WHERE ap.id_profesor = ? AND ap.año_academico = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TeacherAssignment ta = new TeacherAssignment();
                    ta.setId(rs.getInt("id_asignacion"));
                    ta.setTeacherId(rs.getInt("id_profesor"));
                    ta.setSubjectId(rs.getInt("id_materia"));
                    ta.setAcademicYear(rs.getInt("año_academico"));
                    ta.setSection(rs.getString("seccion"));
                    ta.setTeacherName(rs.getString("nom_prof"));
                    ta.setSubjectName(rs.getString("nombre_materia"));
                    ta.setSubjectCode(rs.getString("codigo_materia"));
                    list.add(ta);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching assignments for teacher: " + teacherId, e);
        }
        return list;
    }

    public List<TeacherAssignment> getAllAssignments(int year) {
        List<TeacherAssignment> list = new ArrayList<>();
        String sql = "SELECT ap.*, m.nombre_materia, m.codigo_materia, "
                + "CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM asignaciones_profesor ap "
                + "JOIN materias m ON ap.id_materia = m.id_materia "
                + "JOIN profesores p ON ap.id_profesor = p.id_profesor "
                + "WHERE ap.año_academico = ? ORDER BY p.primer_apellido, m.nombre_materia";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TeacherAssignment ta = new TeacherAssignment();
                    ta.setId(rs.getInt("id_asignacion"));
                    ta.setTeacherId(rs.getInt("id_profesor"));
                    ta.setSubjectId(rs.getInt("id_materia"));
                    ta.setAcademicYear(rs.getInt("año_academico"));
                    ta.setSection(rs.getString("seccion"));
                    ta.setTeacherName(rs.getString("nom_prof"));
                    ta.setSubjectName(rs.getString("nombre_materia"));
                    ta.setSubjectCode(rs.getString("codigo_materia"));
                    list.add(ta);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching all assignments for year: " + year, e);
        }
        return list;
    }

    public static Teacher mapTeacher(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setId(rs.getInt("id_profesor"));
        t.setDocumentType(rs.getString("tipo_documento"));
        t.setDocumentNumber(rs.getString("numero_documento"));
        t.setFirstName(rs.getString("primer_nombre"));
        t.setSecondName(rs.getString("segundo_nombre"));
        t.setLastName(rs.getString("primer_apellido"));
        t.setSecondLastName(rs.getString("segundo_apellido"));
        t.setEmail(rs.getString("correo_electronico"));
        t.setPhone(rs.getString("telefono"));
        t.setSpecialization(rs.getString("especializacion"));
        t.setHireDate(rs.getDate("fecha_contratacion").toLocalDate());
        t.setStatus(rs.getString("estado"));
        return t;
    }
}
