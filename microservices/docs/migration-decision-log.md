# Migration Decision Log

Date: 2026-04-10
Project: Food Delivery Platform Monolith to Microservices Migration
Status: Active

## Decision Format
Each record captures context, options considered, final decision, and trade-offs.

---

## D-001 Service Boundary: Customer Separate from Order
- Context: The monolith mixed user profile/auth concerns with ordering workflows.
- Options considered:
  - Keep customer and order in one service.
  - Split into customer-service and order-service.
- Decision: Split customer and order into separate services.
- Rationale:
  - Different change velocity and ownership concerns.
  - Independent scaling for read-heavy profile/auth traffic versus transactional order processing.
  - Cleaner bounded context around identity and profile data.
- Trade-offs:
  - Extra network calls from order-service to validate customer.
  - Need for resilience logic and timeout/fallback behavior.
- Outcome: Implemented via customer-service and order-service with OpenFeign + Resilience4j.

## D-002 Service Boundary: Restaurant and Menu in Same Service
- Context: Restaurant metadata and menu catalog are strongly cohesive and frequently accessed together.
- Options considered:
  - Separate restaurant and menu into two services.
  - Keep both in restaurant-service.
- Decision: Keep restaurant and menu in restaurant-service.
- Rationale:
  - High data cohesion and shared lifecycle.
  - Reduces cross-service joins and operational overhead.
  - Simplifies owner authorization and menu mutation workflows.
- Trade-offs:
  - Service may grow broader over time.
  - Requires careful module boundaries inside the service.
- Outcome: Implemented as restaurant-service owning Restaurant and MenuItem domains.

## D-003 Service Boundary: Delivery Separate from Order
- Context: Delivery lifecycle is operationally distinct and event-driven.
- Options considered:
  - Keep delivery logic embedded in order-service.
  - Separate delivery-service and trigger through events.
- Decision: Separate delivery-service from order-service.
- Rationale:
  - Independent operational cadence for assignment and status transitions.
  - Enables asynchronous processing and eventual consistency.
  - Isolates delivery failures from order placement critical path.
- Trade-offs:
  - Eventual consistency complexity.
  - Need for idempotency and retry-safe consumers.
- Outcome: Implemented with RabbitMQ events and dedicated delivery-service persistence.

## D-004 Communication Pattern: Sync vs Async
- Context: Not all service interactions have the same latency and consistency requirements.
- Options considered:
  - Fully synchronous REST-only architecture.
  - Fully asynchronous event-driven architecture.
  - Hybrid model based on use case.
- Decision: Hybrid communication model.
- Rationale:
  - Synchronous calls for immediate validation and request-time decisions.
  - Asynchronous events for post-commit side effects and decoupled workflows.
- Trade-offs:
  - Increased architectural complexity.
  - Requires both HTTP and messaging observability.
- Outcome: Implemented with OpenFeign for validation calls and RabbitMQ for delivery/order events.

## D-005 Synchronous Client Choice: OpenFeign
- Context: Multiple service-to-service REST calls required declarative client code.
- Options considered:
  - RestTemplate/WebClient manually in each service.
  - OpenFeign declarative clients.
- Decision: OpenFeign.
- Rationale:
  - Cleaner client interfaces and reduced boilerplate.
  - Native Spring Cloud integration with Eureka and circuit breakers.
  - Better readability and maintainability of downstream contracts.
- Trade-offs:
  - Additional abstraction layer to debug.
  - Requires careful timeout and fallback tuning.
- Outcome: Implemented in order-service for customer and restaurant dependencies.

## D-006 Event Broker Choice: RabbitMQ
- Context: Delivery creation and delivery status propagation needed durable asynchronous messaging.
- Options considered:
  - In-process async events only.
  - Kafka.
  - RabbitMQ.
- Decision: RabbitMQ.
- Rationale:
  - Straightforward queue/exchange model for current scale and complexity.
  - Good fit for command-like and domain event fanout in this project scope.
  - Fast local setup and Docker-based development integration.
- Trade-offs:
  - Message contract governance required as system grows.
  - Throughput model different from log-based streaming systems.
- Outcome: Implemented with order and delivery event publishers/consumers.

## D-007 Database Strategy: Database per Service
- Context: Monolith used shared data model and tight entity relationships.
- Options considered:
  - Shared database with service-level schemas.
  - Strict database-per-service ownership.
- Decision: Database per service.
- Rationale:
  - Strong service autonomy and ownership boundaries.
  - Independent schema evolution and deployment.
  - Reduced cross-domain coupling at persistence layer.
- Trade-offs:
  - No cross-service joins.
  - Requires API composition and eventual consistency patterns.
- Outcome: Implemented as customer_db, restaurant_db, order_db, delivery_db.

## D-008 Security Model: Gateway-Level JWT Validation
- Context: Repeating JWT validation in every service created duplicated security logic.
- Options considered:
  - Validate JWT independently in each service.
  - Validate once at API gateway and forward trusted user context.
- Decision: Validate JWT at gateway and propagate context headers.
- Rationale:
  - Centralized auth policy and reduced duplication.
  - Simpler service internals focused on business logic.
  - Consistent security behavior at ingress.
- Trade-offs:
  - Gateway becomes critical trust boundary.
  - Internal services must trust gateway network path and headers.
- Outcome: Implemented with gateway filters and downstream X-User-* context usage.

## D-009 Fault Tolerance Approach
- Context: Downstream outages must not cascade into total platform failure.
- Options considered:
  - Basic retries only.
  - Circuit breakers, retries, and fallbacks.
- Decision: Resilience4j circuit breakers + retry + fallback strategy.
- Rationale:
  - Protects call chains during outage conditions.
  - Supports graceful degradation with meaningful HTTP 503 responses.
  - Enables monitored recovery through actuator events.
- Trade-offs:
  - More configuration and runtime tuning needed.
  - Possible temporary false opens if thresholds are too aggressive.
- Outcome: Implemented and verified in Epic 5 and Epic 7.3 tests.

## D-010 Containerization and Operational Baseline
- Context: Need repeatable local and deployment-like environments.
- Options considered:
  - Manual local runs only.
  - Dockerfiles and docker-compose orchestration.
- Decision: Full Docker Compose baseline.
- Rationale:
  - One-command startup for complete platform.
  - Reproducible dependencies and network topology.
  - Better parity with production-style runtime concerns.
- Trade-offs:
  - Larger setup complexity versus single-process local run.
  - Additional image/build maintenance.
- Outcome: Implemented in Epic 6 with health checks and startup dependencies.

---

## Summary of Key Trade-Off Themes
- Higher operational complexity accepted for stronger service autonomy.
- Eventual consistency accepted for scalability and decoupling.
- Centralized gateway trust accepted to simplify downstream services.
- Additional resilience configuration accepted to improve outage behavior.
