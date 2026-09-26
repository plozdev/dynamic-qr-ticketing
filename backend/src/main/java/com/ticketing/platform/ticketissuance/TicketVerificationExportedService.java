package com.ticketing.platform.ticketissuance;

import java.util.Optional;
import java.util.UUID;

/**
 * Public Boundary Interface (SPI) exposed by Ticket Issuance module.
 * Bounded contexts such as gate-validator invoke this interface to verify
 * ticket status and obtain dynamic QR cryptographic parameters without coupling
 * to ticket-issuance internals.
 */
public interface TicketVerificationExportedService {

    Optional<TicketVerificationData> getTicketForValidation(UUID ticketId);

    boolean markTicketAsUsed(UUID ticketId, String gateId);

    record TicketVerificationData(
            UUID ticketId,
            UUID userId,
            UUID eventId,
            String status,
            String secretKey,
            long issuedAtEpochSeconds
    ) {}
}
