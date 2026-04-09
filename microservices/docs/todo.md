# Food Delivery Platform - Complete TODO List

## Epic 1: Service Decomposition and Database Separation

### 1.1 Create Service Scaffolding
- [ ] Create Customer Service Spring Boot project
  - [ ] Initialize with Spring Initializr (Web, JPA, Security, Actuator)
  - [ ] Set up `application.yml` and `application-docker.yml`
  - [ ] Configure port 8081
  - [ ] Add PostgreSQL dependency
  
- [ ] Create Restaurant Service Spring Boot project
  - [ ] Initialize with Spring Initializr (Web, JPA, Security, Actuator)
  - [ ] Set up `application.yml` and `application-docker.yml`
  - [ ] Configure port 8082
  - [ ] Add PostgreSQL dependency

- [ ] Create Order Service Spring Boot project
  - [ ] Initialize with Spring Initializr (Web, JPA, Security, Actuator, OpenFeign)
  - [ ] Set up `application.yml` and `application-docker.yml`
  - [ ] Configure port 8083
  - [ ] Add PostgreSQL dependency

- [ ] Create Delivery Service Spring Boot project
  - [ ] Initialize with Spring Initializr (Web, JPA, Security, Actuator, AMQP)
  - [ ] Set up `application.yml` and `application-docker.yml`
  - [ ] Configure port 8084
  - [ ] Add PostgreSQL dependency

### 1.2 Extract Customer Service Domain
- [ ] Copy `Customer` entity from monolith
- [ ] Create `CustomerRepository` with required queries
- [ ] Implement `CustomerService` business logic
- [ ] Create `CustomerController` with endpoints:
  - [ ] GET `/api/customers/me`
  - [ ] GET `/api/customers/{id}`
  - [ ] PUT `/api/customers/me`
- [ ] Implement DTOs: `CustomerRequest`, `CustomerResponse`
- [ ] Set up JWT security configuration
- [ ] Implement authentication endpoints:
  - [ ] POST `/api/auth/register`
  - [ ] POST `/api/auth/login`
- [ ] Test Customer Service independently

### 1.3 Extract Restaurant Service Domain
- [ ] Copy `Restaurant` entity (remove customer relationship)
- [ ] Copy `MenuItem` entity
- [ ] Create `RestaurantRepository` with search queries
- [ ] Create `MenuItemRepository`
- [ ] Implement `RestaurantService` business logic
- [ ] Implement `MenuService` business logic
- [ ] Create `RestaurantController` with endpoints:
  - [ ] GET `/api/restaurants/search/all`
  - [ ] GET `/api/restaurants/search/city/{city}`
  - [ ] GET `/api/restaurants/search/cuisine/{type}`
  - [ ] GET `/api/restaurants/{id}`
  - [ ] GET `/api/restaurants/{id}/menu`
  - [ ] POST `/api/restaurants`
  - [ ] POST `/api/restaurants/{id}/menu`
  - [ ] PUT `/api/restaurants/menu/{itemId}`
  - [ ] PATCH `/api/restaurants/menu/{itemId}/toggle`
- [ ] Implement DTOs: `RestaurantRequest`, `RestaurantResponse`, `MenuItemRequest`, `MenuItemResponse`
- [ ] Add validation for restaurant ownership (use customerId from JWT)
- [ ] Test Restaurant Service independently

### 1.4 Extract Order Service Domain
- [ ] Copy `Order` entity (remove customer/restaurant relationships, use IDs only)
- [ ] Copy `OrderItem` entity (remove MenuItem relationship)
- [ ] Create `OrderRepository` with query methods
- [ ] Create `OrderItemRepository`
- [ ] Implement `OrderService` business logic
- [ ] Create `OrderController` with endpoints:
  - [ ] POST `/api/orders`
  - [ ] GET `/api/orders/{id}`
  - [ ] GET `/api/orders/my-orders`
  - [ ] GET `/api/orders/restaurant/{id}`
  - [ ] PATCH `/api/orders/{id}/status`
  - [ ] POST `/api/orders/{id}/cancel`
- [ ] Implement DTOs: `OrderRequest`, `OrderResponse`, `OrderItemRequest`
- [ ] Test Order Service independently (with mocked external calls)

### 1.5 Extract Delivery Service Domain
- [ ] Copy `Delivery` entity (remove order relationship, use orderId only)
- [ ] Create `DeliveryRepository`
- [ ] Implement `DeliveryService` business logic
- [ ] Create `DeliveryController` with endpoints:
  - [ ] GET `/api/deliveries/{id}`
  - [ ] GET `/api/deliveries/order/{orderId}`
  - [ ] GET `/api/deliveries/status/{status}`
  - [ ] PATCH `/api/deliveries/{id}/status`
- [ ] Implement DTOs: `DeliveryResponse`
- [ ] Test Delivery Service independently

### 1.6 Database Setup
- [ ] Create PostgreSQL schema for `customer_db`
- [ ] Create PostgreSQL schema for `restaurant_db`
- [ ] Create PostgreSQL schema for `order_db`
- [ ] Create PostgreSQL schema for `delivery_db`
- [ ] Add Flyway/Liquibase migration scripts for each service
- [ ] Test database migrations

---

## Epic 2: Inter-Service Communication

### 2.1 Implement OpenFeign Clients
- [ ] Add Spring Cloud OpenFeign dependency to Order Service
- [ ] Create `CustomerServiceClient` Feign interface
  - [ ] Add method: `getCustomerById(Long id)`
- [ ] Create `RestaurantServiceClient` Feign interface
  - [ ] Add method: `getRestaurant(Long id)`
  - [ ] Add method: `getMenuItem(Long id)`
  - [ ] Add method: `validateMenuItems(List<Long> itemIds)`
- [ ] Enable Feign clients with `@EnableFeignClients`
- [ ] Configure Feign logging level

### 2.2 Replace Direct Calls with REST Calls
- [ ] Update `OrderService.placeOrder()`:
  - [ ] Call Customer Service to validate customer and get delivery address
  - [ ] Call Restaurant Service to validate menu items and prices
  - [ ] Calculate total amount from API response
  - [ ] Remove direct repository calls
- [ ] Update `RestaurantController` ownership validation:
  - [ ] Use JWT customerId instead of CustomerRepository
- [ ] Remove all cross-domain repository dependencies

### 2.3 Error Handling for Service Calls
- [ ] Implement custom exception classes:
  - [ ] `ServiceUnavailableException`
  - [ ] `CustomerNotFoundException`
  - [ ] `RestaurantNotFoundException`
  - [ ] `MenuItemNotFoundException`
- [ ] Add `@ControllerAdvice` global exception handler in each service
- [ ] Map Feign exceptions to proper HTTP status codes
- [ ] Return meaningful error messages to clients

---

## Epic 3: Event-Driven Communication

### 3.1 RabbitMQ Setup
- [ ] Add Spring Boot AMQP dependency to Order and Delivery services
- [ ] Create RabbitMQ configuration class with:
  - [ ] Topic exchange: `food-delivery-exchange`
  - [ ] Queue: `delivery.order-placed`
  - [ ] Queue: `delivery.order-cancelled`
  - [ ] Queue: `order.delivery-status-updated`
  - [ ] Dead letter queue: `dlq.delivery`
- [ ] Configure RabbitMQ connection properties in `application.yml`
- [ ] Set up message converter (JSON)

### 3.2 Define Event DTOs
- [ ] Create `OrderPlacedEvent`:
  - [ ] Fields: orderId, customerId, restaurantId, items, totalAmount, deliveryAddress
- [ ] Create `OrderCancelledEvent`:
  - [ ] Fields: orderId, reason
- [ ] Create `DeliveryStatusUpdatedEvent`:
  - [ ] Fields: deliveryId, orderId, status, timestamp

### 3.3 Implement Event Publishers
- [ ] In Order Service, create `OrderEventPublisher`:
  - [ ] Method: `publishOrderPlaced(Order order)`
  - [ ] Method: `publishOrderCancelled(Long orderId, String reason)`
- [ ] Update `OrderService.placeOrder()` to publish `OrderPlacedEvent`
- [ ] Update `OrderService.cancelOrder()` to publish `OrderCancelledEvent`
- [ ] Add logging for published events

### 3.4 Implement Event Consumers
- [ ] In Delivery Service, create `OrderEventListener`:
  - [ ] Method: `handleOrderPlaced(OrderPlacedEvent event)` with `@RabbitListener`
  - [ ] Method: `handleOrderCancelled(OrderCancelledEvent event)`
- [ ] Implement automatic delivery creation logic
- [ ] In Order Service, create `DeliveryEventListener`:
  - [ ] Method: `handleDeliveryStatusUpdated(DeliveryStatusUpdatedEvent event)`
- [ ] Update order status based on delivery status
- [ ] Add error handling and dead letter queue processing

### 3.5 Remove Synchronous Delivery Creation
- [ ] Remove `DeliveryService` injection from `OrderService` in monolith
- [ ] Remove `createDeliveryForOrder()` synchronous call
- [ ] Verify order placement returns immediately without blocking
- [ ] Test async delivery creation via events

---

## Epic 4: Service Discovery and API Gateway

### 4.1 Create Eureka Server
- [ ] Initialize Eureka Server Spring Boot project
- [ ] Add `spring-cloud-starter-netflix-eureka-server` dependency
- [ ] Enable Eureka Server with `@EnableEurekaServer`
- [ ] Configure `application.yml`:
  - [ ] Port: 8761
  - [ ] Disable self-registration
  - [ ] Set hostname and service URL
- [ ] Test Eureka dashboard at `http://localhost:8761`

### 4.2 Register Services with Eureka
- [ ] Add Eureka Client dependency to all four microservices
- [ ] Configure Eureka client in each service's `application.yml`:
  - [ ] Set `spring.application.name` (customer-service, restaurant-service, order-service, delivery-service)
  - [ ] Set Eureka server URL
  - [ ] Configure instance metadata
- [ ] Update Feign clients to use service names instead of URLs:
  - [ ] `@FeignClient(name = "customer-service")`
  - [ ] `@FeignClient(name = "restaurant-service")`
- [ ] Enable load balancing with Eureka
- [ ] Test service registration in Eureka dashboard

### 4.3 Create API Gateway
- [ ] Initialize Spring Cloud Gateway project
- [ ] Add dependencies:
  - [ ] `spring-cloud-starter-gateway`
  - [ ] `spring-cloud-starter-netflix-eureka-client`
  - [ ] `spring-boot-starter-security`
- [ ] Configure routing in `application.yml`:
  - [ ] Route `/api/customers/**` → `lb://customer-service`
  - [ ] Route `/api/restaurants/**` → `lb://restaurant-service`
  - [ ] Route `/api/orders/**` → `lb://order-service`
  - [ ] Route `/api/deliveries/**` → `lb://delivery-service`
  - [ ] Route `/api/auth/**` → `lb://customer-service`
- [ ] Set gateway port to 8080
- [ ] Register gateway with Eureka

### 4.4 Implement Gateway Filters
- [ ] Create JWT authentication filter:
  - [ ] Extract token from Authorization header
  - [ ] Validate JWT signature and expiration
  - [ ] Add user information to request headers
  - [ ] Allow unauthenticated paths: `/api/auth/**`, `/api/restaurants/search/**`
- [ ] Create request rate limiting filter:
  - [ ] Apply to `/api/orders/**` (e.g., 10 requests per minute)
- [ ] Add logging filter for request/response tracking
- [ ] Test authentication and routing through gateway

### 4.5 Update Services for Gateway
- [ ] Remove JWT validation from individual services (trust gateway)
- [ ] Read user context from gateway-added headers
- [ ] Update CORS configuration to allow gateway origin
- [ ] Test all endpoints through gateway at `http://localhost:8080`

---

## Epic 5: Fault Tolerance with Resilience4j

### 5.1 Add Resilience4j Dependencies
- [ ] Add `spring-cloud-starter-circuitbreaker-resilience4j` to Order Service
- [ ] Add `resilience4j-spring-boot2` dependency
- [ ] Add `spring-boot-starter-aop` for aspects

### 5.2 Configure Circuit Breakers
- [ ] Create `resilience4j` configuration in `application.yml`:
  - [ ] Configure `customerService` circuit breaker (failure rate threshold: 50%, wait duration: 10s)
  - [ ] Configure `restaurantService` circuit breaker
  - [ ] Set sliding window size and minimum calls
- [ ] Enable circuit breaker endpoints in Actuator

### 5.3 Add Circuit Breakers to Feign Clients
- [ ] Annotate `CustomerServiceClient` methods with `@CircuitBreaker`:
  - [ ] Add fallback method for `getCustomerById()`
- [ ] Annotate `RestaurantServiceClient` methods with `@CircuitBreaker`:
  - [ ] Add fallback method for `getRestaurant()`
  - [ ] Add fallback method for `getMenuItem()`
- [ ] Implement fallback methods with degraded responses

### 5.4 Add Retry Logic
- [ ] Configure retry policy in `application.yml`:
  - [ ] Max retry attempts: 3
  - [ ] Wait duration: 1s with exponential backoff
- [ ] Add `@Retry` annotation to Feign client methods
- [ ] Test retry behavior with temporary service outages

### 5.5 Test Fault Tolerance
- [ ] Verify order placement fails gracefully when Customer Service is down
- [ ] Verify meaningful error messages are returned
- [ ] Verify circuit breaker opens after repeated failures
- [ ] Verify circuit breaker closes after service recovery
- [ ] Check circuit breaker states via Actuator endpoints

---

## Epic 6: Containerization with Docker

### 6.1 Create Dockerfiles
- [ ] Write Dockerfile for Customer Service:
  - [ ] Stage 1: Maven build stage
  - [ ] Stage 2: Runtime stage with JRE
  - [ ] Expose port 8081
  - [ ] Set health check
- [ ] Write Dockerfile for Restaurant Service (port 8082)
- [ ] Write Dockerfile for Order Service (port 8083)
- [ ] Write Dockerfile for Delivery Service (port 8084)
- [ ] Write Dockerfile for Eureka Server (port 8761)
- [ ] Write Dockerfile for API Gateway (port 8080)
- [ ] Create `.dockerignore` files for each service

### 6.2 Create Docker-Specific Configuration
- [ ] Create `application-docker.yml` for Customer Service:
  - [ ] Database URL: `jdbc:postgresql://postgres:5432/customer_db`
  - [ ] Eureka URL: `http://eureka-server:8761/eureka`
  - [ ] RabbitMQ host: `rabbitmq`
- [ ] Create `application-docker.yml` for Restaurant Service
- [ ] Create `application-docker.yml` for Order Service
- [ ] Create `application-docker.yml` for Delivery Service
- [ ] Create `application-docker.yml` for Eureka Server
- [ ] Create `application-docker.yml` for API Gateway
- [ ] Use environment variables for sensitive values

### 6.3 Create Docker Compose File
- [ ] Create root `docker-compose.yml` with services:
  - [ ] PostgreSQL (with 4 databases)
  - [ ] RabbitMQ (with management plugin)
  - [ ] Eureka Server
  - [ ] Customer Service
  - [ ] Restaurant Service
  - [ ] Order Service
  - [ ] Delivery Service
  - [ ] API Gateway
- [ ] Configure service dependencies with `depends_on`
- [ ] Add health checks for each service
- [ ] Set proper startup order
- [ ] Configure volumes for PostgreSQL persistence
- [ ] Expose necessary ports to host
- [ ] Set up networks for service communication

### 6.4 Test Docker Setup
- [ ] Build all Docker images
- [ ] Run `docker compose up` and verify all services start
- [ ] Check Eureka dashboard shows all registered services
- [ ] Verify health checks pass
- [ ] Test API calls through gateway
- [ ] Check RabbitMQ management console
- [ ] Verify database connections
- [ ] Test complete order flow in containerized environment

---

## Epic 7: Testing and Documentation

### 7.1 Create Postman Collection
- [ ] Set up environment variables (base_url, token)
- [ ] Create folder: Authentication
  - [ ] POST Register customer
  - [ ] POST Login (save token to environment)
- [ ] Create folder: Customers
  - [ ] GET My profile
  - [ ] GET Customer by ID
  - [ ] PUT Update profile
- [ ] Create folder: Restaurants
  - [ ] GET All restaurants
  - [ ] GET Search by city
  - [ ] GET Search by cuisine
  - [ ] GET Restaurant details
  - [ ] GET Restaurant menu
  - [ ] POST Create restaurant
  - [ ] POST Add menu item
  - [ ] PUT Update menu item
  - [ ] PATCH Toggle item availability
- [ ] Create folder: Orders
  - [ ] POST Place order
  - [ ] GET Order by ID
  - [ ] GET My orders
  - [ ] GET Restaurant orders
  - [ ] PATCH Update order status
  - [ ] POST Cancel order
- [ ] Create folder: Deliveries
  - [ ] GET Delivery by ID
  - [ ] GET Delivery by order
  - [ ] GET Deliveries by status
  - [ ] PATCH Update delivery status
- [ ] Add pre-request scripts for token management
- [ ] Add tests for status codes and response validation

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
