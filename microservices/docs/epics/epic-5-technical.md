# Epic 5 Technical Report

## Epic
Epic 5: Fault Tolerance with Resilience4j

## Status
Complete

## Completion Summary
- 5.1 Add Resilience4j Dependencies: Complete (3 of 3)
- 5.2 Configure Circuit Breakers: Complete (4 of 4)
- 5.3 Add Circuit Breakers to Feign Clients: Complete (3 of 3)
- 5.4 Add Retry Logic: Complete (3 of 3)
- 5.5 Test Fault Tolerance: Complete (5 of 5)
- Overall Epic 5: Complete (18 of 18)

## 5.1 Add Resilience4j Dependencies

### What Was Implemented
- Added fault-tolerance dependencies to order-service:
	- spring-cloud-starter-circuitbreaker-resilience4j
	- resilience4j-spring-boot3
	- resilience4j-retry
	- spring-boot-starter-aop

### Technical Outcome
- The order-service now has the required runtime modules for circuit breaker and retry behavior.

## 5.2 Configure Circuit Breakers

### What Was Implemented
- Added circuit breaker configuration in order-service application.yml.
- Defined a shared default circuit breaker profile:
	- failureRateThreshold: 50
	- waitDurationInOpenState: 10s
	- slidingWindowType: COUNT_BASED
	- slidingWindowSize: 10
	- minimumNumberOfCalls: 5
	- permittedNumberOfCallsInHalfOpenState: 3
	- registerHealthIndicator: true
- Added named instances:
	- customerService
	- restaurantService
- Enabled relevant actuator endpoint exposure for resilience monitoring.

### Technical Outcome
- Circuit breaker behavior is centrally configured and available for downstream call protection.

## 5.3 Add Circuit Breakers to Feign Clients

### What Was Implemented
- Enabled OpenFeign circuit breaker integration in order-service configuration.
- Wired fallback factories into Feign clients:
	- CustomerServiceClient -> CustomerServiceClientFallbackFactory
	- RestaurantServiceClient -> RestaurantServiceClientFallbackFactory
- Implemented fallback behaviors that convert downstream failures into ServiceUnavailableException with contextual messages.

### Key Technical Notes
- Customer and restaurant outage paths now degrade consistently via fallback factories.
- Fallbacks preserve the existing global error handling path that maps ServiceUnavailableException to HTTP 503.

### Technical Outcome
- Inter-service failures are now isolated and mapped through controlled fallback behavior instead of raw transport failures.

## 5.4 Add Retry Logic

### What Was Implemented
- Added retry configuration in order-service application.yml:
	- maxAttempts: 3
	- waitDuration: 1s
	- enableExponentialBackoff: true
	- exponentialBackoffMultiplier: 2
- Added named retry instances:
	- customerService
	- restaurantService
- Annotated downstream Feign methods with retry behavior:
	- CustomerServiceClient.getCustomerById
	- RestaurantServiceClient.getRestaurantById
	- RestaurantServiceClient.getMenuByRestaurantId

### Technical Outcome
- Temporary downstream issues now get retried automatically before fallback failure handling is applied.

## 5.5 Test Fault Tolerance

### What Was Implemented
- Added focused fault tolerance tests in order-service:
	- OrderServiceFaultToleranceTest:
		- verifies graceful failure when customer-service is unavailable
		- verifies graceful failure when restaurant-service is unavailable
		- verifies circuit breaker open -> half-open -> closed transitions after recovery
	- GlobalExceptionHandlerTest:
		- verifies ServiceUnavailableException maps to HTTP 503 with meaningful message
- Executed full order-service test suite after resilience changes.

### Technical Outcome
- Fault tolerance behavior has direct test coverage for outage handling and circuit breaker state behavior.

## Validation and Verification
- order-service tests: Passed
- Existing order-service controller/listener tests: Passed after resilience additions
- New fault-tolerance-focused tests: Passed

## Key Files Added/Updated
- order-service:
	- pom.xml
	- src/main/resources/application.yml
	- src/main/java/com/fooddelivery/client/CustomerServiceClient.java
	- src/main/java/com/fooddelivery/client/RestaurantServiceClient.java
	- src/main/java/com/fooddelivery/client/CustomerServiceClientFallbackFactory.java
	- src/main/java/com/fooddelivery/client/RestaurantServiceClientFallbackFactory.java
	- src/test/java/com/fooddelivery/service/OrderServiceFaultToleranceTest.java
	- src/test/java/com/fooddelivery/exception/GlobalExceptionHandlerTest.java

## Outcome
Epic 5 is fully complete and introduces practical resilience controls for downstream communication in order-service. Circuit breakers, retries, and fallbacks are now configured, integrated, and validated through tests.
