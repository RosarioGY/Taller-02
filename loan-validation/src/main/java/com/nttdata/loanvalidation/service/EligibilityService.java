package com.nttdata.loanvalidation.service;

import com.nttdata.loanvalidation.model.LoanValidationRequest;
import com.nttdata.loanvalidation.model.LoanValidationResponse;
import com.nttdata.loanvalidation.model.Reason;
import com.nttdata.loanvalidation.port.LoanHistoryClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
* Evaluate loan request.
* R1: No loans in the last 3 months (inclusive).
* R2: 1 <= termMonths <= 36.
* R3: monthlyPayment <= 0.40 * monthlySalary.
* R4: monthlySalary > 0 and requestedAmount > 0.
*/
@Service
public class EligibilityService {
    private final LoanHistoryClient loanHistoryClient;
    private final Clock clock;

    public EligibilityService(LoanHistoryClient loanHistoryClient, Clock clock) {
        this.loanHistoryClient = loanHistoryClient;
        this.clock = clock;
    }

    public Mono<LoanValidationResponse> evaluate(LoanValidationRequest req) {
        LocalDate today = LocalDate.now(clock);
        LocalDate since = today.minusMonths(3);
        List<Reason> reasons = new ArrayList<>();

        // R4 – Basic data validity
        if (isNullOrNonPositive(req.getMonthlySalary()) || isNullOrNonPositive(req.getRequestedAmount())) {
            reasons.add(Reason.builder()
                .code("R4")
                .message("Datos inválidos: monthlySalary y requestedAmount deben ser > 0.")
                .build());
        }

        // R2 – Term range
        Integer term = req.getTermMonths();
        if (term == null || term < 1 || term > 36) {
            reasons.add(Reason.builder()
                .code("R2")
                .message("Plazo inválido: termMonths debe estar entre 1 y 36.")
                .build());
        }

        // Monthly payment (computed anyway for response)
        BigDecimal monthlyPayment = computeMonthlyPayment(req.getRequestedAmount(), term);

        // R3 – Simple capacity: monthlyPayment <= 0.40 * monthlySalary
        if (req.getMonthlySalary() != null && monthlyPayment != null) {
            BigDecimal threshold = req.getMonthlySalary().multiply(new BigDecimal("0.40"));
            if (monthlyPayment.compareTo(threshold) > 0) {
                reasons.add(Reason.builder()
                    .code("R3")
                    .message("Capacidad de pago insuficiente: cuota supera el 40% del sueldo.")
                    .build());
            }
        }

        // R1 – No loans in last 3 months (inclusive)
        return loanHistoryClient.getLastLoanDate(req.getApplicantId())
            .defaultIfEmpty(null)
            .map(lastLoanDate -> {
                if (lastLoanDate != null && !lastLoanDate.isBefore(since)) { // >= since → fail
                    reasons.add(Reason.builder()
                        .code("R1")
                        .message("Antigüedad insuficiente: existe préstamo en los últimos 3 meses.")
                        .build());
                }
                boolean eligible = reasons.isEmpty();
                return LoanValidationResponse.builder()
                    .eligible(eligible)
                    .reasons(reasons)
                    .monthlyPayment(monthlyPayment)
                    .evaluatedAt(today)
                    .build();
            });
    }

    private boolean isNullOrNonPositive(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) <= 0;
    }

    private BigDecimal computeMonthlyPayment(BigDecimal requestedAmount, Integer termMonths) {
        if (requestedAmount == null || termMonths == null || termMonths <= 0) {
            return null;
        }
        return requestedAmount
            .divide(new BigDecimal(termMonths), 2, RoundingMode.HALF_UP);
    }
}

