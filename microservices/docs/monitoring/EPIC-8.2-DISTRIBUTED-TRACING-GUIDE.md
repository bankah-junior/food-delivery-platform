# Epic 8.2 - Distributed Tracing Guide

## Overview

Distributed tracing has been enabled across all microservices using Spring Boot 3's tracing stack.

Note: In Spring Boot 3, Spring Cloud Sleuth is replaced by Micrometer Tracing.
This project uses:

- `io.micrometer:micrometer-tracing-bridge-brave`
- `io.zipkin.reporter2:zipkin-reporter-brave`

## Services Covered

Tracing is enabled in all runtime services:

- API Gateway (8080)
- Customer Service (8081)
- Restaurant Service (8082)
- Order Service (8083)
- Delivery Service (8084)
- Eureka Server (8761)

## Configuration Applied

Each service has:

```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: ${ZIPKIN_ENDPOINT:http://localhost:9411/api/v2/spans}

logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```

Docker profiles use:

```yaml
management:
  zipkin:
    tracing:
      endpoint: ${ZIPKIN_ENDPOINT:http://zipkin:9411/api/v2/spans}
```

## Docker Compose Zipkin

An optional Zipkin service is available in `docker-compose.yml`:

- Container: `zipkin`
- Port: `9411`
- UI: `http://localhost:9411`
- Ingestion endpoint: `http://zipkin:9411/api/v2/spans`

## Verifying Tracing

1. Start the system:

```bash
docker compose up -d
```

2. Call APIs through gateway to generate traces:

```bash
curl http://localhost:8080/api/restaurants/search/all
curl http://localhost:8080/api/orders/my-orders
```

3. Open Zipkin:

```text
http://localhost:9411
```

4. In Zipkin UI:

- Select a service (for example `api-gateway`)
- Click `Run Query`
- Open a trace to inspect parent-child spans across service boundaries

## Log Correlation

Service logs now include correlation data:

```text
INFO [order-service,0f7aa2e4d85f0b13,6e9f9b2d1c01d6c1]
```

Format:

- `service-name`
- `traceId`
- `spanId`

Use this to follow a single request across gateway and downstream services.

## Troubleshooting

- No traces in Zipkin:
  - Confirm `zipkin` container is running.
  - Confirm `ZIPKIN_ENDPOINT` resolves inside Docker network.
  - Ensure requests are hitting instrumented HTTP endpoints.
- Trace ID missing in logs:
  - Confirm tracing dependencies are present in the service POM.
  - Confirm the custom `logging.pattern.level` is loaded from active profile.
- Low trace volume:
  - Verify `management.tracing.sampling.probability` is not below `1.0` for local testing.
