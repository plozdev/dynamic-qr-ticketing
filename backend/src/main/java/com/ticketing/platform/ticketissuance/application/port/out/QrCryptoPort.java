package com.ticketing.platform.ticketissuance.application.port.out;

import java.util.UUID;

/**
 * Outbound Port for Cryptographic Dynamic QR token computation.
 */
public interface QrCryptoPort {

    String computeTotpToken(UUID ticketId, String secretBase64Key, long timestampEpochSeconds);

    int getValiditySeconds();
}
