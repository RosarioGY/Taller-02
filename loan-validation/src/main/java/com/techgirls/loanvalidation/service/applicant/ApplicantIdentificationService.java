package com.techgirls.loanvalidation.service.applicant;

import com.techgirls.loanvalidation.model.LoanValidationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for generating or retrieving applicant identifiers.
 * In a real system, this would integrate with authentication/user management systems.
 * This follows Single Responsibility Principle by handling only applicant ID logic.
 */
@Service
@Slf4j
public class ApplicantIdentificationService {

    /**
     * Generates a simulated applicant ID for testing purposes.
     * In a real system, this would come from authentication context or user session.
     * 
     * @param request the loan validation request
     * @return a simulated applicant ID based on request characteristics
     */
    public String generateApplicantId(LoanValidationRequest request) {
        log.debug("Generating applicant ID for request");
        
        // Create different scenarios for testing based on request characteristics
        if (request.getRequestedAmount() != null && request.getRequestedAmount() > 15000) {
            log.debug("Generated applicant ID for high amount scenario");
            return "applicant-recent-loans"; // Will trigger recent loans scenario
        } else if (request.getRequestedAmount() != null && request.getRequestedAmount() > 8000) {
            log.debug("Generated applicant ID for medium amount scenario");
            return "applicant-old-loans"; // Will trigger old loans scenario
        }
        
        log.debug("Generated applicant ID for low amount scenario");
        return "applicant-no-loans"; // No loans scenario
    }
    
    /**
     * In a real implementation, this would retrieve the applicant ID from 
     * the current security context or authentication token.
     * 
     * @return the authenticated user's applicant ID
     */
    public String getCurrentApplicantId() {
        // In a real system:
        // return SecurityContextHolder.getContext().getAuthentication().getName();
        
        log.debug("Retrieved current applicant ID from security context");
        return "authenticated-applicant-id";
    }
}