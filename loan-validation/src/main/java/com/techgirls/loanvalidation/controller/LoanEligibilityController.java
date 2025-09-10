package com.nttdata.loanvalidation.controller;

import com.nttdata.loanvalidation.model.LoanValidationRequest;
import com.nttdata.loanvalidation.model.LoanValidationResult;
import com.nttdata.loanvalidation.model.ReasonType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/loan-validations")
public class LoanEligibilityController {
    @PostMapping
    public LoanValidationResult validateLoan(@RequestBody LoanValidationRequest request) {
        List<ReasonType> reasons = new ArrayList<>();
        double monthlyPayment = 0.0;
        boolean datosInvalidos = false;

        // Validación de datos
        if (request.getMonthlySalary() < 0.01 || request.getRequestedAmount() < 0.01 || request.getTermMonths() < 1 || request.getTermMonths() > 36) {
            reasons.add(ReasonType.DATOS_INVALIDOS);
            datosInvalidos = true;
        }

        // Validación de plazo máximo
        if (request.getTermMonths() > 36) {
            reasons.add(ReasonType.PLAZO_MAXIMO_SUPERADO);
        }

        // Validación de préstamos recientes
        if (request.getLastLoanDate() != null) {
            LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
            if (!request.getLastLoanDate().isAfter(threeMonthsAgo)) {
                reasons.add(ReasonType.HAS_RECENT_LOANS);
            }
        }

        // Cálculo de pago mensual
        if (!datosInvalidos) {
            monthlyPayment = request.getRequestedAmount() / request.getTermMonths();
            // Validaci��n de capacidad
            if (monthlyPayment > request.getMonthlySalary() * 0.5) {
                reasons.add(ReasonType.CAPACIDAD_INSUFICIENTE);
            }
        }

        boolean eligible = reasons.isEmpty();
        return new LoanValidationResult(eligible, reasons, monthlyPayment);
    }
}
