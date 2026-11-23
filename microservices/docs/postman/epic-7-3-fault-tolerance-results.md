# Epic 7.3 Fault Tolerance Test Results

Date: 2026-04-10

## Test Method
- Controlled stop/start of `customer-service`, `restaurant-service`, and `delivery-service` via Docker Compose.
- Order placement tests executed against `order-service` endpoint (`http://localhost:8083/api/orders`) with trusted header `X-User-Id` to avoid API gateway rate-limit interference.
- Circuit breaker verification via order-service Actuator endpoints:
  - `GET /actuator/circuitbreakers`
  - `GET /actuator/circuitbreakerevents`

## Configuration Adjustment for Observability
- Enabled circuit breaker actuator endpoints in Docker profile:
  - `management.endpoints.web.exposure.include=health,info,circuitbreakers,circuitbreakerevents`
  - `management.endpoint.health.show-details=always`

## Scenario Outcomes

### 1) Stop Customer Service
- Action: `docker compose stop customer-service`
- Result: repeated order placement returned HTTP 503.
- Error behavior: fallback path returned service-unavailable responses.
- Circuit breaker evidence (events):
  - `CLOSED_TO_OPEN`
  - `OPEN_TO_HALF_OPEN`
  - `HALF_OPEN_TO_OPEN`
  - `NOT_PERMITTED`

### 2) Stop Restaurant Service
- Action: `docker compose stop restaurant-service`
- Result: repeated order placement returned HTTP 503.
- Error behavior: fallback path returned service-unavailable responses.
- Circuit breaker evidence (events):
  - `CLOSED_TO_OPEN`
  - `OPEN_TO_HALF_OPEN`

### 3) Stop Delivery Service
- Action: `docker compose stop delivery-service`
- Result: order placement continued to return HTTP 201.
- Interpretation: order flow remains available when delivery consumer service is down (async design).

### 4) Recovery Verification
- Action: restarted stopped services and generated successful downstream traffic.
- Result:
  - customer-service reached healthy state.
  - restaurant-service reached healthy state.
  - order placement returned HTTP 201 after recovery traffic.
- Circuit breaker evidence (events):
  - `CustomerServiceClientgetCustomerByIdLong`: `HALF_OPEN_TO_CLOSED`
  - `RestaurantServiceClientgetRestaurantByIdLong`: `HALF_OPEN_TO_CLOSED`

## Conclusion
Epic 7.3 fault-tolerance testing is complete. Service outages are handled with graceful degradation (HTTP 503), order placement stays functional during delivery-service outage, and circuit breakers transition through open/half-open/closed states during outage and recovery cycles.
