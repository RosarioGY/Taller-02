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

class AmountValidationRuleTest {

    private final AmountValidationRule rule = new AmountValidationRule();

    @Test
    void shouldPassValidationWithValidAmounts() {
        LoanValidationRequest request = createValidRequest();
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> assertTrue(reasons.isEmpty()))
                .verifyComplete();
    }

    @Test
    void shouldFailValidationWhenMonthlySalaryIsNull() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(null);
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertEquals(LoanValidationResult.ReasonsEnum.DATOS_INVALIDOS, reasons.get(0));
                })
                .verifyComplete();
    }

    @Test
    void shouldFailValidationWhenMonthlySalaryIsZero() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(0.0);
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertEquals(LoanValidationResult.ReasonsEnum.DATOS_INVALIDOS, reasons.get(0));
                })
                .verifyComplete();
    }

    @Test
    void shouldFailValidationWhenMonthlySalaryIsNegative() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(-1000.0);
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertEquals(LoanValidationResult.ReasonsEnum.DATOS_INVALIDOS, reasons.get(0));
                })
                .verifyComplete();
    }

    @Test
    void shouldFailValidationWhenRequestedAmountIsNull() {
        LoanValidationRequest request = createValidRequest();
        request.setRequestedAmount(null);
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertEquals(LoanValidationResult.ReasonsEnum.DATOS_INVALIDOS, reasons.get(0));
                })
                .verifyComplete();
    }

    @Test
    void shouldFailValidationWhenRequestedAmountIsZero() {
        LoanValidationRequest request = createValidRequest();
        request.setRequestedAmount(0.0);
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertEquals(LoanValidationResult.ReasonsEnum.DATOS_INVALIDOS, reasons.get(0));
                })
                .verifyComplete();
    }

    @Test
    void shouldFailValidationWhenRequestedAmountIsNegative() {
        LoanValidationRequest request = createValidRequest();
        request.setRequestedAmount(-5000.0);
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertEquals(LoanValidationResult.ReasonsEnum.DATOS_INVALIDOS, reasons.get(0));
                })
                .verifyComplete();
    }

    @Test
    void shouldFailValidationWhenBothAmountsAreInvalid() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(null);
        request.setRequestedAmount(-1000.0);
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertEquals(LoanValidationResult.ReasonsEnum.DATOS_INVALIDOS, reasons.get(0));
                })
                .verifyComplete();
    }

    @Test
    void shouldReturnCorrectPriority() {
        assertEquals(10, rule.getPriority());
    }

    @Test
    void shouldReturnCorrectRuleName() {
        assertEquals("Amount Validation Rule", rule.getRuleName());
    }

    @Test
    void shouldAcceptValidPositiveAmounts() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(0.01); // Minimum positive value
        request.setRequestedAmount(0.01);
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> assertTrue(reasons.isEmpty()))
                .verifyComplete();
    }

    @Test
    void shouldAcceptLargeValidAmounts() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(1_000_000.0);
        request.setRequestedAmount(10_000_000.0);
        ValidationContext context = createValidationContext();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> assertTrue(reasons.isEmpty()))
                .verifyComplete();
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