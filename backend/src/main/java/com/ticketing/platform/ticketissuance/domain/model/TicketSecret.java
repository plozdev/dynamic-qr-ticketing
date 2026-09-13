package com.ticketing.platform.ticketissuance.domain.model;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Khung sườn Value Object đại diện cho khóa bí mật (Secret Seed) dùng sinh Dynamic QR.
 */
public record TicketSecret(String base64Key) {

    public static TicketSecret generate() {
        // TODO: Sinh mảng byte ngẫu nhiên (32 bytes = 256 bits) bằng SecureRandom và encode Base64 URL safe
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return new TicketSecret(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
    }

    public static TicketSecret of(String base64Key) {
        return new TicketSecret(base64Key);
    }
}
