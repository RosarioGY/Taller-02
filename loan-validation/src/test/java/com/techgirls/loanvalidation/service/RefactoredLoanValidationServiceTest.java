package com.techgirls.loanvalidation.service;

import java.time.Clock;
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
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.techgirls.loanvalidation.exception.ExternalServiceException;
import com.techgirls.loanvalidation.exception.LoanValidationException;
import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.service.applicant.ApplicantIdentificationService;
import com.techgirls.loanvalidation.service.calculation.PaymentCalculationService;
import com.techgirls.loanvalidation.service.validation.LoanValidationRule;
import com.techgirls.loanvalidation.service.validation.ValidationContext;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for RefactoredLoanValidationService.
 * Tests the refactored service following SOLID principles.
 */
@ExtendWith(MockitoExtension.class)
class RefactoredLoanValidationServiceTest {

    @Mock
    private LoanValidationRule rule1;

    @Mock
    private LoanValidationRule rule2;

    @Mock
    private PaymentCalculationService paymentCalculationService;

    @Mock
    private ApplicantIdentificationService applicantIdentificationService;

    @Mock
    private Clock clock;

    @InjectMocks
    private RefactoredLoanValidationService refactoredService;

    private LoanValidationRequest request;
    private final LocalDate fixedDate = LocalDate.of(2024, 9, 11);

    @BeforeEach
    void setUp() {
        // Mock clock to return fixed date
        when(clock.instant()).thenReturn(fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        
        // Setup request
        request = new LoanValidationRequest();
        request.setRequestedAmount(50000.0);
        request.setTermMonths(24);
        request.setMonthlySalary(8000.0);

        // Setup service mocks
        when(paymentCalculationService.calculateMonthlyPayment(50000.0, 24)).thenReturn(2500.0);
        when(applicantIdentificationService.generateApplicantId(request)).thenReturn("APP123");

        // Setup validation rules list
        List<LoanValidationRule> rules = Arrays.asList(rule1, rule2);
        refactoredService = new RefactoredLoanValidationService(rules, paymentCalculationService, 
                applicantIdentificationService, clock);
    }

    @Test
    void shouldEvaluateSuccessfullyWhenAllRulesPass() {
        // Given
        when(rule1.getPriority()).thenReturn(1);
        when(rule1.getRuleName()).thenReturn("Rule1");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Collections.emptyList()));

        when(rule2.getPriority()).thenReturn(2);
        when(rule2.getRuleName()).thenReturn("Rule2");
        when(rule2.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Collections.emptyList()));

        // When
        Mono<LoanValidationResult> result = refactoredService.evaluate(request);

        // Then
        StepVerifier.create(result)
            .assertNext(validationResult -> {
                assertTrue(validationResult.getEligible());
                assertEquals(0, validationResult.getReasons().size());
                assertEquals(2500.0, validationResult.getMonthlyPayment());
            })
            .verifyComplete();
    }

    @Test
    void shouldRejectWhenRulesFailWithReasons() {
        // Given
        when(rule1.getPriority()).thenReturn(1);
        when(rule1.getRuleName()).thenReturn("Rule1");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Arrays.asList(LoanValidationResult.ReasonsEnum.CAPACIDAD_INSUFICIENTE)));

        when(rule2.getPriority()).thenReturn(2);
        when(rule2.getRuleName()).thenReturn("Rule2");
        when(rule2.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Arrays.asList(LoanValidationResult.ReasonsEnum.PLAZO_MAXIMO_SUPERADO)));

        // When
        Mono<LoanValidationResult> result = refactoredService.evaluate(request);

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
        when(rule1.getPriority()).thenReturn(2); // Lower priority
        when(rule1.getRuleName()).thenReturn("Rule1");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Collections.emptyList()));

        when(rule2.getPriority()).thenReturn(1); // Higher priority
        when(rule2.getRuleName()).thenReturn("Rule2");
        when(rule2.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Collections.emptyList()));

        // When
        StepVerifier.create(refactoredService.evaluate(request))
            .assertNext(result -> assertTrue(result.getEligible()))
            .verifyComplete();

        // Then - verify rules were called in correct order
        verify(rule2).validate(any(), any()); // Priority 1 (higher priority)
        verify(rule1).validate(any(), any()); // Priority 2 (lower priority)
    }

    @Test
    void shouldHandleExternalServiceException() {
        // Given
        when(rule1.getPriority()).thenReturn(1);
        when(rule1.getRuleName()).thenReturn("Rule1");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.error(new ExternalServiceException("TestService", "External service error")));

        // When
        Mono<LoanValidationResult> result = refactoredService.evaluate(request);

        // Then
        StepVerifier.create(result)
            .expectError(ExternalServiceException.class)
            .verify();
    }

    @Test
    void shouldMapGenericExceptionToExternalServiceException() {
        // Given
        when(rule1.getPriority()).thenReturn(1);
        when(rule1.getRuleName()).thenReturn("Rule1");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.error(new RuntimeException("Generic error")));

        // When
        Mono<LoanValidationResult> result = refactoredService.evaluate(request);

        // Then
        StepVerifier.create(result)
            .expectError(ExternalServiceException.class)
            .verify();
    }

    @Test
    void shouldHandlePaymentCalculationException() {
        // Given
        when(paymentCalculationService.calculateMonthlyPayment(50000.0, 24))
            .thenThrow(new RuntimeException("Payment calculation error"));

        // When
        Mono<LoanValidationResult> result = refactoredService.evaluate(request);

        // Then
        StepVerifier.create(result)
            .expectError(LoanValidationException.class)
            .verify();
    }

    @Test
    void shouldHandleApplicantIdGenerationException() {
        // Given
        when(applicantIdentificationService.generateApplicantId(request))
            .thenThrow(new RuntimeException("ID generation error"));

        // When
        Mono<LoanValidationResult> result = refactoredService.evaluate(request);

        // Then
        StepVerifier.create(result)
            .expectError(LoanValidationException.class)
            .verify();
    }

    @Test
    void shouldBuildValidationContextCorrectly() {
        // Given
        when(rule1.getPriority()).thenReturn(1);
        when(rule1.getRuleName()).thenReturn("Rule1");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenAnswer(invocation -> {
                ValidationContext context = invocation.getArgument(1);
                // Verify context is built correctly
                assertEquals(fixedDate, context.getCurrentDate());
                assertEquals(fixedDate.minusMonths(3), context.getRecentLoanThreshold());
                assertEquals(2500.0, context.getMonthlyPayment());
                assertEquals("APP123", context.getApplicantId());
                assertTrue(context.isExternalDataAvailable());
                return Mono.just(Collections.emptyList());
            });

        // When & Then
        StepVerifier.create(refactoredService.evaluate(request))
            .assertNext(result -> assertTrue(result.getEligible()))
            .verifyComplete();
    }

    @Test
    void shouldHandleEmptyRulesList() {
        // Given
        RefactoredLoanValidationService serviceWithNoRules = new RefactoredLoanValidationService(
                Collections.emptyList(), paymentCalculationService, applicantIdentificationService, clock);

        // When
        Mono<LoanValidationResult> result = serviceWithNoRules.evaluate(request);

        // Then
        StepVerifier.create(result)
            .assertNext(validationResult -> {
                assertTrue(validationResult.getEligible());
                assertEquals(0, validationResult.getReasons().size());
                assertEquals(2500.0, validationResult.getMonthlyPayment());
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleMultipleReasonsFromSingleRule() {
        // Given
        when(rule1.getPriority()).thenReturn(1);
        when(rule1.getRuleName()).thenReturn("Rule1");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Arrays.asList(
                LoanValidationResult.ReasonsEnum.CAPACIDAD_INSUFICIENTE,
                LoanValidationResult.ReasonsEnum.DATOS_INVALIDOS
            )));

        // When
        Mono<LoanValidationResult> result = refactoredService.evaluate(request);

        // Then
        StepVerifier.create(result)
            .assertNext(validationResult -> {
                assertFalse(validationResult.getEligible());
                assertEquals(2, validationResult.getReasons().size());
                assertTrue(validationResult.getReasons().contains(LoanValidationResult.ReasonsEnum.CAPACIDAD_INSUFICIENTE));
                assertTrue(validationResult.getReasons().contains(LoanValidationResult.ReasonsEnum.DATOS_INVALIDOS));
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleNullMonthlyPayment() {
        // Given
        when(paymentCalculationService.calculateMonthlyPayment(50000.0, 24)).thenReturn(null);
        
        when(rule1.getPriority()).thenReturn(1);
        when(rule1.getRuleName()).thenReturn("Rule1");
        when(rule1.validate(any(LoanValidationRequest.class), any(ValidationContext.class)))
            .thenReturn(Mono.just(Collections.emptyList()));

        // When
        Mono<LoanValidationResult> result = refactoredService.evaluate(request);

        // Then
        StepVerifier.create(result)
            .assertNext(validationResult -> {
                assertTrue(validationResult.getEligible());
                assertEquals(0.0, validationResult.getMonthlyPayment());
            })
            .verifyComplete();
    }
}