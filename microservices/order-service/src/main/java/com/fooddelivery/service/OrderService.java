package com.fooddelivery.service;

import com.fooddelivery.client.CustomerServiceClient;
import com.fooddelivery.client.RestaurantServiceClient;
import com.fooddelivery.client.dto.RemoteCustomerResponse;
import com.fooddelivery.client.dto.RemoteMenuItemResponse;
import com.fooddelivery.client.dto.RemoteRestaurantResponse;
import com.fooddelivery.exception.CustomerNotFoundException;
import com.fooddelivery.exception.MenuItemNotFoundException;
import com.fooddelivery.dto.OrderItemRequest;
import com.fooddelivery.dto.OrderResponse;
import com.fooddelivery.dto.OrderStatusUpdateRequest;
import com.fooddelivery.dto.PlaceOrderRequest;
import com.fooddelivery.exception.ResourceNotFoundException;
import com.fooddelivery.exception.RestaurantNotFoundException;
import com.fooddelivery.exception.UnauthorizedException;
import com.fooddelivery.model.Order;
import com.fooddelivery.model.OrderItem;
import com.fooddelivery.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OrderService {

    private static final int DEFAULT_ESTIMATED_DELIVERY_MINUTES = 30;

    private final OrderRepository orderRepository;
    private final CustomerServiceClient customerServiceClient;
    private final RestaurantServiceClient restaurantServiceClient;

    public OrderService(
            OrderRepository orderRepository,
            CustomerServiceClient customerServiceClient,
            RestaurantServiceClient restaurantServiceClient) {
        this.orderRepository = orderRepository;
        this.customerServiceClient = customerServiceClient;
        this.restaurantServiceClient = restaurantServiceClient;
    }

    @Transactional
    public OrderResponse placeOrder(Long customerId, PlaceOrderRequest request) {
        if (customerId == null) {
            throw new IllegalArgumentException("customerId is required");
        }

        RemoteCustomerResponse customer = customerServiceClient.getCustomerById(customerId);
        if (customer == null || customer.getId() == null) {
            throw new CustomerNotFoundException(customerId);
        }

        RemoteRestaurantResponse restaurant = restaurantServiceClient.getRestaurantById(request.getRestaurantId());
        if (restaurant == null || restaurant.getId() == null) {
            throw new RestaurantNotFoundException(request.getRestaurantId());
        }
        if (!restaurant.isActive()) {
            throw new IllegalArgumentException("Restaurant is currently inactive");
        }

        List<Long> requestedMenuItemIds = request.getItems().stream()
                .map(OrderItemRequest::getMenuItemId)
                .toList();

        List<RemoteMenuItemResponse> validMenuItems = restaurantServiceClient
                .validateMenuItems(request.getRestaurantId(), requestedMenuItemIds);
        Map<Long, RemoteMenuItemResponse> menuItemById = new HashMap<>();
        for (RemoteMenuItemResponse item : validMenuItems) {
            menuItemById.put(item.getId(), item);
        }

        Set<Long> uniqueRequestedIds = Set.copyOf(requestedMenuItemIds);
        if (menuItemById.size() < uniqueRequestedIds.size()) {
            throw new IllegalArgumentException("One or more menu items are invalid or unavailable");
        }

        String deliveryAddress = StringUtils.hasText(request.getDeliveryAddress())
                ? request.getDeliveryAddress()
                : customer.getDeliveryAddress();
        if (!StringUtils.hasText(deliveryAddress)) {
            throw new IllegalArgumentException("deliveryAddress is required");
        }

        int deliveryMinutes = restaurant.getEstimatedDeliveryMinutes() > 0
                ? restaurant.getEstimatedDeliveryMinutes()
                : DEFAULT_ESTIMATED_DELIVERY_MINUTES;

        Order order = Order.builder()
                .customerId(customerId)
                .restaurantId(request.getRestaurantId())
                .deliveryAddress(deliveryAddress)
                .specialInstructions(request.getSpecialInstructions())
                .estimatedDeliveryTime(LocalDateTime.now().plusMinutes(deliveryMinutes))
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            RemoteMenuItemResponse menuItem = menuItemById.get(itemRequest.getMenuItemId());
            if (menuItem == null) {
                throw new MenuItemNotFoundException(itemRequest.getMenuItemId());
            }

            Double rawPrice = menuItem.getPrice();
            if (rawPrice == null || rawPrice <= 0) {
                throw new IllegalArgumentException("Invalid menu item price for id: " + itemRequest.getMenuItemId());
            }

            BigDecimal unitPrice = BigDecimal.valueOf(rawPrice);
            OrderItem orderItem = toOrderItem(itemRequest, order, unitPrice);
            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        return OrderResponse.fromEntity(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return OrderResponse.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getCustomerOrders(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getRestaurantOrders(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        order.setStatus(Order.OrderStatus.valueOf(request.getStatus().toUpperCase()));
        return OrderResponse.fromEntity(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getCustomerId().equals(customerId)) {
            throw new UnauthorizedException("You can only cancel your own orders");
        }

        if (order.getStatus() != Order.OrderStatus.PLACED && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new IllegalArgumentException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        return OrderResponse.fromEntity(orderRepository.save(order));
    }

    private OrderItem toOrderItem(OrderItemRequest request, Order order, BigDecimal unitPrice) {
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));

        return OrderItem.builder()
                .order(order)
                .menuItemId(request.getMenuItemId())
                .quantity(request.getQuantity())
                .unitPrice(unitPrice)
                .subtotal(subtotal)
                .specialInstructions(request.getSpecialInstructions())
                .build();
    }
}
