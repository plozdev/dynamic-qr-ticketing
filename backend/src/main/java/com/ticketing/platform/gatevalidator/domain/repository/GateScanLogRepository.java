package com.ticketing.platform.gatevalidator.domain.repository;

import com.ticketing.platform.gatevalidator.domain.model.ValidationResult;

import java.util.UUID;

/**
 * Domain Outbound Port for recording turnstile/gate scan logs.
 */
public interface GateScanLogRepository {

    void recordScan(UUID scanId, UUID ticketId, String gateId, ValidationResult result);
}
