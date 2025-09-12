package com.techgirls.loanvalidation.port;

import java.time.LocalDate;
import reactor.core.publisher.Mono;

/**
* External port to query applicant's loan history.
* Returns empty Mono if applicant has no past loans.
* 
* Implementation note: In a real system, this would connect to an external
* loan history service. For testing, we use a configurable stub.
*/
public interface LoanHistoryClient {
    /**
     * Retrieves the last loan date for the given applicant.
     * @param applicantId unique identifier for the loan applicant
     * @return Mono containing the last loan date, or empty if no loans exist
     */
    Mono<LocalDate> getLastLoanDate(String applicantId);
    
    /**
     * Checks if a customer has any default history in their loan records.
     * @param customerId the unique identifier of the customer
     * @return a Mono that emits true if the customer has default history, false otherwise
     */
    Mono<Boolean> hasDefaultHistory(String customerId);
}
