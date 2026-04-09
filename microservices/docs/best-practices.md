# Food Delivery Platform - Best Practices

## Table of Contents
1. [Microservices Design Principles](#microservices-design-principles)
2. [Spring Boot Best Practices](#spring-boot-best-practices)
3. [Database Per Service Pattern](#database-per-service-pattern)
4. [API Design and REST](#api-design-and-rest)
5. [Inter-Service Communication](#inter-service-communication)
6. [Event-Driven Architecture](#event-driven-architecture)
7. [Service Discovery and API Gateway](#service-discovery-and-api-gateway)
8. [Security Best Practices](#security-best-practices)
9. [Fault Tolerance and Resilience](#fault-tolerance-and-resilience)
10. [Docker and Containerization](#docker-and-containerization)
11. [Testing Strategies](#testing-strategies)
12. [Logging and Monitoring](#logging-and-monitoring)
13. [Code Organization](#code-organization)

---

## Microservices Design Principles

### 1. Single Responsibility
Each microservice should have **one clear business capability**.

✅ **DO:**
```
Customer Service → Manages customer data and authentication
Restaurant Service → Manages restaurants and menus
Order Service → Handles order lifecycle
Delivery Service → Tracks deliveries
```

❌ **DON'T:**
```
CustomerOrderService → Handles both customers and orders (too coupled)
```

### 2. Loose Coupling
Services should be independent with minimal dependencies.

✅ **DO:**
- Use IDs to reference data in other services
- Communicate via REST APIs or events
- Each service owns its data

❌ **DON'T:**
- Share databases between services
- Use direct repository calls across services
- Create tight entity relationships with JPA `@ManyToOne`

### 3. High Cohesion
Related functionality should be grouped together.

✅ **DO:**
- Keep Restaurant and MenuItem together (they're tightly related)
- Order and OrderItem in the same service

❌ **DON'T:**
- Split MenuItem into a separate service (creates chattiness)

### 4. Bounded Context (Domain-Driven Design)
Define clear boundaries based on business domains.

✅ **DO:**
- Customer context: User identity, authentication, profile
- Restaurant context: Restaurant info, menu, pricing
- Order context: Order placement, order status
- Delivery context: Delivery assignment, tracking

### 5. API-First Design
Design APIs before implementation.

✅ **DO:**
- Document endpoints with OpenAPI/Swagger
- Define request/response DTOs clearly
- Version your APIs (`/api/v1/customers`)

---

## Spring Boot Best Practices

### 1. Project Structure
Organize code by feature/domain, not by layer.

✅ **DO:**
```
customer-service/
├── src/main/java/com/fooddelivery/customer/
│   ├── CustomerApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── JwtConfig.java
│   ├── controller/
│   │   ├── CustomerController.java
│   │   └── AuthController.java
│   ├── service/
│   │   └── CustomerService.java
│   ├── repository/
│   │   └── CustomerRepository.java
│   ├── model/
│   │   └── Customer.java
│   ├── dto/
│   │   ├── CustomerRequest.java
│   │   └── CustomerResponse.java
│   └── exception/
│       ├── CustomerNotFoundException.java
│       └── GlobalExceptionHandler.java
```

### 2. Configuration Management
Use environment-specific configurations.

✅ **DO:**
```yaml
# application.yml (default)
spring:
  application:
    name: customer-service
  profiles:
    active: ${SPRING_PROFILE:dev}

# application-dev.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/customer_db

# application-docker.yml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/customer_db
```

### 3. Use DTOs for API Contracts
Never expose entities directly in REST APIs.

✅ **DO:**
```java
public class CustomerResponse {
    private Long id;
    private String firstName;
    private String email;
    // Only fields needed by clients
}
```

❌ **DON'T:**
```java
@GetMapping("/{id}")
public Customer getCustomer(@PathVariable Long id) {
    return customerRepository.findById(id); // Exposes internal structure
}
```

### 4. Exception Handling
Use `@ControllerAdvice` for centralized exception handling.

✅ **DO:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(CustomerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }
    
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(ServiceUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse("Dependent service unavailable, please try again"));
    }
}
```

### 5. Validation
Use Bean Validation annotations.

✅ **DO:**
```java
public class CustomerRequest {
    @NotBlank(message = "Username is required")
    private String username;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}

@PostMapping("/register")
public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CustomerRequest request) {
    // ...
}
```

---

## Database Per Service Pattern

### 1. Separate Databases
Each microservice must have its own database.

✅ **DO:**
```
customer-service → customer_db
restaurant-service → restaurant_db
order-service → order_db
delivery-service → delivery_db
```

### 2. No Shared Tables
Never query another service's database directly.

❌ **DON'T:**
```java
// In OrderService - BAD!
@Autowired
private CustomerRepository customerRepository; // From another service's DB
```

✅ **DO:**
```java
// In OrderService - GOOD!
@Autowired
private CustomerServiceClient customerServiceClient; // REST call
```

### 3. Data Duplication is Acceptable
Store necessary data locally for performance.

✅ **DO:**
```java
// In Order Service
public class Order {
    private Long customerId;
    private String customerName; // Cached for display
    private String deliveryAddress; // Cached locally
}
```

Update cached data via events when the source changes.

### 4. Use Database Migration Tools
Version control your database schema.

✅ **DO:**
```xml
<!-- Flyway dependency -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

```sql
-- V1__create_customer_table.sql
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## API Design and REST

### 1. RESTful Naming Conventions
Use nouns, not verbs, in URLs.

✅ **DO:**
```
GET    /api/customers           (list customers)
GET    /api/customers/{id}      (get one customer)
POST   /api/customers           (create customer)
PUT    /api/customers/{id}      (update customer)
DELETE /api/customers/{id}      (delete customer)
```

❌ **DON'T:**
```
POST /api/getCustomer
POST /api/createCustomer
```

### 2. Use HTTP Status Codes Correctly
Return appropriate status codes.

✅ **DO:**
```java
- 200 OK: Successful GET, PUT, PATCH
- 201 Created: Successful POST
- 204 No Content: Successful DELETE
- 400 Bad Request: Validation error
- 401 Unauthorized: Missing/invalid authentication
- 403 Forbidden: Authenticated but not allowed
- 404 Not Found: Resource doesn't exist
- 409 Conflict: Business logic violation
- 500 Internal Server Error: Unexpected server error
- 503 Service Unavailable: Dependent service down
```

### 3. API Versioning
Version your APIs from the start.

✅ **DO:**
```java
@RequestMapping("/api/v1/customers")
public class CustomerController {
    // ...
}
```

### 4. Pagination for List Endpoints
Always paginate large result sets.

✅ **DO:**
```java
@GetMapping
public ResponseEntity<Page<CustomerResponse>> getCustomers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Customer> customers = customerRepository.findAll(pageable);
    return ResponseEntity.ok(customers.map(this::toResponse));
}
```

### 5. Consistent Error Response Format
Use a standard error structure.

✅ **DO:**
```java
public class ErrorResponse {
    private String message;
    private String errorCode;
    private LocalDateTime timestamp;
    private List<String> details;
}
```

---

## Inter-Service Communication

### 1. Use OpenFeign for Synchronous REST Calls
Declarative REST clients are cleaner than RestTemplate.

✅ **DO:**
```java
@FeignClient(name = "customer-service")
public interface CustomerServiceClient {
    
    @GetMapping("/api/customers/{id}")
    CustomerResponse getCustomer(@PathVariable Long id);
}
```

### 2. Configure Timeouts
Always set timeouts to prevent hanging requests.

✅ **DO:**
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
```

### 3. Use Logical Service Names with Eureka
Let service discovery handle routing.

✅ **DO:**
```java
@FeignClient(name = "restaurant-service") // Logical name from Eureka
```

❌ **DON'T:**
```java
@FeignClient(url = "http://localhost:8082") // Hardcoded URL
```

### 4. Handle Partial Failures Gracefully
One service failure shouldn't break the entire flow.

✅ **DO:**
```java
@CircuitBreaker(name = "customerService", fallbackMethod = "getCustomerFallback")
public CustomerResponse getCustomer(Long id) {
    return customerServiceClient.getCustomer(id);
}

private CustomerResponse getCustomerFallback(Long id, Exception ex) {
    return new CustomerResponse(id, "Unknown Customer");
}
```

---

## Event-Driven Architecture

### 1. Use Events for Async Operations
Don't block the response for non-critical operations.

✅ **DO:**
```java
// In OrderService
public Order placeOrder(OrderRequest request) {
    Order order = createOrder(request);
    orderRepository.save(order);
    
    // Publish event asynchronously
    orderEventPublisher.publishOrderPlaced(order);
    
    return order; // Return immediately
}

// In DeliveryService (separate service)
@RabbitListener(queues = "delivery.order-placed")
public void handleOrderPlaced(OrderPlacedEvent event) {
    createDeliveryForOrder(event);
}
```

### 2. Use Topic Exchanges for Multiple Consumers
Allow multiple services to react to the same event.

✅ **DO:**
```java
@Bean
public TopicExchange foodDeliveryExchange() {
    return new TopicExchange("food-delivery-exchange");
}

// Delivery Service subscribes
@Bean
public Binding orderPlacedBinding() {
    return BindingBuilder
        .bind(orderPlacedQueue())
        .to(foodDeliveryExchange())
        .with("order.placed");
}

// Notification Service can also subscribe
@Bean
public Binding orderPlacedNotificationBinding() {
    return BindingBuilder
        .bind(notificationQueue())
        .to(foodDeliveryExchange())
        .with("order.placed");
}
```

### 3. Implement Dead Letter Queues
Handle message processing failures.

✅ **DO:**
```java
@Bean
public Queue orderPlacedQueue() {
    return QueueBuilder.durable("delivery.order-placed")
        .withArgument("x-dead-letter-exchange", "dlx")
        .withArgument("x-dead-letter-routing-key", "delivery.order-placed.dlq")
        .build();
}
```

### 4. Make Events Self-Contained
Include all necessary data in the event payload.

✅ **DO:**
```java
public class OrderPlacedEvent {
    private Long orderId;
    private Long customerId;
    private Long restaurantId;
    private String deliveryAddress; // Include needed data
    private List<OrderItemDto> items;
    private BigDecimal totalAmount;
    private LocalDateTime orderTime;
}
```

❌ **DON'T:**
```java
public class OrderPlacedEvent {
    private Long orderId; // Not enough - receiver must make REST calls
}
```

### 5. Use Idempotent Event Handlers
Messages might be delivered multiple times.

✅ **DO:**
```java
@RabbitListener(queues = "delivery.order-placed")
public void handleOrderPlaced(OrderPlacedEvent event) {
    // Check if already processed
    if (deliveryRepository.existsByOrderId(event.getOrderId())) {
        log.info("Delivery already created for order {}, skipping", event.getOrderId());
        return;
    }
    
    createDelivery(event);
}
```

---

## Service Discovery and API Gateway

### 1. Eureka Server Configuration
Keep Eureka Server simple and standalone.

✅ **DO:**
```yaml
# Eureka Server application.yml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

### 2. Service Registration
Configure health checks and metadata.

✅ **DO:**
```yaml
# Customer Service application.yml
spring:
  application:
    name: customer-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
```

### 3. API Gateway Routing
Use path-based routing with load balancing.

✅ **DO:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: customer-service
          uri: lb://customer-service
          predicates:
            - Path=/api/customers/**, /api/auth/**
        - id: restaurant-service
          uri: lb://restaurant-service
          predicates:
            - Path=/api/restaurants/**
```

### 4. Gateway Filters
Handle cross-cutting concerns at the gateway.

✅ **DO:**
```java
@Component
public class JwtAuthenticationFilter implements GatewayFilter {
    
    @Override
    public Mono<ServerHttpResponse> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        // Allow public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }
        
        // Validate JWT
        String token = extractToken(exchange.getRequest());
        if (token == null || !jwtValidator.validate(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // Add user info to headers for downstream services
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
            .header("X-User-Id", getUserIdFromToken(token))
            .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
}
```

### 5. Rate Limiting at Gateway
Protect your services from overload.

✅ **DO:**
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

---

## Security Best Practices

### 1. Centralize Authentication at Gateway
Don't validate JWT in every microservice.

✅ **DO:**
- API Gateway validates JWT
- Gateway adds user context to request headers
- Microservices trust requests from gateway

### 2. Use Strong JWT Configuration
Configure JWT properly.

✅ **DO:**
```java
@Bean
public JwtDecoder jwtDecoder() {
    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
    return NimbusJwtDecoder.withSecretKey(key).build();
}

public String generateToken(String username, Long userId) {
    return Jwts.builder()
        .setSubject(username)
        .claim("userId", userId)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
}
```

### 3. Never Hardcode Secrets
Use environment variables for sensitive data.

✅ **DO:**
```yaml
jwt:
  secret: ${JWT_SECRET}
  
spring:
  datasource:
    password: ${DB_PASSWORD}
```

❌ **DON'T:**
```yaml
jwt:
  secret: mySecretKey123
```

### 4. Use HTTPS in Production
Always encrypt traffic.

✅ **DO:**
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_PASSWORD}
```

### 5. Implement Role-Based Access Control
Check permissions for sensitive operations.

✅ **DO:**
```java
@PreAuthorize("hasRole('RESTAURANT_OWNER')")
@PostMapping("/{id}/menu")
public ResponseEntity<MenuItemResponse> addMenuItem(
    @PathVariable Long id,
    @RequestBody MenuItemRequest request
) {
    // Only restaurant owners can add menu items
}
```

---

## Fault Tolerance and Resilience

### 1. Circuit Breaker Pattern
Prevent cascading failures.

✅ **DO:**
```yaml
resilience4j:
  circuitbreaker:
    instances:
      customerService:
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10000
        sliding-window-size: 10
        minimum-number-of-calls: 5
```

```java
@CircuitBreaker(name = "customerService", fallbackMethod = "getCustomerFallback")
public CustomerResponse getCustomer(Long id) {
    return customerServiceClient.getCustomer(id);
}

private CustomerResponse getCustomerFallback(Long id, Exception ex) {
    log.warn("Customer service unavailable, using fallback for customer {}", id);
    return new CustomerResponse(id, "Customer info unavailable");
}
```

### 2. Retry with Exponential Backoff
Retry transient failures.

✅ **DO:**
```yaml
resilience4j:
  retry:
    instances:
      restaurantService:
        max-attempts: 3
        wait-duration: 1000
        exponential-backoff-multiplier: 2
```

```java
@Retry(name = "restaurantService")
public RestaurantResponse getRestaurant(Long id) {
    return restaurantServiceClient.getRestaurant(id);
}
```

### 3. Timeouts
Don't wait forever for responses.

✅ **DO:**
```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
```

### 4. Bulkhead Pattern
Isolate thread pools to prevent resource exhaustion.

✅ **DO:**
```yaml
resilience4j:
  bulkhead:
    instances:
      customerService:
        max-concurrent-calls: 10
        max-wait-duration: 1000
```

### 5. Health Checks
Monitor service health.

✅ **DO:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics
  endpoint:
    health:
      show-details: always
```

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            return Health.up().withDetail("database", "Reachable").build();
        } catch (SQLException ex) {
            return Health.down(ex).build();
        }
    }
}
```

---

## Docker and Containerization

### 1. Multi-Stage Dockerfiles
Reduce image size and build time.

✅ **DO:**
```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
HEALTHCHECK --interval=30s --timeout=3s \
  CMD curl -f http://localhost:8081/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Use .dockerignore
Exclude unnecessary files from Docker context.

✅ **DO:**
```
# .dockerignore
target/
.git/
.gitignore
*.md
.mvn/
.idea/
*.iml
```

### 3. Environment-Specific Configuration
Use Spring profiles for different environments.

✅ **DO:**
```dockerfile
ENV SPRING_PROFILES_ACTIVE=docker
```

```yaml
# application-docker.yml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/customer_db
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  rabbitmq:
    host: rabbitmq
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
```

### 4. Docker Compose for Local Development
Define all services in one file.

✅ **DO:**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"

  eureka-server:
    build: ./eureka-server
    ports:
      - "8761:8761"
    environment:
      SPRING_PROFILES_ACTIVE: docker

  customer-service:
    build: ./customer-service
    ports:
      - "8081:8081"
    depends_on:
      postgres:
        condition: service_healthy
      eureka-server:
        condition: service_started
    environment:
      SPRING_PROFILES_ACTIVE: docker
      DB_USER: postgres
      DB_PASSWORD: postgres

volumes:
  postgres-data:
```

### 5. Health Checks and Depends_on
Ensure proper startup order.

✅ **DO:**
```yaml
depends_on:
  postgres:
    condition: service_healthy
  eureka-server:
    condition: service_started
```

---

## Testing Strategies

### 1. Unit Tests
Test business logic in isolation.

✅ **DO:**
```java
@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    
    @Mock
    private CustomerRepository customerRepository;
    
    @InjectMocks
    private CustomerService customerService;
    
    @Test
    void shouldCreateCustomer() {
        CustomerRequest request = new CustomerRequest("john", "john@example.com");
        Customer customer = new Customer();
        
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        
        CustomerResponse response = customerService.createCustomer(request);
        
        assertNotNull(response);
        verify(customerRepository).save(any(Customer.class));
    }
}
```

### 2. Integration Tests
Test with real database.

✅ **DO:**
```java
@SpringBootTest
@AutoConfigureTestDatabase
@Testcontainers
class CustomerRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb");
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Test
    void shouldSaveAndFindCustomer() {
        Customer customer = new Customer();
        customer.setUsername("john");
        customer.setEmail("john@example.com");
        
        Customer saved = customerRepository.save(customer);
        Optional<Customer> found = customerRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals("john", found.get().getUsername());
    }
}
```

### 3. API Tests
Test REST endpoints.

✅ **DO:**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class CustomerControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldReturnCustomer() throws Exception {
        mockMvc.perform(get("/api/customers/1")
                .header("Authorization", "Bearer " + getTestToken()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.username").exists());
    }
}
```

### 4. Contract Tests
Verify inter-service communication.

✅ **DO:**
```java
@SpringBootTest
@AutoConfigureWireMock
class CustomerServiceClientTest {
    
    @Autowired
    private CustomerServiceClient customerServiceClient;
    
    @Test
    void shouldGetCustomerFromService() {
        stubFor(get(urlEqualTo("/api/customers/1"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"id\":1,\"username\":\"john\"}")));
        
        CustomerResponse response = customerServiceClient.getCustomer(1L);
        
        assertEquals(1L, response.getId());
        assertEquals("john", response.getUsername());
    }
}
```

### 5. End-to-End Tests
Test complete flows with Postman or automated scripts.

✅ **DO:**
- Use Postman collections with pre-request scripts
- Test happy path and error scenarios
- Automate with Newman (Postman CLI)

---

## Logging and Monitoring

### 1. Structured Logging
Use consistent log format.

✅ **DO:**
```java
@Slf4j
@Service
public class OrderService {
    
    public Order placeOrder(OrderRequest request) {
        log.info("Placing order for customer {} at restaurant {}", 
            request.getCustomerId(), request.getRestaurantId());
        
        try {
            Order order = createOrder(request);
            log.info("Order {} created successfully", order.getId());
            return order;
        } catch (Exception ex) {
            log.error("Failed to place order for customer {}: {}", 
                request.getCustomerId(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
```

### 2. Correlation IDs
Track requests across services.

✅ **DO:**
```java
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        
        MDC.put("correlationId", correlationId);
        response.setHeader("X-Correlation-ID", correlationId);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

### 3. Actuator Endpoints
Expose metrics and health checks.

✅ **DO:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### 4. Application Metrics
Track business metrics.

✅ **DO:**
```java
@Service
public class OrderService {
    
    private final MeterRegistry meterRegistry;
    
    public Order placeOrder(OrderRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Order order = createOrder(request);
            meterRegistry.counter("orders.placed", "status", "success").increment();
            return order;
        } catch (Exception ex) {
            meterRegistry.counter("orders.placed", "status", "failure").increment();
            throw ex;
        } finally {
            sample.stop(meterRegistry.timer("orders.placement.duration"));
        }
    }
}
```

---

## Code Organization

### 1. Package Structure
```
com.fooddelivery.customer/
├── CustomerApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   └── SwaggerConfig.java
├── controller/
│   ├── CustomerController.java
│   └── AuthController.java
├── service/
│   └── CustomerService.java
├── repository/
│   └── CustomerRepository.java
├── model/
│   └── Customer.java
├── dto/
│   ├── request/
│   │   └── CustomerRequest.java
│   └── response/
│       └── CustomerResponse.java
├── exception/
│   ├── CustomerNotFoundException.java
│   └── GlobalExceptionHandler.java
└── util/
    └── JwtUtil.java
```

### 2. Naming Conventions
- **Controllers:** `CustomerController`, `OrderController`
- **Services:** `CustomerService`, `OrderService`
- **Repositories:** `CustomerRepository`, `OrderRepository`
- **Entities:** `Customer`, `Order`, `MenuItem`
- **DTOs:** `CustomerRequest`, `CustomerResponse`
- **Exceptions:** `CustomerNotFoundException`, `ServiceUnavailableException`

### 3. Code Quality
- Use **Lombok** to reduce boilerplate
- Follow **SOLID** principles
- Keep methods small and focused
- Write meaningful variable and method names
- Add JavaDoc for public APIs

### 4. Git Commit Messages
Use conventional commit format.

✅ **DO:**
```
feat: add customer registration endpoint
fix: resolve race condition in order placement
refactor: extract menu validation to separate method
docs: update API documentation for restaurant service
test: add unit tests for delivery service
```

---

## Summary Checklist

✅ Each microservice has a single, clear responsibility  
✅ Services communicate via REST APIs or events, not direct DB access  
✅ Each service has its own database  
✅ Use DTOs for API contracts, never expose entities  
✅ Implement circuit breakers on all inter-service calls  
✅ Use events for asynchronous, non-blocking operations  
✅ Centralize authentication at the API Gateway  
✅ All services register with Eureka  
✅ Use multi-stage Dockerfiles for efficient builds  
✅ Implement comprehensive error handling  
✅ Add health checks and monitoring  
✅ Write tests at all levels (unit, integration, API)  
✅ Use structured logging with correlation IDs  
✅ Never hardcode secrets, use environment variables  
✅ Document your APIs with OpenAPI/Swagger  

---

**Remember:** These are guidelines, not absolute rules. Adapt them based on your specific requirements and constraints.
