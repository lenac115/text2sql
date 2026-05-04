package com.ai.main.service;

import com.ai.main.domain.*;
import com.ai.main.dto.order.*;
import com.ai.main.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final ProductRepository productRepository;
    private final UsersRepository usersRepository;
    private final UserCouponRepository userCouponRepository;
    private final SseEmitterService sseEmitterService;

    @Transactional
    public OrderResponse createOrder(String email, OrderCreateRequest request) {
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        Address shippingAddress = request.shippingAddress() != null
                ? request.shippingAddress().toEntity()
                : user.getDefaultAddress();
        if (shippingAddress == null) {
            throw new IllegalArgumentException("배송지가 필요합니다. 기본 배송지를 등록하거나 주문 시 입력하세요.");
        }

        Orders order = Orders.builder()
                .users(user)
                .orderStatus(Orders.OrderStatus.PAYMENT_PENDING)
                .orderedAt(LocalDateTime.now())
                .shippingAddress(shippingAddress)
                .build();

        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findByIdWithLock(itemReq.productId())
                    .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다: " + itemReq.productId()));

            product.decreaseStock(itemReq.quantity());

            OrderItems item = OrderItems.builder()
                    .orders(order)
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(product.getPrice())
                    .subtotal(product.getPrice() * itemReq.quantity())
                    .build();

            order.addOrderItem(item);
        }

        ordersRepository.save(order); // CASCADE로 OrderItems 함께 저장, IDENTITY 전략으로 ID 즉시 확정

        if (request.userCouponId() != null) {
            UserCoupon userCoupon = userCouponRepository.findByIdAndUserId(
                            request.userCouponId(), user.getId())
                    .orElseThrow(() -> new EntityNotFoundException("쿠폰을 찾을 수 없습니다."));

            Coupon coupon = userCoupon.getCoupon();
            if (order.getTotalAmount() < coupon.getMinOrderAmount()) {
                throw new IllegalArgumentException(
                        "최소 주문 금액(" + coupon.getMinOrderAmount() + "원)을 충족해야 합니다.");
            }

            int discount = coupon.calculateDiscount(order.getTotalAmount());
            order.applyDiscount(discount, userCoupon);
            userCoupon.use(order.getId());
        }

        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId) {
        Orders order = ordersRepository.findByIdAndUserEmail(orderId, email)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));
        cancelInternal(order);
        return OrderResponse.from(order);
    }

    /**
     * 결제 실패/타임아웃 시 시스템이 호출. 소유자 검증 없이 취소 + 재고/쿠폰 복구.
     */
    @Transactional
    public void cancelByPaymentFailure(Long orderId) {
        Orders order = ordersRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));
        cancelInternal(order);
    }

    private void cancelInternal(Orders order) {
        Orders.OrderStatus prevStatus = order.getOrderStatus();
        order.changeStatus(Orders.OrderStatus.CANCELLED);

        order.getOrderItemsList().forEach(item ->
                item.getProduct().increaseStock(item.getQuantity())
        );

        if (order.getTimeDeal() != null) {
            int dealQuantity = order.getOrderItemsList().stream()
                    .mapToInt(OrderItems::getQuantity).sum();
            order.getTimeDeal().restoreStock(dealQuantity);
        }

        if (order.getUserCoupon() != null) {
            order.getUserCoupon().restore();
        }

        sseEmitterService.sendOrderStatus(order.getUsers().getEmail(),
                new OrderStatusEvent(order.getId(), prevStatus.name(),
                        Orders.OrderStatus.CANCELLED.name(), LocalDateTime.now()));
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, Orders.OrderStatus newStatus) {
        Orders order = ordersRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        Orders.OrderStatus prevStatus = order.getOrderStatus();
        order.changeStatus(newStatus);

        String email = order.getUsers().getEmail();
        sseEmitterService.sendOrderStatus(email,
                new OrderStatusEvent(orderId, prevStatus.name(), newStatus.name(), LocalDateTime.now()));

        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String email) {
        return ordersRepository.findByUserEmailWithItems(email).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String email, Long orderId) {
        Orders order = ordersRepository.findByIdAndUserEmail(orderId, email)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));
        return OrderResponse.from(order);
    }
}