# Loan Validation Service

Un microservicio reactivo desarrollado con Spring Boot WebFlux que valida la elegibilidad de solicitudes de préstamos bancarios utilizando arquitectura hexagonal y contract-first development.

## 🚀 Quick Start

### Prerrequisitos
- Java 17+
- Maven 3.8+
- Git

### Instalación

```bash
# Clonar el repositorio
git clone https://github.com/RosarioGY/Taller-02.git
cd Taller-02/loan-validation

# Compilar el proyecto
mvn clean compile

# Ejecutar tests (cuando estén implementados)
mvn test

# Ejecutar la aplicación
mvn spring-boot:run
```

### Verificar la instalación

```bash
# Health check
curl http://localhost:8080/actuator/health

# Documentación de la API
curl http://localhost:8080/v3/api-docs
```

## 📋 Funcionalidades

### Validación de Préstamos
El servicio evalúa solicitudes de préstamos aplicando las siguientes reglas de negocio:

- **R1**: Sin préstamos en los últimos 3 meses
- **R2**: Plazo entre 1-36 meses
- **R3**: Cuota mensual ≤ 40% del salario mensual  
- **R4**: Datos válidos (montos > 0)

### API Endpoint

```http
POST /loan-validations
Content-Type: application/json

{
  "monthlySalary": 2500.00,
  "requestedAmount": 6000.00,
  "termMonths": 24,
  "lastLoanDate": "2025-04-01"
}
```

**Respuesta:**
```json
{
  "eligible": true,
  "reasons": [],
  "monthlyPayment": 250.0
}
```

## 🏗️ Arquitectura

### Stack Tecnológico
- **Framework**: Spring Boot 3.5.5
- **Programación Reactiva**: Spring WebFlux
- **Java**: 17
- **Build Tool**: Maven
- **API Design**: OpenAPI 3.0.3
- **Testing**: JUnit 5, Mockito, Reactor Test
- **Code Quality**: Checkstyle, JaCoCo

### Arquitectura Hexagonal

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Controllers   │────│    Services      │────│   Adapters      │
│   (Web Layer)   │    │ (Business Logic) │    │ (External APIs) │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   OpenAPI       │    │     Ports        │    │ Loan History    │
│   Generated     │    │   (Interfaces)   │    │    Client       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## 📁 Estructura del Proyecto

```
loan-validation/
├── src/main/java/com/techgirls/loanvalidation/
│   ├── adapter/              # Implementaciones externas
│   ├── config/               # Configuración Spring
│   ├── controller/           # REST Controllers
│   ├── model/                # Modelos de dominio
│   ├── port/                 # Interfaces (puertos)
│   ├── service/              # Lógica de negocio
│   └── web/                  # Manejo de excepciones
├── src/main/resources/
│   ├── openapi/              # Especificaciones API
│   └── application.yml       # Configuración
├── src/test/java/            # Tests unitarios e integración
├── target/generated-sources/ # Código generado por OpenAPI
└── pom.xml                   # Configuración Maven
```

## 🧪 Testing

### Ejecutar Tests
```bash
# Todos los tests
mvn test

# Tests específicos
mvn test -Dtest=EligibilityServiceTest

# Con cobertura
mvn jacoco:report
```

### Cobertura de Código
Los reportes de cobertura se generan en `target/site/jacoco/index.html`

## 🔧 Desarrollo

### Generar Código desde OpenAPI
```bash
mvn clean generate-sources
```

### Verificar Estilo de Código
```bash
mvn checkstyle:check
```

### Profiles Disponibles
- `default`: Configuración básica
- `stub`: Usa StubLoanHistoryClient para desarrollo

```bash
mvn spring-boot:run -Dspring.profiles.active=stub
```

## 📚 Documentación de la API

### OpenAPI/Swagger
- **Especificación**: `src/main/resources/openapi/loan-eligibility.yaml`
- **Documentación interactiva**: http://localhost:8080/swagger-ui.html (cuando esté configurado)
- **JSON Schema**: http://localhost:8080/v3/api-docs

### Ejemplos de Uso

#### Solicitud Elegible
```bash
curl -X POST http://localhost:8080/loan-validations \
  -H "Content-Type: application/json" \
  -d '{
    "monthlySalary": 3000.00,
    "requestedAmount": 5000.00,
    "termMonths": 20,
    "lastLoanDate": "2024-01-01"
  }'
```

#### Solicitud No Elegible
```bash
curl -X POST http://localhost:8080/loan-validations \
  -H "Content-Type: application/json" \
  -d '{
    "monthlySalary": 1000.00,
    "requestedAmount": 10000.00,
    "termMonths": 12
  }'
```

## � Estado del Proyecto

> ⚠️ **Nota**: Este proyecto está en desarrollo. Ver [ANALYSIS.md](./ANALYSIS.md) para un análisis detallado del estado actual y mejoras pendientes.

### ✅ Milestones Completados
- ✅ Suite completa de tests implementada
- ✅ Packages unificados y consistentes  
- ✅ Manejo de errores mejorado (RFC 7807)
- ✅ Configuración multi-entorno (dev/test/prod)
- ✅ 100% contract-first compliance

### Próximos Milestones
- [ ] Implementar métricas con Micrometer
- [ ] Añadir seguridad básica
- [ ] Configurar CI/CD pipeline
- [ ] Documentación de arquitectura

## 🤝 Contribuir

### Flujo de Desarrollo
1. Fork del repositorio
2. Crear feature branch (`git checkout -b feature/nueva-funcionalidad`)
3. Commit cambios (`git commit -am 'Add nueva funcionalidad'`)
4. Push al branch (`git push origin feature/nueva-funcionalidad`)
5. Crear Pull Request

### Estándares de Código
- Seguir las reglas de Checkstyle configuradas
- Mantener cobertura de tests >80%
- Documentar APIs con OpenAPI
- Usar conventional commits

## 📞 Soporte

- **Issues**: [GitHub Issues](https://github.com/RosarioGY/Taller-02/issues)
- **Discusiones**: [GitHub Discussions](https://github.com/RosarioGY/Taller-02/discussions)

## � Licencia

Este proyecto está bajo la licencia MIT. Ver el archivo [LICENSE](LICENSE) para más detalles.

---

**Desarrollado con ❤️ por RosarioGY**