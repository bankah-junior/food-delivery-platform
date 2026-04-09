package com.fooddelivery.controller;

import com.fooddelivery.dto.DeliveryResponse;
import com.fooddelivery.dto.DeliveryStatusUpdateRequest;
import com.fooddelivery.dto.CreateDeliveryRequest;
import com.fooddelivery.service.DeliveryService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.Matchers.equalTo;

class DeliveryControllerTest {

    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = new DeliveryService(null) {
            @Override
            public DeliveryResponse getById(Long deliveryId) {
                DeliveryResponse response = new DeliveryResponse();
                response.setId(deliveryId);
                response.setStatus("ASSIGNED");
                response.setOrderId(10L);
                response.setDriverName("Carlos Martinez");
                return response;
            }

            @Override
            public DeliveryResponse getByOrderId(Long orderId) {
                DeliveryResponse response = new DeliveryResponse();
                response.setId(23L);
                response.setOrderId(orderId);
                response.setStatus("ASSIGNED");
                return response;
            }

            @Override
            public List<DeliveryResponse> getByStatus(String status) {
                DeliveryResponse response = new DeliveryResponse();
                response.setId(24L);
                response.setStatus(status.toUpperCase());
                response.setOrderId(11L);
                return List.of(response);
            }

            @Override
            public DeliveryResponse updateStatus(Long deliveryId, DeliveryStatusUpdateRequest request) {
                DeliveryResponse response = new DeliveryResponse();
                response.setId(deliveryId);
                response.setStatus(request.getStatus().toUpperCase());
                response.setOrderId(10L);
                return response;
            }

            @Override
            public DeliveryResponse createDeliveryForOrder(CreateDeliveryRequest request) {
                DeliveryResponse response = new DeliveryResponse();
                response.setId(25L);
                response.setOrderId(request.getOrderId());
                response.setPickupAddress(request.getPickupAddress());
                response.setDeliveryAddress(request.getDeliveryAddress());
                response.setStatus("ASSIGNED");
                return response;
            }
        };
        RestAssuredMockMvc.standaloneSetup(new DeliveryController(deliveryService));
    }

    @Test
    @DisplayName("GET /api/deliveries/{id} - Success")
    void getByIdReturnsDelivery() {
        RestAssuredMockMvc.given()
                .when()
                .get("/api/deliveries/{id}", 22)
                .then()
                .statusCode(200)
                .body("id", equalTo(22))
                .body("status", equalTo("ASSIGNED"))
                .body("orderId", equalTo(10));
    }

            @Test
            @DisplayName("GET /api/deliveries/order/{orderId} - Success")
            void getByOrderIdReturnsDelivery() {
            RestAssuredMockMvc.given()
                .when()
                .get("/api/deliveries/order/{orderId}", 11)
                .then()
                .statusCode(200)
                .body("id", equalTo(23))
                .body("orderId", equalTo(11));
            }

            @Test
            @DisplayName("GET /api/deliveries/status/{status} - Success")
            void getByStatusReturnsDeliveries() {
            RestAssuredMockMvc.given()
                .when()
                .get("/api/deliveries/status/{status}", "assigned")
                .then()
                .statusCode(200)
                .body("[0].status", equalTo("ASSIGNED"));
            }

            @Test
            @DisplayName("PATCH /api/deliveries/{id}/status - Success")
            void updateStatusReturnsUpdatedDelivery() {
            String body = """
                {
                  "status": "delivered"
                }
                """;

            RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .patch("/api/deliveries/{id}/status", 22)
                .then()
                .statusCode(200)
                .body("id", equalTo(22))
                .body("status", equalTo("DELIVERED"));
            }

            @Test
            @DisplayName("POST /api/deliveries - Success")
            void createDeliveryReturnsDelivery() {
            String body = """
                {
                  "orderId": 77,
                  "pickupAddress": "Osu",
                  "deliveryAddress": "East Legon"
                }
                """;

            RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/deliveries")
                .then()
                .statusCode(200)
                .body("id", equalTo(25))
                .body("orderId", equalTo(77));
            }
}
