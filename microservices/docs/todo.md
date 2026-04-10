# Food Delivery Platform - Complete TODO List

## Epic 1: Service Decomposition and Database Separation

### 1.1 Create Service Scaffolding
- [x] Create Customer Service Spring Boot project
  - [x] Initialize with Spring Initializr (Web, JPA, Security, Actuator)
  - [x] Set up `application.yml` and `application-docker.yml`
  - [x] Configure port 8081
  - [x] Add PostgreSQL dependency
  
- [x] Create Restaurant Service Spring Boot project
  - [x] Initialize with Spring Initializr (Web, JPA, Security, Actuator)
  - [x] Set up `application.yml` and `application-docker.yml`
  - [x] Configure port 8082
  - [x] Add PostgreSQL dependency

- [x] Create Order Service Spring Boot project
  - [x] Initialize with Spring Initializr (Web, JPA, Security, Actuator, OpenFeign)
  - [x] Set up `application.yml` and `application-docker.yml`
  - [x] Configure port 8083
  - [x] Add PostgreSQL dependency

- [x] Create Delivery Service Spring Boot project
  - [x] Initialize with Spring Initializr (Web, JPA, Security, Actuator, AMQP)
  - [x] Set up `application.yml` and `application-docker.yml`
  - [x] Configure port 8084
  - [x] Add PostgreSQL dependency

### 1.2 Extract Customer Service Domain
- [x] Copy `Customer` entity from monolith
- [x] Create `CustomerRepository` with required queries
- [x] Implement `CustomerService` business logic
- [x] Create `CustomerController` with endpoints:
  - [x] GET `/api/customers/me`
  - [x] GET `/api/customers/{id}`
  - [x] PUT `/api/customers/me`
- [x] Implement DTOs: `CustomerRequest`, `CustomerResponse`
- [x] Set up JWT security configuration
- [x] Implement authentication endpoints:
  - [x] POST `/api/auth/register`
  - [x] POST `/api/auth/login`
- [x] Test Customer Service independently

### 1.3 Extract Restaurant Service Domain
- [x] Copy `Restaurant` entity (remove customer relationship)
- [x] Copy `MenuItem` entity
- [x] Create `RestaurantRepository` with search queries
- [x] Create `MenuItemRepository`
- [x] Implement `RestaurantService` business logic
- [x] Implement `MenuService` business logic
- [x] Create `RestaurantController` with endpoints:
  - [x] GET `/api/restaurants/search/all`
  - [x] GET `/api/restaurants/search/city/{city}`
  - [x] GET `/api/restaurants/search/cuisine/{type}`
  - [x] GET `/api/restaurants/{id}`
  - [x] GET `/api/restaurants/{id}/menu`
  - [x] POST `/api/restaurants`
  - [x] POST `/api/restaurants/{id}/menu`
  - [x] PUT `/api/restaurants/menu/{itemId}`
  - [x] PATCH `/api/restaurants/menu/{itemId}/toggle`
- [x] Implement DTOs: `RestaurantRequest`, `RestaurantResponse`, `MenuItemRequest`, `MenuItemResponse`
- [x] Add validation for restaurant ownership (use ownerId from gateway-provided header)
- [x] Test Restaurant Service independently

### 1.4 Extract Order Service Domain
- [x] Copy `Order` entity (remove customer/restaurant relationships, use IDs only)
- [x] Copy `OrderItem` entity (remove MenuItem relationship)
- [x] Create `OrderRepository` with query methods
- [x] Create `OrderItemRepository`
- [x] Implement `OrderService` business logic
- [x] Create `OrderController` with endpoints:
  - [x] POST `/api/orders`
  - [x] GET `/api/orders/{id}`
  - [x] GET `/api/orders/my-orders`
  - [x] GET `/api/orders/restaurant/{id}`
  - [x] PATCH `/api/orders/{id}/status`
  - [x] POST `/api/orders/{id}/cancel`
- [x] Implement DTOs: `OrderRequest`, `OrderResponse`, `OrderItemRequest`
- [x] Test Order Service independently (with mocked external calls)

### 1.5 Extract Delivery Service Domain
- [x] Copy `Delivery` entity (remove order relationship, use orderId only)
- [x] Create `DeliveryRepository`
- [x] Implement `DeliveryService` business logic
- [x] Create `DeliveryController` with endpoints:
  - [x] GET `/api/deliveries/{id}`
  - [x] GET `/api/deliveries/order/{orderId}`
  - [x] GET `/api/deliveries/status/{status}`
  - [x] PATCH `/api/deliveries/{id}/status`
- [x] Implement DTOs: `DeliveryResponse`
- [x] Test Delivery Service independently

### 1.6 Database Setup
- [x] Create PostgreSQL schema for `customer_db`
- [x] Create PostgreSQL schema for `restaurant_db`
- [x] Create PostgreSQL schema for `order_db`
- [x] Create PostgreSQL schema for `delivery_db`
- [x] Add Flyway/Liquibase migration scripts for each service
- [x] Test database migrations

---

## Epic 2: Inter-Service Communication

### 2.1 Implement OpenFeign Clients
- [x] Add Spring Cloud OpenFeign dependency to Order Service
- [x] Create `CustomerServiceClient` Feign interface
  - [x] Add method: `getCustomerById(Long id)`
- [x] Create `RestaurantServiceClient` Feign interface
  - [x] Add method: `getRestaurantById(Long id)`
  - [x] Add method: `getMenuItem(Long restaurantId, Long itemId)`
  - [x] Add method: `validateMenuItems(Long restaurantId, List<Long> itemIds)`
- [x] Enable Feign clients with `@EnableFeignClients`
- [x] Configure Feign logging level

### 2.2 Replace Direct Calls with REST Calls
- [x] Update `OrderService.placeOrder()`:
  - [x] Call Customer Service to validate customer and get delivery address
  - [x] Call Restaurant Service to validate menu items and prices
  - [x] Calculate total amount from API response
  - [x] Remove direct repository calls
- [x] Update `RestaurantController` ownership validation:
  - [x] Use JWT customerId instead of CustomerRepository
- [x] Remove all cross-domain repository dependencies

### 2.3 Error Handling for Service Calls
- [x] Implement custom exception classes:
  - [x] `ServiceUnavailableException`
  - [x] `CustomerNotFoundException`
  - [x] `RestaurantNotFoundException`
  - [x] `MenuItemNotFoundException`
- [x] Add `@ControllerAdvice` global exception handler in each service
- [x] Map Feign exceptions to proper HTTP status codes
- [x] Return meaningful error messages to clients
- [x] Map downstream service outages to HTTP 503 responses

---

## Epic 3: Event-Driven Communication

### 3.1 RabbitMQ Setup
- [x] Add Spring Boot AMQP dependency to Order and Delivery services
- [x] Create RabbitMQ configuration class with:
  - [x] Topic exchange: `food-delivery-exchange`
  - [x] Queue: `delivery.order-placed`
  - [x] Queue: `delivery.order-cancelled`
  - [x] Queue: `order.delivery-status-updated`
  - [x] Dead letter queue: `dlq.delivery`
- [x] Configure RabbitMQ connection properties in `application.yml`
- [x] Set up message converter (JSON)

### 3.2 Define Event DTOs
- [x] Create `OrderPlacedEvent`:
  - [x] Fields: orderId, customerId, restaurantId, items, totalAmount, deliveryAddress
- [x] Create `OrderCancelledEvent`:
  - [x] Fields: orderId, reason
- [x] Create `DeliveryStatusUpdatedEvent`:
  - [x] Fields: deliveryId, orderId, status, timestamp

### 3.3 Implement Event Publishers
- [x] In Order Service, create `OrderEventPublisher`:
  - [x] Method: `publishOrderPlaced(Order order)`
  - [x] Method: `publishOrderCancelled(Long orderId, String reason)`
- [x] Update `OrderService.placeOrder()` to publish `OrderPlacedEvent`
- [x] Update `OrderService.cancelOrder()` to publish `OrderCancelledEvent`
- [x] Add logging for published events

### 3.4 Implement Event Consumers
- [x] In Delivery Service, create `OrderEventListener`:
  - [x] Method: `handleOrderPlaced(OrderPlacedEvent event)` with `@RabbitListener`
  - [x] Method: `handleOrderCancelled(OrderCancelledEvent event)`
- [x] Implement automatic delivery creation logic
- [x] In Order Service, create `DeliveryEventListener`:
  - [x] Method: `handleDeliveryStatusUpdated(DeliveryStatusUpdatedEvent event)`
- [x] Update order status based on delivery status
- [x] Add error handling and dead letter queue processing

### 3.5 Remove Synchronous Delivery Creation
- [x] Remove `DeliveryService` injection from `OrderService` in monolith
- [x] Remove `createDeliveryForOrder()` synchronous call
- [x] Verify order placement returns immediately without blocking
- [x] Test async delivery creation via events

---

## Epic 4: Service Discovery and API Gateway

### 4.1 Create Eureka Server
- [x] Initialize Eureka Server Spring Boot project
- [x] Add `spring-cloud-starter-netflix-eureka-server` dependency
- [x] Enable Eureka Server with `@EnableEurekaServer`
- [x] Configure `application.yml`:
  - [x] Port: 8761
  - [x] Disable self-registration
  - [x] Set hostname and service URL
- [x] Test Eureka dashboard at `http://localhost:8761`

### 4.2 Register Services with Eureka
- [x] Add Eureka Client dependency to all four microservices
- [x] Configure Eureka client in each service's `application.yml`:
  - [x] Set `spring.application.name` (customer-service, restaurant-service, order-service, delivery-service)
  - [x] Set Eureka server URL
  - [x] Configure instance metadata
- [x] Update Feign clients to use service names instead of URLs:
  - [x] `@FeignClient(name = "customer-service")`
  - [x] `@FeignClient(name = "restaurant-service")`
- [x] Enable load balancing with Eureka
- [x] Test service registration in Eureka dashboard

### 4.3 Create API Gateway
- [x] Initialize Spring Cloud Gateway project
- [x] Add dependencies:
  - [x] `spring-cloud-starter-gateway`
  - [x] `spring-cloud-starter-netflix-eureka-client`
  - [x] `spring-boot-starter-security`
- [x] Configure routing in `application.yml`:
  - [x] Route `/api/customers/**` → `lb://customer-service`
  - [x] Route `/api/restaurants/**` → `lb://restaurant-service`
  - [x] Route `/api/orders/**` → `lb://order-service`
  - [x] Route `/api/deliveries/**` → `lb://delivery-service`
  - [x] Route `/api/auth/**` → `lb://customer-service`
- [x] Set gateway port to 8080
- [x] Register gateway with Eureka

### 4.4 Implement Gateway Filters
- [x] Create JWT authentication filter:
  - [x] Extract token from Authorization header
  - [x] Validate JWT signature and expiration
  - [x] Add user information to request headers
  - [x] Allow unauthenticated paths: `/api/auth/**`, `/api/restaurants/search/**`
- [x] Create request rate limiting filter:
  - [x] Apply to `/api/orders/**` (e.g., 10 requests per minute)
- [x] Add logging filter for request/response tracking
- [x] Test authentication and routing through gateway

### 4.5 Update Services for Gateway
- [x] Remove JWT validation from individual services (trust gateway)
- [x] Read user context from gateway-added headers
- [x] Update CORS configuration to allow gateway origin
- [x] Test all endpoints through gateway at `http://localhost:8080`

---

## Epic 5: Fault Tolerance with Resilience4j

### 5.1 Add Resilience4j Dependencies
- [x] Add `spring-cloud-starter-circuitbreaker-resilience4j` to Order Service
- [x] Add `resilience4j-spring-boot3` dependency
- [x] Add `spring-boot-starter-aop` for aspects

### 5.2 Configure Circuit Breakers
- [x] Create `resilience4j` configuration in `application.yml`:
  - [x] Configure `customerService` circuit breaker (failure rate threshold: 50%, wait duration: 10s)
  - [x] Configure `restaurantService` circuit breaker
  - [x] Set sliding window size and minimum calls
- [x] Enable circuit breaker endpoints in Actuator

### 5.3 Add Circuit Breakers to Feign Clients
- [x] Annotate `CustomerServiceClient` methods with circuit breaker fallback wiring:
  - [x] Add fallback method for `getCustomerById()`
- [x] Annotate `RestaurantServiceClient` methods with circuit breaker fallback wiring:
  - [x] Add fallback method for `getRestaurant()`
  - [x] Add fallback method for `getMenuItem()`
- [x] Implement fallback methods with degraded responses

### 5.4 Add Retry Logic
- [x] Configure retry policy in `application.yml`:
  - [x] Max retry attempts: 3
  - [x] Wait duration: 1s with exponential backoff
- [x] Add `@Retry` annotation to Feign client methods
- [x] Test retry behavior with temporary service outages

### 5.5 Test Fault Tolerance
- [x] Verify order placement fails gracefully when Customer Service is down
- [x] Verify meaningful error messages are returned
- [x] Verify circuit breaker opens after repeated failures
- [x] Verify circuit breaker closes after service recovery
- [x] Check circuit breaker states via Actuator endpoints

---

## Epic 6: Containerization with Docker

### 6.1 Create Dockerfiles
- [x] Write Dockerfile for Customer Service:
  - [x] Stage 1: Maven build stage
  - [x] Stage 2: Runtime stage with JRE
  - [x] Expose port 8081
  - [x] Set health check
- [x] Write Dockerfile for Restaurant Service (port 8082)
- [x] Write Dockerfile for Order Service (port 8083)
- [x] Write Dockerfile for Delivery Service (port 8084)
- [x] Write Dockerfile for Eureka Server (port 8761)
- [x] Write Dockerfile for API Gateway (port 8080)
- [x] Create `.dockerignore` files for each service

### 6.2 Create Docker-Specific Configuration
- [x] Create `application-docker.yml` for Customer Service:
  - [x] Database URL: `jdbc:postgresql://postgres:5432/customer_db`
  - [x] Eureka URL: `http://eureka-server:8761/eureka`
  - [x] RabbitMQ host: `rabbitmq` (where applicable)
- [x] Create `application-docker.yml` for Restaurant Service
- [x] Create `application-docker.yml` for Order Service
- [x] Create `application-docker.yml` for Delivery Service
- [x] Create `application-docker.yml` for Eureka Server
- [x] Create `application-docker.yml` for API Gateway
- [x] Use environment variables for sensitive values

### 6.3 Create Docker Compose File
- [x] Create root `docker-compose.yml` with services:
  - [x] PostgreSQL (with 4 databases)
  - [x] RabbitMQ (with management plugin)
  - [x] Eureka Server
  - [x] Customer Service
  - [x] Restaurant Service
  - [x] Order Service
  - [x] Delivery Service
  - [x] API Gateway
- [x] Configure service dependencies with `depends_on`
- [x] Add health checks for each service
- [x] Set proper startup order
- [x] Configure volumes for PostgreSQL persistence
- [x] Expose necessary ports to host
- [x] Set up networks for service communication

### 6.4 Test Docker Setup
- [x] Build all Docker images
- [x] Run `docker compose up` and verify all services start
- [x] Check Eureka dashboard shows all registered services
- [x] Verify health checks pass
- [x] Test API calls through gateway
- [x] Check RabbitMQ management console
- [x] Verify database connections
- [x] Test complete order flow in containerized environment

---

## Epic 7: Testing and Documentation

### 7.1 Create Postman Collection
- [x] Set up environment variables (base_url, token)
- [x] Create folder: Authentication
  - [x] POST Register customer
  - [x] POST Login (save token to environment)
- [x] Create folder: Customers
  - [x] GET My profile
  - [x] GET Customer by ID
  - [x] PUT Update profile
- [x] Create folder: Restaurants
  - [x] GET All restaurants
  - [x] GET Search by city
  - [x] GET Search by cuisine
  - [x] GET Restaurant details
  - [x] GET Restaurant menu
  - [x] POST Create restaurant
  - [x] POST Add menu item
  - [x] PUT Update menu item
  - [x] PATCH Toggle item availability
- [x] Create folder: Orders
  - [x] POST Place order
  - [x] GET Order by ID
  - [x] GET My orders
  - [x] GET Restaurant orders
  - [x] PATCH Update order status
  - [x] POST Cancel order
- [x] Create folder: Deliveries
  - [x] GET Delivery by ID
  - [x] GET Delivery by order
  - [x] GET Deliveries by status
  - [x] PATCH Update delivery status
- [x] Add pre-request scripts for token management
- [x] Add tests for status codes and response validation

### 7.2 End-to-End Testing
- [ ] Test complete order flow:
  - [ ] Register new customer
  - [ ] Login and get token
  - [ ] Create restaurant
  - [ ] Add menu items
  - [ ] Browse restaurants
  - [ ] Place order
  - [ ] Verify delivery created automatically (async)
  - [ ] Update delivery status
  - [ ] Verify order status updated
  - [ ] Check order history
- [ ] Test edge cases:
  - [ ] Order from non-existent restaurant
  - [ ] Order unavailable menu items
  - [ ] Cancel already delivered order
  - [ ] Invalid JWT token

### 7.3 Fault Tolerance Testing
- [ ] Stop Customer Service and test order placement:
  - [ ] Verify circuit breaker fallback works
  - [ ] Verify meaningful error returned
- [ ] Stop Restaurant Service and test order placement
- [ ] Stop Delivery Service and verify orders can still be placed
- [ ] Test circuit breaker state transitions
- [ ] Restart services and verify auto-recovery

### 7.4 Create Architecture Diagrams
- [ ] Draw current monolith architecture diagram
- [ ] Draw target microservices architecture diagram
- [ ] Create sequence diagram for order placement flow
- [ ] Create sequence diagram for event-driven delivery creation
- [ ] Document service dependencies and communication patterns
- [ ] Export diagrams as PNG/SVG

### 7.5 Write API Documentation
- [ ] Document Customer Service API contract:
  - [ ] List all endpoints with request/response examples
  - [ ] Document authentication requirements
- [ ] Document Restaurant Service API contract
- [ ] Document Order Service API contract
- [ ] Document Delivery Service API contract
- [ ] Create OpenAPI/Swagger specs for each service
- [ ] Generate API documentation with Swagger UI

### 7.6 Create Migration Decision Log
- [ ] Document service boundary decisions:
  - [ ] Why Customer is separate from Order
  - [ ] Why Restaurant includes Menu
  - [ ] Why Delivery is separate from Order
- [ ] Document communication pattern choices:
  - [ ] When to use sync vs async
  - [ ] Why OpenFeign for REST calls
  - [ ] Why RabbitMQ for events
- [ ] Document database separation strategy
- [ ] Document security approach (gateway-level JWT)
- [ ] List trade-offs and alternatives considered

### 7.7 Update README
- [ ] Add comprehensive project description
- [ ] Document prerequisites (Java 21, Maven, Docker)
- [ ] Add setup instructions:
  - [ ] Clone repository
  - [ ] Build all services
  - [ ] Run with Docker Compose
  - [ ] Access Eureka dashboard
  - [ ] Access RabbitMQ console
- [ ] Document API Gateway URL and routing
- [ ] Add troubleshooting section
- [ ] Link to architecture diagrams
- [ ] Link to Postman collection
- [ ] Add contribution guidelines

---

## Epic 8: Performance and Monitoring

### 8.1 Add Monitoring with Spring Boot Actuator
- [ ] Enable Actuator endpoints in all services
- [ ] Expose health, metrics, and info endpoints
- [ ] Configure Actuator security (allow Prometheus scraping)
- [ ] Add custom health indicators for database and RabbitMQ

### 8.2 Add Distributed Tracing (Optional)
- [ ] Add Spring Cloud Sleuth dependency
- [ ] Configure trace and span IDs in logs
- [ ] Set up Zipkin server (optional)
- [ ] Test tracing across service calls

### 8.3 Performance Testing
- [ ] Use JMeter/Gatling to load test order placement
- [ ] Test concurrent order processing
- [ ] Verify RabbitMQ handles message throughput
- [ ] Monitor circuit breaker behavior under load
- [ ] Check database connection pool usage

---

## Final Checklist

- [ ] All services start independently
- [ ] Complete system starts with `docker compose up`
- [ ] All API endpoints functional through gateway
- [ ] JWT authentication working
- [ ] Event-driven delivery creation working
- [ ] Circuit breakers functioning properly
- [ ] All tests passing
- [ ] Postman collection complete and tested
- [ ] Architecture diagrams created
- [ ] API contracts documented
- [ ] Migration decision log written
- [ ] README updated with complete instructions
- [ ] Code reviewed and cleaned up
- [ ] Git repository organized with clear commits
