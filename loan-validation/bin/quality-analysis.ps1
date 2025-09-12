# PowerShell Script para Análisis de Calidad de Código
# JaCoCo + CheckStyle para Loan Validation Service

param(
    [Parameter(Mandatory=$false)]
    [string]$Profile = "dev",
    
    [Parameter(Mandatory=$false)]
    [switch]$SkipTests = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$OpenReports = $true
)

# Configuración
$ProjectRoot = $PSScriptRoot
$TargetDir = "$ProjectRoot\target"
$JaCoCoReport = "$TargetDir\site\jacoco\index.html"
$CheckStyleReport = "$TargetDir\checkstyle-result.xml"

# Colores para output
function Write-ColoredOutput {
    param([string]$Message, [string]$Color = "White")
    switch ($Color) {
        "Green" { Write-Host $Message -ForegroundColor Green }
        "Yellow" { Write-Host $Message -ForegroundColor Yellow }
        "Red" { Write-Host $Message -ForegroundColor Red }
        "Cyan" { Write-Host $Message -ForegroundColor Cyan }
        default { Write-Host $Message }
    }
}

# Header
Write-ColoredOutput "`n========================================" "Cyan"
Write-ColoredOutput "   ANÁLISIS DE CALIDAD - LOAN VALIDATION" "Cyan"
Write-ColoredOutput "========================================`n" "Cyan"

Write-ColoredOutput "Configuración:" "Yellow"
Write-ColoredOutput "  - Profile: $Profile" "White"
Write-ColoredOutput "  - Skip Tests: $SkipTests" "White"
Write-ColoredOutput "  - Open Reports: $OpenReports`n" "White"

# Verificar Maven wrapper
if (-not (Test-Path "mvnw.cmd")) {
    Write-ColoredOutput "❌ ERROR: mvnw.cmd no encontrado" "Red"
    Write-ColoredOutput "Ejecutando desde directorio: $ProjectRoot" "Yellow"
    exit 1
}

# Función para ejecutar comandos Maven
function Invoke-MavenCommand {
    param([string]$Goals, [string]$Description)
    
    Write-ColoredOutput "🔄 $Description..." "Yellow"
    $startTime = Get-Date
    
    $result = & .\mvnw.cmd $Goals.Split(' ') 2>&1
    $exitCode = $LASTEXITCODE
    $endTime = Get-Date
    $duration = [math]::Round(($endTime - $startTime).TotalSeconds, 2)
    
    if ($exitCode -eq 0) {
        Write-ColoredOutput "✅ $Description completado en ${duration}s" "Green"
        return $true
    } else {
        Write-ColoredOutput "❌ $Description falló en ${duration}s" "Red"
        Write-ColoredOutput "Output:" "Yellow"
        $result | ForEach-Object { Write-ColoredOutput "  $_" "White" }
        return $false
    }
}

# Pipeline de análisis
try {
    # 1. Limpiar y generar código
    if (-not (Invoke-MavenCommand "clean generate-sources" "Limpieza y generación de código OpenAPI")) {
        throw "Falló la generación de código"
    }

    # 2. Compilar
    if (-not (Invoke-MavenCommand "compile test-compile" "Compilación")) {
        throw "Falló la compilación"
    }

    # 3. Ejecutar tests (opcional)
    if (-not $SkipTests) {
        $testResult = Invoke-MavenCommand "test" "Ejecución de tests con JaCoCo"
        if (-not $testResult) {
            Write-ColoredOutput "⚠️  Algunos tests fallaron, continuando..." "Yellow"
        }
    } else {
        Write-ColoredOutput "⏩ Tests omitidos por parámetro" "Yellow"
    }

    # 4. Generar reportes JaCoCo
    if (-not (Invoke-MavenCommand "jacoco:report" "Generación de reporte JaCoCo")) {
        Write-ColoredOutput "⚠️  Reporte JaCoCo falló, continuando..." "Yellow"
    }

    # 5. CheckStyle
    $checkStyleResult = Invoke-MavenCommand "checkstyle:check" "Análisis CheckStyle"
    if (-not $checkStyleResult) {
        Write-ColoredOutput "⚠️  CheckStyle encontró violaciones" "Yellow"
    }

    # 6. Verificar cobertura (solo en profile quality)
    if ($Profile -eq "quality") {
        $coverageResult = Invoke-MavenCommand "jacoco:check" "Verificación de umbrales de cobertura"
        if (-not $coverageResult) {
            Write-ColoredOutput "⚠️  Cobertura no cumple umbrales del profile quality" "Yellow"
        }
    }

    # Resumen
    Write-ColoredOutput "`n========================================" "Cyan"
    Write-ColoredOutput "   ANÁLISIS COMPLETADO" "Cyan"
    Write-ColoredOutput "========================================`n" "Cyan"

    # Información de reportes
    Write-ColoredOutput "📊 Reportes Generados:" "Green"
    
    if (Test-Path $JaCoCoReport) {
        $jacocoSize = [math]::Round((Get-Item $JaCoCoReport).Length / 1KB, 2)
        Write-ColoredOutput "  ✅ JaCoCo HTML: target\site\jacoco\index.html (${jacocoSize} KB)" "White"
    } else {
        Write-ColoredOutput "  ❌ JaCoCo HTML: No generado" "Red"
    }

    if (Test-Path "$TargetDir\site\jacoco\jacoco.xml") {
        Write-ColoredOutput "  ✅ JaCoCo XML: target\site\jacoco\jacoco.xml" "White"
    }

    if (Test-Path $CheckStyleReport) {
        Write-ColoredOutput "  ✅ CheckStyle XML: target\checkstyle-result.xml" "White"
    } else {
        Write-ColoredOutput "  ❌ CheckStyle XML: No generado" "Red"
    }

    # Abrir reportes
    if ($OpenReports -and (Test-Path $JaCoCoReport)) {
        Write-ColoredOutput "`n🌐 Abriendo reporte de cobertura..." "Green"
        Start-Process $JaCoCoReport
    }

    # Estadísticas rápidas de JaCoCo
    if (Test-Path "$TargetDir\site\jacoco\jacoco.csv") {
        Write-ColoredOutput "`n📈 Resumen de Cobertura:" "Green"
        $csv = Import-Csv "$TargetDir\site\jacoco\jacoco.csv"
        $total = $csv | Where-Object { $_.CLASS -eq "Total" }
        if ($total) {
            $lineCoverage = [math]::Round((1 - ($total.LINE_MISSED / $total.LINE_COVERED)) * 100, 2)
            $branchCoverage = [math]::Round((1 - ($total.BRANCH_MISSED / $total.BRANCH_COVERED)) * 100, 2)
            Write-ColoredOutput "  - Cobertura de Líneas: ${lineCoverage}%" "White"
            Write-ColoredOutput "  - Cobertura de Ramas: ${branchCoverage}%" "White"
        }
    }

    Write-ColoredOutput "`n🎉 Análisis de calidad completado exitosamente!" "Green"

} catch {
    Write-ColoredOutput "`n❌ ERROR: $($_.Exception.Message)" "Red"
    exit 1
}

# Comandos útiles
Write-ColoredOutput "`n🔧 Comandos Útiles:" "Cyan"
Write-ColoredOutput "  .\mvnw test jacoco:report                 # Solo cobertura" "White"
Write-ColoredOutput "  .\mvnw checkstyle:check                   # Solo CheckStyle" "White"
Write-ColoredOutput "  .\mvnw clean test -Pquality               # Profile quality" "White"
Write-ColoredOutput "  .\quality-analysis.ps1 -Profile quality   # Este script en modo quality" "White"
Write-ColoredOutput "  .\quality-analysis.ps1 -SkipTests         # Sin ejecutar tests`n" "White"