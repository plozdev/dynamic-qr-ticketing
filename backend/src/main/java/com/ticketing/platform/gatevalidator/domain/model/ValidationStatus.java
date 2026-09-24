package com.ticketing.platform.gatevalidator.domain.model;

public enum ValidationStatus {
    GRANTED,
    DENIED_EXPIRED_QR,
    DENIED_INVALID_SIGNATURE,
    DENIED_ALREADY_USED,
    DENIED_REVOKED,
    DENIED_REPLAY_ATTACK,
    DENIED_TICKET_NOT_FOUND,
    DENIED_CHECK_IN_CLOSED
}
