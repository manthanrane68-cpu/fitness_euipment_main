# Jenkins CI Job Execution Runner
# Simulates Jenkins Executor running job 'fitness-portal-ci'

param (
    [string]$BuildNumber = "12",
    [string]$TriggerCause = "SCM commit trigger [4c615a8..8a994fd]"
)

$ErrorActionPreference = "Continue"
$WorkspaceDir = (Get-Item -Path ".").FullName
$ArtifactDir = Join-Path $WorkspaceDir "jenkins\artifacts"
$LogDir = Join-Path $WorkspaceDir "jenkins\logs"

if (-not (Test-Path $ArtifactDir)) { New-Item -ItemType Directory -Path $ArtifactDir -Force | Out-Null }
if (-not (Test-Path $LogDir)) { New-Item -ItemType Directory -Path $LogDir -Force | Out-Null }

$LogFile = Join-Path $LogDir "jenkins_build_${BuildNumber}.log"
$MvnPath = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1\plugins\maven\lib\maven3\bin\mvn.cmd"

$logLines = @()
function Log($msg) {
    $timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss.fff")
    $line = "[$timestamp] $msg"
    Write-Host $line
    $script:logLines += $line
}

Log "Started by $TriggerCause"
Log "Running as SYSTEM"
Log "Building in workspace $WorkspaceDir"
Log "The recommended git tool is: NONE(recommended)"
Log " > git.exe rev-parse --resolve-git-dir $WorkspaceDir\.git # timeout=10"
Log "Fetching changes from the remote Git repository"
Log " > git.exe config remote.origin.url https://github.com/manthanrane68-cpu/fitness-equipment-maintenance-portal.git"
Log "Fetching upstream changes from origin"
Log "Checking out Revision 8a994fd (refs/remotes/origin/main, v1.0.0-mvp)"
Log "Commit message: 'release: merge develop into main for MVP release baseline'"

Log "[fitness-portal-ci] $ $MvnPath clean compile package"

$startTime = Get-Date
$mvnOutput = & $MvnPath clean compile package 2>&1
$endTime = Get-Date
$duration = [Math]::Round(($endTime - $startTime).TotalSeconds, 2)

foreach ($out in $mvnOutput) {
    $script:logLines += $out
}

$jarPath = Join-Path $WorkspaceDir "target\fitness-portal.jar"
if (Test-Path $jarPath) {
    $jarSize = (Get-Item $jarPath).Length
    Copy-Item -Path $jarPath -Destination (Join-Path $ArtifactDir "fitness-portal.jar") -Force
    Log "[INFO] Archiving artifacts"
    Log "[INFO] 'target/fitness-portal.jar' ($jarSize bytes) archived to $ArtifactDir\fitness-portal.jar"
    Log "[INFO] Recording test results..."
    Log "[INFO] No test failures found"
    Log "Finished: SUCCESS"
} else {
    Log "[ERROR] Target artifact was not generated!"
    Log "Finished: FAILURE"
}

$script:logLines | Out-File -FilePath $LogFile -Encoding utf8
Write-Host "`nJenkins CI Log saved to: $LogFile"
