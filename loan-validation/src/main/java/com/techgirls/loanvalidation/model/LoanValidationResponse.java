package com.nttdata.loanvalidation.model;

import java.util.List;

public class LoanValidationResult {
    private boolean eligible;
    private List<ReasonType> reasons;
    private double monthlyPayment;

    public LoanValidationResult() {}

    public LoanValidationResult(boolean eligible, List<ReasonType> reasons, double monthlyPayment) {
        this.eligible = eligible;
        this.reasons = reasons;
        this.monthlyPayment = monthlyPayment;
    }

    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }
    public List<ReasonType> getReasons() { return reasons; }
    public void setReasons(List<ReasonType> reasons) { this.reasons = reasons; }
    public double getMonthlyPayment() { return monthlyPayment; }
    public void setMonthlyPayment(double monthlyPayment) { this.monthlyPayment = monthlyPayment; }
}
