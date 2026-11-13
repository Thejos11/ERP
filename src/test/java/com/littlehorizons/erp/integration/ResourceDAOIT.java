package com.littlehorizons.erp.integration;

import com.littlehorizons.erp.dao.ResourceDAO;
import com.littlehorizons.erp.integration.support.DaoIntegrationSupport;
import com.littlehorizons.erp.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResourceDAOIT extends DaoIntegrationSupport {
    @Test void createsResourceAndChangesStatus() throws Exception {
        ResourceDAO dao = new ResourceDAO();
        ResourceItem resource = new ResourceItem(0, fixtures.token + "N", "Recurso ficticio", fixtures.category,
                "Integration only", fixtures.date, "Laboratorio ficticio", "Disponible");
        boolean saved = dao.createResource(resource);
        if (resource.getId() > 0) fixtures.track("recursos", "id_recurso", resource.getId());
        assertTrue(saved);
        assertTrue(dao.updateResourceStatus(resource.getId(), "En Mantenimiento"));
        assertEquals("En Mantenimiento", dao.findResourceById(resource.getId()).getStatus());
    }

    @Test void studentAndTeacherLoanTransactionsUpdateBothTables() throws Exception {
        ResourceDAO dao = new ResourceDAO();
        StudentLoan student = new StudentLoan(0, fixtures.resource, fixtures.student, fixtures.date,
                fixtures.date.plusDays(7), null, "Activo", "Fictitious loan");
        assertTrue(dao.createStudentLoan(student));
        assertEquals("Prestado", dao.findResourceById(fixtures.resource).getStatus());
        assertTrue(dao.returnStudentLoan(student.getId(), fixtures.resource, fixtures.date.plusDays(1), "Returned"));
        assertEquals("Disponible", dao.findResourceById(fixtures.resource).getStatus());
        assertEquals(1, count("SELECT COUNT(*) FROM prestamos_estudiante WHERE id_prestamo=? AND estado_prestamo='Devuelto'", student.getId()));
        TeacherLoan teacher = new TeacherLoan(0, fixtures.resource, fixtures.teacher, fixtures.date,
                fixtures.date.plusDays(7), null, "Activo", "Fictitious loan");
        assertTrue(dao.createTeacherLoan(teacher));
        assertTrue(dao.returnTeacherLoan(teacher.getId(), fixtures.resource, fixtures.date.plusDays(1), "Returned"));
        assertEquals("Disponible", dao.findResourceById(fixtures.resource).getStatus());
        assertEquals(1, count("SELECT COUNT(*) FROM prestamos_profesor WHERE id_prestamo=? AND estado_prestamo='Devuelto'", teacher.getId()));
    }

    @Test void invalidBorrowerDoesNotChangeResourceState() throws Exception {
        ResourceDAO dao = new ResourceDAO();
        assertFalse(dao.createStudentLoan(new StudentLoan(0, fixtures.resource, -1, fixtures.date,
                fixtures.date.plusDays(7), null, "Activo", "Invalid FK")));
        assertEquals("Disponible", dao.findResourceById(fixtures.resource).getStatus());
        assertEquals(0, count("SELECT COUNT(*) FROM prestamos_estudiante WHERE id_recurso=?", fixtures.resource));
    }
}
