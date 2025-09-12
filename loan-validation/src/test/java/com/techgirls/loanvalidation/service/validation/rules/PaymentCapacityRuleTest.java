package com.techgirls.loanvalidation.service.validation.rules;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.service.validation.ValidationContext;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PaymentCapacityRuleTest {

    private final PaymentCapacityRule rule = new PaymentCapacityRule();

    @Test
    void shouldPassValidationWithSufficientCapacity() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(5000.0);
        ValidationContext context = ValidationContext.builder()
                .monthlyPayment(1500.0) // 30% of salary - within limit
                .build();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> assertTrue(reasons.isEmpty()))
                .verifyComplete();
    }

    @Test
    void shouldFailValidationWithInsufficientCapacity() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(5000.0);
        ValidationContext context = ValidationContext.builder()
                .monthlyPayment(2500.0) // 50% of salary - exceeds 40% limit
                .build();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertEquals(LoanValidationResult.ReasonsEnum.CAPACIDAD_INSUFICIENTE, reasons.get(0));
                })
                .verifyComplete();
    }

    @Test
    void shouldPassValidationWhenSalaryIsNull() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(null);
        ValidationContext context = ValidationContext.builder()
                .monthlyPayment(2000.0)
                .build();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> assertTrue(reasons.isEmpty()))
                .verifyComplete();
    }

    @Test
    void shouldPassValidationWhenPaymentIsNull() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(5000.0);
        ValidationContext context = ValidationContext.builder()
                .monthlyPayment(null)
                .build();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> assertTrue(reasons.isEmpty()))
                .verifyComplete();
    }

    @Test
    void shouldAcceptExactlyAtThreshold() {
        LoanValidationRequest request = createValidRequest();
        request.setMonthlySalary(5000.0);
        ValidationContext context = ValidationContext.builder()
                .monthlyPayment(2000.0) // Exactly 40% of salary
                .build();

        Mono<List<LoanValidationResult.ReasonsEnum>> result = rule.validate(request, context);

        StepVerifier.create(result)
                .assertNext(reasons -> assertTrue(reasons.isEmpty()))
                .verifyComplete();
    }

    @Test
    void shouldReturnCorrectPriority() {
        assertEquals(30, rule.getPriority());
    }

    @Test
    void shouldReturnCorrectRuleName() {
        assertEquals("Payment Capacity Rule", rule.getRuleName());
    }

    private LoanValidationRequest createValidRequest() {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setRequestedAmount(50000.0);
        request.setTermMonths(12);
        return request;
    }
}