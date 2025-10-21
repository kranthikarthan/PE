# Docker Services Metrics Verification - Delegation Prompt

## Task Overview
Verify that the validation-service and account-adapter-service are properly configured for Prometheus metrics scraping, including endpoint accessibility and Prometheus target discovery.

## Prerequisites
- Docker Desktop must be running and accessible
- Services must be built and available as Docker images
- Prometheus container should be running for target verification
- `jq` command-line JSON processor should be installed for API parsing

## Step-by-Step Execution

### 1. Start Required Services
```bash
# Start the two services using docker compose
docker compose up -d validation-service account-adapter-service
```

**Expected Outcome**: Both services should start successfully without errors.

**Verification**: Check that containers are running:
```bash
docker ps --filter "name=payments-(validation|account-adapter)-service"
```

### 2. Start Prometheus for Target Discovery
```bash
# Start Prometheus to enable target discovery verification
docker compose up -d prometheus
```

**Expected Outcome**: Prometheus should start and be accessible on port 9090.

### 3. Verify Validation Service Metrics Endpoint
```bash
# Check that the actuator endpoint returns metrics (no redirect)
docker exec payments-validation-service curl -s http://localhost:18082/actuator/prometheus | head -20
```

**Expected Outcome**: Should return Prometheus metrics in text format, starting with `# HELP` and `# TYPE` comments followed by metric data.

**Sample Output**:
```
# HELP jvm_memory_used_bytes Used bytes of a given JVM memory area.
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space"} 1.23456789E8
# HELP http_server_requests_seconds Duration of HTTP server request handling
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{method="GET",status="200",uri="/actuator/health"} 1.0
```

### 4. Verify Account Adapter Service Metrics Endpoint
```bash
# Check that the actuator endpoint returns metrics (no redirect)
docker exec payments-account-adapter-service curl -s http://localhost:18083/actuator/prometheus | head -20
```

**Expected Outcome**: Should return Prometheus metrics in text format, starting with `# HELP` and `# TYPE` comments followed by metric data.

### 5. Verify Service Health Endpoints
```bash
# Check validation service health
docker exec payments-validation-service curl -s http://localhost:18082/actuator/health

# Check account adapter service health
docker exec payments-account-adapter-service curl -s http://localhost:18083/actuator/health
```

**Expected Outcome**: Both should return `{"status":"UP"}` with service details.

### 6. Reload Prometheus Configuration
```bash
# Reload Prometheus to pick up new targets
docker exec payments-prometheus wget -qO- --post-data='' http://localhost:9090/-/reload
```

**Expected Outcome**: Should return "OK" or no error message.

### 7. Verify Prometheus Target Discovery
```bash
# Check that both services appear as targets in Prometheus
curl -s "http://localhost:9090/api/v1/targets?state=any" | jq '.data.activeTargets[] | select(.labels.job | contains("validation") or contains("account")) | {job: .labels.job, health: .health, lastScrape: .lastScrape, scrapeUrl: .scrapeUrl}'
```

**Expected Outcome**: Both services should appear as healthy targets with recent scrape times.

**Sample Output**:
```json
{
  "job": "validation-service",
  "health": "up",
  "lastScrape": "2024-01-15T10:30:45.123Z",
  "scrapeUrl": "http://validation-service:18082/actuator/prometheus"
}
{
  "job": "account-adapter-service", 
  "health": "up",
  "lastScrape": "2024-01-15T10:30:45.456Z",
  "scrapeUrl": "http://account-adapter-service:18083/actuator/prometheus"
}
```

### 8. Verify Metrics Collection in Prometheus UI
```bash
# Open Prometheus UI and check targets
echo "Open http://localhost:9090/targets in your browser to verify both services are UP"
```

**Expected Outcome**: Both services should show as "UP" in the Prometheus targets page.

## Troubleshooting

### If Docker Desktop is not running:
1. Start Docker Desktop manually
2. Wait for it to fully initialize (check with `docker ps`)
3. Retry the service startup

### If services fail to start:
1. Check Docker logs: `docker compose logs validation-service account-adapter-service`
2. Verify images exist: `docker images | grep -E "(validation|account)"`
3. Rebuild if necessary: `docker compose build validation-service account-adapter-service`
4. Check for port conflicts: `netstat -an | grep -E "(18082|18083)"`

### If metrics endpoints return errors:
1. Check service health: `docker exec payments-validation-service curl -s http://localhost:18082/actuator/health`
2. Verify actuator is enabled in application configuration
3. Check service logs for any startup errors: `docker logs payments-validation-service`
4. Verify ManagementSecurityConfig is properly configured
5. Check if services are listening on correct ports: `docker exec payments-validation-service netstat -tlnp`

### If Prometheus doesn't discover targets:
1. Check Prometheus configuration in docker-compose.yml
2. Verify service labels match Prometheus scrape configuration
3. Ensure services are accessible from Prometheus container network
4. Check Prometheus logs: `docker logs payments-prometheus`
5. Verify network connectivity: `docker exec payments-prometheus wget -qO- http://validation-service:18082/actuator/health`

### If metrics endpoints return redirects:
1. Verify ManagementSecurityConfig is properly configured
2. Check that `@ManagementContextConfiguration` is applied
3. Ensure actuator endpoints are not being intercepted by main security configuration
4. Verify `MANAGEMENT_SERVER_PORT` is set correctly in docker-compose.yml

### If jq command is not found:
```bash
# Install jq (Ubuntu/Debian)
sudo apt-get install jq

# Install jq (macOS)
brew install jq

# Install jq (Windows with Chocolatey)
choco install jq
```

## Success Criteria Checklist
- [ ] Both services start successfully without errors
- [ ] Validation service metrics endpoint returns Prometheus format data (no redirects)
- [ ] Account adapter service metrics endpoint returns Prometheus format data (no redirects)
- [ ] Both service health endpoints return `{"status":"UP"}`
- [ ] Prometheus starts and is accessible on port 9090
- [ ] Prometheus reloads configuration successfully
- [ ] Both services appear as healthy targets in Prometheus API
- [ ] No redirects occur when accessing metrics endpoints
- [ ] ManagementSecurityConfig is properly configured for both services
- [ ] Metrics are accessible without authentication

## Expected Metrics Format
The metrics endpoints should return data in this format:
```
# HELP jvm_memory_used_bytes Used bytes of a given JVM memory area.
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="PS Eden Space"} 1.23456789E8
# HELP http_server_requests_seconds Duration of HTTP server request handling
# TYPE http_server_requests_seconds summary
http_server_requests_seconds_count{method="GET",status="200",uri="/actuator/health"} 1.0
# HELP jvm_gc_memory_allocated_bytes_total Total bytes allocated in a given JVM memory pool.
# TYPE jvm_gc_memory_allocated_bytes_total counter
jvm_gc_memory_allocated_bytes_total{pool="PS Eden Space"} 1.23456789E8
```

## Port Mappings
- **Validation Service**: 
  - Application: `8082:8082`
  - Management/Actuator: `18082` (internal)
- **Account Adapter Service**: 
  - Application: `8083:8083` 
  - Management/Actuator: `18083` (internal)
- **Prometheus**: `9090:9090`

## Configuration Notes

### ManagementSecurityConfig
Both services have `ManagementSecurityConfig` configured to:
- Allow public access to actuator endpoints on dedicated management ports
- Disable authentication, CSRF, and form login for metrics scraping
- Use `@ManagementContextConfiguration` to apply only to management context
- Ensure Prometheus can scrape metrics without authentication redirects

### Key Environment Variables
- `MANAGEMENT_SERVER_PORT`: Sets dedicated port for actuator endpoints
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE`: Exposes prometheus endpoint
- `SPRING_SECURITY_OAUTH2_CLIENT_ENABLED`: Disabled to prevent OAuth redirects

## Optional Cleanup
```bash
# Clean validation-service target directory if needed for clean tree
mvn -pl validation-service clean

# Stop all services when verification is complete
docker compose down
```

## Notes
- Port 18082 is for validation-service actuator
- Port 18083 is for account-adapter-service actuator
- Both services should have ManagementSecurityConfig configured to allow actuator access
- Metrics should be available without authentication redirects
- Prometheus configuration targets the correct internal ports and paths
- Services use dedicated management ports to avoid conflicts with main application security
