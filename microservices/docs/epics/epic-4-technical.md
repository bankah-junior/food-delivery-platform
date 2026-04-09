# Epic 4 Technical Report

## Epic
Epic 4: Service Discovery and API Gateway

## Status
Complete

## Completion Summary
- 4.1 Create Eureka Server: Complete (5 of 5)
- 4.2 Register Services with Eureka: Complete (5 of 5)
- 4.3 Create API Gateway: Complete (4 of 4)
- 4.4 Implement Gateway Filters: Complete (4 of 4)
- 4.5 Update Services for Gateway: Complete (4 of 4)
- Overall Epic 4: Complete (22 of 22)

## 4.1 Create Eureka Server

### What Was Implemented
- Created a new `eureka-server` Spring Boot service.
- Added `spring-cloud-starter-netflix-eureka-server` dependency.
- Enabled Eureka Server with `@EnableEurekaServer`.
- Configured `application.yml` with:
	- Port `8761`
	- `register-with-eureka: false`
	- `fetch-registry: false`
	- Local service URL and hostname

### Technical Outcome
- The platform now has a dedicated service registry for discovery-based communication.

## 4.2 Register Services with Eureka

### What Was Implemented
- Added Eureka Client dependency to all four domain services:
	- customer-service
	- restaurant-service
	- order-service
	- delivery-service
- Configured each service with:
	- `spring.application.name`
	- Eureka server URL
	- `prefer-ip-address: true`
- Updated Order Service Feign clients to use discovery names instead of hard-coded URLs.
- Verified service registration through the Eureka dashboard and build validation.

### Key Technical Notes
- Order Service now uses `@FeignClient(name = "customer-service")` and `@FeignClient(name = "restaurant-service")`.
- Load balancing is resolved through service discovery rather than fixed endpoints.

### Technical Outcome
- Services can locate one another dynamically through Eureka.

## 4.3 Create API Gateway

### What Was Implemented
- Created a new `api-gateway` Spring Boot service.
- Added gateway dependencies:
	- `spring-cloud-starter-gateway`
	- `spring-cloud-starter-netflix-eureka-client`
	- `spring-boot-starter-security`
- Added JJWT dependencies for token parsing at the gateway edge.
- Configured `application.yml` with:
	- Port `8080`
	- Eureka client registration
	- Gateway route definitions for all major service paths
- Configured route mappings:
	- `/api/customers/**` -> `lb://customer-service`
	- `/api/restaurants/**` -> `lb://restaurant-service`
	- `/api/orders/**` -> `lb://order-service`
	- `/api/deliveries/**` -> `lb://delivery-service`
	- `/api/auth/**` -> `lb://customer-service`

### Technical Outcome
- The gateway is now the single edge entry point for the platform.

## 4.4 Implement Gateway Filters

### What Was Implemented
- Added a JWT authentication filter at the gateway.
- Added a reusable JWT utility for parsing and validating tokens.
- Added a request rate limiting filter for `/api/orders/**`.
- Added a request/response logging filter for traffic visibility.
- JWT filter behavior:
	- Accepts Authorization Bearer tokens
	- Allows unauthenticated access to `/api/auth/**` and `/api/restaurants/search/**`
	- Extracts `userId`, username, and role from JWT claims
	- Adds gateway-trusted headers:
		- `X-User-Id`
		- `X-User-Username`
		- `X-User-Role`
- Rate limiting behavior:
	- Fixed-window limiter
	- Applies to order routes only
	- Default limit: 10 requests per minute
	- Returns HTTP 429 with `Retry-After`
- Logging behavior:
	- Logs incoming method/path
	- Logs outgoing status and request duration

### Technical Outcome
- Authentication, throttling, and request tracing now live at the gateway edge.

## 4.5 Update Services for Gateway

### What Was Implemented
- Removed service-side JWT validation from customer-service.
- Updated customer-service profile endpoints to read gateway-forwarded identity headers.
- Extended customer JWT issuance so the gateway can forward both numeric user id and username.
- Added CORS configuration to services so requests from the gateway origin are accepted.
- Applied gateway-origin CORS support to:
	- customer-service
	- order-service
	- restaurant-service
	- delivery-service
- Verified that the services now trust gateway headers instead of decoding tokens locally.

### Key Technical Notes
- Customer Service no longer uses `JwtAuthenticationFilter`.
- Gateway forwards both `X-User-Id` and `X-User-Username` because downstream services expect different identity forms.
- CORS is constrained to `http://localhost:8080` for the current local setup.

### Technical Outcome
- Security responsibilities are now split cleanly:
	- the gateway authenticates requests
	- backend services trust forwarded identity headers

## Validation and Verification
- api-gateway tests/build: Passed
- customer-service tests: Passed
- order-service tests: Passed
- restaurant-service tests: Passed
- delivery-service tests: Passed

## Key Files Added/Updated
- eureka-server:
	- `pom.xml`
	- `src/main/java/com/fooddelivery/EurekaServerApplication.java`
	- `src/main/resources/application.yml`
- api-gateway:
	- `pom.xml`
	- `src/main/java/com/fooddelivery/ApiGatewayApplication.java`
	- `src/main/java/com/fooddelivery/config/SecurityConfig.java`
	- `src/main/java/com/fooddelivery/security/JwtUtil.java`
	- `src/main/java/com/fooddelivery/filter/GatewayJwtAuthenticationFilter.java`
	- `src/main/java/com/fooddelivery/filter/OrderRateLimitingFilter.java`
	- `src/main/java/com/fooddelivery/filter/RequestResponseLoggingFilter.java`
	- `src/main/resources/application.yml`
- customer-service:
	- `security/JwtUtil.java`
	- `controller/CustomerController.java`
	- `config/SecurityConfig.java`
	- `service/CustomerService.java`
	- `test/controller/CustomerControllerTest.java`
- order-service, restaurant-service, delivery-service:
	- `config/SecurityConfig.java`

## Outcome
Epic 4 is fully complete and establishes the platform’s discovery and gateway layer. The system now uses Eureka for service lookup, routes traffic through a central gateway, enforces authentication at the edge, and lets backend services rely on gateway-forwarded identity headers.
