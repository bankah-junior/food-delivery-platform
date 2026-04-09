package com.fooddelivery.controller;

import com.fooddelivery.dto.CustomerResponse;
import com.fooddelivery.dto.CustomerUpdateRequest;
import com.fooddelivery.service.CustomerService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.hamcrest.Matchers.equalTo;

class CustomerControllerTest {

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(null, null, null) {
            @Override
            public CustomerResponse getProfile(String username) {
                CustomerResponse response = new CustomerResponse();
                response.setId(1L);
                response.setUsername(username);
                response.setEmail("john@example.com");
                response.setCity("Accra");
                return response;
            }

            @Override
            public CustomerResponse getById(Long id) {
                CustomerResponse response = new CustomerResponse();
                response.setId(id);
                response.setUsername("john");
                response.setEmail("john@example.com");
                return response;
            }

            @Override
            public CustomerResponse updateProfile(String username, CustomerUpdateRequest request) {
                CustomerResponse response = new CustomerResponse();
                response.setId(1L);
                response.setUsername(username);
                response.setFirstName(request.getFirstName());
                response.setCity(request.getCity());
                return response;
            }
        };

        RestAssuredMockMvc.standaloneSetup(new CustomerController(customerService));
    }

    @Test
    @DisplayName("GET /api/customers/me - Success")
    void getMyProfileReturnsCurrentCustomer() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("john", "n/a");
        RequestPostProcessor withAuth = request -> {
            request.setUserPrincipal(auth);
            return request;
        };

        RestAssuredMockMvc.given()
                .postProcessors(withAuth)
                .when()
                .get("/api/customers/me")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("username", equalTo("john"));
    }

    @Test
    @DisplayName("GET /api/customers/{id} - Success")
    void getByIdReturnsCustomer() {
        RestAssuredMockMvc.given()
                .when()
                .get("/api/customers/{id}", 2)
                .then()
                .statusCode(200)
                .body("id", equalTo(2))
                .body("username", equalTo("john"));
    }

    @Test
    @DisplayName("PUT /api/customers/me - Success")
    void updateProfileReturnsUpdatedCustomer() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("john", "n/a");
        RequestPostProcessor withAuth = request -> {
            request.setUserPrincipal(auth);
            return request;
        };
        String body = """
                {
                  "firstName": "John",
                  "city": "Kumasi"
                }
                """;

        RestAssuredMockMvc.given()
            .postProcessors(withAuth)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/api/customers/me")
                .then()
                .statusCode(200)
                .body("username", equalTo("john"))
                .body("firstName", equalTo("John"))
                .body("city", equalTo("Kumasi"));
    }
}
