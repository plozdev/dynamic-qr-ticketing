package com.ticketing.platform.gatevalidator.api.dto;

import jakarta.validation.constraints.NotBlank;

public record GateScanRequest(
        @NotBlank(message = "Dynamic QR payload is required")
        String rawQrPayload
) {}
