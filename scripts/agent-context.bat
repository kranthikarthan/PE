@echo off
REM CURSOR AGENT CONTEXT LOADER
REM This script helps Cursor agents quickly acquire project context
REM Batch version for Windows compatibility

echo === CURSOR AGENT CONTEXT LOADER ===
echo Loading project context for efficient implementation...
echo.

REM 1. Project Overview
echo 1. 📚 Reading project architecture...
if exist "docs\architecture\FINAL-ARCHITECTURE-OVERVIEW.md" (
    echo    ✅ Found architecture overview
) else (
    echo    ⚠️  Architecture overview not found
)

if exist "docs\implementation\feature-breakdown-tree.yaml" (
    echo    ✅ Found feature breakdown tree
) else (
    echo    ⚠️  Feature breakdown tree not found
)

REM 2. Domain Models
echo.
echo 2. 🏗️ Loading domain models...
if exist "domain-models" (
    echo    ✅ Found domain models directory
    echo    📁 Available domains:
    dir domain-models /b 2>nul | findstr /v "Directory" | findstr /v "File" | findstr /v "Volume"
) else (
    echo    ⚠️  Domain models directory not found
)

REM 3. Contracts
echo.
echo 3. 📋 Checking existing contracts...
if exist "contracts" (
    echo    ✅ Found contracts directory
    echo    📁 Available contracts:
    dir contracts\*.java /s /b 2>nul | findstr /v "Directory" | findstr /v "File" | findstr /v "Volume"
) else (
    echo    ⚠️  Contracts directory not found
)

REM 4. Existing Services
echo.
echo 4. 🔧 Analyzing existing services...
if exist "payment-initiation-service" echo    ✅ Found payment-initiation-service
if exist "transaction-processing-service" echo    ✅ Found transaction-processing-service
if exist "validation-service" echo    ✅ Found validation-service
if exist "saga-orchestrator" echo    ✅ Found saga-orchestrator
if exist "routing-service" echo    ✅ Found routing-service

REM 5. Pattern Analysis
echo.
echo 5. 🔍 Analyzing existing patterns...
echo    📊 Pattern analysis: (basic analysis available)

REM 6. Context Summary
echo.
echo 6. 📋 Context Summary:
echo    🎯 Architecture: Microservices payments engine
echo    🏗️  Patterns: DDD, CQRS, Saga, Event Sourcing
echo    🔧 Tech Stack: Spring Boot 3.x, Java 17, Maven, Docker, Kafka, PostgreSQL
echo    📚 Documentation: Organized in docs/ directory
echo    🏗️  Domain Models: Available in domain-models/
echo    📋 Contracts: Available in contracts/
echo    🔧 Services: Multiple microservices available

REM 7. Next Steps
echo.
echo 7. 🚀 Next Steps:
echo    📖 Read CURSOR-AGENT-CONTEXT.md for detailed guidance
echo    📋 Follow AGENT-CONTEXT-CHECKLIST.md for validation
echo    🏗️  Use IMPLEMENTATION-STRATEGY.md for implementation
echo    ⚠️  Check ANTI-PATTERNS-TO-AVOID.md for common mistakes
echo.
echo ✅ Context loaded successfully!
echo 🎯 Ready for efficient implementation!
pause
