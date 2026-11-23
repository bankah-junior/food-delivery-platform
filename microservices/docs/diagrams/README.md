# Epic 7.4 Diagrams

## Mermaid Sources
- monolith-architecture.mmd
- microservices-architecture.mmd
- sequence-order-placement.mmd
- sequence-event-driven-delivery.mmd

## Service Dependencies and Communication Patterns
- Order Service depends on Customer Service for customer validation and profile lookup.
- Order Service depends on Restaurant Service for restaurant/menu validation and pricing.
- Order Service and Delivery Service communicate asynchronously through RabbitMQ events.
- API Gateway is the single client entry point and routes requests to domain services.
- Eureka Server provides service discovery for gateway and microservices.
- Each domain service owns its own database and persists independently.

## Exported Artifacts
- exports/monolith-architecture.png
- exports/monolith-architecture.svg
- exports/microservices-architecture.png
- exports/microservices-architecture.svg
- exports/sequence-order-placement.png
- exports/sequence-order-placement.svg
- exports/sequence-event-driven-delivery.png
- exports/sequence-event-driven-delivery.svg
