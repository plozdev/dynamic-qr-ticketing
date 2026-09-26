package com.ticketing.platform.gatevalidator.application.service;

import com.ticketing.platform.shared.event.TicketValidatedIntegrationEvent;
import com.ticketing.platform.gatevalidator.application.dto.GateValidationResultDto;
import com.ticketing.platform.gatevalidator.application.dto.ValidateGateCommand;
import com.ticketing.platform.gatevalidator.application.port.in.ValidateTicketAtGateUseCase;
import com.ticketing.platform.gatevalidator.application.port.out.ReplayCheckPort;
import com.ticketing.platform.gatevalidator.domain.model.ValidationResult;
import com.ticketing.platform.gatevalidator.domain.model.ValidationStatus;
import com.ticketing.platform.gatevalidator.domain.repository.GateScanLogRepository;
import com.ticketing.platform.eventcatalog.EventCatalogExportedService;
import com.ticketing.platform.ticketissuance.TicketVerificationExportedService;
import com.ticketing.platform.ticketissuance.TicketVerificationExportedService.TicketVerificationData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service điều phối quy trình soát vé tại cổng.
 * Triển khai pipeline xác thực mã QR động, chống replay, đối chiếu chữ ký HMAC và cập nhật trạng thái vé.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GateValidatorService implements ValidateTicketAtGateUseCase {

    private static final int ROTATION_INTERVAL_SECONDS = 30;

    private final TicketVerificationExportedService ticketVerificationService;
    private final EventCatalogExportedService eventCatalogService;
    private final ReplayCheckPort replayCheckPort;
    private final GateScanLogRepository scanLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public GateValidationResultDto validateAtGate(ValidateGateCommand command) {
        if (command == null || command.rawQrPayload() == null || command.rawQrPayload().isBlank()) {
            return recordAndReturnDenied(null, command != null ? command.gateId() : "UNKNOWN",
                    ValidationStatus.DENIED_INVALID_SIGNATURE, "Mã QR trống hoặc không tồn tại");
        }

        // 1. Phân tích định dạng payload: Chuỗi có dạng "TICKETING:<ticketId>:<expiresAt>:<token>"
        String[] parts = command.rawQrPayload().split(":");
        if (parts.length != 4 || !"TICKETING".equals(parts[0])) {
            return recordAndReturnDenied(null, command.gateId(),
                    ValidationStatus.DENIED_INVALID_SIGNATURE, "Mã QR không đúng định dạng chuẩn TICKETING");
        }

        // 2. Trích xuất thông tin an toàn với try-catch
        UUID ticketId;
        long expiresAt;
        String token = parts[3];
        try {
            ticketId = UUID.fromString(parts[1]);
            expiresAt = Long.parseLong(parts[2]);
        } catch (Exception e) {
            return recordAndReturnDenied(null, command.gateId(),
                    ValidationStatus.DENIED_INVALID_SIGNATURE, "Dữ liệu vé trong mã QR bị lỗi cấu trúc");
        }

        // 3. Kiểm tra độ tươi (Freshness Check): Mã QR chỉ có hiệu lực trong cửa sổ 30s
        long now = Instant.now().getEpochSecond();
        if (now > expiresAt) {
            return recordAndReturnDenied(ticketId, command.gateId(),
                    ValidationStatus.DENIED_EXPIRED_QR, "Mã QR đã hết hạn, vui lòng làm mới trên ứng dụng");
        }
        // The timestamp must be the end of the current (or one clock-drift) window.
        // Reject arbitrary future timestamps even if a client knows a ticket's secret.
        if (expiresAt % ROTATION_INTERVAL_SECONDS != 0
                || expiresAt > ((now / ROTATION_INTERVAL_SECONDS) + 2) * ROTATION_INTERVAL_SECONDS) {
            return recordAndReturnDenied(ticketId, command.gateId(),
                    ValidationStatus.DENIED_INVALID_SIGNATURE, "Thời hạn mã QR không hợp lệ");
        }

        // 4. Tra cứu thông tin vé qua Boundary SPI
        Optional<TicketVerificationData> ticketOpt = ticketVerificationService.getTicketForValidation(ticketId);
        if (ticketOpt.isEmpty()) {
            return recordAndReturnDenied(ticketId, command.gateId(),
                    ValidationStatus.DENIED_TICKET_NOT_FOUND, "Không tìm thấy vé trong hệ thống");
        }

        TicketVerificationData ticketData = ticketOpt.get();

        // 6. Xác thực chữ ký mật mã HMAC-SHA256
        if (!verifyHmacToken(ticketId, ticketData.secretKey(), expiresAt, token)) {
            return recordAndReturnDenied(ticketId, command.gateId(),
                    ValidationStatus.DENIED_INVALID_SIGNATURE, "Chữ ký mật mã của mã QR không hợp lệ");
        }

        if (!eventCatalogService.isCheckInOpen(ticketData.eventId(), Instant.now())) {
            return recordAndReturnDenied(ticketId, command.gateId(),
                    ValidationStatus.DENIED_CHECK_IN_CLOSED, "Check-in chưa được mở cho sự kiện này");
        }

        // Only authenticated tokens for open events enter the replay cache.
        if (replayCheckPort.markIfSeen(token, Duration.ofSeconds(60))) {
            return recordAndReturnDenied(ticketId, command.gateId(),
                    ValidationStatus.DENIED_REPLAY_ATTACK, "Phát hiện mã QR quét lại (Replay Attack), từ chối vào cổng");
        }

        // 7. Kiểm tra trạng thái vé
        if ("USED".equalsIgnoreCase(ticketData.status())) {
            return recordAndReturnDenied(ticketId, command.gateId(),
                    ValidationStatus.DENIED_ALREADY_USED, "Vé đã được sử dụng qua cổng trước đó");
        }
        if ("REVOKED".equalsIgnoreCase(ticketData.status())) {
            return recordAndReturnDenied(ticketId, command.gateId(),
                    ValidationStatus.DENIED_REVOKED, "Vé đã bị thu hồi hoặc hủy bỏ");
        }
        if (!"ACTIVE".equalsIgnoreCase(ticketData.status())) {
            return recordAndReturnDenied(ticketId, command.gateId(),
                    ValidationStatus.DENIED_ALREADY_USED, "Vé không ở trạng thái hợp lệ: " + ticketData.status());
        }

        // 8. Đánh dấu vé đã qua cổng
        if (!ticketVerificationService.markTicketAsUsed(ticketId, command.gateId())) {
            return recordAndReturnDenied(ticketId, command.gateId(),
                    ValidationStatus.DENIED_ALREADY_USED, "Vé đã được sử dụng qua cổng trước đó");
        }

        // 9. Ghi nhận lịch sử soát vé thành công vào scanLogRepository
        scanLogRepository.recordScan(UUID.randomUUID(), ticketId, command.gateId(), ValidationResult.granted(ticketId));

        // 10. Phát sự kiện liên module (Spring Modulith Outbox)
        eventPublisher.publishEvent(new TicketValidatedIntegrationEvent(ticketId, ticketData.userId(), command.gateId(), "GRANTED", "Access granted"));

        // 11. Trả về kết quả thành công
        return GateValidationResultDto.granted(ticketId, "Xác thực vé thành công, mời vào cổng");
    }

    private boolean verifyHmacToken(UUID ticketId, String secretBase64Key, long expiresAt, String token) {
        try {
            if (token.length() != 43 || !token.matches("[A-Za-z0-9_-]+")) {
                return false;
            }
            long expectedTimeWindow = (expiresAt / ROTATION_INTERVAL_SECONDS) - 1;
            byte[] keyBytes = Base64.getUrlDecoder().decode(secretBase64Key);
            byte[] actualTokenBytes = Base64.getUrlDecoder().decode(token);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));

            // Cửa sổ thời gian hiện tại
            String message = ticketId.toString() + ":" + expectedTimeWindow;
            byte[] expectedHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            if (MessageDigest.isEqual(expectedHmac, actualTokenBytes)) {
                return true;
            }

            // Cho phép lệch 1 chu kỳ để tránh clock drift
            String prevMessage = ticketId.toString() + ":" + (expectedTimeWindow - 1);
            byte[] prevHmac = mac.doFinal(prevMessage.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(prevHmac, actualTokenBytes);
        } catch (Exception e) {
            log.debug("HMAC verification failed for ticket {}: {}", ticketId, e.toString());
            return false;
        }
    }

    private GateValidationResultDto recordAndReturnDenied(UUID ticketId, String gateId, ValidationStatus status, String message) {
        scanLogRepository.recordScan(UUID.randomUUID(), ticketId, gateId, ValidationResult.denied(ticketId, status, message));
        eventPublisher.publishEvent(new TicketValidatedIntegrationEvent(
                ticketId, gateId, status.name(), message
        ));
        return GateValidationResultDto.denied(ticketId, status.name(), message);
    }
}
