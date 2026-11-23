package com.fooddelivery.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestResponseLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        String method = exchange.getRequest().getMethod() == null
                ? "UNKNOWN"
                : exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getURI().getPath();

        logger.info("Incoming request: {} {}", method, path);

        return chain.filter(exchange).doFinally(signalType -> {
            long durationMs = System.currentTimeMillis() - start;
            HttpStatusCode statusCode = exchange.getResponse().getStatusCode();
            int status = statusCode == null ? 0 : statusCode.value();
            logger.info("Outgoing response: {} {} -> status={} durationMs={}", method, path, status, durationMs);
        });
    }

    @Override
    public int getOrder() {
        return -10;
    }
}
