package com.fooddelivery.service;

import com.fooddelivery.client.CustomerServiceClient;
import com.fooddelivery.client.RestaurantServiceClient;
import com.fooddelivery.client.dto.RemoteCustomerResponse;
import com.fooddelivery.client.dto.RemoteMenuItemResponse;
import com.fooddelivery.client.dto.RemoteRestaurantResponse;
import com.fooddelivery.dto.OrderItemRequest;
import com.fooddelivery.dto.PlaceOrderRequest;
import com.fooddelivery.exception.ServiceUnavailableException;
import com.fooddelivery.model.Order;
import com.fooddelivery.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceFaultToleranceTest {

    @Test
    void placeOrderFailsGracefullyWhenCustomerServiceIsUnavailable() {
        OrderService orderService = new OrderService(
                null,
                customerId -> {
                    throw new ServiceUnavailableException("Customer service is unavailable for customerId=" + customerId);
                },
                new RestaurantServiceClient() {
                    @Override
                    public RemoteRestaurantResponse getRestaurantById(Long id) {
                        throw new AssertionError("Restaurant service should not be called when customer service fails");
                    }

                    @Override
                    public List<RemoteMenuItemResponse> getMenuByRestaurantId(Long restaurantId) {
                        throw new AssertionError("Restaurant service should not be called when customer service fails");
                    }
                },
                null
        );

        PlaceOrderRequest request = buildRequest();

        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> orderService.placeOrder(42L, request)
        );

        assertEquals("Customer service is unavailable for customerId=42", exception.getMessage());
    }

    @Test
    void placeOrderFailsGracefullyWhenRestaurantServiceIsUnavailable() {
        OrderService orderService = new OrderService(
                null,
                customerId -> {
                    RemoteCustomerResponse response = new RemoteCustomerResponse();
                    response.setId(customerId);
                    response.setDeliveryAddress("Spintex Road");
                    return response;
                },
                new RestaurantServiceClient() {
                    @Override
                    public RemoteRestaurantResponse getRestaurantById(Long id) {
                        throw new ServiceUnavailableException("Restaurant service is unavailable for restaurantId=" + id);
                    }

                    @Override
                    public List<RemoteMenuItemResponse> getMenuByRestaurantId(Long restaurantId) {
                        throw new AssertionError("Menu lookup should not be called when restaurant lookup fails");
                    }
                },
                null
        );

        PlaceOrderRequest request = buildRequest();

        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> orderService.placeOrder(42L, request)
        );

        assertEquals("Restaurant service is unavailable for restaurantId=9", exception.getMessage());
    }

    @Test
    void circuitBreakerOpensAfterRepeatedFailuresAndClosesAfterRecovery() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .permittedNumberOfCallsInHalfOpenState(3)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .build();

        CircuitBreaker circuitBreaker = CircuitBreaker.of("customerService", config);
        Supplier<String> failingSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
            throw new IllegalStateException("temporary outage");
        });

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThrows(IllegalStateException.class, failingSupplier::get);
        }

        assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.getState());

        circuitBreaker.transitionToHalfOpenState();
        AtomicInteger successCounter = new AtomicInteger();
        Supplier<String> successfulSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, () -> "ok-" + successCounter.incrementAndGet());

        assertEquals("ok-1", successfulSupplier.get());
        assertEquals("ok-2", successfulSupplier.get());
        assertEquals("ok-3", successfulSupplier.get());
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    private PlaceOrderRequest buildRequest() {
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setMenuItemId(101L);
        itemRequest.setQuantity(2);

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setRestaurantId(9L);
        request.setItems(List.of(itemRequest));
        request.setDeliveryAddress("East Legon");
        request.setSpecialInstructions("Leave at reception");
        return request;
    }
}
