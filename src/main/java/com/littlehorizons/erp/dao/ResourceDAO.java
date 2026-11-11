package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.ResourceItem;
import com.littlehorizons.erp.model.StudentLoan;
import com.littlehorizons.erp.model.TeacherLoan;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.LoggerUtil;

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
 * Data Access Object for School Resources (Library, Labs, Sports, Tech)
 * and Resource Loans to both Students and Teachers.
 */
public class ResourceDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public List<ResourceCategory> findAllCategories() {
        List<ResourceCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM categorias_recurso ORDER BY id_categoria";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new ResourceCategory(
                        rs.getInt("id_categoria"),
                        rs.getString("nombre_categoria"),
                        rs.getString("descripcion")
                ));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching resource categories", e);
        }
        return list;
    }

    public boolean createResource(ResourceItem r) {
        String sql = "INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, descripcion, fecha_adquisicion, ubicacion_fisica, estado_recurso) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getResourceCode());
            ps.setString(2, r.getResourceName());
            ps.setInt(3, r.getCategoryId());
            ps.setString(4, r.getDescription());
            if (r.getAcquisitionDate() != null) {
                ps.setDate(5, Date.valueOf(r.getAcquisitionDate()));
            } else {
                ps.setNull(5, java.sql.Types.DATE);
            }
            ps.setString(6, r.getPhysicalLocation());
            ps.setString(7, r.getStatus() != null ? r.getStatus() : "Disponible");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        r.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error inserting resource: " + r.getResourceCode(), e);
        }
        return false;
    }

    public ResourceItem findResourceById(int id) {
        String sql = "SELECT r.*, c.nombre_categoria FROM recursos r JOIN categorias_recurso c ON r.id_categoria = c.id_categoria WHERE r.id_recurso = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResource(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error finding resource by id: " + id, e);
        }
        return null;
    }

    public List<ResourceItem> findAllResources() {
        List<ResourceItem> list = new ArrayList<>();
        String sql = "SELECT r.*, c.nombre_categoria FROM recursos r JOIN categorias_recurso c ON r.id_categoria = c.id_categoria ORDER BY c.nombre_categoria, r.nombre_recurso";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResource(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching all resources", e);
        }
        return list;
    }

    public boolean updateResourceStatus(int resourceId, String newStatus) {
        String sql = "UPDATE recursos SET estado_recurso = ? WHERE id_recurso = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.logError("Error updating status for resource: " + resourceId, e);
        }
        return false;
    }

    public boolean createStudentLoan(StudentLoan loan) {
        String insertSql = "INSERT INTO prestamos_estudiante (id_recurso, id_estudiante, fecha_prestamo, fecha_devolucion_esperada, estado_prestamo, observaciones) "
                + "VALUES (?, ?, ?, ?, 'Activo', ?)";
        String updateResourceSql = "UPDATE recursos SET estado_recurso = 'Prestado' WHERE id_recurso = ?";

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psUpdate = conn.prepareStatement(updateResourceSql)) {

                psInsert.setInt(1, loan.getResourceId());
                psInsert.setInt(2, loan.getStudentId());
                psInsert.setDate(3, Date.valueOf(loan.getLoanDate()));
                psInsert.setDate(4, Date.valueOf(loan.getExpectedReturnDate()));
                psInsert.setString(5, loan.getRemarks());
                psInsert.executeUpdate();

                try (ResultSet rs = psInsert.getGeneratedKeys()) {
                    if (rs.next()) {
                        loan.setId(rs.getInt(1));
                    }
                }

                psUpdate.setInt(1, loan.getResourceId());
                psUpdate.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error creating student loan for resource: " + loan.getResourceId(), e);
        }
        return false;
    }

    public boolean returnStudentLoan(int loanId, int resourceId, LocalDate returnDate, String remarks) {
        String updateLoanSql = "UPDATE prestamos_estudiante SET fecha_devolucion_real = ?, estado_prestamo = 'Devuelto', observaciones = CONCAT(IFNULL(observaciones,''), ' | Retorno: ', ?) WHERE id_prestamo = ?";
        String updateResourceSql = "UPDATE recursos SET estado_recurso = 'Disponible' WHERE id_recurso = ?";

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psLoan = conn.prepareStatement(updateLoanSql);
                 PreparedStatement psResource = conn.prepareStatement(updateResourceSql)) {

                psLoan.setDate(1, Date.valueOf(returnDate));
                psLoan.setString(2, remarks != null ? remarks : "Devolución registrada");
                psLoan.setInt(3, loanId);
                psLoan.executeUpdate();

                psResource.setInt(1, resourceId);
                psResource.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error returning student loan: " + loanId, e);
        }
        return false;
    }

    public List<StudentLoan> findActiveStudentLoans() {
        List<StudentLoan> list = new ArrayList<>();
        String sql = "SELECT pe.*, r.nombre_recurso, r.codigo_recurso, "
                + "CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est "
                + "FROM prestamos_estudiante pe "
                + "JOIN recursos r ON pe.id_recurso = r.id_recurso "
                + "JOIN estudiantes e ON pe.id_estudiante = e.id_estudiante "
                + "WHERE pe.estado_prestamo = 'Activo' ORDER BY pe.fecha_devolucion_esperada";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapStudentLoan(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching active student loans", e);
        }
        return list;
    }

    public List<StudentLoan> findOverdueStudentLoans() {
        List<StudentLoan> list = new ArrayList<>();
        String sql = "SELECT pe.*, r.nombre_recurso, r.codigo_recurso, "
                + "CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est "
                + "FROM prestamos_estudiante pe "
                + "JOIN recursos r ON pe.id_recurso = r.id_recurso "
                + "JOIN estudiantes e ON pe.id_estudiante = e.id_estudiante "
                + "WHERE pe.estado_prestamo = 'Vencido' OR (pe.estado_prestamo = 'Activo' AND pe.fecha_devolucion_esperada < CURDATE()) "
                + "ORDER BY pe.fecha_devolucion_esperada";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapStudentLoan(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching overdue student loans", e);
        }
        return list;
    }

    public boolean createTeacherLoan(TeacherLoan loan) {
        String insertSql = "INSERT INTO prestamos_profesor (id_recurso, id_profesor, fecha_prestamo, fecha_devolucion_esperada, estado_prestamo, observaciones) "
                + "VALUES (?, ?, ?, ?, 'Activo', ?)";
        String updateResourceSql = "UPDATE recursos SET estado_recurso = 'Prestado' WHERE id_recurso = ?";

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psUpdate = conn.prepareStatement(updateResourceSql)) {

                psInsert.setInt(1, loan.getResourceId());
                psInsert.setInt(2, loan.getTeacherId());
                psInsert.setDate(3, Date.valueOf(loan.getLoanDate()));
                psInsert.setDate(4, Date.valueOf(loan.getExpectedReturnDate()));
                psInsert.setString(5, loan.getRemarks());
                psInsert.executeUpdate();

                try (ResultSet rs = psInsert.getGeneratedKeys()) {
                    if (rs.next()) {
                        loan.setId(rs.getInt(1));
                    }
                }

                psUpdate.setInt(1, loan.getResourceId());
                psUpdate.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error creating teacher loan for resource: " + loan.getResourceId(), e);
        }
        return false;
    }

    public boolean returnTeacherLoan(int loanId, int resourceId, LocalDate returnDate, String remarks) {
        String updateLoanSql = "UPDATE prestamos_profesor SET fecha_devolucion_real = ?, estado_prestamo = 'Devuelto', observaciones = CONCAT(IFNULL(observaciones,''), ' | Retorno: ', ?) WHERE id_prestamo = ?";
        String updateResourceSql = "UPDATE recursos SET estado_recurso = 'Disponible' WHERE id_recurso = ?";

        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psLoan = conn.prepareStatement(updateLoanSql);
                 PreparedStatement psResource = conn.prepareStatement(updateResourceSql)) {

                psLoan.setDate(1, Date.valueOf(returnDate));
                psLoan.setString(2, remarks != null ? remarks : "Devolución registrada");
                psLoan.setInt(3, loanId);
                psLoan.executeUpdate();

                psResource.setInt(1, resourceId);
                psResource.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error returning teacher loan: " + loanId, e);
        }
        return false;
    }

    public List<TeacherLoan> findActiveTeacherLoans() {
        List<TeacherLoan> list = new ArrayList<>();
        String sql = "SELECT pp.*, r.nombre_recurso, r.codigo_recurso, "
                + "CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM prestamos_profesor pp "
                + "JOIN recursos r ON pp.id_recurso = r.id_recurso "
                + "JOIN profesores p ON pp.id_profesor = p.id_profesor "
                + "WHERE pp.estado_prestamo = 'Activo' ORDER BY pp.fecha_devolucion_esperada";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapTeacherLoan(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching active teacher loans", e);
        }
        return list;
    }

    public List<TeacherLoan> findOverdueTeacherLoans() {
        List<TeacherLoan> list = new ArrayList<>();
        String sql = "SELECT pp.*, r.nombre_recurso, r.codigo_recurso, "
                + "CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM prestamos_profesor pp "
                + "JOIN recursos r ON pp.id_recurso = r.id_recurso "
                + "JOIN profesores p ON pp.id_profesor = p.id_profesor "
                + "WHERE pp.estado_prestamo = 'Vencido' OR (pp.estado_prestamo = 'Activo' AND pp.fecha_devolucion_esperada < CURDATE()) "
                + "ORDER BY pp.fecha_devolucion_esperada";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapTeacherLoan(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching overdue teacher loans", e);
        }
        return list;
    }

    private static ResourceItem mapResource(ResultSet rs) throws SQLException {
        ResourceItem r = new ResourceItem();
        r.setId(rs.getInt("id_recurso"));
        r.setResourceCode(rs.getString("codigo_recurso"));
        r.setResourceName(rs.getString("nombre_recurso"));
        r.setCategoryId(rs.getInt("id_categoria"));
        r.setDescription(rs.getString("descripcion"));
        Date acq = rs.getDate("fecha_adquisicion");
        if (acq != null) r.setAcquisitionDate(acq.toLocalDate());
        r.setPhysicalLocation(rs.getString("ubicacion_fisica"));
        r.setStatus(rs.getString("estado_recurso"));
        r.setCategoryName(rs.getString("nombre_categoria"));
        return r;
    }

    private static StudentLoan mapStudentLoan(ResultSet rs) throws SQLException {
        StudentLoan sl = new StudentLoan();
        sl.setId(rs.getInt("id_prestamo"));
        sl.setResourceId(rs.getInt("id_recurso"));
        sl.setStudentId(rs.getInt("id_estudiante"));
        sl.setLoanDate(rs.getDate("fecha_prestamo").toLocalDate());
        sl.setExpectedReturnDate(rs.getDate("fecha_devolucion_esperada").toLocalDate());
        Date ret = rs.getDate("fecha_devolucion_real");
        if (ret != null) sl.setActualReturnDate(ret.toLocalDate());
        sl.setStatus(rs.getString("estado_prestamo"));
        sl.setRemarks(rs.getString("observaciones"));
        sl.setResourceName(rs.getString("nombre_recurso"));
        sl.setResourceCode(rs.getString("codigo_recurso"));
        sl.setStudentName(rs.getString("nom_est"));
        return sl;
    }

    private static TeacherLoan mapTeacherLoan(ResultSet rs) throws SQLException {
        TeacherLoan tl = new TeacherLoan();
        tl.setId(rs.getInt("id_prestamo"));
        tl.setResourceId(rs.getInt("id_recurso"));
        tl.setTeacherId(rs.getInt("id_profesor"));
        tl.setLoanDate(rs.getDate("fecha_prestamo").toLocalDate());
        tl.setExpectedReturnDate(rs.getDate("fecha_devolucion_esperada").toLocalDate());
        Date ret = rs.getDate("fecha_devolucion_real");
        if (ret != null) tl.setActualReturnDate(ret.toLocalDate());
        tl.setStatus(rs.getString("estado_prestamo"));
        tl.setRemarks(rs.getString("observaciones"));
        tl.setResourceName(rs.getString("nombre_recurso"));
        tl.setResourceCode(rs.getString("codigo_recurso"));
        tl.setTeacherName(rs.getString("nom_prof"));
        return tl;
    }
}
