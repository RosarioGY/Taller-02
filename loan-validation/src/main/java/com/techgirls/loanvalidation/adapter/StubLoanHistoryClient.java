package com.nttdata.loanvalidation.adapter;

import com.nttdata.loanvalidation.port.LoanHistoryClient;
import java.time.LocalDate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
* Simple stub that always returns empty (no prior loans).
* Activate with profile 'stub'.
*/
@Component
@Profile("stub")
public class StubLoanHistoryClient implements LoanHistoryClient {
    @Override
    public Mono<LocalDate> getLastLoanDate(String applicantId) {
        return Mono.empty();
    }
}
