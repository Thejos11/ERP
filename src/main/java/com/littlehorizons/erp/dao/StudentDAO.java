package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
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
 * Data Access Object for Student and Guardian operations.
 * Implements prepared statements, robust exception handling, and transaction safety.
 */
public class StudentDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean createStudent(Student s) {
        String sql = "INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, segundo_nombre, "
                + "primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, correo_electronico, "
                + "tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getDocumentType());
            ps.setString(2, s.getDocumentNumber());
            ps.setString(3, s.getFirstName());
            ps.setString(4, s.getSecondName());
            ps.setString(5, s.getLastName());
            ps.setString(6, s.getSecondLastName());
            ps.setDate(7, Date.valueOf(s.getBirthDate()));
            ps.setString(8, s.getGender());
            ps.setString(9, s.getAddress());
            ps.setString(10, s.getPhone());
            ps.setString(11, s.getEmail());
            ps.setString(12, s.getBloodType());
            ps.setString(13, s.getAllergies());
            ps.setString(14, s.getMedicalConditions());
            ps.setDate(15, Date.valueOf(s.getEntryDate()));
            ps.setString(16, s.getStatus() != null ? s.getStatus() : "Activo");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        s.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error inserting student: " + s.getDocumentNumber(), e);
        }
        return false;
    }

    public Student findStudentById(int id) {
        String sql = "SELECT * FROM estudiantes WHERE id_estudiante = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStudent(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error finding student by id: " + id, e);
        }
        return null;
    }

    public Student findStudentByDocument(String docNumber) {
        String sql = "SELECT * FROM estudiantes WHERE numero_documento = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, docNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapStudent(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error finding student by doc: " + docNumber, e);
        }
        return null;
    }

    public List<Student> findAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes ORDER BY primer_apellido, primer_nombre";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapStudent(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching all students", e);
        }
        return list;
    }

    public boolean linkGuardianToStudent(int studentId, int guardianId, boolean isPrimary, boolean isEmergency) {
        String sql = "INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, es_contacto_emergencia) "
                + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE es_principal = VALUES(es_principal), es_contacto_emergencia = VALUES(es_contacto_emergencia)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, guardianId);
            ps.setBoolean(3, isPrimary);
            ps.setBoolean(4, isEmergency);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.logError("Error linking student " + studentId + " with guardian " + guardianId, e);
        }
        return false;
    }

    public List<Guardian> getGuardiansForStudent(int studentId) {
        List<Guardian> list = new ArrayList<>();
        String sql = "SELECT a.* FROM acudientes a "
                + "JOIN estudiante_acudiente ea ON a.id_acudiente = ea.id_acudiente "
                + "WHERE ea.id_estudiante = ? ORDER BY ea.es_principal DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(GuardianDAO.mapGuardian(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching guardians for student: " + studentId, e);
        }
        return list;
    }

    public List<Student> getStudentsForGuardian(int guardianId) {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT e.* FROM estudiantes e "
                + "JOIN estudiante_acudiente ea ON e.id_estudiante = ea.id_estudiante "
                + "WHERE ea.id_acudiente = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, guardianId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapStudent(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching students for guardian: " + guardianId, e);
        }
        return list;
    }

    public boolean enrollStudentInGrade(int studentId, int gradeId, int year, String remarks) {
        String sql = "INSERT INTO matriculas (id_estudiante, id_grado, año_academico, estado_matricula, fecha_matricula, observaciones) "
                + "VALUES (?, ?, ?, 'Activa', CURDATE(), ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, gradeId);
            ps.setInt(3, year);
            ps.setString(4, remarks);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.logError("Error enrolling student " + studentId + " in grade " + gradeId, e);
        }
        return false;
    }

    public List<Enrollment> getEnrollmentsForStudent(int studentId) {
        List<Enrollment> list = new ArrayList<>();
        String sql = "SELECT m.*, g.nombre_grado, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est "
                + "FROM matriculas m "
                + "JOIN grados g ON m.id_grado = g.id_grado "
                + "JOIN estudiantes e ON m.id_estudiante = e.id_estudiante "
                + "WHERE m.id_estudiante = ? ORDER BY m.año_academico DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Enrollment en = new Enrollment();
                    en.setId(rs.getInt("id_matricula"));
                    en.setStudentId(rs.getInt("id_estudiante"));
                    en.setGradeId(rs.getInt("id_grado"));
                    en.setAcademicYear(rs.getInt("año_academico"));
                    en.setStatus(rs.getString("estado_matricula"));
                    en.setEnrollmentDate(rs.getDate("fecha_matricula").toLocalDate());
                    Date ret = rs.getDate("fecha_retiro");
                    if (ret != null) en.setWithdrawalDate(ret.toLocalDate());
                    en.setRemarks(rs.getString("observaciones"));
                    en.setGradeName(rs.getString("nombre_grado"));
                    en.setStudentName(rs.getString("nom_est"));
                    list.add(en);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching enrollments for student: " + studentId, e);
        }
        return list;
    }

    public List<GradeLevel> findAllGradeLevels() {
        List<GradeLevel> list = new ArrayList<>();
        String sql = "SELECT * FROM grados ORDER BY nivel_numerico";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new GradeLevel(
                        rs.getInt("id_grado"),
                        rs.getString("nombre_grado"),
                        rs.getInt("nivel_numerico")
                ));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching grade levels", e);
        }
        return list;
    }

    public static Student mapStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id_estudiante"));
        s.setDocumentType(rs.getString("tipo_documento"));
        s.setDocumentNumber(rs.getString("numero_documento"));
        s.setFirstName(rs.getString("primer_nombre"));
        s.setSecondName(rs.getString("segundo_nombre"));
        s.setLastName(rs.getString("primer_apellido"));
        s.setSecondLastName(rs.getString("segundo_apellido"));
        s.setBirthDate(rs.getDate("fecha_nacimiento").toLocalDate());
        s.setGender(rs.getString("genero"));
        s.setAddress(rs.getString("direccion"));
        s.setPhone(rs.getString("telefono"));
        s.setEmail(rs.getString("correo_electronico"));
        s.setBloodType(rs.getString("tipo_sangre"));
        s.setAllergies(rs.getString("alergias"));
        s.setMedicalConditions(rs.getString("condiciones_medicas"));
        s.setEntryDate(rs.getDate("fecha_ingreso").toLocalDate());
        s.setStatus(rs.getString("estado"));
        return s;
    }
}
