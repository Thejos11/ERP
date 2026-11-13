package com.littlehorizons.erp.integration;

import com.littlehorizons.erp.dao.FinanceDAO;
import com.littlehorizons.erp.integration.support.DaoIntegrationSupport;
import com.littlehorizons.erp.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class FinanceDAOIT extends DaoIntegrationSupport {
    @Test void createsConceptPaymentAndInvoiceWithoutGeneratingFiles() throws Exception {
        FinanceDAO dao = new FinanceDAO();
        PaymentConcept concept = new PaymentConcept(0, fixtures.token, "Concepto ficticio", "Integration only", new BigDecimal("123.45"), false, true);
        boolean saved = dao.createConcept(concept);
        if (concept.getId() > 0) fixtures.track("conceptos_pago", "id_concepto", concept.getId());
        assertTrue(saved);
        assertEquals(new BigDecimal("123.45"), dao.findConceptById(concept.getId()).getBaseAmount());
        Payment payment = new Payment(0, fixtures.student, fixtures.guardian, concept.getId(), new BigDecimal("123.45"),
                fixtures.date, "Efectivo", fixtures.token, fixtures.date.getYear(), "I", "Fictitious payment");
        saved = dao.recordPayment(payment);
        if (payment.getId() > 0) fixtures.track("pagos", "id_pago", payment.getId());
        assertTrue(saved);
        assertEquals(new BigDecimal("123.45"), dao.findPaymentById(payment.getId()).getAmountPaid());
        assertTrue(dao.findPaymentsByStudent(fixtures.student).stream().anyMatch(p -> p.getId() == payment.getId()));
        assertTrue(dao.findPaymentsByGuardian(fixtures.guardian).stream().anyMatch(p -> p.getId() == payment.getId()));
        assertFalse(dao.recordPayment(payment));
        Invoice invoice = new Invoice(0, fixtures.token, payment.getId(), fixtures.date, "0".repeat(64), "fictitious.pdf", "not-generated/fictitious.pdf");
        saved = dao.createInvoice(invoice);
        if (invoice.getId() > 0) fixtures.track("facturas", "id_factura", invoice.getId());
        assertTrue(saved);
        assertFalse(dao.createInvoice(invoice));
        assertEquals(1, count("SELECT COUNT(*) FROM facturas WHERE id_pago=?", payment.getId()));
    }
}
