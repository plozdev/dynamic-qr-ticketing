package com.ticketing.platform.gatevalidator.api.web;

import com.ticketing.platform.gatevalidator.api.dto.GateScanRequest;
import com.ticketing.platform.gatevalidator.application.dto.GateValidationResultDto;
import com.ticketing.platform.gatevalidator.application.port.in.ValidateTicketAtGateUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Khung sườn REST Controller cho Gate Validator.
 */
@RestController
@RequestMapping("/api/v1/gates")
public class GateValidationController {

    private final ValidateTicketAtGateUseCase validateTicketAtGateUseCase;

    public GateValidationController(ValidateTicketAtGateUseCase validateTicketAtGateUseCase) {
        this.validateTicketAtGateUseCase = validateTicketAtGateUseCase;
    }

    @PostMapping("/{gateId}/validate")
    public ResponseEntity<GateValidationResultDto> validateGateScan(
            @PathVariable String gateId,
            @Valid @RequestBody GateScanRequest request) {
        // TODO: Đóng gói ValidateGateCommand, gọi validateTicketAtGateUseCase.
        // Trả về HTTP 200 OK nếu thành công, hoặc HTTP 403 Forbidden nếu không hợp lệ
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai endpoint POST /api/v1/gates/{gateId}/validate");
    }

    @org.springframework.web.bind.annotation.GetMapping("/{gateId}/sync-roster")
    public ResponseEntity<com.ticketing.platform.gatevalidator.api.dto.GateRosterSyncResponse> syncGateRoster(
            @PathVariable String gateId) {
        // TODO: Đồng bộ danh sách vé ngoại tuyến (Offline Gate Sync) trước giờ G:
        // 1. Xác định eventId tương ứng với gateId
        // 2. Tra cứu danh sách vé hợp lệ kèm secretKeyBase64
        // 3. Trả về GateRosterSyncResponse để thiết bị Scanner lưu trữ cục bộ (Local SQLite/Memory)
        // 4. Cho phép Scanner tự soát vé Offline mà không bị nghẽn mạng
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai endpoint GET /api/v1/gates/{gateId}/sync-roster");
    }
}
