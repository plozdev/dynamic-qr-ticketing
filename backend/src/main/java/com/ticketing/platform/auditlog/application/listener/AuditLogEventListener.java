package com.ticketing.platform.auditlog.application.listener;

import com.ticketing.platform.auditlog.application.port.in.RecordAuditLogUseCase;
import com.ticketing.platform.auditlog.domain.model.AuditEventType;
import com.ticketing.platform.eventcatalog.EventPublishedIntegrationEvent;
import com.ticketing.platform.shared.event.TicketValidatedIntegrationEvent;
import com.ticketing.platform.ticketissuance.TicketIssuedIntegrationEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Listener lắng nghe bất đồng bộ các sự kiện liên module bằng Spring Modulith.
 * Cơ chế @ApplicationModuleListener đảm bảo tính toàn vẹn Outbox, chạy bất đồng bộ và tự động retry.
 */
@Component
public class AuditLogEventListener {

    private final RecordAuditLogUseCase recordAuditLogUseCase;

    public AuditLogEventListener(RecordAuditLogUseCase recordAuditLogUseCase) {
        this.recordAuditLogUseCase = recordAuditLogUseCase;
    }

    @ApplicationModuleListener
    public void onTicketIssued(TicketIssuedIntegrationEvent event) {
        if (event == null) return;
        recordAuditLogUseCase.record(
                AuditEventType.TICKET_ISSUED,
                "ticketissuance",
                event.userId() != null ? event.userId().toString() : "SYSTEM",
                String.format("Phát hành vé %s cho sự kiện %s (hạng vé: %s)",
                        event.ticketId(), event.eventId(), event.ticketCategory())
        );
    }

    @ApplicationModuleListener
    public void onTicketValidated(TicketValidatedIntegrationEvent event) {
        if (event == null) return;
        AuditEventType type = "GRANTED".equalsIgnoreCase(event.validationStatus())
                ? AuditEventType.TICKET_VALIDATED
                : AuditEventType.SECURITY_ALERT;
        recordAuditLogUseCase.record(
                type,
                "gatevalidator",
                "gate:" + event.gateId(),
                String.format("Soát vé %s tại cổng %s, kết quả: %s - %s",
                        event.ticketId(), event.gateId(), event.validationStatus(), event.reason())
        );
    }

    @ApplicationModuleListener
    public void onEventPublished(EventPublishedIntegrationEvent event) {
        if (event == null) return;
        recordAuditLogUseCase.record(
                AuditEventType.EVENT_PUBLISHED,
                "eventcatalog",
                "ORGANIZER",
                String.format("Sự kiện '%s' (ID: %s) đã được mở bán chính thức",
                        event.eventName(), event.eventId())
        );
    }
}
