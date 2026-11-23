# API Documentation Index

## Service API Guides
- customer-service-api.md
- restaurant-service-api.md
- order-service-api.md
- delivery-service-api.md

## OpenAPI Specifications
- specs/customer-service-openapi.yaml
- specs/restaurant-service-openapi.yaml
- specs/order-service-openapi.yaml
- specs/delivery-service-openapi.yaml

## Swagger UI Usage
- Option 1: Open https://editor.swagger.io and import any YAML file from specs/.
- Option 2: Use Docker Swagger UI locally:
  - docker run --rm -p 9000:8080 -e SWAGGER_JSON=/spec.yaml -v ${PWD}/microservices/docs/apis/specs/customer-service-openapi.yaml:/spec.yaml swaggerapi/swagger-ui
  - Then browse http://localhost:9000

## Authentication Notes
- In production flow, clients call through API Gateway on port 8080.
- Gateway validates JWT and forwards user context headers.
- Service endpoints that require user context expect one or more of:
  - X-User-Id
  - X-User-Username
- Auth endpoints are provided by customer-service under /api/auth.
