package com.ticketing.platform.gatevalidator.application.port.out;

import java.time.Duration;

/**
 * Outbound Port for Anti-Replay checking.
 * Ensures a dynamic QR token cannot be re-scanned within its validity window.
 */
public interface ReplayCheckPort {

    boolean markIfSeen(String tokenFingerprint, Duration ttl);
}
