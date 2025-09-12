@echo off
REM Script para ejecutar JaCoCo y CheckStyle en el proyecto
REM Uso: quality-check.bat

echo.
echo ========================================
echo   ANALISIS DE CALIDAD - LOAN VALIDATION
echo ========================================
echo.

REM Verificar si Maven está instalado
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven no está instalado o no está en el PATH
    echo Instalando Maven usando Chocolatey...
    where choco >nul 2>nul
    if %ERRORLEVEL% NEQ 0 (
        echo ERROR: Chocolatey no está instalado
        echo Por favor instale Maven manualmente desde: https://maven.apache.org/download.cgi
        pause
        exit /b 1
    )
    choco install maven -y
)

echo 1. Generando modelos desde OpenAPI...
call mvn clean generate-sources
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Falló la generación de código OpenAPI
    pause
    exit /b 1
)

echo.
echo 2. Compilando el proyecto...
call mvn compile test-compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Falló la compilación
    pause
    exit /b 1
)

echo.
echo 3. Ejecutando tests con JaCoCo...
call mvn test
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Algunos tests fallaron, continuando...
)

echo.
echo 4. Generando reporte de cobertura JaCoCo...
call mvn jacoco:report
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Falló la generación del reporte JaCoCo
    pause
    exit /b 1
)

echo.
echo 5. Ejecutando análisis CheckStyle...
call mvn checkstyle:check
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: CheckStyle encontró violaciones de estilo
)

echo.
echo 6. Verificando cobertura de código...
call mvn jacoco:check
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: La cobertura de código no cumple los umbrales mínimos
)

echo.
echo ========================================
echo   ANALISIS COMPLETADO
echo ========================================
echo.
echo Reportes generados:
echo   - JaCoCo HTML: target\site\jacoco\index.html
echo   - JaCoCo XML:  target\site\jacoco\jacoco.xml
echo   - CheckStyle:  target\checkstyle-result.xml
echo.
echo Para ver el reporte de cobertura:
echo   start target\site\jacoco\index.html
echo.

REM Mostrar resumen de cobertura si existe
if exist "target\site\jacoco\index.html" (
    echo Abriendo reporte de cobertura...
    start "" "target\site\jacoco\index.html"
)

pause