package com.fooddelivery.controller;

import com.fooddelivery.dto.RestaurantResponse;
import com.fooddelivery.dto.MenuItemResponse;
import com.fooddelivery.dto.MenuItemRequest;
import com.fooddelivery.dto.RestaurantRequest;
import com.fooddelivery.service.RestaurantService;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;

class RestaurantControllerTest {

    private RestaurantService restaurantService;

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantService(null, null) {
            @Override
            public RestaurantResponse getById(Long id) {
                RestaurantResponse response = new RestaurantResponse();
                response.setId(id);
                response.setName("Mama's Kitchen");
                response.setCity("Accra");
                response.setOwnerId(7L);
                return response;
            }

            @Override
            public List<RestaurantResponse> searchByCity(String city) {
                RestaurantResponse response = new RestaurantResponse();
                response.setId(2L);
                response.setName("City Bites");
                response.setCity(city);
                return List.of(response);
            }

            @Override
            public List<RestaurantResponse> searchByCuisine(String cuisineType) {
                RestaurantResponse response = new RestaurantResponse();
                response.setId(3L);
                response.setName("Spice Hub");
                response.setCuisineType(cuisineType);
                return List.of(response);
            }

            @Override
            public List<RestaurantResponse> getAllActive() {
                RestaurantResponse response = new RestaurantResponse();
                response.setId(4L);
                response.setName("Active Place");
                return List.of(response);
            }

            @Override
            public List<MenuItemResponse> getMenu(Long restaurantId) {
                MenuItemResponse response = new MenuItemResponse();
                response.setId(11L);
                response.setName("Jollof Rice");
                response.setRestaurantId(restaurantId);
                response.setPrice(new BigDecimal("20.00"));
                return List.of(response);
            }

            @Override
            public RestaurantResponse createRestaurant(Long ownerId, RestaurantRequest request) {
                RestaurantResponse response = new RestaurantResponse();
                response.setId(5L);
                response.setName(request.getName());
                response.setCity(request.getCity());
                response.setOwnerId(ownerId);
                return response;
            }

            @Override
            public MenuItemResponse addMenuItem(Long restaurantId, Long ownerId, MenuItemRequest request) {
                MenuItemResponse response = new MenuItemResponse();
                response.setId(12L);
                response.setName(request.getName());
                response.setRestaurantId(restaurantId);
                response.setPrice(request.getPrice());
                return response;
            }

            @Override
            public MenuItemResponse updateMenuItem(Long itemId, Long ownerId, MenuItemRequest request) {
                MenuItemResponse response = new MenuItemResponse();
                response.setId(itemId);
                response.setName(request.getName());
                response.setPrice(request.getPrice());
                return response;
            }

            @Override
            public void toggleMenuItemAvailability(Long itemId, Long ownerId) {
                // no-op for controller test
            }
        };
        RestAssuredMockMvc.standaloneSetup(new RestaurantController(restaurantService));
    }

    @Test
    @DisplayName("GET /api/restaurants/{id} - Success")
    void getByIdReturnsRestaurant() {
        RestAssuredMockMvc.given()
                .when()
                .get("/api/restaurants/{id}", 1)
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("name", equalTo("Mama's Kitchen"))
                .body("city", equalTo("Accra"));
    }

            @Test
            @DisplayName("GET /api/restaurants/search/city/{city} - Success")
            void searchByCityReturnsRestaurants() {
            RestAssuredMockMvc.given()
                .when()
                .get("/api/restaurants/search/city/{city}", "Accra")
                .then()
                .statusCode(200)
                .body("[0].city", equalTo("Accra"));
            }

            @Test
            @DisplayName("GET /api/restaurants/search/cuisine/{type} - Success")
            void searchByCuisineReturnsRestaurants() {
            RestAssuredMockMvc.given()
                .when()
                .get("/api/restaurants/search/cuisine/{type}", "Ghanaian")
                .then()
                .statusCode(200)
                .body("[0].cuisineType", equalTo("Ghanaian"));
            }

            @Test
            @DisplayName("GET /api/restaurants/search/all - Success")
            void getAllActiveReturnsRestaurants() {
            RestAssuredMockMvc.given()
                .when()
                .get("/api/restaurants/search/all")
                .then()
                .statusCode(200)
                .body("[0].name", equalTo("Active Place"));
            }

            @Test
            @DisplayName("GET /api/restaurants/{id}/menu - Success")
            void getMenuReturnsItems() {
            RestAssuredMockMvc.given()
                .when()
                .get("/api/restaurants/{id}/menu", 1)
                .then()
                .statusCode(200)
                .body("[0].name", equalTo("Jollof Rice"))
                .body("[0].restaurantId", equalTo(1));
            }

            @Test
            @DisplayName("POST /api/restaurants - Success")
            void createRestaurantReturnsCreated() {
            String body = """
                {
                  "name": "Ocean Grill",
                  "cuisineType": "Seafood",
                  "address": "Labone",
                  "city": "Accra"
                }
                """;

            RestAssuredMockMvc.given()
                .header("X-User-Id", 9)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/restaurants")
                .then()
                .statusCode(201)
                .body("name", equalTo("Ocean Grill"))
                .body("ownerId", equalTo(9));
            }

            @Test
            @DisplayName("POST /api/restaurants/{id}/menu - Success")
            void addMenuItemReturnsCreated() {
            String body = """
                {
                  "name": "Banku",
                  "price": 18.50
                }
                """;

            RestAssuredMockMvc.given()
                .header("X-User-Id", 9)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/restaurants/{id}/menu", 1)
                .then()
                .statusCode(201)
                .body("id", equalTo(12))
                .body("restaurantId", equalTo(1));
            }

            @Test
            @DisplayName("PUT /api/restaurants/menu/{itemId} - Success")
            void updateMenuItemReturnsUpdated() {
            String body = """
                {
                  "name": "Updated Banku",
                  "price": 20.00
                }
                """;

            RestAssuredMockMvc.given()
                .header("X-User-Id", 9)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/api/restaurants/menu/{itemId}", 12)
                .then()
                .statusCode(200)
                .body("id", equalTo(12))
                .body("name", equalTo("Updated Banku"));
            }

            @Test
            @DisplayName("PATCH /api/restaurants/menu/{itemId}/toggle - Success")
            void toggleAvailabilityReturnsNoContent() {
            RestAssuredMockMvc.given()
                .header("X-User-Id", 9)
                .when()
                .patch("/api/restaurants/menu/{itemId}/toggle", 12)
                .then()
                .statusCode(204);
            }
}
