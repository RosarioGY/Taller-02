package com.techgirls.loanvalidation.service;

import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanEligibilityService {
  private final Clock clock;

  public Mono<LoanValidationResponse> validate(LoanValidationRequest req) {
    List<String> reasons = new ArrayList<>();

    // R4: Datos válidos
    if (req.getMonthlySalary() == null || req.getMonthlySalary() <= 0) {
      reasons.add("R4: monthlySalary debe ser > 0");
    }
    if (req.getRequestedAmount() == null || req.getRequestedAmount() <= 0) {
      reasons.add("R4: requestedAmount debe ser > 0");
    }
    if (req.getTermMonths() == null || req.getTermMonths() < 1) {
      reasons.add("R4: termMonths debe ser >= 1");
    }
    if (req.getEmploymentMonths() == null || req.getEmploymentMonths() < 0) {
      reasons.add("R4: employmentMonths debe ser >= 0");
    }

    double monthlyPayment = 0.0;
    if (reasons.isEmpty()) {
      // R1: Antigüedad laboral
      if (req.getEmploymentMonths() < 3) {
        reasons.add("R1: Antigüedad laboral menor a 3 meses");
      }

      // R2: Plazo 1..36
      if (req.getTermMonths() > 36) {
        reasons.add("R2: termMonths debe ser <= 36");
      }

      // Cálculo de cuota
      monthlyPayment = req.getRequestedAmount() / req.getTermMonths();

      // R3: Capacidad de pago
      if (monthlyPayment > 0.40 * req.getMonthlySalary()) {
        reasons.add(String.format("R3: monthlyPayment (%.2f) > 40%% del salario (%.2f)", monthlyPayment, req.getMonthlySalary()));
      }
    }

    boolean eligible = reasons.isEmpty();
    LoanValidationResponse resp = LoanValidationResponse.builder()
        .eligible(eligible)
        .monthlyPayment(monthlyPayment)
        .reasons(reasons)
        .build();
    return Mono.just(resp);
  }
}
