# Taller 02 — Microservicio único: Validación de Préstamos (WebFlux)

Microservicio **único** que evalúa la elegibilidad de un préstamo aplicando reglas R1–R4. Incluye contrato **OpenAPI**, **programación reactiva (WebFlux/Mono)**, **programación funcional**, **pruebas (JUnit + Mockito + WebTestClient)** y **calidad (JaCoCo + Checkstyle)**, además de **diagramas** y **colección Postman**.

---

## ✔️ Entregables
- **Repositorio de código**: `/loan-validation-ms`
- **Colección de Postman**: `/loan-validation-ms/postman/Taller02-LoanEligibility.postman_collection.json`
- **Word (diagramas + evidencias de uso)**: ver documentos compartidos y `/loan-validation-ms/docs/`
- **Diagramas**:  
  - Flujo (`.drawio` y `.png`): `/loan-validation-ms/docs/flowchart_eligibilidad.drawio`, `/loan-validation-ms/docs/arquitectura_taller02.png`  
  - Arquitectura (`.drawio`): `/loan-validation-ms/docs/architecture (2).drawio`

---

## 🧠 Reglas de negocio (R1–R4)
- **R1**: Antigüedad laboral `employmentMonths ≥ 3`
- **R2**: Plazo `1 ≤ termMonths ≤ 36`
- **R3**: Capacidad de pago `monthlyPayment = requestedAmount/termMonths ≤ 40% * monthlySalary`
- **R4**: Datos válidos y positivos (`monthlySalary`, `requestedAmount`, `termMonths`)

---

## 🏗️ Tech stack
- **Java 17**, **Spring Boot 3.3.x**, **WebFlux (Mono)**
- **Maven**, **Lombok**
- **JUnit 5, Mockito, WebTestClient**
- **JaCoCo**, **Checkstyle (google_checks.xml)**
- **OpenAPI 3.0** (springdoc)

---

## ▶️ Cómo ejecutar
```bash
cd loan-validation-ms
mvn clean verify          # compila, ejecuta tests, genera cobertura y checkstyle
mvn spring-boot:run       # levanta en http://localhost:8080
