package com.ticketing.platform.gatevalidator.infrastructure.persistence.adapter;

import com.ticketing.platform.gatevalidator.domain.model.ValidationResult;
import com.ticketing.platform.gatevalidator.domain.repository.GateScanLogRepository;
import com.ticketing.platform.gatevalidator.infrastructure.persistence.repository.SpringDataGateScanLogRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Khung sườn Adapter lưu trữ nhật ký quét mã tại cổng soát vé.
 */
@Component
public class GateScanLogRepositoryAdapter implements GateScanLogRepository {

    private final SpringDataGateScanLogRepository jpaRepository;

    public GateScanLogRepositoryAdapter(SpringDataGateScanLogRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void recordScan(UUID scanId, UUID ticketId, String gateId, ValidationResult result) {
        // TODO: Chuyển sang GateScanLogJpaEntity và lưu vào cơ sở dữ liệu qua jpaRepository
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai recordScan()");
    }
}
