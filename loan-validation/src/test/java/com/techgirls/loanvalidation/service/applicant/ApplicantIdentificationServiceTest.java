package com.techgirls.loanvalidation.service.applicant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.techgirls.loanvalidation.model.LoanValidationRequest;

class ApplicantIdentificationServiceTest {

    private final ApplicantIdentificationService applicantIdentificationService = new ApplicantIdentificationService();

    @Test
    void shouldGenerateRecentLoansIdForHighAmount() {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setRequestedAmount(20000.0); // High amount > 15000
        
        String result = applicantIdentificationService.generateApplicantId(request);
        
        assertEquals("applicant-recent-loans", result);
    }

    @Test
    void shouldGenerateOldLoansIdForMediumAmount() {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setRequestedAmount(10000.0); // Medium amount > 8000 but <= 15000
        
        String result = applicantIdentificationService.generateApplicantId(request);
        
        assertEquals("applicant-old-loans", result);
    }

    @Test
    void shouldGenerateNoLoansIdForLowAmount() {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setRequestedAmount(5000.0); // Low amount <= 8000
        
        String result = applicantIdentificationService.generateApplicantId(request);
        
        assertEquals("applicant-no-loans", result);
    }

    @Test
    void shouldGenerateNoLoansIdForNullAmount() {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setRequestedAmount(null);
        
        String result = applicantIdentificationService.generateApplicantId(request);
        
        assertEquals("applicant-no-loans", result);
    }

    @Test
    void shouldGenerateRecentLoansIdForExactHighAmountThreshold() {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setRequestedAmount(15000.01); // Just above threshold
        
        String result = applicantIdentificationService.generateApplicantId(request);
        
        assertEquals("applicant-recent-loans", result);
    }

    @Test
    void shouldGenerateOldLoansIdForExactMediumAmountThreshold() {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setRequestedAmount(8000.01); // Just above medium threshold
        
        String result = applicantIdentificationService.generateApplicantId(request);
        
        assertEquals("applicant-old-loans", result);
    }

    @Test
    void shouldGenerateNoLoansIdForExactLowAmountThreshold() {
        LoanValidationRequest request = new LoanValidationRequest();
        request.setRequestedAmount(8000.0); // Exactly at threshold (not greater)
        
        String result = applicantIdentificationService.generateApplicantId(request);
        
        assertEquals("applicant-no-loans", result);
    }

    @Test
    void shouldReturnAuthenticatedApplicantId() {
        String result = applicantIdentificationService.getCurrentApplicantId();
        
        assertNotNull(result);
        assertEquals("authenticated-applicant-id", result);
    }

    @Test
    void shouldGenerateConsistentIdsForSameAmount() {
        LoanValidationRequest request1 = new LoanValidationRequest();
        LoanValidationRequest request2 = new LoanValidationRequest();
        request1.setRequestedAmount(25000.0);
        request2.setRequestedAmount(25000.0);
        
        String result1 = applicantIdentificationService.generateApplicantId(request1);
        String result2 = applicantIdentificationService.generateApplicantId(request2);
        
        assertEquals(result1, result2);
    }
}