package com.techgirls.loanvalidation.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanRulesService {

    private final Clock clock;

    public LoanRulesService(Clock clock) {
        this.clock = clock;
    }

    /**
     * Aplica reglas R1-R4 y devuelve una lista de razones de rechazo.
     */
    public List<String> validate(BigDecimal salary,
                                 BigDecimal amount,
                                 int termMonths,
                                 LocalDate lastLoanDate) {

        List<String> reasons = new ArrayList<>();

        // R4: Datos válidos
        if (salary == null || salary.compareTo(BigDecimal.ZERO) <= 0
                || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
                || termMonths <= 0) {
            reasons.add("DATOS_INVALIDOS");
            return reasons; // no seguimos validando si los datos son inválidos
        }

        // R2: Plazo máximo
        if (termMonths > 36) {
            reasons.add("PLAZO_MAXIMO_SUPERADO");
        }

        // R1: Antigüedad de préstamos (<3 meses)
        if (lastLoanDate != null) {
            LocalDate threeMonthsAgo = LocalDate.now(clock).minusMonths(3);
            if (lastLoanDate.isAfter(threeMonthsAgo)) {
                reasons.add("HAS_RECENT_LOANS");
            }
        }

        // R3: Capacidad de pago (cuota ≤ 40% del sueldo)
        BigDecimal monthly = monthlyPayment(amount, termMonths);
        BigDecimal maxAllowed = salary.multiply(BigDecimal.valueOf(0.40));
        if (monthly.compareTo(maxAllowed) > 0) {
            reasons.add("CAPACIDAD_INSUFICIENTE");
        }

        return reasons;
    }

    /**
     * Calcula el pago mensual.
     */
    public BigDecimal monthlyPayment(BigDecimal amount, int termMonths) {
        if (termMonths <= 0) return BigDecimal.ZERO;
        return amount.divide(BigDecimal.valueOf(termMonths), 2, BigDecimal.ROUND_HALF_UP);
    }
}
