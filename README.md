# Food Delivery Platform - Project Understanding

## Project Overview

This project involves refactoring a **monolithic Spring Boot application** into a **microservices architecture** for a food delivery platform. The transformation includes service decomposition, containerization, service discovery, API gateway implementation, and event-driven communication.

**Complexity:** Advanced  
**Tech Stack:** Spring Boot 3.x, Java 21, Docker, PostgreSQL, RabbitMQ, Eureka, Spring Cloud Gateway

---

## Current State: Monolith Architecture

### Characteristics
- **Single Application:** All functionality in one Spring Boot app running on port 8080
- **Single Database:** H2 in-memory database (to be replaced with PostgreSQL in microservices)
- **Tight Coupling:** Direct method calls between services (e.g., `OrderService` → `RestaurantRepository`)
- **Synchronous Processing:** Delivery creation blocks order placement response
- **Shared Entities:** JPA entities with `@ManyToOne` and `@OneToOne` relationships across domains
- **Monolithic Deployment:** Single JAR deployment - scaling requires scaling everything

### Domains in the Monolith
1. **Customer Management:** User registration, authentication, profile management
2. **Restaurant & Menu Management:** Restaurant listings, menu items, cuisine types
3. **Order Management:** Order placement, status tracking, order history
4. **Delivery Management:** Delivery assignment, status updates, tracking

### Key Coupling Points to Break
1. `OrderService` directly injects `CustomerService`, `RestaurantService`, and `DeliveryService`
2. `RestaurantService` accesses `CustomerRepository` for ownership validation
3. `DeliveryService.createDeliveryForOrder()` called synchronously during order placement
4. `DeliveryService.updateStatus()` directly modifies `Order` entity status
5. Entity relationships (`@ManyToOne`, `@OneToOne`) cross domain boundaries
6. DTO mapping traverses entities across domains

---

## Target State: Microservices Architecture

### Architecture Components

```
Client → API Gateway (:8080)
              │
              ├── /api/customers/**   → Customer Service   (:8081) → customer_db
              ├── /api/restaurants/** → Restaurant Service (:8082) → restaurant_db
              ├── /api/orders/**      → Order Service      (:8083) → order_db
              └── /api/deliveries/**  → Delivery Service   (:8084) → delivery_db

Eureka Server (:8761) — Service Registry
RabbitMQ (:5672)      — Event Bus
```

### Four Independent Microservices

#### 1. Customer Service (:8081)
- **Responsibilities:** User registration, authentication, profile management
- **Database:** `customer_db`
- **Entities:** Customer
- **API:** `/api/customers/**`, `/api/auth/**`

#### 2. Restaurant Service (:8082)
- **Responsibilities:** Restaurant management, menu management, cuisine search
- **Database:** `restaurant_db`
- **Entities:** Restaurant, MenuItem
- **API:** `/api/restaurants/**`

#### 3. Order Service (:8083)
- **Responsibilities:** Order placement, order tracking, order history
- **Database:** `order_db`
- **Entities:** Order, OrderItem
- **API:** `/api/orders/**`
- **Inter-service Calls:** 
  - Customer Service (validate customer, get delivery address)
  - Restaurant Service (validate menu items, check prices)

#### 4. Delivery Service (:8084)
- **Responsibilities:** Delivery assignment, status tracking, delivery completion
- **Database:** `delivery_db`
- **Entities:** Delivery
- **API:** `/api/deliveries/**`
- **Event-Driven:** Creates deliveries when `OrderPlacedEvent` is received

### Infrastructure Services

#### Eureka Server (:8761)
- Service registry for dynamic service discovery
- All microservices register on startup
- Services use logical names instead of hardcoded IPs

#### API Gateway (:8080)
- Single entry point for all client requests
- Request routing based on path patterns
- JWT authentication filter at gateway level
- Rate limiting on high-traffic endpoints
- Load balancing via Eureka integration

#### RabbitMQ (:5672)
- Message broker for asynchronous communication
- Event types:
  - `OrderPlacedEvent` (Order → Delivery)
  - `OrderCancelledEvent` (Order → Delivery)
  - `DeliveryStatusUpdatedEvent` (Delivery → Order/Notification)
- Dead letter queue for failed message processing

---

## Key Transformations Required

### 1. Database Separation
**From:** Single shared database with foreign key relationships  
**To:** Four independent databases with ID references only

- Remove JPA entity relationships across services
- Replace with REST API calls to fetch related data
- Each service owns its data completely

### 2. Communication Patterns

#### Synchronous (OpenFeign)
- Used for request-response scenarios requiring immediate data
- Example: Order Service validates menu items with Restaurant Service
- Circuit breakers for fault tolerance

#### Asynchronous (RabbitMQ)
- Used for fire-and-forget, eventual consistency scenarios
- Example: Order placed → Delivery created asynchronously
- Improves performance and decouples services

### 3. Service Discovery
**From:** Hardcoded URLs  
**To:** Dynamic discovery via Eureka

- Services register with Eureka on startup
- Feign clients use logical service names: `http://restaurant-service`
- Automatic load balancing across multiple instances

### 4. Authentication & Authorization
**From:** JWT validation in each controller  
**To:** Centralized JWT validation at API Gateway

- Gateway validates tokens once
- Propagates user context to downstream services
- Services trust authenticated requests from gateway

### 5. Fault Tolerance
- **Circuit Breakers:** Resilience4j on all Feign clients
- **Fallback Responses:** Graceful degradation when services are down
- **Timeouts:** Prevent cascading failures
- **Bulkheads:** Isolate thread pools per service call

---

## Technical Implementation Details

### Docker Containerization
- Multi-stage Dockerfiles (build + runtime stages)
- Separate containers for each service and database
- Docker Compose orchestration for entire system
- Health checks and proper startup ordering
- Environment-based configuration with `application-docker.yml`

### Spring Cloud Stack
- **Spring Cloud Netflix Eureka:** Service discovery
- **Spring Cloud Gateway:** API gateway with filters and predicates
- **Spring Cloud OpenFeign:** Declarative REST clients
- **Spring Boot Actuator:** Health checks and metrics
- **Resilience4j:** Circuit breakers, retry, rate limiting

### Database Per Service
- PostgreSQL for each microservice
- Schema migration via Flyway or Liquibase
- No shared databases or cross-service queries
- Data duplication acceptable for bounded contexts

---

## Migration Strategy

### Phase 1: Service Decomposition
1. Create four separate Spring Boot projects
2. Extract entities and repositories per domain
3. Break entity relationships, use IDs only
4. Implement REST APIs for each service
5. Test each service independently

### Phase 2: Inter-service Communication
1. Implement OpenFeign clients for synchronous calls
2. Add circuit breakers with Resilience4j
3. Define fallback strategies
4. Handle partial failures gracefully

### Phase 3: Event-Driven Architecture
1. Set up RabbitMQ with exchanges and queues
2. Implement event publishers in Order Service
3. Implement event consumers in Delivery Service
4. Add dead letter queue handling
5. Test async message flows

### Phase 4: Infrastructure Setup
1. Create Eureka Server
2. Register all services with Eureka
3. Build API Gateway with routing rules
4. Add JWT authentication filter at gateway
5. Configure rate limiting

### Phase 5: Containerization
1. Write Dockerfiles for each service
2. Create docker-compose.yml
3. Configure Docker-specific profiles
4. Set up health checks
5. Test complete system startup

### Phase 6: Testing & Documentation
1. Create Postman collection for all endpoints
2. Test end-to-end user flows
3. Test fault tolerance scenarios
4. Draw architecture diagrams
5. Document API contracts and migration decisions

---

## Success Criteria

### Functional Requirements
✓ All original monolith features work in microservices version  
✓ Order placement flow: customer → restaurant → order → delivery  
✓ Event-driven delivery creation happens asynchronously  
✓ JWT authentication works through gateway  

### Non-Functional Requirements
✓ Each service can be deployed independently  
✓ Services can scale independently  
✓ System remains functional when one service is down  
✓ Circuit breakers prevent cascading failures  
✓ Complete system starts with `docker compose up`  

### Documentation Requirements
✓ Architecture diagram showing all services and data flows  
✓ API contracts for each microservice  
✓ Migration decision log explaining decomposition choices  
✓ README with setup and testing instructions  

---

## Key Challenges & Solutions

### Challenge 1: Data Consistency
**Problem:** No distributed transactions across services  
**Solution:** Event-driven eventual consistency, compensating transactions for failures

### Challenge 2: Joined Queries
**Problem:** Can't JOIN across service databases  
**Solution:** API composition pattern - make multiple REST calls and aggregate in memory

### Challenge 3: Service Availability
**Problem:** Dependent service might be down  
**Solution:** Circuit breakers with fallback responses, graceful degradation

### Challenge 4: Data Duplication
**Problem:** Need customer name in order history  
**Solution:** Cache necessary data in Order Service, use events to keep it updated

### Challenge 5: Testing Complexity
**Problem:** Need entire system running to test one feature  
**Solution:** Contract testing, service mocking, Docker Compose for integration tests

---

## Benefits of Microservices Architecture

1. **Independent Deployment:** Deploy Restaurant Service without touching Order Service
2. **Technology Flexibility:** Each service can use different frameworks/databases
3. **Team Autonomy:** Teams can work on different services independently
4. **Scalability:** Scale only the bottleneck service (e.g., Order Service during peak hours)
5. **Fault Isolation:** Bug in Delivery Service won't crash order placement
6. **Better Organization:** Clear boundaries make codebase easier to understand

---
