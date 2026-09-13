package com.ticketing.platform.gatevalidator.infrastructure.adapter;

import com.ticketing.platform.gatevalidator.application.port.out.ReplayCheckPort;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Khung sườn Adapter kiểm tra chống Replay Attack (Mã QR bị quét lại trong cửa sổ thời gian).
 * Bạn tự triển khai cơ chế lưu vết (ConcurrentHashMap hoặc Redis) tại đây.
 */
@Component
public class InMemoryReplayCheckAdapter implements ReplayCheckPort {

    @Override
    public boolean markIfSeen(String tokenFingerprint, Duration ttl) {
        // TODO: Bạn tự triển khai:
        // Lưu token vào bộ nhớ tạm kèm thời gian hết hạn TTL.
        // Trả về true nếu token ĐÃ TỒN TẠI (phát hiện replay attack).
        // Trả về false nếu token MỚI (chưa từng thấy trước đó) và lưu lại.
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai markIfSeen()");
    }
}
