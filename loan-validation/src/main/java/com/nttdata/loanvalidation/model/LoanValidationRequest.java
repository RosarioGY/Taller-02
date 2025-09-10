package com.nttdata.loanvalidation.model;

import java.time.LocalDate;

public class LoanValidationRequest {
    private double monthlySalary;
    private double requestedAmount;
    private int termMonths;
    private LocalDate lastLoanDate;

    public LoanValidationRequest() {}

    public LoanValidationRequest(double monthlySalary, double requestedAmount, int termMonths, LocalDate lastLoanDate) {
        this.monthlySalary = monthlySalary;
        this.requestedAmount = requestedAmount;
        this.termMonths = termMonths;
        this.lastLoanDate = lastLoanDate;
    }

    public double getMonthlySalary() { return monthlySalary; }
    public void setMonthlySalary(double monthlySalary) { this.monthlySalary = monthlySalary; }
    public double getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(double requestedAmount) { this.requestedAmount = requestedAmount; }
    public int getTermMonths() { return termMonths; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }
    public LocalDate getLastLoanDate() { return lastLoanDate; }
    public void setLastLoanDate(LocalDate lastLoanDate) { this.lastLoanDate = lastLoanDate; }
}
