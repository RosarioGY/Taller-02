# loan-validation-ms (Single Microservice)

Microservicio **único** para validar elegibilidad de préstamos (WebFlux, contract-first).

## Reglas
- **R1**: `employmentMonths >= 3`
- **R2**: `1 <= termMonths <= 36`
- **R3**: `monthlyPayment = requestedAmount/termMonths <= 40% * monthlySalary`
- **R4**: Datos válidos (positivos y presentes)

## Ejecutar
```bash
mvn clean verify
mvn spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Postman
`postman/Taller02-LoanEligibility.postman_collection.json`

## Calidad
- JaCoCo (coverage) y Checkstyle (google_checks.xml)
