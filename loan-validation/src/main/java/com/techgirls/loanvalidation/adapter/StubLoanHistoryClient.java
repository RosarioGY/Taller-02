package com.nttdata.loanvalidation.adapter;

import com.nttdata.loanvalidation.port.LoanHistoryClient;
import org.springframework.stereotype.Component;

@Component
public class StubLoanHistoryClient implements LoanHistoryClient {
    @Override
    public boolean hasGoodHistory(String customerId) {
        // Simulación: todos los clientes tienen buen historial
        return true;
    }
}

