# ELK Stack Setup Script for Bookstore Application (PowerShell)
# This script sets up and verifies the ELK stack on Windows

$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "ELK Stack Setup for Bookstore Application" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

function Print-Success {
    param([string]$Message)
    Write-Host "✓ $Message" -ForegroundColor Green
}

function Print-Error {
    param([string]$Message)
    Write-Host "✗ $Message" -ForegroundColor Red
}

function Print-Info {
    param([string]$Message)
    Write-Host "ℹ $Message" -ForegroundColor Yellow
}

# Check if Docker is running
Write-Host "Checking prerequisites..."
try {
    docker info | Out-Null
    Print-Success "Docker is running"
} catch {
    Print-Error "Docker is not running. Please start Docker Desktop and try again."
    exit 1
}

# Check if docker-compose is available
try {
    docker-compose --version | Out-Null
    Print-Success "docker-compose is available"
} catch {
    Print-Error "docker-compose is not installed. Please install it and try again."
    exit 1
}

Write-Host ""
Write-Host "Starting ELK stack..."
Write-Host ""

# Navigate to project root
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location "$scriptPath\.."

# Create log directories if they don't exist
Print-Info "Creating log directories..."
$logDirs = @(
    "backend\auth-service\logs",
    "backend\book-service\logs",
    "backend\cart-service\logs",
    "backend\inventory-service\logs",
    "backend\notification-service\logs"
)

foreach ($dir in $logDirs) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
    }
}
Print-Success "Log directories created"

# Start ELK stack
Print-Info "Starting ELK services (this may take a few minutes)..."
docker-compose -f docker-compose-elk.yml up -d

Write-Host ""
Print-Info "Waiting for services to be healthy..."
Write-Host ""

# Wait for Elasticsearch
Write-Host "Waiting for Elasticsearch..." -NoNewline
$maxAttempts = 60
$attempt = 0
$elasticsearchReady = $false

while ($attempt -lt $maxAttempts) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:9200/_cluster/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host ""
            Print-Success "Elasticsearch is ready"
            $elasticsearchReady = $true
            break
        }
    } catch {
        # Ignore errors and continue waiting
    }
    Write-Host "." -NoNewline
    Start-Sleep -Seconds 2
    $attempt++
}

if (-not $elasticsearchReady) {
    Write-Host ""
    Print-Error "Elasticsearch failed to start within 2 minutes"
    exit 1
}

# Wait for Logstash
Write-Host "Waiting for Logstash..." -NoNewline
$attempt = 0
$logstashReady = $false

while ($attempt -lt $maxAttempts) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:9600/_node/stats" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host ""
            Print-Success "Logstash is ready"
            $logstashReady = $true
            break
        }
    } catch {
        # Ignore errors and continue waiting
    }
    Write-Host "." -NoNewline
    Start-Sleep -Seconds 2
    $attempt++
}

if (-not $logstashReady) {
    Write-Host ""
    Print-Error "Logstash failed to start within 2 minutes"
    exit 1
}

# Wait for Kibana
Write-Host "Waiting for Kibana..." -NoNewline
$attempt = 0
$kibanaReady = $false

while ($attempt -lt $maxAttempts) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:5601/api/status" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            Write-Host ""
            Print-Success "Kibana is ready"
            $kibanaReady = $true
            break
        }
    } catch {
        # Ignore errors and continue waiting
    }
    Write-Host "." -NoNewline
    Start-Sleep -Seconds 2
    $attempt++
}

if (-not $kibanaReady) {
    Write-Host ""
    Print-Error "Kibana failed to start within 2 minutes"
    exit 1
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Print-Success "ELK Stack is ready!"
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Access URLs:"
Write-Host "  • Elasticsearch: http://localhost:9200"
Write-Host "  • Kibana:        http://localhost:5601"
Write-Host "  • Logstash:      http://localhost:9600"
Write-Host ""
Write-Host "Next steps:"
Write-Host "  1. Open Kibana at http://localhost:5601"
Write-Host "  2. Go to Management → Stack Management → Index Patterns"
Write-Host "  3. Create index pattern: bookstore-logs-*"
Write-Host "  4. Select @timestamp as the time field"
Write-Host "  5. Start your microservices to generate logs"
Write-Host ""
Write-Host "For more information, see ELK_LOGGING_GUIDE.md"
Write-Host ""

# Check service status
Write-Host "Service Status:"
docker-compose -f docker-compose-elk.yml ps

Write-Host ""
Print-Info "To view logs: docker-compose -f docker-compose-elk.yml logs -f"
Print-Info "To stop ELK:  docker-compose -f docker-compose-elk.yml down"
Write-Host ""

# Made with Bob
