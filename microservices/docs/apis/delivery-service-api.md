# Delivery Service API

Base path: /api/deliveries  
Service port: 8084 (behind gateway 8080)

## Authentication Requirements
- Public/internal routing policy depends on gateway setup.
- In normal user flow, requests should pass through gateway.

## Endpoints

### GET /api/deliveries/{id}
- Description: Get delivery by id.
- Path params: id (Long)
- Success: 200 OK with DeliveryResponse

### GET /api/deliveries/order/{orderId}
- Description: Get delivery by order id.
- Path params: orderId (Long)
- Success: 200 OK with DeliveryResponse

### GET /api/deliveries/status/{status}
- Description: Get deliveries by delivery status.
- Path params: status (String)
- Success: 200 OK with array of DeliveryResponse

### PATCH /api/deliveries/{id}/status
- Description: Update delivery status.
- Path params: id (Long)
- Request body: DeliveryStatusUpdateRequest
- Success: 200 OK with DeliveryResponse

### POST /api/deliveries
- Description: Create delivery for order (internal/operational endpoint).
- Request body: CreateDeliveryRequest
- Success: 200 OK with DeliveryResponse

## Key DTOs
- CreateDeliveryRequest: orderId, pickupAddress, deliveryAddress
- DeliveryStatusUpdateRequest: status
- DeliveryResponse: id, status, orderId, driverName, driverPhone, pickupAddress, deliveryAddress, assignedAt, pickedUpAt, deliveredAt, createdAt
