package com.ticketing.platform.auditlog.infrastructure.persistence.adapter;

import com.ticketing.platform.auditlog.domain.model.AuditLogEntry;
import com.ticketing.platform.auditlog.domain.repository.AuditLogRepository;
import com.ticketing.platform.auditlog.infrastructure.persistence.repository.SpringDataAuditLogRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Khung sườn Adapter lưu trữ và truy vấn Audit Log bằng Spring Data JPA.
 */
@Component
public class AuditLogRepositoryAdapter implements AuditLogRepository {

    private final SpringDataAuditLogRepository jpaRepository;

    public AuditLogRepositoryAdapter(SpringDataAuditLogRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AuditLogEntry save(AuditLogEntry entry) {
        // TODO: Chuyển sang AuditLogJpaEntity và lưu qua jpaRepository
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai save()");
    }

    @Override
    public List<AuditLogEntry> findRecent(int limit) {
        // TODO: Truy vấn danh sách gần nhất qua jpaRepository và map sang AuditLogEntry domain model
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai findRecent()");
    }
}
