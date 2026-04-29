package com.ai.main.service;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 외부 PG 응답을 흉내냅니다. 실제 운영에서는 토스/아임포트/PayPal 등으로 교체.
 * 95% 성공 확률로 트랜잭션 ID를 반환하고, 실패 시 예외를 던집니다.
 */
@Component
public class MockPgClient {

    public String charge(Long orderId, int amount, String method) {
        if (ThreadLocalRandom.current().nextInt(100) < 5) {
            throw new IllegalStateException("PG 결제 거절 (한도 초과 또는 카드 오류)");
        }
        return "PG-" + method + "-" + UUID.randomUUID();
    }
}