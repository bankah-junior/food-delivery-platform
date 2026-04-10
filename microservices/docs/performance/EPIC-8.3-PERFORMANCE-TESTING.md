# Epic 8.3 - Performance Testing Guide

## Scope

This guide covers all checklist items for Epic 8.3:

- Load test order placement with JMeter
- Test concurrent order processing
- Verify RabbitMQ message throughput
- Monitor circuit breaker behavior under load
- Check database connection pool usage

## Prerequisites

- Docker stack running from project root:

```powershell
docker compose up -d
```

- Services healthy on:
  - Gateway: `http://localhost:8080`
  - Order service actuator: `http://localhost:8083/actuator`
  - RabbitMQ management: `http://localhost:15672` (guest/guest)
  - Zipkin (optional): `http://localhost:9411`

## 1. JMeter Load Test (Order Placement)

Test plan file:

- `microservices/docs/performance/jmeter/order-placement-load-test.jmx`

### Option A: Local JMeter

```powershell
jmeter -n -t microservices/docs/performance/jmeter/order-placement-load-test.jmx -l microservices/docs/performance/results/order-placement-results.jtl -JbaseUrl=http://localhost:8080
```

### Option B: Dockerized JMeter

```powershell
docker run --rm --network food-delivery-platform_food-delivery-network -v "${PWD}/microservices/docs/performance/jmeter:/tests" justb4/jmeter:latest -n -t /tests/order-placement-load-test.jmx -l /tests/order-placement-results.jtl -JbaseUrl=http://api-gateway:8080
```

Quick status-code summary from JTL output:

```powershell
Import-Csv microservices/docs/performance/jmeter/order-placement-results.jtl | Group-Object responseCode | Sort-Object Name
```

## 2. Concurrent Order Processing + Monitoring Snapshot

Run the Epic 8.3 smoke script:

```powershell
powershell -ExecutionPolicy Bypass -File microservices/docs/performance/scripts/run-epic-8.3-smoke.ps1 -ConcurrentUsers 20 -RequestsPerUser 5 -RestaurantId 1 -MenuItemId 1
```

What the script does:

- Creates and authenticates a temporary user
- Fires concurrent order placement requests through API gateway
- Captures HTTP status-code breakdown
- Captures order-service circuit breaker state and event payloads
- Captures RabbitMQ overview and `delivery.order-placed` queue depth
- Captures HikariCP metrics (`connections.active`, `connections.pending`)
- Writes output to:
  - `microservices/docs/performance/results/epic-8.3-smoke-<timestamp>.json`

## 3. Validation Metrics to Review

- Order API success ratio:
  - `successfulOrders / totalRequests`
- Circuit breaker behavior:
  - `http://localhost:8083/actuator/circuitbreakers`
  - `http://localhost:8083/actuator/circuitbreakerevents`
- RabbitMQ throughput indicators:
  - `http://localhost:15672/api/overview`
  - `http://localhost:15672/api/queues/%2F/delivery.order-placed`
- Database pool usage:
  - `http://localhost:8083/actuator/metrics/hikaricp.connections.active`
  - `http://localhost:8083/actuator/metrics/hikaricp.connections.pending`

## Notes

- If order placement returns validation errors, confirm test data exists for `restaurantId` and `menuItemId`.
- For stable local benchmarking, run one warm-up pass before collecting final numbers.
- Keep `management.tracing.sampling.probability` lower in non-test environments.
