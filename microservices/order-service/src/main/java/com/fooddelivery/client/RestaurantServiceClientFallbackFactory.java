package com.fooddelivery.client;

import com.fooddelivery.client.dto.RemoteMenuItemResponse;
import com.fooddelivery.client.dto.RemoteRestaurantResponse;
import com.fooddelivery.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RestaurantServiceClientFallbackFactory implements FallbackFactory<RestaurantServiceClient> {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantServiceClientFallbackFactory.class);

    @Override
    public RestaurantServiceClient create(Throwable cause) {
        logger.warn("Restaurant service fallback triggered", cause);
        return new RestaurantServiceClient() {
            @Override
            public RemoteRestaurantResponse getRestaurantById(Long id) {
                throw new ServiceUnavailableException(
                        "Restaurant service is unavailable for restaurantId=" + id,
                        cause
                );
            }

            @Override
            public List<RemoteMenuItemResponse> getMenuByRestaurantId(Long restaurantId) {
                throw new ServiceUnavailableException(
                        "Restaurant service is unavailable for menu lookup restaurantId=" + restaurantId,
                        cause
                );
            }

            @Override
            public RemoteMenuItemResponse getMenuItem(Long restaurantId, Long itemId) {
                throw new ServiceUnavailableException(
                        "Restaurant service is unavailable for menu item lookup restaurantId=" + restaurantId +
                                " itemId=" + itemId,
                        cause
                );
            }

            @Override
            public List<RemoteMenuItemResponse> validateMenuItems(Long restaurantId, List<Long> itemIds) {
                throw new ServiceUnavailableException(
                        "Restaurant service is unavailable for validating menu items restaurantId=" + restaurantId,
                        cause
                );
            }
        };
    }
}
