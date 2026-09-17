package com.ticketing.platform.ticketissuance.infrastructure.adapter;

import com.ticketing.platform.shared.exception.DomainException;
import com.ticketing.platform.ticketissuance.application.port.out.QrCryptoPort;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Khung sườn Adapter tính toán mã Dynamic QR sử dụng thuật toán HMAC-SHA256.
 * Bạn tự triển khai thuật toán mật mã hóa tại đây.
 */
@Component
public class HmacSha256QrCryptoAdapter implements QrCryptoPort {

    private static final int ROTATION_INTERVAL_SECONDS = 30;

    @Override
    public String computeTotpToken(UUID ticketId, String secretBase64Key, long timestampEpochSeconds) {
        try {
            long timeWindow = timestampEpochSeconds / ROTATION_INTERVAL_SECONDS;
            String msg = ticketId.toString() + ":" + timeWindow;

            byte[] keyBytes = Base64.getUrlDecoder().decode(secretBase64Key);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new DomainException("Failed to compute HMAC dynamic QR Token");
        }
    }

    @Override
    public int getValiditySeconds() {
        return ROTATION_INTERVAL_SECONDS;
    }
}
