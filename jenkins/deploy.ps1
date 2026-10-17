# Automated Target Server Deployment Script
# Parameterized for environment and target port

param (
    [string]$Environment = "staging",
    [string]$Port = "8080"
)

$ErrorActionPreference = "Continue"
$WorkspaceDir = (Get-Item -Path ".").FullName
$DeployDir = Join-Path $WorkspaceDir "deployments\$Environment"
$SourceJar = Join-Path $WorkspaceDir "target\fitness-portal.jar"
$TargetJar = Join-Path $DeployDir "fitness-portal.jar"

Write-Host "=========================================================="
Write-Host " [DEPLOY] Initializing Server Deployment"
Write-Host " Environment: $Environment"
Write-Host " Port:        $Port"
Write-Host " Target Path: $TargetJar"
Write-Host "=========================================================="

if (-not (Test-Path $SourceJar)) {
    Write-Error "Source JAR not found at $SourceJar. Build must run before deploy."
    exit 1
}

# Create deployment directory
if (-not (Test-Path $DeployDir)) {
    New-Item -ItemType Directory -Path $DeployDir -Force | Out-Null
}

# Copy artifact
Copy-Item -Path $SourceJar -Destination $TargetJar -Force
Write-Host "[DEPLOY] Copied artifact to $TargetJar"

# Check if an existing instance is running on this port and stop it
$existingProcesses = Get-NetTCPConnection -LocalPort $Port -ErrorAction SilentlyContinue
if ($existingProcesses) {
    foreach ($conn in $existingProcesses) {
        $pId = $conn.OwningProcess
        if ($pId -gt 0) {
            Write-Host "[DEPLOY] Stopping existing process PID $pId on port $Port"
            Stop-Process -Id $pId -Force -ErrorAction SilentlyContinue
        }
    }
    Start-Sleep -Seconds 1
}

# Start the application in the background on the parameterized port
$javaArgs = "-Dserver.port=$Port -jar `"$TargetJar`""
Write-Host "[DEPLOY] Launching: java $javaArgs"

$process = Start-Process -FilePath "java" -ArgumentList "-Dserver.port=$Port", "-jar", "`"$TargetJar`"" -PassThru -WindowStyle Hidden
Write-Host "[DEPLOY] Service started with PID: $($process.Id)"

# Verify Health Check with retry loop
$healthUri = "http://localhost:$Port/api/health"
$maxRetries = 10
$retryCount = 0
$healthy = $false

Write-Host "[DEPLOY] Probing health endpoint: $healthUri ..."

while ($retryCount -lt $maxRetries) {
    Start-Sleep -Seconds 1
    $retryCount++
    try {
        $res = Invoke-RestMethod -Uri $healthUri -TimeoutSec 3 -ErrorAction SilentlyContinue
        if ($res.status -eq "UP") {
            Write-Host "[DEPLOY] SUCCESS! Application is UP (Response: $($res.status) | Uptime: $($res.uptimeSeconds)s | Heap: $($res.heapMemoryMb))"
            $healthy = $true
            break
        }
    } catch {
        Write-Host "[DEPLOY] Waiting for service to bind... attempt $retryCount/$maxRetries"
    }
}

if (-not $healthy) {
    Write-Error "[DEPLOY] Health check failed after $maxRetries attempts."
    exit 1
}

Write-Host "=========================================================="
Write-Host " [DEPLOY] Deployment to [$Environment] on port [$Port] SUCCESSFUL"
Write-Host " Access Web UI: http://localhost:$Port/"
Write-Host "=========================================================="
