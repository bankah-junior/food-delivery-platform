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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private static final int DEFAULT_ESTIMATED_DELIVERY_MINUTES = 30;

    private final OrderRepository orderRepository;
    private final CustomerServiceClient customerServiceClient;
    private final RestaurantServiceClient restaurantServiceClient;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            CustomerServiceClient customerServiceClient,
            RestaurantServiceClient restaurantServiceClient,
            OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.customerServiceClient = customerServiceClient;
        this.restaurantServiceClient = restaurantServiceClient;
        this.orderEventPublisher = orderEventPublisher;
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
        Order savedOrder = orderRepository.save(order);

        orderEventPublisher.publishOrderPlaced(savedOrder);
        logger.info("Order placed and event published for orderId={}", savedOrder.getId());

        return OrderResponse.fromEntity(savedOrder);
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
    public void handleDeliveryStatusUpdated(Long orderId, String deliveryStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        Order.OrderStatus mappedStatus = mapOrderStatusFromDelivery(deliveryStatus);
        if (mappedStatus == null) {
            logger.warn("No order status mapping for deliveryStatus={} orderId={}", deliveryStatus, orderId);
            return;
        }

        order.setStatus(mappedStatus);
        orderRepository.save(order);
        logger.info("Order status synchronized from delivery event: orderId={} status={}", orderId, mappedStatus);
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
        Order savedOrder = orderRepository.save(order);

        orderEventPublisher.publishOrderCancelled(savedOrder.getId(), "Cancelled by customer");
        logger.info("Order cancelled and event published for orderId={}", savedOrder.getId());

        return OrderResponse.fromEntity(savedOrder);
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

    private Order.OrderStatus mapOrderStatusFromDelivery(String deliveryStatus) {
        if (deliveryStatus == null) {
            return null;
        }

        return switch (deliveryStatus.toUpperCase()) {
            case "ASSIGNED" -> Order.OrderStatus.READY_FOR_PICKUP;
            case "PICKED_UP", "IN_TRANSIT" -> Order.OrderStatus.OUT_FOR_DELIVERY;
            case "DELIVERED" -> Order.OrderStatus.DELIVERED;
            case "FAILED" -> Order.OrderStatus.CANCELLED;
            default -> null;
        };
    }
}
