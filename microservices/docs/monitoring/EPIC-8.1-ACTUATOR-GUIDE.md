# Epic 8.1 - Actuator Monitoring Documentation

## Overview

This document describes the Actuator monitoring endpoints standardized across all microservices in the Food Delivery Platform.

## Actuator Endpoints

All services expose the following Actuator endpoints:

| Endpoint | Purpose | Example |
|----------|---------|---------|
| `/actuator/health` | Service health status | `http://localhost:8081/actuator/health` |
| `/actuator/health/db` | Database connection status | `http://localhost:8081/actuator/health/db` |
| `/actuator/health/custom` | Custom health indicators | `http://localhost:8081/actuator/health/custom` |
| `/actuator/info` | Service metadata | `http://localhost:8081/actuator/info` |
| `/actuator/metrics` | Available metrics | `http://localhost:8081/actuator/metrics` |
| `/actuator/prometheus` | Prometheus-format metrics | `http://localhost:8081/actuator/prometheus` |

### Order Service Specific Endpoints

Order Service (port 8083) additionally exposes circuit breaker metrics:

| Endpoint | Purpose |
|----------|---------|
| `/actuator/circuitbreakers` | All circuit breaker instances |
| `/actuator/circuitbreakerevents` | Circuit breaker state transitions |

### Delivery Service Specific Endpoints

Delivery Service (port 8084) additionally exposes:

| Endpoint | Purpose |
|----------|---------|
| `/actuator/health/rabbitmq` | RabbitMQ connection status |

## Service Ports and Health Checks

| Service | Port | Health URL | Description |
|---------|------|-----------|-------------|
| API Gateway | 8080 | `http://localhost:8080/actuator/health` | Gateway entry point |
| Customer Service | 8081 | `http://localhost:8081/actuator/health` | Auth and customer domain |
| Restaurant Service | 8082 | `http://localhost:8082/actuator/health` | Restaurant domain |
| Order Service | 8083 | `http://localhost:8083/actuator/health` | Order domain with circuit breakers |
| Delivery Service | 8084 | `http://localhost:8084/actuator/health` | Delivery domain with RabbitMQ |
| Eureka Server | 8761 | `http://localhost:8761/actuator/health` | Service registry |

## Custom Health Indicators

### 1. Database Health Indicator

**Package:** `com.amalitech.[service].health.DatabaseHealthIndicator`

Indicates the status of PostgreSQL database connectivity by attempting to acquire and release a connection from the connection pool.

**Response (UP):**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "status": "Database connection successful"
      }
    }
  }
}
```

**Response (DOWN):**
```json
{
  "status": "DOWN",
  "components": {
    "db": {
      "status": "DOWN",
      "details": {
        "error": "Connection could not be acquired"
      }
    }
  }
}
```

### 2. RabbitMQ Health Indicator

**Package:** `com.amalitech.[service].health.RabbitMQHealthIndicator`

Available in Order Service and Delivery Service only. Verifies the connection to RabbitMQ broker.

**Response (UP):**
```json
{
  "status": "UP",
  "components": {
    "rabbitmq": {
      "status": "UP",
      "details": {
        "status": "RabbitMQ connection successful"
      }
    }
  }
}
```

## Example Health Check Requests

### Using curl to check service health

```bash
# Check Customer Service health
curl http://localhost:8081/actuator/health

# Check with detailed health info
curl http://localhost:8081/actuator/health?details=when-unauthorized

# Check specific component (database)
curl http://localhost:8081/actuator/health/db

# Check Order Service including circuit breakers
curl http://localhost:8083/actuator/health
curl http://localhost:8083/actuator/circuitbreakers
curl http://localhost:8083/actuator/circuitbreakerevents

# Check Delivery Service including RabbitMQ
curl http://localhost:8084/actuator/health
curl http://localhost:8084/actuator/health/rabbitmq
```

### Using Newman to monitor during tests

```bash
# Run Postman collection and monitor health endpoints in parallel
newman run food-delivery-microservices.postman_collection.json \
  -e food-delivery-local.postman_environment.json \
  --reporters cli,json
```

## Metrics Collection

All services expose standard Spring Boot metrics including:

- **JVM Metrics:** Memory, GC, threads, class loading
- **HTTP Metrics:** Request count, latency, error rates
- **Database Metrics:** Connection pool usage, query times
- **Custom Metrics:** Business metrics (orders placed, etc.)

### Accessing Prometheus Metrics

```bash
# Get all metrics in Prometheus format
curl http://localhost:8081/actuator/prometheus | grep -E '^(jvm|process|http)'
```

## Health Endpoint Configuration

All services include the following Actuator configuration:

```yaml
management:
  endpoint:
    health:
      show-details: always
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,circuitbreakers,circuitbreakerevents
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

## Monitoring in Docker

When running with Docker Compose, health checks are configured as:

```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -fsS http://localhost:PORT/actuator/health | grep -q UP"]
  interval: 15s
  timeout: 5s
  retries: 10
```

This ensures containers are marked as healthy only when the `/actuator/health` endpoint returns UP status.

## Integration with Monitoring Tools

### Prometheus Integration

The `/actuator/prometheus` endpoint provides metrics in Prometheus format. Configure Prometheus to scrape metrics:

```yaml
# prometheus.yml
scrape_configs:
  - job_name: 'food-delivery'
    static_configs:
      - targets:
        - 'localhost:8081'  # customer-service
        - 'localhost:8082'  # restaurant-service
        - 'localhost:8083'  # order-service
        - 'localhost:8084'  # delivery-service
        - 'localhost:8080'  # api-gateway
        - 'localhost:8761'  # eureka-server
    metrics_path: '/actuator/prometheus'
```

### Grafana Dashboards

Grafana can visualize Prometheus data collected from the actuator endpoints. Import or create dashboards focusing on:

- Service health status
- Request latency percentiles (p50, p95, p99)
- Error rates and circuit breaker states
- Database connection pool usage
- JVM memory and GC metrics

## Troubleshooting

### Service health is DOWN despite service running

1. Check database connectivity: `curl http://localhost:8081/actuator/health/db`
2. Verify database is accessible and credentials are correct
3. Check service logs for connection pool exhaustion

### RabbitMQ health is DOWN

1. Verify RabbitMQ is running: `docker ps | grep rabbitmq`
2. Check RabbitMQ logs: `docker logs rabbitmq`
3. Verify connection configuration in `application.yml`

### Metrics endpoint returns empty

1. Ensure metrics endpoint is included in exposure: `include: health,info,metrics,prometheus`
2. Allow time for metrics collection to begin (usually < 1 minute)
3. Check that `management.metrics.export.prometheus.enabled` is not set to false

## Next Steps

- Epic 8.2: Add distributed tracing support with Spring Cloud Sleuth
- Epic 8.3: Performance testing and load testing using JMeter or Gatling
