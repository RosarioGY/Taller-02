
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(int termMonths) {
        this.termMonths = termMonths;
    }
}
package com.nttdata.loanvalidation.model;

public class LoanValidationRequest {
    private String customerId;
    private double amount;
    private int termMonths;

    public LoanValidationRequest() {}

    public LoanValidationRequest(String customerId, double amount, int termMonths) {
        this.customerId = customerId;
        this.amount = amount;
        this.termMonths = termMonths;
    }

    public String getCustomerId() {
        return customerId;
    }

