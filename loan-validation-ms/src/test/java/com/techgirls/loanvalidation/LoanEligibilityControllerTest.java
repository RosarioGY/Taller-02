package com.techgirls.loanvalidation;

import com.techgirls.loanvalidation.model.LoanValidationRequest;
import com.techgirls.loanvalidation.service.LoanEligibilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

@WebFluxTest
public class LoanEligibilityControllerTest {
  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private LoanEligibilityService service;

  private LoanValidationRequest ok;

  @BeforeEach
  void setup() {
    ok = LoanValidationRequest.builder()
        .monthlySalary(2500.0)
        .requestedAmount(6000.0)
        .termMonths(24)
        .employmentMonths(12)
        .build();
  }

  @Test
  void post_validate_returns_200() {
    Mockito.when(service.validate(Mockito.any()))
        .thenReturn(Mono.just(new com.techgirls.loanvalidation.model.LoanValidationResponse(true, 250.0, List.of())));

    webTestClient.post()
        .uri("/api/loan-eligibility")
        .bodyValue(ok)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.eligible").isEqualTo(true);
  }
}
