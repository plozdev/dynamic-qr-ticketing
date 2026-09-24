package com.ticketing.platform.ticketissuance.infrastructure.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service quản lý kết nối Server-Sent Events (SSE) đẩy trạng thái soát vé thời gian thực
 * từ Backend tới thiết bị Mobile.
 */
@Slf4j
@Service
public class TicketSseService {

    // Thời gian timeout cho SSE connection: 30 phút
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    // Lưu trữ kết nối SSE theo userId
    private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    // Lưu trữ kết nối SSE theo ticketId cụ thể (khi người dùng đang mở chi tiết vé/QR)
    private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> ticketEmitters = new ConcurrentHashMap<>();

    public void sendCheckInControlUpdate(UUID eventId, boolean enabled, Instant occurredAt) {
        Map<String, Object> payload = Map.of(
                "eventType", "EVENT_CHECK_IN_CHANGED",
                "eventId", eventId.toString(),
                "ticketId", "",
                "status", enabled ? "OPEN" : "CLOSED",
                "timestamp", occurredAt.getEpochSecond()
        );
        userEmitters.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().name("ticket-update").data(payload));
                } catch (Exception e) {
                    emitter.complete();
                    emitters.remove(emitter);
                }
            }
        });
    }

    /**
     * Tạo và đăng ký một SSE Emitter cho người dùng và/hoặc vé cụ thể.
     */
    public SseEmitter createEmitter(UUID userId, UUID ticketId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        if (userId != null) {
            userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        }
        if (ticketId != null) {
            ticketEmitters.computeIfAbsent(ticketId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        }

        emitter.onCompletion(() -> removeEmitter(userId, ticketId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            removeEmitter(userId, ticketId, emitter);
        });
        emitter.onError(e -> removeEmitter(userId, ticketId, emitter));

        // Gửi thông điệp kết nối thành công đầu tiên để xác nhận stream
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                            "status", "CONNECTED",
                            "userId", userId != null ? userId.toString() : "",
                            "ticketId", ticketId != null ? ticketId.toString() : "",
                            "timestamp", Instant.now().getEpochSecond()
                    )));
            log.info("SSE client connected: userId={}, ticketId={}", userId, ticketId);
        } catch (IOException e) {
            log.debug("Failed to send initial SSE connected event: {}", e.getMessage());
            emitter.complete();
            removeEmitter(userId, ticketId, emitter);
        }

        return emitter;
    }

    /**
     * Đẩy sự kiện cập nhật trạng thái vé (CHECKED_IN / VALIDATED) tới đúng chủ vé và các máy đang mở vé.
     */
    public void sendTicketStatusUpdate(UUID userId, UUID ticketId, String status, String gateId, Instant occurredAt) {
        Map<String, Object> payload = Map.of(
                "eventType", "TICKET_CHECKED_IN",
                "ticketId", ticketId != null ? ticketId.toString() : "",
                "userId", userId != null ? userId.toString() : "",
                "status", status,
                "gateId", gateId != null ? gateId : "",
                "timestamp", occurredAt != null ? occurredAt.getEpochSecond() : Instant.now().getEpochSecond()
        );

        int sentCount = 0;

        // 1. Gửi cho tất cả kết nối của User này
        if (userId != null) {
            CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
            if (emitters != null) {
                for (SseEmitter emitter : emitters) {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("ticket-update")
                                .data(payload));
                        sentCount++;
                    } catch (Exception e) {
                        emitter.complete();
                        emitters.remove(emitter);
                    }
                }
            }
        }

        // 2. Gửi cho kết nối theo ticketId (nếu có kết nối độc lập)
        if (ticketId != null) {
            CopyOnWriteArrayList<SseEmitter> emitters = ticketEmitters.get(ticketId);
            if (emitters != null) {
                for (SseEmitter emitter : emitters) {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("ticket-update")
                                .data(payload));
                        sentCount++;
                    } catch (Exception e) {
                        emitter.complete();
                        emitters.remove(emitter);
                    }
                }
            }
        }

        log.info("SSE pushed ticket-update: ticketId={}, status={}, sentTo={} active emitters",
                ticketId, status, sentCount);
    }

    /**
     * Heartbeat ping định kỳ mỗi 25s để duy trì kết nối qua Proxy/Firewall/NAT trên mạng di động.
     */
    @Scheduled(fixedRate = 25000)
    public void sendHeartbeat() {
        sendPingToMap(userEmitters);
        sendPingToMap(ticketEmitters);
    }

    private void sendPingToMap(ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> map) {
        map.forEach((key, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("ping"));
                } catch (Exception e) {
                    emitter.complete();
                    emitters.remove(emitter);
                }
            }
            if (emitters.isEmpty()) {
                map.remove(key, emitters);
            }
        });
    }

    private void removeEmitter(UUID userId, UUID ticketId, SseEmitter emitter) {
        if (userId != null) {
            CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
            if (emitters != null) {
                emitters.remove(emitter);
                if (emitters.isEmpty()) {
                    userEmitters.remove(userId, emitters);
                }
            }
        }
        if (ticketId != null) {
            CopyOnWriteArrayList<SseEmitter> emitters = ticketEmitters.get(ticketId);
            if (emitters != null) {
                emitters.remove(emitter);
                if (emitters.isEmpty()) {
                    ticketEmitters.remove(ticketId, emitters);
                }
            }
        }
        log.debug("SSE emitter removed for userId={}, ticketId={}", userId, ticketId);
    }
}
