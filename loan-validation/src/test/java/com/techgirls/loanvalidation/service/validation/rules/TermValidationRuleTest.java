package com.techgirls.loanvalidation.service.validation.rules;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.service.validation.ValidationContext;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class TermValidationRuleTest {

    private final TermValidationRule rule = new TermValidationRule();

    @Test
    void shouldPassValidationWithValidTerm() {
        LoanValidationRequest request = createValidRequest();
        request.setTermMonths(12); // Valid term
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> assertTrue(reasons.isEmpty()))
                .verifyComplete();
    }

    @Test
    void shouldFailValidationWhenTermIsNull() {
        LoanValidationRequest request = createValidRequest();
        request.setTermMonths(null);
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertEquals(LoanValidationResult.ReasonsEnum.PLAZO_MAXIMO_SUPERADO, reasons.get(0));
                })
                .verifyComplete();
    }

    @Test
    void shouldFailValidationWhenTermIsZero() {
        LoanValidationRequest request = createValidRequest();
        request.setTermMonths(0);
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertEquals(LoanValidationResult.ReasonsEnum.PLAZO_MAXIMO_SUPERADO, reasons.get(0));
                })
                .verifyComplete();
    }

    @Test
    void shouldFailValidationWhenTermIsTooLow() {
        LoanValidationRequest request = createValidRequest();
        request.setTermMonths(-5); // Below minimum of 1
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertEquals(LoanValidationResult.ReasonsEnum.PLAZO_MAXIMO_SUPERADO, reasons.get(0));
                })
                .verifyComplete();
    }

    @Test
    void shouldFailValidationWhenTermIsTooHigh() {
        LoanValidationRequest request = createValidRequest();
        request.setTermMonths(50); // Above maximum of 36
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertEquals(LoanValidationResult.ReasonsEnum.PLAZO_MAXIMO_SUPERADO, reasons.get(0));
                })
                .verifyComplete();
    }

    @Test
    void shouldAcceptMinimumValidTerm() {
        LoanValidationRequest request = createValidRequest();
        request.setTermMonths(1); // Minimum valid term
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> assertTrue(reasons.isEmpty()))
                .verifyComplete();
    }

    @Test
    void shouldAcceptMaximumValidTerm() {
        LoanValidationRequest request = createValidRequest();
        request.setTermMonths(36); // Maximum valid term
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> assertTrue(reasons.isEmpty()))
                .verifyComplete();
    }

    @Test
    void shouldReturnCorrectPriority() {
        assertEquals(20, rule.getPriority());
    }

    @Test
    void shouldReturnCorrectRuleName() {
        assertEquals("Term Validation Rule", rule.getRuleName());
    }

    private LoanValidationRequest createValidRequest() {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setMonthlySalary(5000.0);
        request.setRequestedAmount(50000.0);
        request.setTermMonths(12);
        return request;
    }

    private ValidationContext createValidationContext() {
        return ValidationContext.builder()
                .currentDate(LocalDate.now())
                .recentLoanThreshold(LocalDate.now().minusMonths(3))
                .monthlyPayment(1000.0)
                .externalDataAvailable(true)
                .applicantId("test-applicant")
                .build();
    }
}