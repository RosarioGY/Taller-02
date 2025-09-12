package com.techgirls.loanvalidation.controller;

import com.techgirls.loanvalidation.api.LoanValidationsApi;
import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.model.LoanValidationResult;
import com.techgirls.loanvalidation.service.LoanRulesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class LoanValidationsController implements LoanValidationsApi {

    private final LoanRulesService rules;

    @Override
    public Mono<ResponseEntity<LoanValidationResult>> validateLoan(
            Mono<LoanValidationRequest> loanValidationRequest) {

        return loanValidationRequest.map(req -> {
            BigDecimal salary = BigDecimal.valueOf(req.getMonthlySalary());
            BigDecimal amount = BigDecimal.valueOf(req.getRequestedAmount());
            int term = req.getTermMonths();

            LocalDate lastLoanDate = null;
            if (req.getLastLoanDate() != null) {
                lastLoanDate = LocalDate.parse(req.getLastLoanDate());
            }

            // Reglas R1–R4
            List<String> reasons = rules.validate(salary, amount, term, lastLoanDate);

            // Armar respuesta
            LoanValidationResult res = new LoanValidationResult();
            res.setEligible(reasons.isEmpty());
            res.setReasons(reasons);
            res.setMonthlyPayment(rules.monthlyPayment(amount, term).doubleValue());

            return ResponseEntity.ok(res);
        });
    }
}
