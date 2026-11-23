# Epic 1 Technical Notes

## Scope
Epic 1 established the service boundaries for the microservices refactor and removed the remaining monolith coupling from the domain layer.

## 1.1 Service Scaffolding
Completed four standalone Spring Boot services under `microservices/`:
- [customer-service](../../customer-service)
- [restaurant-service](../../restaurant-service)
- [order-service](../../order-service)
- [delivery-service](../../delivery-service)

Each service now has its own `pom.xml`, Spring Boot entry point, `application.yml`, and `application-docker.yml`.

## 1.2 Customer Service Extraction
The Customer domain was separated into [customer-service](../../customer-service) with:
- `Customer` entity and `CustomerRepository`
- `CustomerService` for register/login/profile operations
- `AuthController` and `CustomerController`
- JWT support via `JwtUtil` and `JwtAuthenticationFilter`
- Customer-specific DTOs and exception handling

Monolith-only coupling was removed:
- Deleted the `Customer.orders` cross-domain relationship
- Removed `orderCount` from `CustomerResponse`
- Added a dedicated `CustomerUpdateRequest` for profile updates

## 1.3 Restaurant Service Extraction
The Restaurant domain was separated into [restaurant-service](../../restaurant-service) with:
- `Restaurant` entity using `ownerId` instead of a `Customer` relationship
- `MenuItem` entity and repositories
- `RestaurantService` for restaurant and menu operations
- `RestaurantController` for search, create, update, and menu endpoints
- DTOs, exception handling, and a lightweight standalone security config

Domain boundary changes:
- Removed the direct `Customer` entity reference from Restaurant
- Replaced owner validation with `ownerId`-based checks
- Kept restaurant and menu data in the same service boundary

## 1.4 Order Service Extraction
The Order domain was separated into [order-service](../../order-service) with:
- `Order` entity storing `customerId` and `restaurantId`
- `OrderItem` entity storing `menuItemId`
- `OrderRepository` and `OrderItemRepository`
- `OrderService` and `OrderController`
- Order-specific request/response DTOs and exception handling

Domain boundary changes:
- Removed all direct references to `Customer`, `Restaurant`, and `MenuItem` entities
- Replaced embedded customer/restaurant data with IDs
- Replaced string-based order status updates with `OrderStatusUpdateRequest`

## 1.5 Delivery Service Extraction
The Delivery domain was separated into [delivery-service](../../delivery-service) with:
- `Delivery` entity storing `orderId`
- `DeliveryRepository`
- `DeliveryService` and `DeliveryController`
- Delivery DTOs and exception handling

Domain boundary changes:
- Removed the direct `Order` relationship from Delivery
- Replaced status and creation endpoints with explicit request DTOs
- Added `CreateDeliveryRequest` and `DeliveryStatusUpdateRequest`

## 1.6 Database Setup and Migrations
Each service now uses PostgreSQL and Flyway:
- `customer_db` for Customer Service
- `restaurant_db` for Restaurant Service
- `order_db` for Order Service
- `delivery_db` for Delivery Service

Implemented Flyway baseline migrations:
- `V1__create_customers_table.sql`
- `V1__create_restaurants_and_menu_items.sql`
- `V1__create_orders_and_order_items.sql`
- `V1__create_deliveries_table.sql`

JPA is configured to `validate` against the schema instead of generating it.

## Verification
All four services compile successfully after extraction and migration setup.

## Result
Epic 1 is complete. The codebase now has four isolated service boundaries, separate database ownership, and explicit API contracts that are ready for inter-service communication work in Epic 2.
