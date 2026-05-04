package com.ai.main.service;

import com.ai.main.config.RedisConfig;
import com.ai.main.domain.*;
import com.ai.main.dto.order.OrderResponse;
import com.ai.main.dto.timedeal.*;
import com.ai.main.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeDealService {

    private final TimeDealRepository timeDealRepository;
    private final ProductRepository productRepository;
    private final UsersRepository usersRepository;
    private final OrdersRepository ordersRepository;
    private final SseEmitterService sseEmitterService;

    @Cacheable(value = RedisConfig.CACHE_ACTIVE_DEALS, key = "'all'")
    @Transactional(readOnly = true)
    public List<TimeDealResponse> getActiveDeals() {
        return timeDealRepository.findActiveDeals(LocalDateTime.now()).stream()
                .map(TimeDealResponse::from)
                .toList();
    }

    @Cacheable(value = RedisConfig.CACHE_UPCOMING_DEALS, key = "'all'")
    @Transactional(readOnly = true)
    public List<TimeDealResponse> getUpcomingDeals() {
        return timeDealRepository.findUpcomingDeals(LocalDateTime.now()).stream()
                .map(TimeDealResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TimeDealResponse getDeal(Long id) {
        TimeDeal deal = timeDealRepository.findByIdWithProduct(id)
                .orElseThrow(() -> new EntityNotFoundException("딜을 찾을 수 없습니다."));
        return TimeDealResponse.from(deal);
    }

    @CacheEvict(value = {RedisConfig.CACHE_ACTIVE_DEALS, RedisConfig.CACHE_UPCOMING_DEALS}, allEntries = true)
    @Transactional
    public TimeDealResponse createDeal(TimeDealCreateRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        if (!request.endAt().isAfter(request.startAt())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 이후여야 합니다.");
        }
        if (request.endAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("이미 종료된 시점으로는 딜을 만들 수 없습니다.");
        }
        if (request.dealPrice() <= 0 || request.dealPrice() >= product.getPrice()) {
            throw new IllegalArgumentException(
                    "딜 가격은 정가(" + product.getPrice() + "원)보다 낮아야 합니다.");
        }
        if (request.totalStock() <= 0 || request.totalStock() > product.getStock()) {
            throw new IllegalArgumentException(
                    "딜 수량은 1 이상이고 상품 재고(" + product.getStock() + ")를 초과할 수 없습니다.");
        }
        if (request.maxPerUser() < 0) {
            throw new IllegalArgumentException("1인당 구매 제한은 0 이상이어야 합니다.");
        }

        TimeDeal deal = TimeDeal.builder()
                .title(request.title())
                .product(product)
                .dealPrice(request.dealPrice())
                .totalStock(request.totalStock())
                .remainingStock(request.totalStock())
                .maxPerUser(request.maxPerUser())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .createdAt(LocalDateTime.now())
                .build();

        timeDealRepository.save(deal);
        return TimeDealResponse.from(deal);
    }

    @CacheEvict(value = RedisConfig.CACHE_ACTIVE_DEALS, allEntries = true)
    @Transactional
    public OrderResponse purchaseDeal(String email, Long dealId, TimeDealPurchaseRequest request) {
        TimeDeal deal = timeDealRepository.findByIdWithLock(dealId)
                .orElseThrow(() -> new EntityNotFoundException("딜을 찾을 수 없습니다."));

        if (deal.getMaxPerUser() > 0) {
            int alreadyPurchased = ordersRepository.countUserDealPurchases(email, dealId);
            if (alreadyPurchased + request.quantity() > deal.getMaxPerUser()) {
                throw new IllegalStateException(
                        "1인당 최대 " + deal.getMaxPerUser() + "개까지 구매 가능합니다. (이미 " + alreadyPurchased + "개 구매)");
            }
        }

        deal.purchase(request.quantity());

        Product product = productRepository.findByIdWithLock(deal.getProduct().getId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));
        product.decreaseStock(request.quantity());

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));

        Address shippingAddress = request.shippingAddress() != null
                ? request.shippingAddress().toEntity()
                : user.getDefaultAddress();
        if (shippingAddress == null) {
            throw new IllegalArgumentException("배송지가 필요합니다. 기본 배송지를 등록하거나 구매 시 입력하세요.");
        }

        Orders order = Orders.builder()
                .users(user)
                .orderStatus(Orders.OrderStatus.PAYMENT_PENDING)
                .orderedAt(LocalDateTime.now())
                .timeDeal(deal)
                .shippingAddress(shippingAddress)
                .build();

        OrderItems item = OrderItems.builder()
                .orders(order)
                .product(product)
                .quantity(request.quantity())
                .unitPrice(deal.getDealPrice())
                .subtotal(deal.getDealPrice() * request.quantity())
                .build();

        order.addOrderItem(item);
        ordersRepository.save(order);

        return OrderResponse.from(order);
    }

    /** TTL은 짧지만, 시작/종료 경계에서 즉시 반영을 보장하기 위해 강제 무효화. */
    @CacheEvict(value = RedisConfig.CACHE_ACTIVE_DEALS, allEntries = true)
    @Scheduled(fixedDelay = 60_000L)
    public void refreshDealCacheBoundaries() {
        log.debug("핫딜 활성 캐시 만료 (스케줄러)");
    }
}