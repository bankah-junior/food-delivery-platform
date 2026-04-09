# Epic 3 Non-Technical Summary

## Epic
Epic 3: Event-Driven Communication

## Status
Complete

## What This Epic Achieved
This epic moved the platform from direct service-to-service waiting into asynchronous event processing for order and delivery workflows. As a result, order actions are now decoupled from delivery execution and can scale more reliably.

## Sub-Epic 3.1: RabbitMQ Setup

### Completed
- Added RabbitMQ support in Order and Delivery services.
- Configured core messaging infrastructure:
	- Exchange
	- Queues
	- Routing keys
	- Dead-letter queue
- Enabled JSON message conversion for event payloads.

### Business Value
- Established the communication backbone needed for non-blocking service workflows.

## Sub-Epic 3.2: Event DTO Definitions

### Completed
- Defined three event contracts used by the services:
	- OrderPlacedEvent
	- OrderCancelledEvent
	- DeliveryStatusUpdatedEvent

### Business Value
- Ensures consistent and predictable message formats between teams/services.

## Sub-Epic 3.3: Event Publishers in Order Service

### Completed
- Added an Order event publisher component.
- Publishing now happens automatically when:
	- An order is placed
	- An order is cancelled
- Added publish logging for traceability.

### Business Value
- Order service now notifies other services in real time without waiting on them.

## Sub-Epic 3.4: Event Consumers

### Completed
- Delivery service now listens for order events and reacts by:
	- Creating deliveries when orders are placed
	- Cancelling deliveries when orders are cancelled
- Order service now listens for delivery status updates and keeps order status synchronized.
- Added dead-letter handling path for failed event processing.

### Business Value
- Delivery operations are now automated from events.
- Order tracking remains aligned with delivery progress.
- Failure scenarios are safer and easier to investigate.

## Sub-Epic 3.5: Remove Synchronous Delivery Creation

### Completed
- Confirmed delivery creation and cancellation are handled asynchronously through events.
- Added tests that verify listener-driven behavior.
- Verified order flow no longer depends on direct synchronous delivery creation logic.

### Business Value
- Faster order placement responses.
- Better resilience under load because services are less tightly coupled.

## Quality and Validation
- Order service test suite passed.
- Delivery service test suite passed.
- New listener-focused tests passed for async behavior.

## Final Outcome
Epic 3 is complete. The platform now uses event-driven communication for order and delivery interactions, improving decoupling, reliability, and scalability for the next infrastructure-focused epic.

