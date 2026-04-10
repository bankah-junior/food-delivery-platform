# Food Delivery Platform

This repository contains a migration project from a monolithic Spring Boot application to a production-style microservices architecture for a food delivery platform.

## Repository Structure

- `monolithic/`: Starter monolith used as migration baseline.
- `microservices/`: Target architecture with independent services.
- `docker-compose.yml`: Full local orchestration for all services and infrastructure.
- `microservices/docs/`: Testing artifacts, architecture diagrams, API documentation, and migration decisions.

## Microservices Overview

Core services:
- Customer Service (`:8081`)
- Restaurant Service (`:8082`)
- Order Service (`:8083`)
- Delivery Service (`:8084`)

Platform services:
- API Gateway (`:8080`)
- Eureka Server (`:8761`)
- RabbitMQ (`:5672`, management UI `:15672`)
- PostgreSQL (`:5432`)

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker Desktop (or Docker Engine)
- Docker Compose V2
- Optional for API test runbook: Node.js 18+ and Newman

## Setup and Build

1. Clone repository

```bash
git clone <your-repository-url>
cd food-delivery-platform
```

2. Build all microservices

```bash
mvn -f microservices/eureka-server/pom.xml clean package -DskipTests
mvn -f microservices/customer-service/pom.xml clean package -DskipTests
mvn -f microservices/restaurant-service/pom.xml clean package -DskipTests
mvn -f microservices/order-service/pom.xml clean package -DskipTests
mvn -f microservices/delivery-service/pom.xml clean package -DskipTests
mvn -f microservices/api-gateway/pom.xml clean package -DskipTests
```

3. Start the full stack

```bash
docker compose up -d
```

4. Verify containers

```bash
docker compose ps
```

5. Stop the stack

```bash
docker compose down
```

To also remove persistent volumes:

```bash
docker compose down -v
```

## Service Access and Dashboards

- API Gateway base URL: `http://localhost:8080`
- Eureka dashboard: `http://localhost:8761`
- RabbitMQ management console: `http://localhost:15672` (default user/password: `guest` / `guest`)

## API Gateway Routes

All client calls should go through the gateway (`http://localhost:8080`):

- `/api/auth/**` -> Customer Service
- `/api/customers/**` -> Customer Service
- `/api/restaurants/**` -> Restaurant Service
- `/api/orders/**` -> Order Service
- `/api/deliveries/**` -> Delivery Service

## End-to-End API Runbook

Postman artifacts:
- Collection: `microservices/docs/postman/food-delivery-microservices.postman_collection.json`
- Environment: `microservices/docs/postman/food-delivery-local.postman_environment.json`
- Newman report sample: `microservices/docs/postman/newman-epic7-2-report.json`

Optional Newman execution:

```bash
npm install -g newman
newman run microservices/docs/postman/food-delivery-microservices.postman_collection.json \
  -e microservices/docs/postman/food-delivery-local.postman_environment.json
```

## API Specifications and Contracts

Service API docs index:
- `microservices/docs/apis/README.md`

OpenAPI specs:
- `microservices/docs/apis/specs/customer-service-openapi.yaml`
- `microservices/docs/apis/specs/restaurant-service-openapi.yaml`
- `microservices/docs/apis/specs/order-service-openapi.yaml`
- `microservices/docs/apis/specs/delivery-service-openapi.yaml`

You can import any spec into Swagger Editor (`https://editor.swagger.io`) for interactive inspection.

## Architecture Diagrams

Diagram index:
- `microservices/docs/diagrams/README.md`

Mermaid sources:
- `microservices/docs/diagrams/monolith-architecture.mmd`
- `microservices/docs/diagrams/microservices-architecture.mmd`
- `microservices/docs/diagrams/sequence-order-placement.mmd`
- `microservices/docs/diagrams/sequence-event-driven-delivery.mmd`

Exported images (PNG/SVG):
- `microservices/docs/diagrams/exports/`

## Troubleshooting

1. `docker compose up -d` fails
- Ensure Docker is running.
- Check for port collisions on `8080-8084`, `8761`, `5432`, `5672`, and `15672`.
- Review logs: `docker compose logs -f <service-name>`.

2. Gateway returns 503 for service routes
- Confirm Eureka is up (`http://localhost:8761`).
- Confirm target service is healthy in `docker compose ps`.

3. Auth-protected endpoints fail with 401/403
- Authenticate first via `/api/auth/login` through gateway.
- Ensure `Authorization: Bearer <token>` is included in requests.

4. RabbitMQ UI not reachable
- Validate container health: `docker compose ps rabbitmq`.
- Confirm management port `15672` is not used by another process.

5. Stale database state causes duplicate-data errors in tests
- Reset environment when needed:

```bash
docker compose down -v
docker compose up -d
```

## Additional Project Documentation

- Migration decision log: `microservices/docs/migration-decision-log.md`
- Fault tolerance evidence: `microservices/docs/postman/epic-7-3-fault-tolerance-results.md`
- Engineering tracker: `microservices/docs/progress-tracker.md`
- Epic task checklist: `microservices/docs/todo.md`

## Contribution Guidelines

1. Create a feature branch from `main`.
2. Keep changes scoped to one concern (service feature, docs, or infra).
3. Run service build(s) and relevant tests before opening a PR.
4. Update docs in `microservices/docs/` when behavior or API contracts change.
5. Use clear commit messages and include verification notes in PR descriptions.

## License

