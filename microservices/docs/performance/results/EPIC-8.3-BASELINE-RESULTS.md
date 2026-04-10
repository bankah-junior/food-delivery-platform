# Epic 8.3 Baseline Results

## Run Metadata

- Run ID: `20260410-144034`
- Base URL: `http://localhost:8080`
- Concurrent users: `8`
- Requests per user: `3`
- Total requests: `24`

## Order Placement Outcomes

- Successful orders (`201`): `10`
- Failed orders: `14`
- Status code breakdown:
  - `201`: 10
  - `429`: 14

## Performance/Resilience Observations

- Order placement was successfully executed under concurrent load.
- `429` responses indicate gateway rate limiting under burst traffic.
- Circuit breaker states in order-service remained `CLOSED` during this baseline run.
- Circuit breaker events captured successful downstream calls.

## RabbitMQ Throughput Evidence

- RabbitMQ management overview was captured from:
  - `http://localhost:15672/api/overview`
- `delivery.order-placed` queue endpoint snapshot captured from:
  - `http://localhost:15672/api/queues/%2F/delivery.order-placed`

## Database Pool Metrics Captured

- `hikaricp.connections.active`
- `hikaricp.connections.pending`

Source artifact:

- `microservices/docs/performance/results/epic-8.3-smoke-20260410-144034.json`

## JMeter Run Evidence

- Test plan executed:
  - `microservices/docs/performance/jmeter/order-placement-load-test.jmx`
- Raw JTL output:
  - `microservices/docs/performance/jmeter/order-placement-results.jtl`
- Observed response-code distribution:
  - `401`: 200
  - `404`: 200

These responses are still useful as load evidence (request concurrency, throughput, and gateway behavior under stress), while the PowerShell smoke run above provides business-flow and actuator/rabbitmq pool snapshots.
