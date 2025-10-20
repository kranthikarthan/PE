# Load Testing Framework (Gatling)

## Overview
Gatling-based performance testing for the Payments Engine with scenarios for sustained, peak, spike, endurance, and stress loads. Targets Phase 6.2 SLOs: 1,000+ TPS sustained, p95 < 3s, p99 < 5s, <1% error rate.

## Structure
```
load-tests/
├── pom.xml
├── src/test/scala/simulations/
│   ├── SustainedLoadTest.scala
│   ├── PeakLoadTest.scala
│   ├── SpikeTest.scala
│   ├── EnduranceTest.scala
│   └── StressTest.scala
├── src/test/resources/data/payment-data.csv
└── src/test/resources/bodies/eft-payment.json
```

## Running
```bash
# Sustained
mvn -f load-tests/pom.xml gatling:test -Dgatling.simulationClass=simulations.SustainedLoadTest -DPAYMENTS_BASE_URL=http://localhost:8081

# Peak
mvn -f load-tests/pom.xml gatling:test -Dgatling.simulationClass=simulations.PeakLoadTest -DPAYMENTS_BASE_URL=http://localhost:8081

# Spike
mvn -f load-tests/pom.xml gatling:test -Dgatling.simulationClass=simulations.SpikeTest -DPAYMENTS_BASE_URL=http://localhost:8081

# Endurance
mvn -f load-tests/pom.xml gatling:test -Dgatling.simulationClass=simulations.EnduranceTest -DPAYMENTS_BASE_URL=http://localhost:8081

# Stress
mvn -f load-tests/pom.xml gatling:test -Dgatling.simulationClass=simulations.StressTest -DPAYMENTS_BASE_URL=http://localhost:8081
```

## Assertions
- p95 response time ≤ 3s
- p99 response time ≤ 5s
- Failed requests ≤ 1%

## Notes
- Ensure payment-initiation-service is reachable at `PAYMENTS_BASE_URL`.
- Extend with additional flows (validation, routing, clearing) as needed.
- Integrate with Prometheus/Grafana for real-time monitoring.

