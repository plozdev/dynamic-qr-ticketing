package com.ticketing.platform.ticketissuance.application.service;

import com.ticketing.platform.eventcatalog.EventCatalogExportedService;
import com.ticketing.platform.ticketissuance.TicketVerificationExportedService;
import com.ticketing.platform.ticketissuance.application.dto.DynamicQrDto;
import com.ticketing.platform.ticketissuance.application.dto.IssueTicketCommand;
import com.ticketing.platform.ticketissuance.application.port.in.GenerateDynamicQrUseCase;
import com.ticketing.platform.ticketissuance.application.port.in.IssueTicketUseCase;
import com.ticketing.platform.ticketissuance.application.port.out.QrCryptoPort;
import com.ticketing.platform.ticketissuance.domain.repository.TicketRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Khung sườn Application Service cho Ticket Issuance.
 * Bạn tự triển khai logic cấp phát vé, sinh mã động và liên kết sự kiện.
 */
@Service
@Transactional
public class TicketIssuanceService implements IssueTicketUseCase, GenerateDynamicQrUseCase, TicketVerificationExportedService {

    private final TicketRepository ticketRepository;
    private final EventCatalogExportedService eventCatalogService;
    private final QrCryptoPort qrCryptoPort;
    private final ApplicationEventPublisher eventPublisher;

    public TicketIssuanceService(TicketRepository ticketRepository,
                                 EventCatalogExportedService eventCatalogService,
                                 QrCryptoPort qrCryptoPort,
                                 ApplicationEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.eventCatalogService = eventCatalogService;
        this.qrCryptoPort = qrCryptoPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UUID issueTicket(IssueTicketCommand command) {
        // TODO: Bạn tự triển khai:
        // 1. Kiểm tra event có đang active không qua eventCatalogService.isEventActive(command.eventId())
        // 2. Tạo Ticket aggregate và lưu vào ticketRepository
        // 3. Phát sự kiện liên module: eventPublisher.publishEvent(new TicketIssuedIntegrationEvent(...))
        // 4. Trả về UUID của vé đã cấp
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai issueTicket()");
    }

    @Override
    @Transactional(readOnly = true)
    public DynamicQrDto generateDynamicQr(UUID ticketId, UUID requesterUserId) {
        // TODO: Bạn tự triển khai:
        // 1. Tìm vé theo ticketId
        // 2. Kiểm tra requesterUserId có đúng là chủ sở hữu vé không
        // 3. Kiểm tra trạng thái vé có ACTIVE không
        // 4. Gọi qrCryptoPort.computeTotpToken(...) để tính HMAC token tại thời điểm hiện tại
        // 5. Đóng gói payload định dạng: "TICKETING:<ticketId>:<expiresAt>:<token>"
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai generateDynamicQr()");
    }

    // --- Boundary Service (TicketVerificationExportedService implementation) ---

    @Override
    @Transactional(readOnly = true)
    public Optional<TicketVerificationData> getTicketForValidation(UUID ticketId) {
        // TODO: Bạn tự triển khai: Lấy thông tin xác thực vé cho module gate-validator gọi sang
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai getTicketForValidation()");
    }

    @Override
    public void markTicketAsUsed(UUID ticketId, String gateId) {
        // TODO: Bạn tự triển khai: Đánh dấu vé đã sử dụng khi qua cổng
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai markTicketAsUsed()");
    }
}
