# Customer Service API

Base path: /api/auth, /api/customers  
Service port: 8081 (behind gateway 8080)

## Authentication Requirements
- /api/auth/register and /api/auth/login are public.
- /api/customers/me requires X-User-Username header from gateway context.
- /api/customers/{id} and /api/customers/me are expected to be gateway-protected.

## Endpoints

### POST /api/auth/register
- Description: Register a new customer account.
- Request body: RegisterRequest
- Success: 201 Created with AuthResponse

### POST /api/auth/login
- Description: Authenticate customer and issue JWT.
- Request body: AuthRequest
- Success: 200 OK with AuthResponse

### GET /api/customers/me
- Description: Get profile for authenticated user.
- Required header: X-User-Username
- Success: 200 OK with CustomerResponse

### GET /api/customers/{id}
- Description: Get customer profile by id.
- Path params: id (Long)
- Success: 200 OK with CustomerResponse

### PUT /api/customers/me
- Description: Update profile for authenticated user.
- Required header: X-User-Username
- Request body: CustomerUpdateRequest
- Success: 200 OK with CustomerResponse

## Key DTOs
- AuthRequest: username, password
- RegisterRequest: username, email, password, firstName, lastName, phone, deliveryAddress, city
- AuthResponse: token, customerId, username, role
- CustomerUpdateRequest: firstName, lastName, phone, deliveryAddress, city
- CustomerResponse: id, username, email, firstName, lastName, phone, deliveryAddress, city, role, createdAt
