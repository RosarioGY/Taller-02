package com.techgirls.loanvalidation.service.validation.rules;

import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.port.LoanHistoryClient;
import com.techgirls.loanvalidation.service.validation.LoanValidationRule;
import com.techgirls.loanvalidation.service.validation.ValidationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates that the applicant doesn't have recent loans (within 3 months).
 * This rule implements Single Responsibility Principle by handling only recent loan validation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecentLoanRule implements LoanValidationRule {

    private final LoanHistoryClient loanHistoryClient;

    @Override
    public Mono<List<LoanValidationResult.ReasonsEnum>> validate(
            LoanValidationRequest request, 
            ValidationContext context) {
        
        log.debug("Validating recent loans for request");
        
        List<LoanValidationResult.ReasonsEnum> reasons = new ArrayList<>();
        
        // Strategy 1: Use lastLoanDate from request if provided
        if (request.getLastLoanDate() != null && request.getLastLoanDate().isPresent()) {
            LocalDate lastLoanDate = request.getLastLoanDate().get();
            if (hasRecentLoan(lastLoanDate, context.getRecentLoanThreshold())) {
                reasons.add(LoanValidationResult.ReasonsEnum.HAS_RECENT_LOANS);
                log.warn("Recent loan detected: lastLoanDate={}, threshold={}", 
                        lastLoanDate, context.getRecentLoanThreshold());
            }
            return Mono.just(reasons);
        }
        
        // Strategy 2: Query external service if no lastLoanDate provided
        return loanHistoryClient.getLastLoanDate(context.getApplicantId())
                .map(lastLoanDate -> {
                    log.debug("Retrieved last loan date from client: {}", lastLoanDate);
                    if (hasRecentLoan(lastLoanDate, context.getRecentLoanThreshold())) {
                        reasons.add(LoanValidationResult.ReasonsEnum.HAS_RECENT_LOANS);
                        log.warn("Recent loan found via client: lastLoanDate={}", lastLoanDate);
                    }
                    return reasons;
                })
                .defaultIfEmpty(reasons);
    }

    @Override
    public int getPriority() {
        return 40; // Lower priority as it might require external call
    }

    @Override
    public String getRuleName() {
        return "Recent Loan Rule";
    }
    
    private boolean hasRecentLoan(LocalDate lastLoanDate, LocalDate threshold) {
        return lastLoanDate != null && !lastLoanDate.isBefore(threshold);
    }
}