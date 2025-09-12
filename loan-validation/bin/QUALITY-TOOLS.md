# JaCoCo y CheckStyle - Herramientas de Calidad de Código

Este proyecto implementa **JaCoCo** para cobertura de código y **CheckStyle** para análisis estático de código.

## 🎯 JaCoCo - Cobertura de Código

### Configuración

- **Versión**: 0.8.12
- **Umbrales de Cobertura**:
  - Líneas: 80%
  - Ramas: 70% 
  - Métodos: 75%

### Exclusiones

- Clases generadas por OpenAPI (`**/api/**`, `**/model/**`)
- Configuraciones de Spring Boot (`**/config/**`)
- Clase principal (`**/*Application.class`)

### Comandos Maven

```bash
# Ejecutar tests con cobertura
mvn test

# Generar reporte HTML/XML/CSV
mvn jacoco:report

# Verificar umbrales de cobertura
mvn jacoco:check

# Ejecutar todo junto
mvn clean test jacoco:report jacoco:check
```

### Reportes

- **HTML**: `target/site/jacoco/index.html` (navegable)
- **XML**: `target/site/jacoco/jacoco.xml` (CI/CD)
- **CSV**: `target/site/jacoco/jacoco.csv` (análisis)

## 📋 CheckStyle - Análisis Estático

### Configuración

- **Versión Plugin**: 3.3.1
- **Versión CheckStyle**: 10.12.4
- **Archivo Config**: `config/checkstyle.xml`

### Reglas Implementadas

#### Formatting
- Longitud máxima de línea: 120 caracteres
- Uso de espacios (no tabs)
- Espacios después de palabras clave
- Indentación: 4 espacios

#### Naming Conventions
- CamelCase para clases y métodos
- UPPER_CASE para constantes
- camelCase para variables

#### Code Quality
- Llaves obligatorias en if/for/while
- No imports con `*`
- No imports sin usar
- Orden alfabético de imports

#### Best Practices
- Máximo 7 parámetros por método
- Máximo 150 líneas por método
- Máximo 30 statements ejecutables
- Constructor en clases utility oculto

### Comandos Maven

```bash
# Verificar estilo de código
mvn checkstyle:check

# Generar reporte de CheckStyle
mvn checkstyle:checkstyle

# Ejecutar en fase de validación
mvn validate
```

### Reportes

- **XML**: `target/checkstyle-result.xml`
- **Consola**: Output directo durante build

## 🚀 Ejecución Rápida

### Opción 1: Script Automatizado
```bash
./quality-check.bat
```

### Opción 2: Maven Goals
```bash
# Análisis completo
mvn clean validate compile test jacoco:report checkstyle:check jacoco:check

# Solo cobertura
mvn clean test jacoco:report

# Solo CheckStyle  
mvn checkstyle:check
```

### Opción 3: Profiles Maven (Opcional)

Agregar al `pom.xml`:

```xml
<profiles>
    <profile>
        <id>quality</id>
        <build>
            <plugins>
                <!-- Plugins ya están configurados -->
            </plugins>
        </build>
    </profile>
</profiles>
```

Uso: `mvn clean test -Pquality`

## 📊 Interpretación de Resultados

### JaCoCo

- 🟢 **Verde**: Línea cubierta por tests
- 🔴 **Rojo**: Línea no cubierta
- 🟡 **Amarillo**: Rama parcialmente cubierta

**Métricas importantes**:
- **Element**: Class, Method, Line, Branch
- **Missed**: Elementos no cubiertos
- **Cov.**: Porcentaje de cobertura

### CheckStyle

**Severidades**:
- `ERROR`: Violación crítica
- `WARNING`: Violación recomendada
- `INFO`: Sugerencia de mejora

**Categorías comunes**:
- `Whitespace`: Espaciado y formato
- `Naming`: Convenciones de nombres
- `Size`: Límites de tamaño
- `Imports`: Gestión de imports

## 🔧 Configuración Avanzada

### Personalizar Umbrales JaCoCo

En `pom.xml`:

```xml
<properties>
    <jacoco.line.coverage.ratio>0.85</jacoco.line.coverage.ratio>
    <jacoco.branch.coverage.ratio>0.75</jacoco.branch.coverage.ratio>
</properties>
```

### Personalizar CheckStyle

Editar `config/checkstyle.xml`:

```xml
<module name="LineLength">
    <property name="max" value="100"/>
</module>
```

### Integración CI/CD

```yaml
# GitHub Actions / Azure DevOps
- name: Quality Check
  run: |
    mvn clean test jacoco:report checkstyle:check
    # Publicar reportes como artifacts
```

## 📈 Métricas Objetivo

| Métrica | Objetivo | Crítico |
|---------|----------|---------|
| Cobertura Líneas | ≥80% | ≥60% |
| Cobertura Ramas | ≥70% | ≥50% |
| Cobertura Métodos | ≥75% | ≥60% |
| Violaciones CheckStyle | <10 | <50 |

## 🎯 Buenas Prácticas

### Para Desarrolladores

1. **Ejecutar antes de commit**:
   ```bash
   mvn clean test jacoco:report checkstyle:check
   ```

2. **Revisar reportes regularmente**
3. **Escribir tests para nuevas funcionalidades**
4. **Seguir convenciones de CheckStyle**

### Para el Equipo

1. **Definir umbrales mínimos**
2. **Revisar métricas en code reviews**
3. **Automatizar en CI/CD**
4. **Trending de métricas over time**