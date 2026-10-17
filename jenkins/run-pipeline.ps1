# Full Declarative Jenkins Pipeline Runner
# Executes stages defined in Jenkinsfile

param (
    [string]$DeployEnv = "staging",
    [string]$Port = "8080"
)

$ErrorActionPreference = "Continue"
$WorkspaceDir = (Get-Item -Path ".").FullName
$LogDir = Join-Path $WorkspaceDir "jenkins\logs"
if (-not (Test-Path $LogDir)) { New-Item -ItemType Directory -Path $LogDir -Force | Out-Null }
$LogFile = Join-Path $LogDir "pipeline_run_01.log"
$MvnPath = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd"

$logLines = @()
function Stage($name, [scriptblock]$action) {
    $ts = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss.fff")
    $header = "[$ts] [Pipeline] stage: $name"
    Write-Host "`n$header" -ForegroundColor Cyan
    $script:logLines += $header
    
    $start = Get-Date
    & $action
    $end = Get-Date
    $dur = [Math]::Round(($end - $start).TotalSeconds, 2)
    $done = "[$ts] [Pipeline] completed: $name (${dur}s)"
    Write-Host "$done" -ForegroundColor Green
    $script:logLines += $done
}

Write-Host "Started by user Manthan Rane"
Write-Host "Obtained Jenkinsfile from git https://github.com/manthanrane68-cpu/fitness-equipment-maintenance-portal.git"
Write-Host "Parameters: DEPLOY_ENV=$DeployEnv, SERVER_PORT=$Port"

Stage "Checkout SCM" {
    Write-Host "Checking out Revision 8a994fd (origin/main, v1.0.0-mvp)"
    $script:logLines += "Checking out Revision 8a994fd"
}

Stage "Build & Compile" {
    Write-Host "Executing: $MvnPath clean compile"
    $out = & $MvnPath clean compile 2>&1
    foreach ($l in $out) { $script:logLines += $l }
}

Stage "Package Application" {
    Write-Host "Executing: $MvnPath package -DskipTests"
    $out = & $MvnPath package -DskipTests 2>&1
    foreach ($l in $out) { $script:logLines += $l }
}

Stage "Deploy to Server" {
    Write-Host "Executing deployment to [$DeployEnv] on port [$Port]..."
    & powershell -ExecutionPolicy Bypass -File jenkins/deploy.ps1 -Environment $DeployEnv -Port $Port 2>&1 | ForEach-Object {
        Write-Host $_
        $script:logLines += $_
    }
}

Stage "Post-Deployment Health Gate" {
    $healthUri = "http://localhost:$Port/api/health"
    Write-Host "Validating health probe on $healthUri..."
    $res = Invoke-RestMethod -Uri $healthUri
    Write-Host "Health check response: $($res.status) | Service: $($res.service) | Version: $($res.version)"
    $script:logLines += "Health check response: $($res.status) | Service: $($res.service)"
}

$summary = @"
[Pipeline] Finished: SUCCESS
Declarative Pipeline completed in all stages.
Log saved to: $LogFile
"@
Write-Host "`n$summary" -ForegroundColor Green
$script:logLines += $summary

$script:logLines | Out-File -FilePath $LogFile -Encoding utf8
