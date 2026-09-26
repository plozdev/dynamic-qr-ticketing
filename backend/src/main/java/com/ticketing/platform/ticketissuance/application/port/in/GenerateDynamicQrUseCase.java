package com.ticketing.platform.ticketissuance.application.port.in;

import com.ticketing.platform.ticketissuance.application.dto.DynamicQrDto;

import java.util.UUID;

public interface GenerateDynamicQrUseCase {

    DynamicQrDto generateDynamicQr(UUID ticketId, UUID requesterUserId);
}
