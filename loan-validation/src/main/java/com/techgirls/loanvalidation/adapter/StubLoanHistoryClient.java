package com.techgirls.loanvalidation.adapter;

import com.techgirls.loanvalidation.port.LoanHistoryClient;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
* Stub implementation that simulates loan history for testing and development.
* Returns configurable loan dates for different test scenarios.
* Active by default - use a real implementation in production.
*/
@Component
public class StubLoanHistoryClient implements LoanHistoryClient {
    
    /**
     * Simulates loan history lookup.
     * For testing purposes, returns a recent loan date for amounts > 10000,
     * old loan date for amounts 5000-10000, and no loans for smaller amounts.
     */
    @Override
    public Mono<LocalDate> getLastLoanDate(String applicantId) {
        // Simulate different scenarios based on applicantId pattern
        if (applicantId != null && applicantId.contains("recent")) {
            return Mono.just(LocalDate.now().minusMonths(1)); // Recent loan
        } else if (applicantId != null && applicantId.contains("old")) {
            return Mono.just(LocalDate.now().minusMonths(6)); // Old loan
        }
        return Mono.empty(); // No prior loans
    }
    
    /**
     * Simulates default history check.
     * For testing purposes, returns true for customers with "default" in their ID.
     */
    @Override
    public Mono<Boolean> hasDefaultHistory(String customerId) {
        // Simulate different scenarios based on customerId pattern
        if (customerId != null && customerId.contains("default")) {
            return Mono.just(true); // Has default history
        }
        return Mono.just(false); // No default history
    }
}
