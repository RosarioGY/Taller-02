package com.techgirls.loanvalidation.adapter;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

/**
 * Unit tests for StubLoanHistoryClient.
 * Tests all scenarios of the stub implementation to ensure proper simulation
 * of loan history data for testing purposes.
 */
class StubLoanHistoryClientTest {

    private StubLoanHistoryClient stubLoanHistoryClient;

    @BeforeEach
    void setUp() {
        stubLoanHistoryClient = new StubLoanHistoryClient();
    }

    @Test
    void shouldReturnRecentLoanDateForRecentApplicant() {
        // Given
        String applicantId = "recent-loan-user";
        LocalDate today = LocalDate.now();
        LocalDate expectedDate = today.minusMonths(1);

        // When & Then
        StepVerifier.create(stubLoanHistoryClient.getLastLoanDate(applicantId))
            .assertNext(lastLoanDate -> {
                assertNotNull(lastLoanDate);
                assertEquals(expectedDate, lastLoanDate);
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnOldLoanDateForOldApplicant() {
        // Given
        String applicantId = "old-loan-user";
        LocalDate today = LocalDate.now();
        LocalDate expectedDate = today.minusMonths(6);

        // When & Then
        StepVerifier.create(stubLoanHistoryClient.getLastLoanDate(applicantId))
            .assertNext(lastLoanDate -> {
                assertNotNull(lastLoanDate);
                assertEquals(expectedDate, lastLoanDate);
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnEmptyForNoLoansApplicant() {
        // Given
        String applicantId = "regular-user";

        // When & Then
        StepVerifier.create(stubLoanHistoryClient.getLastLoanDate(applicantId))
            .verifyComplete(); // Empty Mono
    }

    @Test
    void shouldReturnEmptyForNullApplicantId() {
        // Given
        String applicantId = null;

        // When & Then
        StepVerifier.create(stubLoanHistoryClient.getLastLoanDate(applicantId))
            .verifyComplete(); // Empty Mono
    }

    @Test
    void shouldReturnTrueForDefaultCustomer() {
        // Given
        String customerId = "default-customer";

        // When & Then
        StepVerifier.create(stubLoanHistoryClient.hasDefaultHistory(customerId))
            .assertNext(hasDefault -> {
                assertNotNull(hasDefault);
                assertTrue(hasDefault);
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnFalseForRegularCustomer() {
        // Given
        String customerId = "regular-customer";

        // When & Then
        StepVerifier.create(stubLoanHistoryClient.hasDefaultHistory(customerId))
            .assertNext(hasDefault -> {
                assertNotNull(hasDefault);
                assertFalse(hasDefault);
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnFalseForNullCustomerId() {
        // Given
        String customerId = null;

        // When & Then
        StepVerifier.create(stubLoanHistoryClient.hasDefaultHistory(customerId))
            .assertNext(hasDefault -> {
                assertNotNull(hasDefault);
                assertFalse(hasDefault);
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleBothKeywordsInApplicantId() {
        // Given
        String applicantId = "recent-old-user"; // Contains both keywords

        // When & Then - "recent" has priority over "old"
        StepVerifier.create(stubLoanHistoryClient.getLastLoanDate(applicantId))
            .assertNext(lastLoanDate -> {
                assertNotNull(lastLoanDate);
                assertEquals(LocalDate.now().minusMonths(1), lastLoanDate);
            })
            .verifyComplete();
    }

    @Test
    void shouldHandleEmptyApplicantId() {
        // Given
        String applicantId = "";

        // When & Then
        StepVerifier.create(stubLoanHistoryClient.getLastLoanDate(applicantId))
            .verifyComplete(); // Empty Mono
    }

    @Test
    void shouldHandleEmptyCustomerId() {
        // Given
        String customerId = "";

        // When & Then
        StepVerifier.create(stubLoanHistoryClient.hasDefaultHistory(customerId))
            .assertNext(hasDefault -> {
                assertNotNull(hasDefault);
                assertFalse(hasDefault);
            })
            .verifyComplete();
    }

    @Test
    void shouldReturnConsistentResultsForSameInput() {
        // Given
        String applicantId = "recent-user";

        // When - Call multiple times
        // Then - Should get consistent results
        for (int i = 0; i < 3; i++) {
            StepVerifier.create(stubLoanHistoryClient.getLastLoanDate(applicantId))
                .assertNext(lastLoanDate -> {
                    assertNotNull(lastLoanDate);
                    assertEquals(LocalDate.now().minusMonths(1), lastLoanDate);
                })
                .verifyComplete();
        }
    }

    @Test
    void shouldHandleCaseSensitiveKeywords() {
        // Given
        String applicantIdUpperCase = "RECENT-USER";
        String applicantIdMixedCase = "Recent-User";

        // When & Then - Should not match due to case sensitivity
        StepVerifier.create(stubLoanHistoryClient.getLastLoanDate(applicantIdUpperCase))
            .verifyComplete(); // Empty Mono

        StepVerifier.create(stubLoanHistoryClient.getLastLoanDate(applicantIdMixedCase))
            .verifyComplete(); // Empty Mono
    }

    @Test
    void shouldHandleSpecialCharacters() {
        // Given
        String applicantId = "recent@user#123";
        String customerId = "default$customer%456";

        // When & Then
        StepVerifier.create(stubLoanHistoryClient.getLastLoanDate(applicantId))
            .assertNext(lastLoanDate -> {
                assertEquals(LocalDate.now().minusMonths(1), lastLoanDate);
            })
            .verifyComplete();

        StepVerifier.create(stubLoanHistoryClient.hasDefaultHistory(customerId))
            .assertNext(hasDefault -> {
                assertTrue(hasDefault);
            })
            .verifyComplete();
    }
}