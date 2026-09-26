package com.ticketing.platform.gatevalidator.application.dto;

public record ValidateGateCommand(
        String rawQrPayload,
        String gateId
) {}
