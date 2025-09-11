package com.techgirls.loanvalidation;

import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.service.LoanEligibilityService;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;

public class LoanEligibilityServiceTest {
  @Test
  void eligible_when_rules_pass() {
    var service = new LoanEligibilityService(Clock.systemUTC());
    var req = LoanValidationRequest.builder()
        .monthlySalary(2500.0)
        .requestedAmount(6000.0)
        .termMonths(24)
        .employmentMonths(12)
        .build();

    Mono<?> mono = service.validate(req);
    StepVerifier.create(mono)
        .expectNextMatches(resp -> ((com.techgirls.loanvalidation.model.LoanValidationResponse)resp).isEligible())
        .verifyComplete();
  }

  @Test
  void not_eligible_when_insufficient_employment() {
    var service = new LoanEligibilityService(Clock.systemUTC());
    var req = LoanValidationRequest.builder()
        .monthlySalary(2500.0)
        .requestedAmount(6000.0)
        .termMonths(24)
        .employmentMonths(1)
        .build();

    StepVerifier.create(service.validate(req))
        .expectNextMatches(resp -> !((com.techgirls.loanvalidation.model.LoanValidationResponse)resp).isEligible())
        .verifyComplete();
  }
}
