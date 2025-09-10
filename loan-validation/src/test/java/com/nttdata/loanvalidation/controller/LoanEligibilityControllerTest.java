package com.nttdata.loanvalidation.controller;

import com.nttdata.loanvalidation.model.LoanValidationRequest;
import com.nttdata.loanvalidation.model.LoanValidationResult;
import com.nttdata.loanvalidation.model.ReasonType;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class LoanEligibilityControllerTest {
    private final LoanEligibilityController controller = new LoanEligibilityController();

    @Test
    void testElegible() {
        LoanValidationRequest req = new LoanValidationRequest(3000, 6000, 24, null);
        LoanValidationResult res = controller.validateLoan(req);
        assertTrue(res.isEligible());
        assertEquals(0, res.getReasons().size());
        assertEquals(250.0, res.getMonthlyPayment());
    }

    @Test
    void testHasRecentLoans() {
        LocalDate recent = LocalDate.now().minusMonths(2);
        LoanValidationRequest req = new LoanValidationRequest(3000, 6000, 24, recent);
        LoanValidationResult res = controller.validateLoan(req);
        assertFalse(res.isEligible());
        assertTrue(res.getReasons().contains(ReasonType.HAS_RECENT_LOANS));
    }

    @Test
    void testCapacidadInsuficiente() {
        LoanValidationRequest req = new LoanValidationRequest(1000, 20000, 24, null);
        LoanValidationResult res = controller.validateLoan(req);
        assertFalse(res.isEligible());
        assertTrue(res.getReasons().contains(ReasonType.CAPACIDAD_INSUFICIENTE));
    }

    @Test
    void testPlazoMaximoSuperado() {
        LoanValidationRequest req = new LoanValidationRequest(3000, 6000, 40, null);
        LoanValidationResult res = controller.validateLoan(req);
        assertFalse(res.isEligible());
        assertTrue(res.getReasons().contains(ReasonType.PLAZO_MAXIMO_SUPERADO));
    }

    @Test
    void testDatosInvalidos() {
        LoanValidationRequest req = new LoanValidationRequest(0, 0, 0, null);
        LoanValidationResult res = controller.validateLoan(req);
        assertFalse(res.isEligible());
        assertTrue(res.getReasons().contains(ReasonType.DATOS_INVALIDOS));
    }
}
