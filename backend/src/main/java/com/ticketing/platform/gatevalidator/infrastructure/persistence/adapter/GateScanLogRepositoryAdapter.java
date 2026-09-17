package com.ticketing.platform.gatevalidator.infrastructure.persistence.adapter;

import com.ticketing.platform.gatevalidator.domain.model.ValidationResult;
import com.ticketing.platform.gatevalidator.domain.repository.GateScanLogRepository;
import com.ticketing.platform.gatevalidator.infrastructure.persistence.entity.GateScanLogJpaEntity;
import com.ticketing.platform.gatevalidator.infrastructure.persistence.repository.SpringDataGateScanLogRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Khung sườn Adapter lưu trữ nhật ký quét mã tại cổng soát vé.
 */
@Component
@RequiredArgsConstructor 
public class GateScanLogRepositoryAdapter implements GateScanLogRepository {

    private final SpringDataGateScanLogRepository jpaRepository;

    @Override
    public void recordScan(UUID scanId, UUID ticketId, String gateId, ValidationResult result) {
        UUID id = (scanId != null) ? scanId : UUID.randomUUID();
        if (gateId == null || gateId.isBlank()) throw new IllegalArgumentException("gateId cannot be blank");
        if (result == null) throw new IllegalArgumentException("ValidationResult must not be null");
        GateScanLogJpaEntity entity = GateScanLogJpaEntity.builder()
                                        .id(id)
                                        .ticketId(ticketId)
                                        .gateId(gateId)
                                        .status(result.status())
                                        .message(result.message())
                                        .scannedAt(result.validatedAt() != null ? result.validatedAt() : java.time.Instant.now())
                                        .build();
        jpaRepository.save(entity);
    }
}
