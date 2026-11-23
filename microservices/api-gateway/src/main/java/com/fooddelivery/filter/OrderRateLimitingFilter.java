package com.fooddelivery.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderRateLimitingFilter implements GlobalFilter, Ordered {

    private static final long WINDOW_MILLIS = 60_000L;

    private final int requestsPerMinute;
    private final Map<String, WindowCounter> requestCounters = new ConcurrentHashMap<>();

    public OrderRateLimitingFilter(
            @Value("${gateway.rate-limit.orders.requests-per-minute:10}") int requestsPerMinute
    ) {
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!path.startsWith("/api/orders")) {
            return chain.filter(exchange);
        }

        String key = resolveClientKey(exchange);
        RateLimitCheck check = checkAndConsume(key, Instant.now().toEpochMilli());

        if (!check.allowed()) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            exchange.getResponse().getHeaders().add("Retry-After", String.valueOf(check.retryAfterSeconds()));
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -90;
    }

    private String resolveClientKey(ServerWebExchange exchange) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId != null && !userId.isBlank()) {
            return "user:" + userId;
        }

        if (exchange.getRequest().getRemoteAddress() != null
                && exchange.getRequest().getRemoteAddress().getAddress() != null) {
            return "ip:" + exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }

        return "anonymous";
    }

    private RateLimitCheck checkAndConsume(String key, long now) {
        WindowCounter counter = requestCounters.computeIfAbsent(key, k -> new WindowCounter(now));

        synchronized (counter) {
            if (now - counter.windowStartMillis >= WINDOW_MILLIS) {
                counter.windowStartMillis = now;
                counter.count.set(0);
            }

            int updatedCount = counter.count.incrementAndGet();
            if (updatedCount <= requestsPerMinute) {
                return new RateLimitCheck(true, 0L);
            }

            long elapsedMillis = now - counter.windowStartMillis;
            long retryAfterSeconds = Math.max(1L, (WINDOW_MILLIS - elapsedMillis + 999L) / 1000L);
            return new RateLimitCheck(false, retryAfterSeconds);
        }
    }

    private static final class WindowCounter {
        private volatile long windowStartMillis;
        private final AtomicInteger count = new AtomicInteger(0);

        private WindowCounter(long windowStartMillis) {
            this.windowStartMillis = windowStartMillis;
        }
    }

    private record RateLimitCheck(boolean allowed, long retryAfterSeconds) {
    }
}
