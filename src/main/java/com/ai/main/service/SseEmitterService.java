package com.ai.main.service;

import com.ai.main.dto.order.OrderStatusEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseEmitterService {

    private static final long TIMEOUT_MS = 30 * 60 * 1000L; // 30분

    private final ConcurrentHashMap<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String email) {
        SseEmitter emitter = new SseEmitter(TIMEOUT_MS);

        emitter.onCompletion(() -> emitters.remove(email, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(email, emitter);
        });
        emitter.onError(e -> emitters.remove(email, emitter));

        emitters.put(email, emitter);

        try {
            emitter.send(SseEmitter.event().name("connect").data("연결됨"));
        } catch (IOException e) {
            emitters.remove(email, emitter);
        }

        return emitter;
    }

    public void sendOrderStatus(String email, OrderStatusEvent event) {
        SseEmitter emitter = emitters.get(email);
        if (emitter == null) return;

        try {
            emitter.send(SseEmitter.event().name("order-status").data(event));
        } catch (IOException e) {
            log.warn("SSE 전송 실패 - email: {}", email);
            emitters.remove(email, emitter);
        }
    }
}