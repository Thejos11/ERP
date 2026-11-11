package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ExtracurricularDAO;
import com.littlehorizons.erp.dao.FinanceDAO;
import com.littlehorizons.erp.dao.ResourceDAO;
import com.littlehorizons.erp.dao.StudentDAO;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.ResourceItem;
import com.littlehorizons.erp.model.Student;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller responsible for generating high-level business intelligence,
 * institutional statistics, rankings, and analytical summaries using Java Streams.
 */
public class ReportsController {

    private final StudentDAO studentDAO = new StudentDAO();
    private final ExtracurricularDAO extracurricularDAO = new ExtracurricularDAO();
    private final FinanceDAO financeDAO = new FinanceDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();
    private final AcademicController academicController = new AcademicController();

    /**
     * Finds top students by period average using Stream API.
     *
     * @param year Academic year.
     * @param period Academic period.
     * @param limit Top N count.
     * @return List of formatted ranking strings.
     */
    public List<String> getTopStudentsRanking(int year, String period, int limit) {
        List<Student> students = studentDAO.findAllStudents();

        record StudentAverage(Student student, double average) {}

        return students.stream()
                .map(s -> new StudentAverage(s, academicController.calculateStudentPeriodAverage(s.getId(), year, period)))
                .filter(sa -> sa.average() > 0.0)
                .sorted(Comparator.comparingDouble(StudentAverage::average).reversed())
                .limit(limit)
                .map(sa -> String.format("🏅 Promedio: %.1f | Estudiante: %s (Doc: %s)",
                        sa.average(), sa.student().getFullName(), sa.student().getDocumentNumber()))
                .collect(Collectors.toList());
    }

    /**
     * Summarizes extracurricular club occupancies using Stream API.
     *
     * @return List of formatted club capacity statuses.
     */
    public List<String> getClubOccupancySummary() {
        List<Club> clubs = extracurricularDAO.findAllClubs();
        return clubs.stream()
                .map(c -> {
                    double pct = (c.getMaxCapacity() > 0)
                            ? (c.getActiveMembersCount() * 100.0 / c.getMaxCapacity())
                            : 0.0;
                    return String.format("%-35s | Cupos: %2d/%-2d | Ocupación: %5.1f%%",
                            c.getClubName(), c.getActiveMembersCount(), c.getMaxCapacity(), pct);
                })
                .collect(Collectors.toList());
    }

    /**
     * Groups resources by status and counts them using Stream API.
     *
     * @return Map with resource counts per status.
     */
    public Map<String, Long> getResourceInventorySummary() {
        List<ResourceItem> resources = resourceDAO.findAllResources();
        return resources.stream()
                .collect(Collectors.groupingBy(ResourceItem::getStatus, Collectors.counting()));
    }

    /**
     * Calculates total income collected across all payments using Stream API.
     *
     * @return Total collection BigDecimal.
     */
    public BigDecimal calculateTotalCollections() {
        List<Student> students = studentDAO.findAllStudents();
        return students.stream()
                .flatMap(s -> financeDAO.findPaymentsByStudent(s.getId()).stream())
                .map(Payment::getAmountPaid)
                .filter(val -> val != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
