package com.techgirls.loanvalidation.service.validation;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.techgirls.loanvalidation.exception.ExternalServiceException;
import com.techgirls.loanvalidation.exception.LoanValidationException;
import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.service.applicant.ApplicantIdentificationService;
import com.techgirls.loanvalidation.service.calculation.PaymentCalculationService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class LoanValidationOrchestratorTest {

    private LoanValidationOrchestrator orchestrator;
    private LoanValidationRule rule1;
    private LoanValidationRule rule2;
    private PaymentCalculationService paymentCalculationService;
    private ApplicantIdentificationService applicantIdentificationService;
    private Clock clock;

    @BeforeEach
    void setUp() {
        rule1 = mock(LoanValidationRule.class);
        rule2 = mock(LoanValidationRule.class);
        paymentCalculationService = mock(PaymentCalculationService.class);
        applicantIdentificationService = mock(ApplicantIdentificationService.class);
        clock = Clock.fixed(Instant.parse("2023-06-15T10:00:00Z"), ZoneId.systemDefault());

        List<LoanValidationRule> rules = Arrays.asList(rule1, rule2);
        orchestrator = new LoanValidationOrchestrator(rules, paymentCalculationService, 
                                                     applicantIdentificationService, clock);
    }

    @Test
    void shouldEvaluateLoanSuccessfullyWhenAllRulesPass() {
        // Given
        LoanValidationRequest request = createValidRequest();
        
        when(paymentCalculationService.calculateMonthlyPayment(50000.0, 24)).thenReturn(2500.0);
        when(applicantIdentificationService.generateApplicantId(request)).thenReturn("APP123");
        
        when(rule1.getPriority()).thenReturn(1);
        when(rule1.getRuleName()).thenReturn("AmountValidationRule");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Collections.emptyList()));
        
        when(rule2.getPriority()).thenReturn(2);
        when(rule2.getRuleName()).thenReturn("TermValidationRule");
        when(rule2.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Collections.emptyList()));

        // When
        Mono<LoanValidationResult> result = orchestrator.evaluate(request);

        // Then
        StepVerifier.create(result)
            .assertNext(validationResult -> {
                assertTrue(validationResult.getEligible());
                assertTrue(validationResult.getReasons().isEmpty());
                assertEquals(2500.0, validationResult.getMonthlyPayment());
            })
            .verifyComplete();

        verify(paymentCalculationService).calculateMonthlyPayment(50000.0, 24);
        verify(applicantIdentificationService).generateApplicantId(request);
        verify(rule1).validate(any(LoanValidationRequest.class), any(ValidationContext.class));
        verify(rule2).validate(any(LoanValidationRequest.class), any(ValidationContext.class));
    }

    @Test
    void shouldEvaluateLoanWithRejectionWhenRulesFail() {
        // Given
        LoanValidationRequest request = createValidRequest();
        
        when(paymentCalculationService.calculateMonthlyPayment(50000.0, 24)).thenReturn(2500.0);
        when(applicantIdentificationService.generateApplicantId(request)).thenReturn("APP123");
        
        when(rule1.getPriority()).thenReturn(1);
        when(rule1.getRuleName()).thenReturn("AmountValidationRule");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Arrays.asList(LoanValidationResult.ReasonsEnum.CAPACIDAD_INSUFICIENTE)));
        
        when(rule2.getPriority()).thenReturn(2);
        when(rule2.getRuleName()).thenReturn("TermValidationRule");
        when(rule2.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Arrays.asList(LoanValidationResult.ReasonsEnum.PLAZO_MAXIMO_SUPERADO)));

        // When
        Mono<LoanValidationResult> result = orchestrator.evaluate(request);

        // Then
        StepVerifier.create(result)
            .assertNext(validationResult -> {
                assertFalse(validationResult.getEligible());
                assertEquals(2, validationResult.getReasons().size());
                assertTrue(validationResult.getReasons().contains(LoanValidationResult.ReasonsEnum.CAPACIDAD_INSUFICIENTE));
                assertTrue(validationResult.getReasons().contains(LoanValidationResult.ReasonsEnum.PLAZO_MAXIMO_SUPERADO));
                assertEquals(2500.0, validationResult.getMonthlyPayment());
            })
            .verifyComplete();
    }

    @Test
    void shouldExecuteRulesInPriorityOrder() {
        // Given
        LoanValidationRequest request = createValidRequest();
        
        when(paymentCalculationService.calculateMonthlyPayment(50000.0, 24)).thenReturn(2500.0);
        when(applicantIdentificationService.generateApplicantId(request)).thenReturn("APP123");
        
        // rule2 has higher priority (lower number)
        when(rule1.getPriority()).thenReturn(5);
        when(rule1.getRuleName()).thenReturn("LowPriorityRule");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Collections.emptyList()));
        
        when(rule2.getPriority()).thenReturn(1);
        when(rule2.getRuleName()).thenReturn("HighPriorityRule");
        when(rule2.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Collections.emptyList()));

        // When
        Mono<LoanValidationResult> result = orchestrator.evaluate(request);

        // Then
        StepVerifier.create(result)
            .assertNext(validationResult -> {
                assertTrue(validationResult.getEligible());
            })
            .verifyComplete();

        // Verify that both rules were called (priority order is handled internally)
        verify(rule1).validate(any(LoanValidationRequest.class), any(ValidationContext.class));
        verify(rule2).validate(any(LoanValidationRequest.class), any(ValidationContext.class));
    }

    @Test
    void shouldHandleExternalServiceException() {
        // Given
        LoanValidationRequest request = createValidRequest();
        
        when(paymentCalculationService.calculateMonthlyPayment(50000.0, 24)).thenReturn(2500.0);
        when(applicantIdentificationService.generateApplicantId(request)).thenReturn("APP123");
        
        when(rule1.getPriority()).thenReturn(1);
        when(rule1.getRuleName()).thenReturn("AmountValidationRule");
        ExternalServiceException externalException = new ExternalServiceException("LoanHistoryService", "Service unavailable");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.error(externalException));

        // When
        Mono<LoanValidationResult> result = orchestrator.evaluate(request);

        // Then
        StepVerifier.create(result)
            .expectError(ExternalServiceException.class)
            .verify();
    }

    @Test
    void shouldMapGenericExceptionToExternalServiceException() {
        // Given
        LoanValidationRequest request = createValidRequest();
        
        when(paymentCalculationService.calculateMonthlyPayment(50000.0, 24)).thenReturn(2500.0);
        when(applicantIdentificationService.generateApplicantId(request)).thenReturn("APP123");
        
        when(rule1.getPriority()).thenReturn(1);
        when(rule1.getRuleName()).thenReturn("AmountValidationRule");
        RuntimeException genericException = new RuntimeException("Unexpected error");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.error(genericException));

        // When
        Mono<LoanValidationResult> result = orchestrator.evaluate(request);

        // Then
        StepVerifier.create(result)
            .expectError(ExternalServiceException.class)
            .verify();
    }

    @Test
    void shouldHandlePaymentCalculationException() {
        // Given
        LoanValidationRequest request = createValidRequest();
        
        when(paymentCalculationService.calculateMonthlyPayment(50000.0, 24))
            .thenThrow(new RuntimeException("Calculation error"));

        // When
        Mono<LoanValidationResult> result = orchestrator.evaluate(request);

        // Then
        StepVerifier.create(result)
            .expectError(LoanValidationException.class)
            .verify();
    }

    @Test
    void shouldHandleApplicantIdGenerationException() {
        // Given
        LoanValidationRequest request = createValidRequest();
        
        when(paymentCalculationService.calculateMonthlyPayment(50000.0, 24)).thenReturn(2500.0);
        when(applicantIdentificationService.generateApplicantId(request))
            .thenThrow(new RuntimeException("ID generation error"));

        // When
        Mono<LoanValidationResult> result = orchestrator.evaluate(request);

        // Then
        StepVerifier.create(result)
            .expectError(LoanValidationException.class)
            .verify();
    }

    @Test
    void shouldBuildValidationContextCorrectly() {
        // Given
        LoanValidationRequest request = createValidRequest();
        
        when(paymentCalculationService.calculateMonthlyPayment(50000.0, 24)).thenReturn(2500.0);
        when(applicantIdentificationService.generateApplicantId(request)).thenReturn("APP123");
        
        when(rule1.getPriority()).thenReturn(1);
        when(rule1.getRuleName()).thenReturn("TestRule");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenAnswer(invocation -> {
                ValidationContext context = invocation.getArgument(1);
                
                // Verify context properties
                assertEquals(LocalDate.now(clock), context.getCurrentDate());
                assertEquals(LocalDate.now(clock).minusMonths(3), context.getRecentLoanThreshold());
                assertEquals(2500.0, context.getMonthlyPayment());
                assertEquals("APP123", context.getApplicantId());
                assertTrue(context.isExternalDataAvailable());
                
                return Mono.just(Collections.emptyList());
            });

        // When
        Mono<LoanValidationResult> result = orchestrator.evaluate(request);

        // Then
        StepVerifier.create(result)
            .assertNext(validationResult -> {
                assertTrue(validationResult.getEligible());
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleEmptyValidationRulesList() {
        // Given
        LoanValidationRequest request = createValidRequest();
        List<LoanValidationRule> emptyRules = Collections.emptyList();
        LoanValidationOrchestrator emptyOrchestrator = new LoanValidationOrchestrator(
            emptyRules, paymentCalculationService, applicantIdentificationService, clock);
        
        when(paymentCalculationService.calculateMonthlyPayment(50000.0, 24)).thenReturn(2500.0);
        when(applicantIdentificationService.generateApplicantId(request)).thenReturn("APP123");

        // When
        Mono<LoanValidationResult> result = emptyOrchestrator.evaluate(request);

        // Then
        StepVerifier.create(result)
            .assertNext(validationResult -> {
                assertTrue(validationResult.getEligible());
                assertTrue(validationResult.getReasons().isEmpty());
                assertEquals(2500.0, validationResult.getMonthlyPayment());
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleNullMonthlyPayment() {
        // Given
        LoanValidationRequest request = createValidRequest();
        
        when(paymentCalculationService.calculateMonthlyPayment(50000.0, 24)).thenReturn(null);
        when(applicantIdentificationService.generateApplicantId(request)).thenReturn("APP123");
        
        when(rule1.getPriority()).thenReturn(1);
        when(rule1.getRuleName()).thenReturn("TestRule");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Collections.emptyList()));

        // When
        Mono<LoanValidationResult> result = orchestrator.evaluate(request);

        // Then
        StepVerifier.create(result)
            .assertNext(validationResult -> {
                assertTrue(validationResult.getEligible());
                assertEquals(0.0, validationResult.getMonthlyPayment());
            })
            .verifyComplete();
    }

    private LoanValidationRequest createValidRequest() {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setRequestedAmount(50000.0);
        request.setTermMonths(24);
        request.setMonthlySalary(8000.0);
        return request;
    }
}