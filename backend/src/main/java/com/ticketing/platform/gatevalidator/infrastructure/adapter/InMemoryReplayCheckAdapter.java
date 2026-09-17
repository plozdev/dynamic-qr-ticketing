package com.ticketing.platform.gatevalidator.infrastructure.adapter;

import com.ticketing.platform.gatevalidator.application.port.out.ReplayCheckPort;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Khung sườn Adapter kiểm tra chống Replay Attack (Mã QR bị quét lại trong cửa sổ thời gian).
 */
@Component
public class InMemoryReplayCheckAdapter implements ReplayCheckPort {

    // Lưu trữ bộ nhớ đệm: token -> thời điểm hết hạn (Instant)
    private final ConcurrentHashMap<String, Instant> cache = new ConcurrentHashMap<>();

    @Override
    public boolean markIfSeen(String tokenFingerprint, Duration ttl) {
        Instant now = Instant.now();

        cache.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
        
        Instant expiresAt = cache.get(tokenFingerprint);
        if (expiresAt != null && expiresAt.isAfter(now)) {
            return true; 
        }
        cache.put(tokenFingerprint, now.plus(ttl));
        return false;
    }
}
