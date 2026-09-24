package com.ticketing.platform.ticketissuance.application.listener;

import com.ticketing.platform.shared.event.TicketValidatedIntegrationEvent;
import com.ticketing.platform.eventcatalog.EventCheckInChangedIntegrationEvent;
import com.ticketing.platform.ticketissuance.infrastructure.sse.TicketSseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Lắng nghe sự kiện TicketValidatedIntegrationEvent và chuyển tiếp tức thời qua Server-Sent Events (SSE)
 * về cho ứng dụng Mobile của chủ vé.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TicketValidationSseListener {

    private final TicketSseService ticketSseService;

    @ApplicationModuleListener
    public void onCheckInChanged(EventCheckInChangedIntegrationEvent event) {
        ticketSseService.sendCheckInControlUpdate(event.eventId(), event.enabled(), event.occurredAt());
    }

    @ApplicationModuleListener
    public void onTicketValidated(TicketValidatedIntegrationEvent event) {
        if (event == null) return;

        log.info("TicketValidationSseListener received validation event: ticketId={}, userId={}, status={}",
                event.ticketId(), event.userId(), event.validationStatus());

        if ("GRANTED".equalsIgnoreCase(event.validationStatus())) {
            ticketSseService.sendTicketStatusUpdate(
                    event.userId(),
                    event.ticketId(),
                    "CHECKED_IN",
                    event.gateId(),
                    event.occurredAt()
            );
        }
    }
}
