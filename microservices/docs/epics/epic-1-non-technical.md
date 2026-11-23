# Epic 1 Non-Technical Summary

## What This Epic Achieved
This epic broke the original monolith into four separate services so each part of the food delivery platform can evolve and run independently.

## 1.1 Service Scaffolding
We created the four service projects that will eventually replace the monolith:
- Customer Service
- Restaurant Service
- Order Service
- Delivery Service

Each service now has its own Spring Boot project structure and can be built on its own.

## 1.2 Customer Service
The customer features were moved into their own service. That means registration, login, and profile management now live in one place instead of being mixed in with the rest of the application.

We also removed a hidden dependency on order data so the customer service no longer relies on information from another service.

## 1.3 Restaurant Service
Restaurant management was moved into a separate service. This includes restaurant details, menu items, searches, and menu updates.

The service now uses a simple owner ID instead of directly linking to customer records. That makes it easier to separate responsibilities between services.

## 1.4 Order Service
Order handling now lives in its own service. It stores only the IDs it needs for customers, restaurants, and menu items, instead of depending on those other parts of the system directly.

This is an important step because it means orders can be managed without tightly connecting them to customer or restaurant code.

## 1.5 Delivery Service
Delivery tracking was moved into a dedicated service. Instead of directly linking deliveries to order objects, the service now uses the order ID.

That keeps delivery concerns separate and makes the service easier to scale and maintain on its own.

## 1.6 Database Setup
Each service now has its own PostgreSQL database setup and migration scripts.

This is the database-per-service approach, which means:
- customer data stays in the customer database
- restaurant data stays in the restaurant database
- order data stays in the order database
- delivery data stays in the delivery database

## Outcome
Epic 1 is complete. The platform now has a clean foundation for microservices, with services separated by responsibility and by database.

The next step is to make the services talk to each other in a controlled way instead of through direct code links.
