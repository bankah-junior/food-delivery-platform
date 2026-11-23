package com.fooddelivery.controller;

import com.fooddelivery.dto.AuthResponse;
import com.fooddelivery.dto.AuthRequest;
import com.fooddelivery.service.CustomerService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;

class AuthControllerTest {

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(null, null, null) {
            @Override
            public AuthResponse register(com.fooddelivery.dto.RegisterRequest request) {
                return new AuthResponse("token-value", 1L, "john", "CUSTOMER");
            }

            @Override
            public AuthResponse login(AuthRequest request) {
                return new AuthResponse("login-token", 1L, "john", "CUSTOMER");
            }
        };
        RestAssuredMockMvc.standaloneSetup(new AuthController(customerService));
    }

    @Test
    @DisplayName("POST /api/auth/register - Success")
    void registerReturnsCreatedResponse() {
        String body = """
                {
                  "username": "john",
                  "email": "john@example.com",
                  "password": "password123"
                }
                """;

        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .body("customerId", equalTo(1))
                .body("username", equalTo("john"));
    }

        @Test
        @DisplayName("POST /api/auth/login - Success")
        void loginReturnsOkResponse() {
                String body = """
                                {
                                    "username": "john",
                                    "password": "password123"
                                }
                                """;

                RestAssuredMockMvc.given()
                                .contentType(ContentType.JSON)
                                .body(body)
                                .when()
                                .post("/api/auth/login")
                                .then()
                                .statusCode(200)
                                .body("token", equalTo("login-token"))
                                .body("username", equalTo("john"));
        }
}
