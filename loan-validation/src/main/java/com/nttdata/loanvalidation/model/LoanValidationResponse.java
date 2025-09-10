package com.nttdata.loanvalidation.model;

import java.util.List;

public class LoanValidationResponse {
    private boolean eligible;
    private List<Reason> reasons;

    public LoanValidationResponse() {}

    public LoanValidationResponse(boolean eligible, List<Reason> reasons) {
        this.eligible = eligible;
        this.reasons = reasons;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public List<Reason> getReasons() {
        return reasons;
    }

    public void setReasons(List<Reason> reasons) {
        this.reasons = reasons;
    }
}

