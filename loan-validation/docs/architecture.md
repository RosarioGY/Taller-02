# Arquitectura — Loan Validation

## C3 — Diagrama de Secuencia
```mermaid
sequenceDiagram
  participant C as Cliente (Postman/App)
  participant API as LoanValidationsController
  participant S as LoanRulesService
  participant R as Reglas R1-R4
  participant RES as LoanValidationResult

  C->>API: POST /loan-validations<br/>LoanValidationRequest
  API->>S: validar(salary, amount, term, lastLoanDate)
  S->>R: Ejecutar R4 (datos válidos)
  R-->>S: Resultado R4
  S->>R: Ejecutar R1 (antigüedad préstamo)
  R-->>S: Resultado R1
  S->>R: Ejecutar R2 (plazo 1-36 meses)
  R-->>S: Resultado R2
  S->>R: Ejecutar R3 (capacidad de pago <= 40%)
  R-->>S: Resultado R3
  S-->>API: Construir LoanValidationResult<br/>eligible + reasons + monthPayment
  API-->>C: Response JSON (LoanValidationResult)
```

## C4 — Flowchart (Rules & Pipeline)
```mermaid
flowchart LR
  %% === Externo ===
  C[Cliente - Postman / App] -->|POST /loan-validations| CTRL

  %% === Microservicio ===
  subgraph SVC[loan-validation - Spring Boot WebFlux]
    direction TB
    CTRL[Controller - LoanValidationsController] --> SRV[Service - LoanRulesService]

    subgraph RULES[Business Rules]
      direction TB
      R4[R4: Datos válidos] 
      R1[R1: Antigüedad - <3m]
      R2[R2: Plazo - 1..36]
      R3[R3: Capacidad - ≤ 40% sueldo]
    end

    SRV --> R4
    SRV --> R1
    SRV --> R2
    SRV --> R3

    MP[(monthlyPayment = requestedAmount / termMonths)]
    SRV --> MP

    subgraph CONTRACT[Contract-first]
      OA[(OpenAPI YAML - src/main/resources/openapi/loan-validation.yaml)]
      GEN[[openapi-generator - mvn generate-sources]]
    end
    OA --> GEN --> CTRL
    GEN --> MDL[Modelos DTO - (LoanValidationRequest/Result)]

    subgraph QA[Quality]
      TST[[JUnit/Mockito - src/test]]
      JACO[[JaCoCo - coverage]]
      CS[[Checkstyle - verify]]
    end
    TST --> JACO
    TST --> CTRL
    TST --> SRV
    CS -->|verifica estilo| SVC

    CFG[(application.yml - server.port=8080)]
  end

  note right of SVC
    Stateless (sin BD)
    Clock inyectable para tests
  end note
```
