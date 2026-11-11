package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.ClubMembership;
import com.littlehorizons.erp.model.EventParticipant;
import com.littlehorizons.erp.model.SchoolEvent;
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
 * Data Access Object for Extracurricular Clubs, Memberships, Events, and Event Participants.
 */
public class ExtracurricularDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public boolean createClub(Club c) {
        String sql = "INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor, capacidad_maxima, horario_reunion, esta_activo) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getClubName());
            ps.setString(2, c.getDescription());
            if (c.getSupervisorTeacherId() != null) {
                ps.setInt(3, c.getSupervisorTeacherId());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setInt(4, c.getMaxCapacity());
            ps.setString(5, c.getMeetingSchedule());
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
            LoggerUtil.logError("Error creating club: " + c.getClubName(), e);
        }
        return false;
    }

    public Club findClubById(int id) {
        String sql = "SELECT c.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof, "
                + "(SELECT COUNT(*) FROM membresias_club mc WHERE mc.id_club = c.id_club AND mc.estado_membresia = 'Activa') AS total_activos "
                + "FROM clubes c "
                + "LEFT JOIN profesores p ON c.id_profesor_supervisor = p.id_profesor "
                + "WHERE c.id_club = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapClub(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error finding club by id: " + id, e);
        }
        return null;
    }

    public List<Club> findAllClubs() {
        List<Club> list = new ArrayList<>();
        String sql = "SELECT c.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof, "
                + "(SELECT COUNT(*) FROM membresias_club mc WHERE mc.id_club = c.id_club AND mc.estado_membresia = 'Activa') AS total_activos "
                + "FROM clubes c "
                + "LEFT JOIN profesores p ON c.id_profesor_supervisor = p.id_profesor "
                + "ORDER BY c.nombre_club";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapClub(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching all clubs", e);
        }
        return list;
    }

    public int getActiveMembersCount(int clubId) {
        String sql = "SELECT COUNT(*) FROM membresias_club WHERE id_club = ? AND estado_membresia = 'Activa'";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error counting members for club: " + clubId, e);
        }
        return 0;
    }

    public boolean registerMembership(int studentId, int clubId, LocalDate joinDate) {
        String sql = "INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, estado_membresia) "
                + "VALUES (?, ?, ?, 'Activa')";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, clubId);
            ps.setDate(3, Date.valueOf(joinDate));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.logError("Error registering membership student " + studentId + " in club " + clubId, e);
        }
        return false;
    }

    public boolean withdrawMembership(int membershipId, String reason) {
        String sql = "UPDATE membresias_club SET estado_membresia = 'Retirada', fecha_retiro = CURDATE(), motivo_retiro = ? "
                + "WHERE id_membresia = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, membershipId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.logError("Error withdrawing membership: " + membershipId, e);
        }
        return false;
    }

    public List<ClubMembership> getMembershipsForStudent(int studentId) {
        List<ClubMembership> list = new ArrayList<>();
        String sql = "SELECT mc.*, c.nombre_club, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est "
                + "FROM membresias_club mc "
                + "JOIN clubes c ON mc.id_club = c.id_club "
                + "JOIN estudiantes e ON mc.id_estudiante = e.id_estudiante "
                + "WHERE mc.id_estudiante = ? ORDER BY mc.fecha_ingreso DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMembership(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching memberships for student: " + studentId, e);
        }
        return list;
    }

    public List<ClubMembership> getMembershipsForClub(int clubId) {
        List<ClubMembership> list = new ArrayList<>();
        String sql = "SELECT mc.*, c.nombre_club, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est "
                + "FROM membresias_club mc "
                + "JOIN clubes c ON mc.id_club = c.id_club "
                + "JOIN estudiantes e ON mc.id_estudiante = e.id_estudiante "
                + "WHERE mc.id_club = ? ORDER BY mc.estado_membresia, e.primer_apellido";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clubId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMembership(rs));
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching memberships for club: " + clubId, e);
        }
        return list;
    }

    public boolean createEvent(SchoolEvent ev) {
        String sql = "INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, tiene_competencia, id_organizador, esta_activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ev.getEventName());
            ps.setString(2, ev.getDescription());
            ps.setString(3, ev.getEventType());
            ps.setDate(4, Date.valueOf(ev.getEventDate()));
            ps.setString(5, ev.getLocation());
            ps.setBoolean(6, ev.isHasCompetition());
            if (ev.getOrganizerTeacherId() != null) {
                ps.setInt(7, ev.getOrganizerTeacherId());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            ps.setBoolean(8, ev.isActive());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        ev.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error creating event: " + ev.getEventName(), e);
        }
        return false;
    }

    public SchoolEvent findEventById(int id) {
        String sql = "SELECT ev.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM eventos ev LEFT JOIN profesores p ON ev.id_organizador = p.id_profesor WHERE ev.id_evento = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEvent(rs);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error finding event by id: " + id, e);
        }
        return null;
    }

    public List<SchoolEvent> findAllEvents() {
        List<SchoolEvent> list = new ArrayList<>();
        String sql = "SELECT ev.*, CONCAT(p.primer_nombre, ' ', p.primer_apellido) AS nom_prof "
                + "FROM eventos ev LEFT JOIN profesores p ON ev.id_organizador = p.id_profesor ORDER BY ev.fecha_evento";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapEvent(rs));
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching all events", e);
        }
        return list;
    }

    public boolean registerParticipant(int eventId, int studentId, String role, String positionWon, String remarks) {
        String sql = "INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion, posicion_obtenida, observaciones) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE rol_participacion = VALUES(rol_participacion), posicion_obtenida = VALUES(posicion_obtenida), observaciones = VALUES(observaciones)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, studentId);
            ps.setString(3, role != null ? role : "Participante");
            ps.setString(4, positionWon);
            ps.setString(5, remarks);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.logError("Error registering participant for event " + eventId, e);
        }
        return false;
    }

    public List<EventParticipant> getParticipantsForEvent(int eventId) {
        List<EventParticipant> list = new ArrayList<>();
        String sql = "SELECT pe.*, ev.nombre_evento, CONCAT(e.primer_nombre, ' ', e.primer_apellido) AS nom_est "
                + "FROM participantes_evento pe "
                + "JOIN eventos ev ON pe.id_evento = ev.id_evento "
                + "JOIN estudiantes e ON pe.id_estudiante = e.id_estudiante "
                + "WHERE pe.id_evento = ? ORDER BY pe.posicion_obtenida, e.primer_apellido";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EventParticipant ep = new EventParticipant();
                    ep.setId(rs.getInt("id_participacion"));
                    ep.setEventId(rs.getInt("id_evento"));
                    ep.setStudentId(rs.getInt("id_estudiante"));
                    ep.setRole(rs.getString("rol_participacion"));
                    ep.setPositionWon(rs.getString("posicion_obtenida"));
                    ep.setRemarks(rs.getString("observaciones"));
                    ep.setEventName(rs.getString("nombre_evento"));
                    ep.setStudentName(rs.getString("nom_est"));
                    list.add(ep);
                }
            }
        } catch (SQLException e) {
            LoggerUtil.logError("Error fetching participants for event: " + eventId, e);
        }
        return list;
    }

    private static Club mapClub(ResultSet rs) throws SQLException {
        Club c = new Club();
        c.setId(rs.getInt("id_club"));
        c.setClubName(rs.getString("nombre_club"));
        c.setDescription(rs.getString("descripcion"));
        int sup = rs.getInt("id_profesor_supervisor");
        if (!rs.wasNull()) c.setSupervisorTeacherId(sup);
        c.setMaxCapacity(rs.getInt("capacidad_maxima"));
        c.setMeetingSchedule(rs.getString("horario_reunion"));
        c.setActive(rs.getBoolean("esta_activo"));
        c.setTeacherName(rs.getString("nom_prof"));
        c.setActiveMembersCount(rs.getInt("total_activos"));
        return c;
    }

    private static ClubMembership mapMembership(ResultSet rs) throws SQLException {
        ClubMembership cm = new ClubMembership();
        cm.setId(rs.getInt("id_membresia"));
        cm.setStudentId(rs.getInt("id_estudiante"));
        cm.setClubId(rs.getInt("id_club"));
        cm.setJoinDate(rs.getDate("fecha_ingreso").toLocalDate());
        cm.setStatus(rs.getString("estado_membresia"));
        Date ret = rs.getDate("fecha_retiro");
        if (ret != null) cm.setWithdrawalDate(ret.toLocalDate());
        cm.setWithdrawalReason(rs.getString("motivo_retiro"));
        cm.setStudentName(rs.getString("nom_est"));
        cm.setClubName(rs.getString("nombre_club"));
        return cm;
    }

    private static SchoolEvent mapEvent(ResultSet rs) throws SQLException {
        SchoolEvent ev = new SchoolEvent();
        ev.setId(rs.getInt("id_evento"));
        ev.setEventName(rs.getString("nombre_evento"));
        ev.setDescription(rs.getString("descripcion"));
        ev.setEventType(rs.getString("tipo_evento"));
        ev.setEventDate(rs.getDate("fecha_evento").toLocalDate());
        ev.setLocation(rs.getString("lugar"));
        ev.setHasCompetition(rs.getBoolean("tiene_competencia"));
        int org = rs.getInt("id_organizador");
        if (!rs.wasNull()) ev.setOrganizerTeacherId(org);
        ev.setActive(rs.getBoolean("esta_activo"));
        ev.setOrganizerName(rs.getString("nom_prof"));
        return ev;
    }
}
