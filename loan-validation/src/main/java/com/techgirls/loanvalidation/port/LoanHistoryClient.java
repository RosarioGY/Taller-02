package com.nttdata.loanvalidation.port;

import java.time.LocalDate;
import reactor.core.publisher.Mono;

/**
* External port to query applicant's last loan date.
* Returns empty if applicant has no past loans.
*/
public interface LoanHistoryClient {
    Mono<LocalDate> getLastLoanDate(String applicantId);
}
