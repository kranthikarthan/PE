#!/bin/bash

# Load Testing Execution Script
# Usage: ./run-load-test.sh [simulation] [base_url]

set -e

SIMULATION=${1:-"SustainedLoadTest"}
BASE_URL=${2:-"http://localhost:8081"}
RESULTS_DIR="target/gatling-results"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo "🚀 Starting Load Test: $SIMULATION"
echo "📍 Target URL: $BASE_URL"
echo "⏰ Timestamp: $TIMESTAMP"

# Create results directory
mkdir -p "$RESULTS_DIR"

# Run Gatling simulation
echo "🏃 Running Gatling simulation..."
mvn -f load-tests/pom.xml gatling:test \
  -Dgatling.simulationClass="simulations.$SIMULATION" \
  -DPAYMENTS_BASE_URL="$BASE_URL" \
  -Dgatling.resultsFolder="$RESULTS_DIR/$SIMULATION-$TIMESTAMP"

# Check if results were generated
if [ -d "$RESULTS_DIR/$SIMULATION-$TIMESTAMP" ]; then
    echo "✅ Load test completed successfully!"
    echo "📊 Results available in: $RESULTS_DIR/$SIMULATION-$TIMESTAMP"
    
    # Generate performance report
    echo "📈 Generating performance report..."
    ./analyze-results.sh "$RESULTS_DIR/$SIMULATION-$TIMESTAMP"
else
    echo "❌ Load test failed or no results generated"
    exit 1
fi

echo "🎯 Load test execution completed!"
