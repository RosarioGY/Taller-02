# Setup Maven and Java Environment for Loan Validation Project
# This script helps set up the development environment

Write-Host "=== Loan Validation Project - Environment Setup ===" -ForegroundColor Green

# Check Java Installation
Write-Host "`nChecking Java installation..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "Java found: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "Java not found. Please install Java 17 or later." -ForegroundColor Red
    Write-Host "Download from: https://adoptium.net/" -ForegroundColor Blue
    exit 1
}

# Check JAVA_HOME
Write-Host "`nChecking JAVA_HOME..." -ForegroundColor Yellow
if ($env:JAVA_HOME) {
    Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
} else {
    Write-Host "JAVA_HOME not set. This may cause issues." -ForegroundColor Yellow
}

# Maven Installation Check and Setup
Write-Host "`nChecking Maven installation..." -ForegroundColor Yellow

# Try to use Chocolatey to install Maven
if (Get-Command choco -ErrorAction SilentlyContinue) {
    Write-Host "Chocolatey found. Installing Maven..." -ForegroundColor Green
    choco install maven -y
} elseif (Get-Command winget -ErrorAction SilentlyContinue) {
    Write-Host "Winget found. Installing Maven..." -ForegroundColor Green
    winget install --id Apache.Maven
} else {
    Write-Host "Package manager not found. Please install Maven manually:" -ForegroundColor Yellow
    Write-Host "1. Download from: https://maven.apache.org/download.cgi" -ForegroundColor Blue
    Write-Host "2. Extract to C:\Program Files\Apache\maven" -ForegroundColor Blue
    Write-Host "3. Add C:\Program Files\Apache\maven\bin to your PATH" -ForegroundColor Blue
    Write-Host "4. Set M2_HOME to C:\Program Files\Apache\maven" -ForegroundColor Blue
}

# Alternative: Try to fix Maven Wrapper
Write-Host "`nTrying to fix Maven Wrapper..." -ForegroundColor Yellow
if (Test-Path ".mvn\wrapper\maven-wrapper.properties") {
    Write-Host "Maven wrapper configuration found." -ForegroundColor Green
    
    # Download maven wrapper jar if missing
    $wrapperJar = ".mvn\wrapper\maven-wrapper.jar"
    if (!(Test-Path $wrapperJar)) {
        Write-Host "Downloading Maven wrapper jar..." -ForegroundColor Yellow
        $wrapperUrl = "https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
        try {
            Invoke-WebRequest -Uri $wrapperUrl -OutFile $wrapperJar
            Write-Host "Maven wrapper jar downloaded successfully." -ForegroundColor Green
        } catch {
            Write-Host "Failed to download Maven wrapper jar: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host "`n=== Setup Instructions ===" -ForegroundColor Green
Write-Host "1. Ensure Java 17+ is installed and JAVA_HOME is set"
Write-Host "2. Install Maven using a package manager or manually"
Write-Host "3. Restart your PowerShell/Terminal"
Write-Host "4. Run: mvn clean generate-sources compile test"
Write-Host "5. Run: mvn jacoco:report" -ForegroundColor Blue

Write-Host "`n=== Project Commands ===" -ForegroundColor Green
Write-Host "Generate OpenAPI classes: mvn generate-sources"
Write-Host "Compile project: mvn compile"
Write-Host "Run tests: mvn test"
Write-Host "Generate coverage report: mvn jacoco:report"
Write-Host "Run quality checks: .\quality-analysis.ps1" -ForegroundColor Blue

Write-Host "`nSetup script completed!" -ForegroundColor Green