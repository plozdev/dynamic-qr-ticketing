package com.ticketing.platform.gatevalidator.api.web;

import com.ticketing.platform.gatevalidator.api.dto.GateRosterSyncResponse;
import com.ticketing.platform.gatevalidator.api.dto.GateScanRequest;
import com.ticketing.platform.gatevalidator.application.dto.GateValidationResultDto;
import com.ticketing.platform.gatevalidator.application.dto.ValidateGateCommand;
import com.ticketing.platform.gatevalidator.application.port.in.ValidateTicketAtGateUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Collections;

/**
 * REST Controller cho Gate Validator điều phối soát vé Online và Offline Gate Sync.
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
        if (gateId == null || gateId.isBlank()) {
            throw new IllegalArgumentException("gateId không được để trống");
        }
        ValidateGateCommand command = new ValidateGateCommand(request.rawQrPayload(), gateId);
        GateValidationResultDto result = validateTicketAtGateUseCase.validateAtGate(command);

        if (result.success()) 
            return ResponseEntity.ok(result);
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
    }

    @GetMapping("/{gateId}/sync-roster")
    public ResponseEntity<GateRosterSyncResponse> syncGateRoster(
            @PathVariable String gateId) {
        if (gateId == null || gateId.isBlank()) {
            throw new IllegalArgumentException("gateId không được để trống");
        }
        // Đồng bộ danh sách vé ngoại tuyến cho thiết bị soát vé trước giờ G
        GateRosterSyncResponse response = new GateRosterSyncResponse(
                gateId,
                null,
                Instant.now().getEpochSecond(),
                0,
                Collections.emptyList()
        );
        return ResponseEntity.ok(response);
    }
}

