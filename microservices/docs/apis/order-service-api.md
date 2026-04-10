# Order Service API

Base path: /api/orders  
Service port: 8083 (behind gateway 8080)

## Authentication Requirements
- Order operations use X-User-Id context from gateway.
- In controller, X-User-Id is marked optional for some operations, but expected in gateway flow.

## Endpoints

### POST /api/orders
- Description: Place order.
- Header: X-User-Id (customer id)
- Request body: PlaceOrderRequest
- Success: 201 Created with OrderResponse

### GET /api/orders/{id}
- Description: Get order by id.
- Path params: id (Long)
- Success: 200 OK with OrderResponse

### GET /api/orders/my-orders
- Description: Get current customer order history.
- Header: X-User-Id
- Success: 200 OK with array of OrderResponse

### GET /api/orders/restaurant/{restaurantId}
- Description: Get orders for a restaurant.
- Path params: restaurantId (Long)
- Success: 200 OK with array of OrderResponse

### PATCH /api/orders/{id}/status
- Description: Update order status.
- Path params: id (Long)
- Request body: OrderStatusUpdateRequest
- Success: 200 OK with OrderResponse

### POST /api/orders/{id}/cancel
- Description: Cancel order.
- Header: X-User-Id
- Path params: id (Long)
- Success: 200 OK with OrderResponse

## Key DTOs
- PlaceOrderRequest: restaurantId, items[], deliveryAddress, specialInstructions
- OrderItemRequest: menuItemId, quantity, specialInstructions
- OrderStatusUpdateRequest: status
- OrderResponse: id, status, totalAmount, deliveryFee, deliveryAddress, specialInstructions, createdAt, estimatedDeliveryTime, customerId, restaurantId, items[]
