package com.techgirls.loanvalidation.service.validation.rules;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.jackson.nullable.JsonNullable;

import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.port.LoanHistoryClient;
import com.techgirls.loanvalidation.service.validation.ValidationContext;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for RecentLoanRule.
 * Tests all validation scenarios for recent loan checks including
 * request-based validation and external service validation.
 */
@ExtendWith(MockitoExtension.class)
class RecentLoanRuleTest {

    @Mock
    private LoanHistoryClient loanHistoryClient;

    @InjectMocks
    private RecentLoanRule recentLoanRule;

    private LoanValidationRequest request;
    private ValidationContext context;

    @BeforeEach
    void setUp() {
        request = new LoanValidationRequest();
        context = ValidationContext.builder()
                .applicantId("APP123")
                .monthlyPayment(2500.0)
                .recentLoanThreshold(LocalDate.now().minusMonths(3))
                .build();
    }

    @Test
    void shouldRejectWhenRequestHasRecentLoanDate() {
        // Given
        LocalDate recentDate = LocalDate.now().minusMonths(2);
        request.setLastLoanDate(JsonNullable.of(recentDate));

        // When
        Mono<List<LoanValidationResult.ReasonsEnum>> result = recentLoanRule.validate(request, context);

        // Then
        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertTrue(reasons.contains(LoanValidationResult.ReasonsEnum.HAS_RECENT_LOANS));
                })
                .verifyComplete();

        verifyNoInteractions(loanHistoryClient);
    }

    @Test
    void shouldAcceptWhenRequestHasOldLoanDate() {
        // Given
        LocalDate oldDate = LocalDate.now().minusMonths(4);
        request.setLastLoanDate(JsonNullable.of(oldDate));

        // When
        Mono<List<LoanValidationResult.ReasonsEnum>> result = recentLoanRule.validate(request, context);

        // Then
        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(0, reasons.size());
                })
                .verifyComplete();

        verifyNoInteractions(loanHistoryClient);
    }

    @Test
    void shouldAcceptWhenRequestHasNullLastLoanDate() {
        // Given
        request.setLastLoanDate(JsonNullable.of(null));

        // When
        Mono<List<LoanValidationResult.ReasonsEnum>> result = recentLoanRule.validate(request, context);

        // Then
        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(0, reasons.size());
                })
                .verifyComplete();

        verifyNoInteractions(loanHistoryClient);
    }

    @Test
    void shouldQueryClientWhenRequestHasNoLastLoanDate() {
        // Given
        request.setLastLoanDate(JsonNullable.undefined());
        LocalDate recentDate = LocalDate.now().minusMonths(2);
        when(loanHistoryClient.getLastLoanDate("APP123")).thenReturn(Mono.just(recentDate));

        // When
        Mono<List<LoanValidationResult.ReasonsEnum>> result = recentLoanRule.validate(request, context);

        // Then
        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertTrue(reasons.contains(LoanValidationResult.ReasonsEnum.HAS_RECENT_LOANS));
                })
                .verifyComplete();

        verify(loanHistoryClient).getLastLoanDate("APP123");
    }

    @Test
    void shouldAcceptWhenClientReturnsOldLoanDate() {
        // Given
        request.setLastLoanDate(JsonNullable.undefined());
        LocalDate oldDate = LocalDate.now().minusMonths(5);
        when(loanHistoryClient.getLastLoanDate("APP123")).thenReturn(Mono.just(oldDate));

        // When
        Mono<List<LoanValidationResult.ReasonsEnum>> result = recentLoanRule.validate(request, context);

        // Then
        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(0, reasons.size());
                })
                .verifyComplete();

        verify(loanHistoryClient).getLastLoanDate("APP123");
    }

    @Test
    void shouldAcceptWhenClientReturnsEmptyMono() {
        // Given
        request.setLastLoanDate(JsonNullable.undefined());
        when(loanHistoryClient.getLastLoanDate("APP123")).thenReturn(Mono.empty());

        // When
        Mono<List<LoanValidationResult.ReasonsEnum>> result = recentLoanRule.validate(request, context);

        // Then
        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(0, reasons.size());
                })
                .verifyComplete();

        verify(loanHistoryClient).getLastLoanDate("APP123");
    }

    @Test
    void shouldHandleClientError() {
        // Given
        request.setLastLoanDate(JsonNullable.undefined());
        when(loanHistoryClient.getLastLoanDate("APP123"))
                .thenReturn(Mono.error(new RuntimeException("External service error")));

        // When
        Mono<List<LoanValidationResult.ReasonsEnum>> result = recentLoanRule.validate(request, context);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(loanHistoryClient).getLastLoanDate("APP123");
    }

    @Test
    void shouldReturnCorrectPriority() {
        // When
        int priority = recentLoanRule.getPriority();

        // Then
        assertEquals(40, priority);
    }

    @Test
    void shouldReturnCorrectRuleName() {
        // When
        String ruleName = recentLoanRule.getRuleName();

        // Then
        assertEquals("Recent Loan Rule", ruleName);
    }

    @Test
    void shouldHandleBoundaryDateExactlyAtThreshold() {
        // Given
        LocalDate thresholdDate = LocalDate.now().minusMonths(3);
        request.setLastLoanDate(JsonNullable.of(thresholdDate));
        context = ValidationContext.builder()
                .applicantId("APP123")
                .monthlyPayment(2500.0)
                .recentLoanThreshold(thresholdDate)
                .build();

        // When
        Mono<List<LoanValidationResult.ReasonsEnum>> result = recentLoanRule.validate(request, context);

        // Then
        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertTrue(reasons.contains(LoanValidationResult.ReasonsEnum.HAS_RECENT_LOANS));
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleFutureLastLoanDate() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        request.setLastLoanDate(JsonNullable.of(futureDate));

        // When
        Mono<List<LoanValidationResult.ReasonsEnum>> result = recentLoanRule.validate(request, context);

        // Then
        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(1, reasons.size());
                    assertTrue(reasons.contains(LoanValidationResult.ReasonsEnum.HAS_RECENT_LOANS));
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleNullContext() {
        // Given
        LocalDate recentDate = LocalDate.now().minusMonths(2);
        request.setLastLoanDate(JsonNullable.of(recentDate));

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            recentLoanRule.validate(request, null);
        });
    }

    @Test
    void shouldAcceptWhenRequestHasEmptyJsonNullable() {
        // Given
        request.setLastLoanDate(JsonNullable.undefined()); // Not set at all
        when(loanHistoryClient.getLastLoanDate("APP123")).thenReturn(Mono.empty());

        // When
        Mono<List<LoanValidationResult.ReasonsEnum>> result = recentLoanRule.validate(request, context);

        // Then
        StepVerifier.create(result)
                .assertNext(reasons -> {
                    assertEquals(0, reasons.size());
                })
                .verifyComplete();

        verify(loanHistoryClient).getLastLoanDate("APP123");
    }
}