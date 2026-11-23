# Restaurant Service API

Base path: /api/restaurants  
Service port: 8082 (behind gateway 8080)

## Authentication Requirements
- Search/read endpoints are readable through gateway routing policy.
- Write endpoints require X-User-Id header (restaurant owner identity from gateway context).

## Endpoints

### GET /api/restaurants/search/all
- Description: List active restaurants.
- Success: 200 OK with array of RestaurantResponse

### GET /api/restaurants/search/city/{city}
- Description: Search restaurants by city.
- Path params: city (String)
- Success: 200 OK with array of RestaurantResponse

### GET /api/restaurants/search/cuisine/{type}
- Description: Search restaurants by cuisine type.
- Path params: type (String)
- Success: 200 OK with array of RestaurantResponse

### GET /api/restaurants/{id}
- Description: Get restaurant details.
- Path params: id (Long)
- Success: 200 OK with RestaurantResponse

### GET /api/restaurants/{id}/menu
- Description: Get menu items for a restaurant.
- Path params: id (Long)
- Success: 200 OK with array of MenuItemResponse

### POST /api/restaurants
- Description: Create restaurant.
- Required header: X-User-Id
- Request body: RestaurantRequest
- Success: 201 Created with RestaurantResponse

### POST /api/restaurants/{restaurantId}/menu
- Description: Add menu item.
- Required header: X-User-Id
- Path params: restaurantId (Long)
- Request body: MenuItemRequest
- Success: 201 Created with MenuItemResponse

### PUT /api/restaurants/menu/{itemId}
- Description: Update menu item.
- Required header: X-User-Id
- Path params: itemId (Long)
- Request body: MenuItemRequest
- Success: 200 OK with MenuItemResponse

### PATCH /api/restaurants/menu/{itemId}/toggle
- Description: Toggle menu item availability.
- Required header: X-User-Id
- Path params: itemId (Long)
- Success: 204 No Content

## Key DTOs
- RestaurantRequest: name, description, cuisineType, address, city, phone, estimatedDeliveryMinutes
- RestaurantResponse: id, name, description, cuisineType, address, city, phone, active, rating, estimatedDeliveryMinutes, menuItemCount, ownerId
- MenuItemRequest: name, description, price, category, imageUrl
- MenuItemResponse: id, name, description, price, category, available, imageUrl, restaurantId, restaurantName
