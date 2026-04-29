package com.ai.main.service;

import com.ai.main.domain.Orders;
import com.ai.main.domain.Payment;
import com.ai.main.dto.OrderStatusEvent;
import com.ai.main.dto.PaymentRequest;
import com.ai.main.dto.PaymentResponse;
import com.ai.main.repository.OrdersRepository;
import com.ai.main.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Duration PAYMENT_TIMEOUT = Duration.ofMinutes(15);

    private final PaymentRepository paymentRepository;
    private final OrdersRepository ordersRepository;
    private final OrderService orderService;
    private final SseEmitterService sseEmitterService;
    private final MockPgClient pgClient;

    /**
     * 결제 요청. PG 호출 후 성공 시 주문을 PAID로, 실패 시 주문을 CANCELLED로 전이.
     */
    @Transactional
    public PaymentResponse pay(String email, Long orderId, PaymentRequest request) {
        Orders order = ordersRepository.findByIdAndUserEmail(orderId, email)
                .orElseThrow(() -> new EntityNotFoundException("주문을 찾을 수 없습니다."));

        if (order.getOrderStatus() != Orders.OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException(
                    "결제 가능한 상태가 아닙니다. (현재: " + order.getOrderStatus() + ")");
        }
        paymentRepository.findByOrderIdWithOrder(orderId).ifPresent(existing -> {
            if (existing.getStatus() == Payment.PaymentStatus.SUCCEEDED) {
                throw new IllegalStateException("이미 결제가 완료된 주문입니다.");
            }
        });

        Payment payment = Payment.builder()
                .orders(order)
                .method(request.method())
                .status(Payment.PaymentStatus.PENDING)
                .amount(order.getFinalAmount())
                .requestedAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        try {
            String pgTxId = pgClient.charge(orderId, payment.getAmount(), request.method().name());
            payment.markSucceeded(pgTxId);

            Orders.OrderStatus prev = order.getOrderStatus();
            order.changeStatus(Orders.OrderStatus.PAID);
            sseEmitterService.sendOrderStatus(email,
                    new OrderStatusEvent(orderId, prev.name(), Orders.OrderStatus.PAID.name(), LocalDateTime.now()));
        } catch (RuntimeException pgError) {
            payment.markFailed(pgError.getMessage());
            // 별도 트랜잭션으로 처리 가능하지만 단순화: 같은 트랜잭션에서 취소
            orderService.cancelByPaymentFailure(orderId);
            throw new IllegalStateException("결제 실패: " + pgError.getMessage());
        }

        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String email, Long orderId) {
        Payment payment = paymentRepository.findByOrderIdWithOrder(orderId)
                .orElseThrow(() -> new EntityNotFoundException("결제 내역이 없습니다."));
        if (!payment.getOrders().getUsers().getEmail().equals(email)) {
            throw new EntityNotFoundException("결제 내역이 없습니다.");
        }
        return PaymentResponse.from(payment);
    }

    /**
     * 1분마다 결제 요청 후 PAYMENT_TIMEOUT 이상 PENDING으로 남아있는 결제를 정리.
     * 실제 운영에서는 PG 콜백 누락 / 클라이언트 이탈 케이스를 대비.
     */
    @Scheduled(fixedDelay = 60_000L)
    @Transactional
    public void expireStaleRequests() {
        LocalDateTime threshold = LocalDateTime.now().minus(PAYMENT_TIMEOUT);
        List<Payment> stale = paymentRepository.findStaleRequests(threshold);
        for (Payment payment : stale) {
            try {
                payment.markCancelled();
                Long orderId = payment.getOrders().getId();
                if (payment.getOrders().getOrderStatus() == Orders.OrderStatus.PAYMENT_PENDING) {
                    orderService.cancelByPaymentFailure(orderId);
                }
            } catch (RuntimeException e) {
                log.warn("결제 타임아웃 정리 실패 - paymentId: {}, reason: {}", payment.getId(), e.getMessage());
            }
        }
    }
}