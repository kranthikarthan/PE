@echo off
REM Load Testing Execution Script for Windows
REM Usage: run-load-test.bat [simulation] [base_url]

setlocal enabledelayedexpansion

set SIMULATION=%1
if "%SIMULATION%"=="" set SIMULATION=SustainedLoadTest

set BASE_URL=%2
if "%BASE_URL%"=="" set BASE_URL=http://localhost:8081

set RESULTS_DIR=target\gatling-results
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "Sec=%dt:~12,2%"
set TIMESTAMP=%YYYY%%MM%%DD%_%HH%%Min%%Sec%

echo 🚀 Starting Load Test: %SIMULATION%
echo 📍 Target URL: %BASE_URL%
echo ⏰ Timestamp: %TIMESTAMP%

REM Create results directory
if not exist "%RESULTS_DIR%" mkdir "%RESULTS_DIR%"

REM Run Gatling simulation
echo 🏃 Running Gatling simulation...
mvn -f load-tests\pom.xml gatling:test -Dgatling.simulationClass=simulations.%SIMULATION% -DPAYMENTS_BASE_URL=%BASE_URL% -Dgatling.resultsFolder=%RESULTS_DIR%\%SIMULATION%-%TIMESTAMP%

REM Check if results were generated
if exist "%RESULTS_DIR%\%SIMULATION%-%TIMESTAMP%" (
    echo ✅ Load test completed successfully!
    echo 📊 Results available in: %RESULTS_DIR%\%SIMULATION%-%TIMESTAMP%
    
    REM Generate performance report
    echo 📈 Generating performance report...
    call analyze-results.bat "%RESULTS_DIR%\%SIMULATION%-%TIMESTAMP%"
) else (
    echo ❌ Load test failed or no results generated
    exit /b 1
)

echo 🎯 Load test execution completed!
