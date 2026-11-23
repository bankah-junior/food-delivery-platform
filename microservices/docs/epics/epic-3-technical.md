# Epic 3 Technical Report

## Epic
Epic 3: Event-Driven Communication

## Status
Complete

## Completion Summary
- 3.1 RabbitMQ Setup: Complete (5 of 5)
- 3.2 Define Event DTOs: Complete (3 of 3)
- 3.3 Implement Event Publishers: Complete (3 of 3)
- 3.4 Implement Event Consumers: Complete (3 of 3)
- 3.5 Remove Synchronous Delivery Creation: Complete (3 of 3)
- Overall Epic 3: Complete (14 of 14)

## 3.1 RabbitMQ Setup

### What Was Implemented
- Added Spring AMQP dependency to:
	- order-service
	- delivery-service
- Added RabbitMQ connection properties in service application.yml files:
	- host, port, username, password (env-aware)
- Added RabbitMqConfig in both services with:
	- Topic exchange: food-delivery-exchange
	- Queues:
		- delivery.order-placed
		- delivery.order-cancelled
		- order.delivery-status-updated
		- dlq.delivery
	- Routing keys:
		- order.placed
		- order.cancelled
		- delivery.status.updated
		- delivery.dead-letter
	- Queue bindings to exchange
	- JSON message converter (Jackson2JsonMessageConverter)

### Technical Outcome
- Messaging infrastructure is ready for asynchronous producer/consumer flow.

## 3.2 Define Event DTOs

### What Was Implemented
- Created OrderPlacedEvent in order-service and delivery-service with fields:
	- orderId, customerId, restaurantId, items, totalAmount, deliveryAddress
- Created OrderCancelledEvent in order-service and delivery-service with fields:
	- orderId, reason
- Created DeliveryStatusUpdatedEvent in order-service and delivery-service with fields:
	- deliveryId, orderId, status, timestamp

### Technical Outcome
- Services exchange strongly typed event payloads without cross-service class dependencies.

## 3.3 Implement Event Publishers

### What Was Implemented
- Added OrderEventPublisher in order-service with methods:
	- publishOrderPlaced(Order order)
	- publishOrderCancelled(Long orderId, String reason)
- Wired publishing into OrderService:
	- placeOrder publishes OrderPlacedEvent after persisting order
	- cancelOrder publishes OrderCancelledEvent after status change persistence
- Added structured logs for publish success paths.

### Technical Outcome
- Order lifecycle changes emit asynchronous domain events through RabbitMQ.

## 3.4 Implement Event Consumers

### What Was Implemented
- Added OrderEventListener in delivery-service:
	- @RabbitListener for delivery.order-placed to auto-create delivery
	- @RabbitListener for delivery.order-cancelled to cancel delivery by orderId
- Added DeliveryEventListener in order-service:
	- @RabbitListener for order.delivery-status-updated to sync order status
- Added status synchronization mapping in order-service:
	- ASSIGNED -> READY_FOR_PICKUP
	- PICKED_UP, IN_TRANSIT -> OUT_FOR_DELIVERY
	- DELIVERED -> DELIVERED
	- FAILED -> CANCELLED
- Added error handling and dead-letter publish paths in both listeners to dlq.delivery.

### Technical Outcome
- Producer-consumer event loop is operational between Order and Delivery services.

## 3.5 Remove Synchronous Delivery Creation

### What Was Implemented
- Order flow no longer performs direct synchronous delivery creation calls.
- Delivery creation is triggered asynchronously from OrderPlacedEvent consumption.
- Cancellation flow is event-driven via OrderCancelledEvent.
- Added async listener behavior verification tests:
	- order-service: DeliveryEventListenerTest
	- delivery-service: OrderEventListenerTest
- Confirmed no synchronous delivery client usage remains in order-service source.

### Technical Outcome
- Order placement returns without blocking on delivery creation.
- Delivery lifecycle is now event-driven and decoupled.

## Validation and Verification
- order-service tests: Passed
- delivery-service tests: Passed
- Listener-focused tests added and passing for event consumption and delegation behavior.

## Key Files Added/Updated
- order-service:
	- config/RabbitMqConfig.java
	- event/*Event.java DTOs
	- service/OrderEventPublisher.java
	- service/DeliveryEventListener.java
	- service/OrderService.java
	- test/service/DeliveryEventListenerTest.java
- delivery-service:
	- config/RabbitMqConfig.java
	- event/*Event.java DTOs
	- service/OrderEventListener.java
	- service/DeliveryService.java
	- test/service/OrderEventListenerTest.java

## Outcome
Epic 3 is fully complete and establishes asynchronous event-driven communication between Order and Delivery domains, preparing the platform for service discovery and gateway integration in Epic 4.

