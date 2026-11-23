# Epic 2 Technical Report

## Epic
Epic 2: Inter-Service Communication

## Status
Complete

## Completion Summary
- 2.1 Implement OpenFeign Clients: Complete (5 of 5)
- 2.2 Replace Direct Calls with REST Calls: Complete (3 of 3)
- 2.3 Error Handling for Service Calls: Complete (5 of 5)
- Overall Epic 2: Complete (13 of 13)

## 2.1 Implement OpenFeign Clients

### What Was Implemented
- Added Spring Cloud dependency management and OpenFeign starter in order-service Maven configuration.
- Enabled Feign scanning in the Order Service application bootstrap.
- Added Feign client configuration for logging and timeouts.
- Added base URL properties for customer-service and restaurant-service.
- Created CustomerServiceClient with getCustomerById(Long id).
- Created RestaurantServiceClient with:
	- getRestaurantById(Long id)
	- getMenuByRestaurantId(Long restaurantId)
	- getMenuItem(Long restaurantId, Long itemId)
	- validateMenuItems(Long restaurantId, List<Long> itemIds)
- Added remote DTOs used by Feign clients:
	- RemoteCustomerResponse
	- RemoteRestaurantResponse
	- RemoteMenuItemResponse

### Key Technical Notes
- Feign clients currently use configured URLs via environment-aware properties.
- Feign logger level is set to BASIC.
- Timeouts are configured via spring.cloud.openfeign.client.config.default.

## 2.2 Replace Direct Calls with REST Calls

### What Was Implemented
- Updated OrderService.placeOrder flow to use inter-service communication:
	- Validates customer using customer-service.
	- Validates restaurant using restaurant-service.
	- Retrieves and validates menu items via restaurant-service.
	- Calculates item subtotals and total amount from remote menu prices.
	- Uses customer delivery address fallback when request delivery address is empty.
	- Uses restaurant estimated delivery minutes when available.
- Updated Restaurant ownership path to rely on gateway/JWT-propagated user context:
	- Write endpoints require X-User-Id.
	- Ownership checks use the propagated customer identity.
- Removed cross-domain repository dependencies:
	- Verified each service only references its own repositories.

### Key Technical Notes
- Order placement now fails fast on invalid customer, restaurant, unavailable menu items, or invalid menu pricing.
- Ownership enforcement is strict for restaurant write operations.

## 2.3 Error Handling for Service Calls

### What Was Implemented
- Added custom exception classes in order-service:
	- ServiceUnavailableException
	- CustomerNotFoundException
	- RestaurantNotFoundException
	- MenuItemNotFoundException
- Added FeignErrorDecoder in order-service to map Feign failures to domain exceptions.
- Updated Order Service GlobalExceptionHandler mappings:
	- 404 for customer/restaurant/menu item not found cases.
	- 503 for downstream service unavailability.
- Added ServiceUnavailableException and 503 mapping in global handlers for:
	- customer-service
	- restaurant-service
	- delivery-service
- Added explicit downstream outage mapping to HTTP 503 response behavior.

### Key Technical Notes
- Service-call failures now return consistent and meaningful error responses.
- Feign mapping centralizes dependent-service error translation.

## Validation and Verification
- order-service tests: Passed
- customer-service tests: Passed
- restaurant-service tests: Passed
- delivery-service tests: Passed

## Files Updated During Epic 2
- order-service Feign setup, clients, DTOs, and error mapping classes
- order-service order placement service logic
- restaurant-service ownership validation path
- global exception handling updates across order-service, customer-service, restaurant-service, and delivery-service

## Outcome
Epic 2 is complete and provides a stable synchronous inter-service communication baseline for Epic 3 event-driven implementation.

