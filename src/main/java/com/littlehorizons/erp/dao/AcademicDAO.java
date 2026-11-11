package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.SubjectEnrollment;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Academic subjects, enrollments, grades (AcademicRecord), and attendance.
 */
public class AcademicDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean createSubject(Subject s) {
        String sql = "INSERT INTO materias (codigo_materia, nombre_materia, descripcion, id_grado, horas_semanales, esta_activa) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getSubjectCode());
            ps.setString(2, s.getSubjectName());
            ps.setString(3, s.getDescription());
            ps.setInt(4, s.getGradeId());
            ps.setInt(5, s.getWeeklyHours());
            ps.setBoolean(6, s.isActive());

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
            LoggerUtil.logError("Error creating subject: " + s.getSubjectCode(), e);
        }
        return false;
    }

    public Subject findSubjectById(int id) {
        String sql = "SELECT m.*, g.nombre_grado FROM materias m JOIN grados g ON m.id_grado = g.id_grado WHERE m.id_materia = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSubject(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error finding subject by id: " + id, e);
        }
        return null;
    }

    public List<Subject> findSubjectsByGrade(int gradeId) {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT m.*, g.nombre_grado FROM materias m JOIN grados g ON m.id_grado = g.id_grado WHERE m.id_grado = ? AND m.esta_activa = TRUE";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gradeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSubject(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching subjects for grade: " + gradeId, e);
        }
        return list;
    }

    public List<Subject> findAllSubjects() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT m.*, g.nombre_grado FROM materias m JOIN grados g ON m.id_grado = g.id_grado ORDER BY g.nivel_numerico, m.nombre_materia";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapSubject(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching all subjects", e);
        }
        return list;
    }

    public boolean enrollStudentInSubject(int studentId, int subjectId, int year, String period, String section) {
        String sql = "INSERT INTO inscripciones_materia (id_estudiante, id_materia, año_academico, periodo_academico, seccion, fecha_inscripcion, estado) "
                + "VALUES (?, ?, ?, ?, ?, CURDATE(), 'Activa')";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, year);
            ps.setString(4, period);
            ps.setString(5, section != null ? section : "A");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.logError("Error enrolling student in subject: " + studentId + " -> " + subjectId, e);
        }
        return false;
    }

    public List<SubjectEnrollment> getStudentSubjectEnrollments(int studentId, int year, String period) {
        List<SubjectEnrollment> list = new ArrayList<>();
        String sql = "SELECT im.*, m.nombre_materia, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est "
                + "FROM inscripciones_materia im "
                + "JOIN materias m ON im.id_materia = m.id_materia "
                + "JOIN estudiantes e ON im.id_estudiante = e.id_estudiante "
                + "WHERE im.id_estudiante = ? AND im.año_academico = ? AND im.periodo_academico = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, year);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SubjectEnrollment se = new SubjectEnrollment();
                    se.setId(rs.getInt("id_inscripcion"));
                    se.setStudentId(rs.getInt("id_estudiante"));
                    se.setSubjectId(rs.getInt("id_materia"));
                    se.setAcademicYear(rs.getInt("año_academico"));
                    se.setAcademicPeriod(rs.getString("periodo_academico"));
                    se.setSection(rs.getString("seccion"));
                    se.setEnrollmentDate(rs.getDate("fecha_inscripcion").toLocalDate());
                    se.setStatus(rs.getString("estado"));
                    se.setSubjectName(rs.getString("nombre_materia"));
                    se.setStudentName(rs.getString("nom_est"));
                    list.add(se);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching subject enrollments for student: " + studentId, e);
        }
        return list;
    }

    public boolean recordGrade(AcademicRecord ar) {
        String sql = "INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, periodo_academico, valor_nota, observaciones, fecha_registro) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, CURDATE()) "
                + "ON DUPLICATE KEY UPDATE valor_nota = VALUES(valor_nota), observaciones = VALUES(observaciones), fecha_actualizacion = CURRENT_TIMESTAMP";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ar.getStudentId());
            ps.setInt(2, ar.getSubjectId());
            ps.setInt(3, ar.getTeacherId());
            ps.setInt(4, ar.getAcademicYear());
            ps.setString(5, ar.getAcademicPeriod());
            ps.setBigDecimal(6, ar.getGradeValue());
            ps.setString(7, ar.getRemarks());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.logError("Error saving grade record for student " + ar.getStudentId() + " in subject " + ar.getSubjectId(), e);
        }
        return false;
    }

    public List<AcademicRecord> getGradesForStudent(int studentId) {
        List<AcademicRecord> list = new ArrayList<>();
        String sql = "SELECT ra.*, m.nombre_materia, "
                + "CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM registros_academicos ra "
                + "JOIN materias m ON ra.id_materia = m.id_materia "
                + "JOIN estudiantes e ON ra.id_estudiante = e.id_estudiante "
                + "JOIN profesores p ON ra.id_profesor = p.id_profesor "
                + "WHERE ra.id_estudiante = ? ORDER BY ra.año_academico DESC, ra.periodo_academico, m.nombre_materia";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAcademicRecord(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching grades for student: " + studentId, e);
        }
        return list;
    }

    public List<AcademicRecord> getGradesBySubjectAndPeriod(int subjectId, int year, String period) {
        List<AcademicRecord> list = new ArrayList<>();
        String sql = "SELECT ra.*, m.nombre_materia, "
                + "CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM registros_academicos ra "
                + "JOIN materias m ON ra.id_materia = m.id_materia "
                + "JOIN estudiantes e ON ra.id_estudiante = e.id_estudiante "
                + "JOIN profesores p ON ra.id_profesor = p.id_profesor "
                + "WHERE ra.id_materia = ? AND ra.año_academico = ? AND ra.periodo_academico = ? "
                + "ORDER BY e.primer_apellido";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, subjectId);
            ps.setInt(2, year);
            ps.setString(3, period);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAcademicRecord(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching grades for subject: " + subjectId, e);
        }
        return list;
    }

    public boolean recordAttendance(Attendance att) {
        String sql = "INSERT INTO asistencias (id_estudiante, id_materia, fecha_asistencia, estado_asistencia, observaciones, registrado_por) "
                + "VALUES (?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE estado_asistencia = VALUES(estado_asistencia), observaciones = VALUES(observaciones)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, att.getStudentId());
            ps.setInt(2, att.getSubjectId());
            ps.setDate(3, Date.valueOf(att.getAttendanceDate()));
            ps.setString(4, att.getStatus());
            ps.setString(5, att.getRemarks());
            if (att.getRegisteredByTeacherId() != null) {
                ps.setInt(6, att.getRegisteredByTeacherId());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.logError("Error recording attendance for student " + att.getStudentId(), e);
        }
        return false;
    }

    public List<Attendance> getAttendanceByStudent(int studentId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT a.*, m.nombre_materia, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est "
                + "FROM asistencias a "
                + "JOIN materias m ON a.id_materia = m.id_materia "
                + "JOIN estudiantes e ON a.id_estudiante = e.id_estudiante "
                + "WHERE a.id_estudiante = ? ORDER BY a.fecha_asistencia DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Attendance att = new Attendance();
                    att.setId(rs.getInt("id_asistencia"));
                    att.setStudentId(rs.getInt("id_estudiante"));
                    att.setSubjectId(rs.getInt("id_materia"));
                    att.setAttendanceDate(rs.getDate("fecha_asistencia").toLocalDate());
                    att.setStatus(rs.getString("estado_asistencia"));
                    att.setRemarks(rs.getString("observaciones"));
                    int reg = rs.getInt("registrado_por");
                    if (!rs.wasNull()) att.setRegisteredByTeacherId(reg);
                    att.setStudentName(rs.getString("nom_est"));
                    att.setSubjectName(rs.getString("nombre_materia"));
                    list.add(att);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching attendance for student: " + studentId, e);
        }
        return list;
    }

    public List<String> getAbsencePatternReport(LocalDate startDate, LocalDate endDate, int minAbsences) {
        List<String> results = new ArrayList<>();
        String sql = "SELECT CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est, "
                + "e.numero_documento, COUNT(*) AS total_faltas "
                + "FROM asistencias a "
                + "JOIN estudiantes e ON a.id_estudiante = e.id_estudiante "
                + "WHERE a.estado_asistencia = 'Ausente' AND a.fecha_asistencia BETWEEN ? AND ? "
                + "GROUP BY e.id_estudiante, e.primer_nombre, e.primer_apellido, e.numero_documento "
                + "HAVING total_faltas >= ? ORDER BY total_faltas DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));
            ps.setInt(3, minAbsences);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String row = String.format("Estudiante: %s (Doc: %s) -> %d Ausencias registradas",
                            rs.getString("nom_est"), rs.getString("numero_documento"), rs.getInt("total_faltas"));
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error querying absence patterns", e);
        }
        return results;
    }

    private static Subject mapSubject(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getInt("id_materia"));
        s.setSubjectCode(rs.getString("codigo_materia"));
        s.setSubjectName(rs.getString("nombre_materia"));
        s.setDescription(rs.getString("descripcion"));
        s.setGradeId(rs.getInt("id_grado"));
        s.setWeeklyHours(rs.getInt("horas_semanales"));
        s.setActive(rs.getBoolean("esta_activa"));
        s.setGradeName(rs.getString("nombre_grado"));
        return s;
    }

    private static AcademicRecord mapAcademicRecord(ResultSet rs) throws SQLException {
        AcademicRecord ar = new AcademicRecord();
        ar.setId(rs.getInt("id_registro"));
        ar.setStudentId(rs.getInt("id_estudiante"));
        ar.setSubjectId(rs.getInt("id_materia"));
        ar.setTeacherId(rs.getInt("id_profesor"));
        ar.setAcademicYear(rs.getInt("año_academico"));
        ar.setAcademicPeriod(rs.getString("periodo_academico"));
        ar.setGradeValue(rs.getBigDecimal("valor_nota"));
        ar.setRemarks(rs.getString("observaciones"));
        ar.setRecordDate(rs.getDate("fecha_registro").toLocalDate());
        ar.setStudentName(rs.getString("nom_est"));
        ar.setSubjectName(rs.getString("nombre_materia"));
        ar.setTeacherName(rs.getString("nom_prof"));
        return ar;
    }
}
