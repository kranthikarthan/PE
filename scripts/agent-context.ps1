# CURSOR AGENT CONTEXT LOADER
# This script helps Cursor agents quickly acquire project context
# PowerShell version for Windows compatibility

Write-Host "=== CURSOR AGENT CONTEXT LOADER ===" -ForegroundColor Cyan
Write-Host "Loading project context for efficient implementation..." -ForegroundColor Green
Write-Host ""

# 1. Project Overview
Write-Host "1. Reading project architecture..." -ForegroundColor Yellow
if (Test-Path "docs/architecture/FINAL-ARCHITECTURE-OVERVIEW.md") {
    Write-Host "   Found architecture overview" -ForegroundColor Green
} else {
    Write-Host "   Architecture overview not found" -ForegroundColor Red
}

if (Test-Path "docs/implementation/feature-breakdown-tree.yaml") {
    Write-Host "   Found feature breakdown tree" -ForegroundColor Green
} else {
    Write-Host "   Feature breakdown tree not found" -ForegroundColor Red
}

# 2. Domain Models
Write-Host ""
Write-Host "2. Loading domain models..." -ForegroundColor Yellow
if (Test-Path "domain-models") {
    Write-Host "   Found domain models directory" -ForegroundColor Green
    Write-Host "   Available domains:" -ForegroundColor Cyan
    try {
        $domains = Get-ChildItem "domain-models" -Directory | Select-Object -ExpandProperty Name
        foreach ($domain in $domains) {
            Write-Host "      - $domain" -ForegroundColor White
        }
    } catch {
        Write-Host "      - (unable to list domains)" -ForegroundColor Red
    }
} else {
    Write-Host "   Domain models directory not found" -ForegroundColor Red
}

# 3. Contracts
Write-Host ""
Write-Host "3. Checking existing contracts..." -ForegroundColor Yellow
if (Test-Path "contracts") {
    Write-Host "   Found contracts directory" -ForegroundColor Green
    Write-Host "   Available contracts:" -ForegroundColor Cyan
    try {
        $contracts = Get-ChildItem "contracts" -Filter "*.java" -Recurse | Select-Object -First 5
        foreach ($contract in $contracts) {
            Write-Host "      - $($contract.Name)" -ForegroundColor White
        }
        $totalContracts = (Get-ChildItem "contracts" -Filter "*.java" -Recurse).Count
        if ($totalContracts -gt 5) {
            $remaining = $totalContracts - 5
            Write-Host "      ... and $remaining more" -ForegroundColor Gray
        }
    } catch {
        Write-Host "      - No Java files found" -ForegroundColor Red
    }
} else {
    Write-Host "   Contracts directory not found" -ForegroundColor Red
}

# 4. Existing Services
Write-Host ""
Write-Host "4. Analyzing existing services..." -ForegroundColor Yellow
$services = @("payment-initiation-service", "transaction-processing-service", "validation-service", "saga-orchestrator", "routing-service")
foreach ($service in $services) {
    if (Test-Path $service) {
        Write-Host "   Found $service" -ForegroundColor Green
    }
}

# 5. Pattern Analysis
Write-Host ""
Write-Host "5. Analyzing existing patterns..." -ForegroundColor Yellow
try {
    $serviceCount = (Get-ChildItem -Path . -Filter "*Service.java" -Recurse | Where-Object { $_.FullName -like "*/src/main/java/*" }).Count
    $repoCount = (Get-ChildItem -Path . -Filter "*Repository.java" -Recurse | Where-Object { $_.FullName -like "*/src/main/java/*" }).Count
    $entityCount = (Get-ChildItem -Path . -Filter "*Entity.java" -Recurse | Where-Object { $_.FullName -like "*/src/main/java/*" }).Count
    
    Write-Host "   Service patterns: $serviceCount services found" -ForegroundColor Cyan
    Write-Host "   Repository patterns: $repoCount repositories found" -ForegroundColor Cyan
    Write-Host "   Entity patterns: $entityCount entities found" -ForegroundColor Cyan
} catch {
    Write-Host "   Pattern analysis: (unable to analyze patterns)" -ForegroundColor Red
}

# 6. Context Summary
Write-Host ""
Write-Host "6. Context Summary:" -ForegroundColor Yellow
Write-Host "   Architecture: Microservices payments engine" -ForegroundColor White
Write-Host "   Patterns: DDD, CQRS, Saga, Event Sourcing" -ForegroundColor White
Write-Host "   Tech Stack: Spring Boot 3.x, Java 17, Maven, Docker, Kafka, PostgreSQL" -ForegroundColor White
Write-Host "   Documentation: Organized in docs/ directory" -ForegroundColor White
Write-Host "   Domain Models: Available in domain-models/" -ForegroundColor White
Write-Host "   Contracts: Available in contracts/" -ForegroundColor White
Write-Host "   Services: Multiple microservices available" -ForegroundColor White

# 7. Next Steps
Write-Host ""
Write-Host "7. Next Steps:" -ForegroundColor Yellow
Write-Host "   Read CURSOR-AGENT-CONTEXT.md for detailed guidance" -ForegroundColor Cyan
Write-Host "   Follow AGENT-CONTEXT-CHECKLIST.md for validation" -ForegroundColor Cyan
Write-Host "   Use IMPLEMENTATION-STRATEGY.md for implementation" -ForegroundColor Cyan
Write-Host "   Check ANTI-PATTERNS-TO-AVOID.md for common mistakes" -ForegroundColor Cyan
Write-Host ""
Write-Host "Context loaded successfully!" -ForegroundColor Green
Write-Host "Ready for efficient implementation!" -ForegroundColor Green
