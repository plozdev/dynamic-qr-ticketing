package com.ticketing.platform.auditlog.application.listener;

import com.ticketing.platform.auditlog.application.port.in.RecordAuditLogUseCase;
import com.ticketing.platform.eventcatalog.EventPublishedIntegrationEvent;
import com.ticketing.platform.gatevalidator.TicketValidatedIntegrationEvent;
import com.ticketing.platform.ticketissuance.TicketIssuedIntegrationEvent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/**
 * Khung sườn Listener lắng nghe bất đồng bộ các sự kiện liên module bằng Spring Modulith.
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
        // TODO: Bạn tự triển khai: Ghi log sự kiện phát hành vé qua recordAuditLogUseCase
    }

    @ApplicationModuleListener
    public void onTicketValidated(TicketValidatedIntegrationEvent event) {
        // TODO: Bạn tự triển khai: Ghi log sự kiện soát vé tại cổng qua recordAuditLogUseCase
    }

    @ApplicationModuleListener
    public void onEventPublished(EventPublishedIntegrationEvent event) {
        // TODO: Bạn tự triển khai: Ghi log sự kiện mở bán sự kiện qua recordAuditLogUseCase
    }
}
