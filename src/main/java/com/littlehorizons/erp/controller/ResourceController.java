package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ResourceDAO;
import com.littlehorizons.erp.model.ResourceCategory;
import com.littlehorizons.erp.model.ResourceItem;
import com.littlehorizons.erp.model.StudentLoan;
import com.littlehorizons.erp.model.TeacherLoan;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller managing library and laboratory resources and loan lifecycles.
 */
public class ResourceController {

    private final ResourceDAO resourceDAO = new ResourceDAO();

    public List<ResourceCategory> getAllCategories() {
        return resourceDAO.findAllCategories();
    }

    public boolean createResource(ResourceItem resource) {
        if (resource == null || resource.getResourceCode() == null || resource.getResourceCode().isBlank()) {
            return false;
        }
        return resourceDAO.createResource(resource);
    }

    public ResourceItem findResourceById(int id) {
        return resourceDAO.findResourceById(id);
    }

    public List<ResourceItem> getAllResources() {
        return resourceDAO.findAllResources();
    }

    public String loanToStudent(int resourceId, int studentId, LocalDate loanDate, LocalDate expectedReturnDate, String remarks) {
        ResourceItem item = resourceDAO.findResourceById(resourceId);
        if (item == null) {
            return "❌ El recurso solicitado no existe.";
        }
        if (!"Disponible".equalsIgnoreCase(item.getStatus())) {
            return String.format("❌ El recurso '%s' no está disponible para préstamo (Estado actual: %s).",
                    item.getResourceName(), item.getStatus());
        }

        StudentLoan loan = new StudentLoan(0, resourceId, studentId, loanDate, expectedReturnDate, null, "Activo", remarks);
        boolean success = resourceDAO.createStudentLoan(loan);
        if (success) {
            return String.format("✅ Recurso '%s' prestado exitosamente a estudiante ID %d hasta el %s.",
                    item.getResourceName(), studentId, expectedReturnDate);
        } else {
            return "❌ Error al registrar el préstamo en el sistema.";
        }
    }

    public String returnStudentLoan(int loanId, int resourceId, LocalDate returnDate, String remarks) {
        boolean success = resourceDAO.returnStudentLoan(loanId, resourceId, returnDate, remarks);
        if (success) {
            return "✅ Devolución registrada correctamente. El recurso vuelve a estar 'Disponible'.";
        } else {
            return "❌ Error al procesar la devolución.";
        }
    }

    public List<StudentLoan> getActiveStudentLoans() {
        return resourceDAO.findActiveStudentLoans();
    }

    public List<StudentLoan> getOverdueStudentLoans() {
        return resourceDAO.findOverdueStudentLoans();
    }

    public String loanToTeacher(int resourceId, int teacherId, LocalDate loanDate, LocalDate expectedReturnDate, String remarks) {
        ResourceItem item = resourceDAO.findResourceById(resourceId);
        if (item == null) {
            return "❌ El recurso solicitado no existe.";
        }
        if (!"Disponible".equalsIgnoreCase(item.getStatus())) {
            return String.format("❌ El recurso '%s' no está disponible para préstamo (Estado actual: %s).",
                    item.getResourceName(), item.getStatus());
        }

        TeacherLoan loan = new TeacherLoan(0, resourceId, teacherId, loanDate, expectedReturnDate, null, "Activo", remarks);
        boolean success = resourceDAO.createTeacherLoan(loan);
        if (success) {
            return String.format("✅ Recurso '%s' prestado exitosamente a profesor ID %d hasta el %s.",
                    item.getResourceName(), teacherId, expectedReturnDate);
        } else {
            return "❌ Error al registrar el préstamo a profesor.";
        }
    }

    public String returnTeacherLoan(int loanId, int resourceId, LocalDate returnDate, String remarks) {
        boolean success = resourceDAO.returnTeacherLoan(loanId, resourceId, returnDate, remarks);
        if (success) {
            return "✅ Devolución de profesor registrada correctamente. Recurso ahora 'Disponible'.";
        } else {
            return "❌ Error al procesar la devolución de profesor.";
        }
    }

    public List<TeacherLoan> getActiveTeacherLoans() {
        return resourceDAO.findActiveTeacherLoans();
    }

    public List<TeacherLoan> getOverdueTeacherLoans() {
        return resourceDAO.findOverdueTeacherLoans();
    }
}
