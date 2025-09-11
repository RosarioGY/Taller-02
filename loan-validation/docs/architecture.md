# Arquitectura — Loan Validation

## C1 — Diagrama de Secuencia
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

## C2 — Diagrama de Componentes
```mermaid
flowchart LR
    C["Cliente / Postman"] --> API["Controller: LoanValidationsController"]
    API --> SVC["Service: LoanRulesService"]

    SVC --> PORT["Port: LoanHistoryClient (interface)"]
    PORT --> ADP["Adapter: StubLoanHistoryClient"]
    ADP -.-> EXT["Loan History Service (externo)"]

    subgraph Modelos
        REQ["LoanValidationRequest"]
        RES["LoanValidationResponse"]
        RSL["LoanValidationResult"]
        RSN["Reason & ReasonType"]
    end
    API <-->|DTO| REQ
    API <-->|DTO| RES
    SVC --> RSL
    RSL --> RSN

    subgraph Config
        CLK["ClockConfig"]
        APP["application.yml"]
    end
    CLK --> SVC
    APP --> API
    APP --> SVC

    subgraph Contract["Contract-first"]
        OAY["OpenAPI YAML (resources/openapi/loan-validation.yaml)"]
        OAG["openapi-generator (mvn generate-sources)"]
    end
    OAY --> OAG --> API

    subgraph Observability
        LOG["Logs"]
        MET["Metrics"]
    end
    SVC --> LOG
    SVC --> MET

```

## C3 — Diagrama de flujo (proceso)
```mermaid
flowchart TD
    START([Inicio])
    V4[Validar datos (R4)]
R1{Antiguedad ≤ 3m?}
R2{Plazo 1..36?}
R3{Capacidad ≤ 40%?}
MP[Calcular monthlyPayment]
APROBAR([Aprobar solicitud])
RECH([Rechazar solicitud])
FIN([Fin])

START --> V4
V4 -->|Invalidos| RECH
V4 -->|OK| R1
R1 -->|No| RECH
R1 -->|Si| R2
R2 -->|No| RECH
R2 -->|Si| R3
R3 -->|No| RECH
R3 -->|Si| MP
MP --> APROBAR
RECH --> FIN
APROBAR --> FIN

```
