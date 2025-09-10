package com.nttdata.loanvalidation.port;

public interface LoanHistoryClient {
    boolean hasGoodHistory(String customerId);
}

