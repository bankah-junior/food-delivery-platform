# Epic 4 Non-Technical Summary

## Epic
Epic 4: Service Discovery and API Gateway

## Status
Complete

## What This Epic Achieved
This epic added the infrastructure that lets the platform find services automatically and route all external traffic through one gateway. It also moved authentication and request controls to the edge so the backend services can focus on business logic.

## Sub-Epic 4.1: Eureka Server Setup

### Completed
- Created a central service registry.
- Registered the registry itself on a fixed port for local development.
- Disabled self-registration so the registry stays stable.

### Business Value
- Services can now discover each other dynamically instead of depending on hard-coded addresses.

## Sub-Epic 4.2: Register Services with Eureka

### Completed
- Connected customer-service, restaurant-service, order-service, and delivery-service to the registry.
- Switched order-service communication to use service names instead of direct URLs.
- Confirmed the services appear in the registry when they start.

### Business Value
- Makes the platform easier to scale and move around because services can be located automatically.

## Sub-Epic 4.3: Create API Gateway

### Completed
- Added a single gateway entry point for the platform.
- Routed customer, restaurant, order, delivery, and auth traffic through that gateway.
- Set the gateway to run on port `8080`.
- Registered the gateway with service discovery.

### Business Value
- External clients now talk to one URL instead of calling each service directly.
- This simplifies the public API surface and makes future changes safer.

## Sub-Epic 4.4: Implement Gateway Filters

### Completed
- Added authentication checks at the gateway.
- Added request rate limiting for order traffic.
- Added logging so requests and responses can be traced more easily.
- Allowed anonymous access only where it is intentionally needed, such as login and restaurant search.

### Business Value
- Security and traffic control are handled in one place.
- The system is less exposed to abuse and easier to monitor.

## Sub-Epic 4.5: Update Services for Gateway

### Completed
- Removed local JWT validation from customer-service.
- Updated services to trust the user information added by the gateway.
- Adjusted CORS rules so the gateway can call backend services cleanly.
- Verified the endpoints still work through the gateway.

### Business Value
- Reduces duplicated security logic in each service.
- Keeps identity handling consistent across the platform.
- Makes the backend simpler and easier to maintain.

## Quality and Validation
- All affected services were rebuilt and tested successfully.
- The gateway builds successfully and routes requests through Eureka.
- Customer profile endpoints were updated and verified against the new gateway header flow.

## Final Outcome
Epic 4 is complete. The platform now has service discovery, a centralized gateway, edge-based authentication, request throttling, and logging, which creates a stronger base for resilience features in the next epic.
