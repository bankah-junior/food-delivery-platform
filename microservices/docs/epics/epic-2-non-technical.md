# Epic 2 Non-Technical Summary

## Epic
Epic 2: Inter-Service Communication

## Status
Complete

## What This Epic Achieved
This epic made the services talk to each other safely using API calls, instead of relying on tightly coupled internal data access. It also added clearer error handling so failures from dependent services are returned in understandable and consistent ways.

## Sub-Epic 2.1: OpenFeign Setup

### Completed
- Connected the Order Service to Customer Service and Restaurant Service through OpenFeign clients.
- Added the required configuration so service calls have controlled timeout behavior.
- Added request and response models needed for clean service-to-service communication.

### Business Value
- Order Service can now request customer and restaurant information directly from the owning services.
- This reduces hidden coupling and prepares the platform for scaling and service discovery.

## Sub-Epic 2.2: Replace Direct Dependencies With API Calls

### Completed
- Order placement now validates customer, restaurant, and menu items through service APIs.
- Price calculation for orders now uses restaurant menu data returned by API calls.
- Restaurant ownership checks for write actions now rely on gateway-provided user identity headers.
- Cross-domain repository usage was removed and verified.

### Business Value
- Data ownership boundaries are now enforced.
- Order totals are calculated from the source-of-truth restaurant data.
- Authorization behavior is aligned with gateway-driven identity propagation.

## Sub-Epic 2.3: Error Handling and Feign Exception Mapping

### Completed
- Added custom service-call exception types.
- Added Feign error decoding to convert external call failures into business-aware errors.
- Added and aligned global error handling across all services.
- Downstream service outages are now mapped to HTTP 503 responses with meaningful messages.

### Business Value
- API consumers receive consistent and actionable error responses.
- Operational failures are easier to diagnose and less confusing for clients.

## Quality and Validation
- Tests were executed and passing for:
	- customer-service
	- restaurant-service
	- order-service
	- delivery-service

## Final Outcome
Epic 2 is complete. The platform now has a reliable synchronous communication layer with clear ownership boundaries and production-ready error handling behavior.

