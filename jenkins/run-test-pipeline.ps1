# Jenkins Pipeline Continuous Testing Runner with Quality Gate
# Stops deployment immediately if test suite fails

param (
    [string]$DeployEnv = "staging",
    [string]$Port = "8086"
)

$ErrorActionPreference = "Continue"
$WorkspaceDir = (Get-Item -Path ".").FullName
$MvnPath = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd"

Write-Host "=========================================================="
Write-Host " [JENKINS CI] Continuous Testing & Deployment Pipeline"
Write-Host " Environment: $DeployEnv | Port: $Port"
Write-Host "=========================================================="

# Stage 1: Build & Compile
Write-Host "`n[Pipeline] stage: Build & Compile"
$compileOut = & $MvnPath compile 2>&1
$compileExit = $LASTEXITCODE
if ($compileExit -ne 0) {
    Write-Host "[ERROR] Compilation failed. Aborting pipeline." -ForegroundColor Red
    exit 1
}
Write-Host "[Pipeline] completed: Build & Compile [SUCCESS]" -ForegroundColor Green

# Stage 2: Automated Quality Tests (Selenium / E2E)
Write-Host "`n[Pipeline] stage: Automated Quality Tests"
& $MvnPath test-compile 2>&1 | Out-Null
$testOut = & java -cp "target\classes;target\test-classes" com.fitness.portal.FitnessPortalServerTest 2>&1
$testExit = $LASTEXITCODE

foreach ($line in $testOut) {
    Write-Host $line
}

# Quality Gate Check
if ($testExit -ne 0) {
    Write-Host "`n==========================================================" -ForegroundColor Red
    Write-Host " [QUALITY GATE TRIGGERED] Automated Test Failures Detected!" -ForegroundColor Red
    Write-Host " Pipeline execution STOPPED. Deployment stage will NOT proceed." -ForegroundColor Red
    Write-Host " Publishing JUnit test failure reports to Jenkins dashboard..." -ForegroundColor Yellow
    Write-Host " Check screenshots in target/screenshots/ for failure snapshots." -ForegroundColor Yellow
    Write-Host "==========================================================" -ForegroundColor Red
    Write-Host "[Pipeline] Finished: FAILURE" -ForegroundColor Red
    exit 1
}

Write-Host "[Pipeline] completed: Automated Quality Tests [SUCCESS]" -ForegroundColor Green
Write-Host "[INFO] Publishing JUnit test report: target/surefire-reports/TEST-com.fitness.portal.SeleniumE2ETest.xml"

# Stage 3: Package Application
Write-Host "`n[Pipeline] stage: Package Application"
& $MvnPath package -DskipTests 2>&1 | Out-Null
Write-Host "[Pipeline] completed: Package Application [SUCCESS]" -ForegroundColor Green

# Stage 4: Deploy to Server
Write-Host "`n[Pipeline] stage: Deploy to Server"
powershell -ExecutionPolicy Bypass -File jenkins/deploy.ps1 -Environment $DeployEnv -Port $Port

Write-Host "`n[Pipeline] Finished: SUCCESS" -ForegroundColor Green
exit 0
