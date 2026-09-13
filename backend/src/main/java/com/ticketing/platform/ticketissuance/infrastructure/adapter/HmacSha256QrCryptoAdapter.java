package com.ticketing.platform.ticketissuance.infrastructure.adapter;

import com.ticketing.platform.ticketissuance.application.port.out.QrCryptoPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Khung sườn Adapter tính toán mã Dynamic QR sử dụng thuật toán HMAC-SHA256.
 * Bạn tự triển khai thuật toán mật mã hóa tại đây.
 */
@Component
public class HmacSha256QrCryptoAdapter implements QrCryptoPort {

    private static final int ROTATION_INTERVAL_SECONDS = 30;

    @Override
    public String computeTotpToken(UUID ticketId, String secretBase64Key, long timestampEpochSeconds) {
        // TODO: Bạn tự triển khai:
        // 1. Tính timeWindow = timestampEpochSeconds / ROTATION_INTERVAL_SECONDS
        // 2. Chuẩn bị message = ticketId.toString() + ":" + timeWindow
        // 3. Sử dụng javax.crypto.Mac với thuật toán "HmacSHA256" và secretBase64Key
        // 4. Trả về chuỗi Base64Url không có padding
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai computeTotpToken()");
    }

    @Override
    public int getValiditySeconds() {
        return ROTATION_INTERVAL_SECONDS;
    }
}
