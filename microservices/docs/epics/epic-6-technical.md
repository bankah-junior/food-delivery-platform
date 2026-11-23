# Epic 6 Technical Report

## Epic
Epic 6: Containerization with Docker

## Status
Complete

## Completion Summary
- 6.1 Create Dockerfiles: Complete (7 of 7)
- 6.2 Create Docker-Specific Configuration: Complete (6 of 6)
- 6.3 Create Docker Compose File: Complete (8 of 8)
- 6.4 Test Docker Setup: Complete (8 of 8)
- Overall Epic 6: Complete (16 of 16)

## 6.1 Create Dockerfiles

### What Was Implemented
- Created Dockerfiles for all services:
	- customer-service
	- restaurant-service
	- order-service
	- delivery-service
	- eureka-server
	- api-gateway
- Standardized each Dockerfile as multi-stage:
	- Stage 1: maven:3.9.11-eclipse-temurin-21 build image
	- Stage 2: eclipse-temurin:21-jre runtime image
- Added runtime health checks in each image using actuator endpoints.
- Added .dockerignore file to each service to reduce build context and avoid copying local artifacts.

### Technical Outcome
- All six services can be built as container images with consistent Java 21 runtime behavior and health probing support.

## 6.2 Create Docker-Specific Configuration

### What Was Implemented
- Added Docker profile configuration files for eureka-server and api-gateway.
- Aligned existing Docker profile configurations for domain services.
- Standardized Eureka service discovery target for Compose networking:
	- defaultZone: ${EUREKA_SERVER_URL:http://eureka-server:8761/eureka/}
- Ensured Docker runtime entrypoints activate docker profile where needed.
- Kept credentials and sensitive values configurable through environment variables.

### Technical Outcome
- All services now boot in containerized mode with correct inter-service discovery and environment-specific settings.

## 6.3 Create Docker Compose File

### What Was Implemented
- Added root docker-compose.yml that defines:
	- Infrastructure: postgres, rabbitmq (management image)
	- Platform services: eureka-server, customer-service, restaurant-service, order-service, delivery-service, api-gateway
- Added Postgres initialization script to create additional service databases:
	- restaurant_db
	- order_db
	- delivery_db
- Configured health checks for infrastructure and application services.
- Configured startup dependencies using depends_on with service_healthy conditions.
- Added named volumes for data persistence:
	- postgres_data
	- rabbitmq_data
- Exposed required host ports for API and operational access.
- Validated compose structure using docker compose config.

### Technical Outcome
- The full platform can be orchestrated from a single Compose file with health-aware startup ordering and persistent infrastructure state.

## 6.4 Test Docker Setup

### What Was Implemented
- Built images and started full stack with:
	- docker compose up --build -d
- Verified container health using:
	- docker compose ps
- Verified service registration in Eureka by querying registry from eureka-server container.
- Verified API gateway behavior:
	- /actuator/health returned UP
	- auth register and login requests succeeded through gateway routes
	- protected customer endpoint /api/customers/me succeeded with bearer token
- Verified end-to-end order flow in containers:
	- created restaurant and menu item
	- placed order via gateway
	- confirmed delivery lookup by order id
	- observed expected statuses (order PLACED, delivery ASSIGNED)
- Verified RabbitMQ management endpoint reachability on localhost:15672.
- Verified Postgres connectivity and expected databases in running container.
- Scanned application logs for startup/runtime error signatures and found no critical error patterns.

### Technical Outcome
- The containerized environment is operational end-to-end, including routing, service discovery, persistence, messaging, and asynchronous order-to-delivery processing.

## Validation and Verification
- Compose configuration validation: Passed
- Container build and startup: Passed
- All service health checks: Passed
- Eureka registration visibility: Passed
- Gateway API smoke tests: Passed
- End-to-end order workflow in containers: Passed
- RabbitMQ management reachability: Passed
- Postgres database initialization and connectivity: Passed

## Key Files Added/Updated
- Root:
	- docker-compose.yml
	- docker/postgres-init/01-create-databases.sql
- Service container assets:
	- microservices/customer-service/Dockerfile
	- microservices/customer-service/.dockerignore
	- microservices/restaurant-service/Dockerfile
	- microservices/restaurant-service/.dockerignore
	- microservices/order-service/Dockerfile
	- microservices/order-service/.dockerignore
	- microservices/delivery-service/Dockerfile
	- microservices/delivery-service/.dockerignore
	- microservices/eureka-server/Dockerfile
	- microservices/eureka-server/.dockerignore
	- microservices/api-gateway/Dockerfile
	- microservices/api-gateway/.dockerignore
- Docker profile configuration:
	- microservices/eureka-server/src/main/resources/application-docker.yml
	- microservices/api-gateway/src/main/resources/application-docker.yml
	- microservices/customer-service/src/main/resources/application-docker.yml
	- microservices/restaurant-service/src/main/resources/application-docker.yml
	- microservices/order-service/src/main/resources/application-docker.yml
	- microservices/delivery-service/src/main/resources/application-docker.yml

## Outcome
Epic 6 is fully complete. The microservices platform is containerized, orchestrated with Docker Compose, and validated through live end-to-end runtime checks.

