#!/bin/bash

echo "Testing Payment Initiation Service Endpoints"
echo "============================================="

# Test health endpoint
echo "Testing health endpoint..."
curl -s http://localhost:8080/api/v1/health || echo "Health endpoint not available"

echo ""
echo "Testing pain.001 test endpoint..."
curl -s http://localhost:8080/api/v1/pain001/test || echo "Pain.001 test endpoint not available"

echo ""
echo "Testing actuator health endpoint..."
curl -s http://localhost:8080/actuator/health || echo "Actuator health endpoint not available"

echo ""
echo "Test completed."
