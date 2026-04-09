package com.fooddelivery.controller;

import com.fooddelivery.dto.OrderResponse;
import com.fooddelivery.dto.OrderStatusUpdateRequest;
import com.fooddelivery.dto.PlaceOrderRequest;
import com.fooddelivery.service.OrderService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

class OrderControllerTest {

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(null, null, null) {
            @Override
            public OrderResponse getOrderById(Long orderId) {
                OrderResponse response = new OrderResponse();
                response.setId(orderId);
                response.setStatus("PLACED");
                response.setTotalAmount(new BigDecimal("42.50"));
                response.setCustomerId(3L);
                response.setRestaurantId(9L);
                return response;
            }

            @Override
            public OrderResponse placeOrder(Long customerId, PlaceOrderRequest request) {
                OrderResponse response = new OrderResponse();
                response.setId(30L);
                response.setStatus("PLACED");
                response.setCustomerId(customerId);
                response.setRestaurantId(request.getRestaurantId());
                response.setDeliveryAddress(request.getDeliveryAddress());
                response.setTotalAmount(new BigDecimal("0.00"));
                return response;
            }

            @Override
            public List<OrderResponse> getCustomerOrders(Long customerId) {
                OrderResponse response = new OrderResponse();
                response.setId(31L);
                response.setCustomerId(customerId);
                response.setStatus("CONFIRMED");
                return List.of(response);
            }

            @Override
            public List<OrderResponse> getRestaurantOrders(Long restaurantId) {
                OrderResponse response = new OrderResponse();
                response.setId(32L);
                response.setRestaurantId(restaurantId);
                response.setStatus("PREPARING");
                return List.of(response);
            }

            @Override
            public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
                OrderResponse response = new OrderResponse();
                response.setId(orderId);
                response.setStatus(request.getStatus().toUpperCase());
                return response;
            }

            @Override
            public OrderResponse cancelOrder(Long orderId, Long customerId) {
                OrderResponse response = new OrderResponse();
                response.setId(orderId);
                response.setStatus("CANCELLED");
                response.setCustomerId(customerId);
                return response;
            }
        };
        RestAssuredMockMvc.standaloneSetup(new OrderController(orderService));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Success")
    void getByIdReturnsOrder() {
        RestAssuredMockMvc.given()
                .when()
                .get("/api/orders/{id}", 10)
                .then()
                .statusCode(200)
                .body("id", equalTo(10))
                .body("status", equalTo("PLACED"))
                .body("customerId", equalTo(3));
    }

            @Test
            @DisplayName("POST /api/orders - Success")
            void placeOrderReturnsCreated() {
            String body = """
                {
                  "restaurantId": 9,
                  "items": [{"menuItemId": 101, "quantity": 2}],
                  "deliveryAddress": "East Legon"
                }
                """;

            RestAssuredMockMvc.given()
                .header("X-User-Id", 3)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(201)
                .body("id", equalTo(30))
                .body("customerId", equalTo(3))
                .body("restaurantId", equalTo(9));
            }

            @Test
            @DisplayName("GET /api/orders/my-orders - Success")
            void getMyOrdersReturnsOrders() {
            RestAssuredMockMvc.given()
                .header("X-User-Id", 3)
                .when()
                .get("/api/orders/my-orders")
                .then()
                .statusCode(200)
                .body("[0].customerId", equalTo(3));
            }

            @Test
            @DisplayName("GET /api/orders/restaurant/{id} - Success")
            void getRestaurantOrdersReturnsOrders() {
            RestAssuredMockMvc.given()
                .when()
                .get("/api/orders/restaurant/{id}", 9)
                .then()
                .statusCode(200)
                .body("[0].restaurantId", equalTo(9));
            }

            @Test
            @DisplayName("PATCH /api/orders/{id}/status - Success")
            void updateStatusReturnsUpdatedOrder() {
            String body = """
                {
                  "status": "delivered"
                }
                """;

            RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .patch("/api/orders/{id}/status", 10)
                .then()
                .statusCode(200)
                .body("id", equalTo(10))
                .body("status", equalTo("DELIVERED"));
            }

            @Test
            @DisplayName("POST /api/orders/{id}/cancel - Success")
            void cancelOrderReturnsCancelledOrder() {
            RestAssuredMockMvc.given()
                .header("X-User-Id", 3)
                .when()
                .post("/api/orders/{id}/cancel", 10)
                .then()
                .statusCode(200)
                .body("id", equalTo(10))
                .body("status", equalTo("CANCELLED"));
            }
}
