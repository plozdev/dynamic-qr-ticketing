package com.ticketing.platform.auditlog.infrastructure.persistence.adapter;

import com.ticketing.platform.auditlog.domain.model.AuditLogEntry;
import com.ticketing.platform.auditlog.domain.repository.AuditLogRepository;
import com.ticketing.platform.auditlog.infrastructure.persistence.entity.AuditLogJpaEntity;
import com.ticketing.platform.auditlog.infrastructure.persistence.repository.SpringDataAuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter lưu trữ và truy vấn Audit Log bằng Spring Data JPA.
 */
@Component
public class AuditLogRepositoryAdapter implements AuditLogRepository {

    private final SpringDataAuditLogRepository jpaRepository;

    public AuditLogRepositoryAdapter(SpringDataAuditLogRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AuditLogEntry save(AuditLogEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("AuditLogEntry không được null");
        }
        AuditLogJpaEntity entity = new AuditLogJpaEntity(
                entry.getId(),
                entry.getEventType(),
                entry.getSourceModule(),
                entry.getPrincipal(),
                entry.getDetails(),
                entry.getRecordedAt()
        );
        AuditLogJpaEntity saved = jpaRepository.save(entity);
        return new AuditLogEntry(
                saved.getId(),
                saved.getEventType(),
                saved.getSourceModule(),
                saved.getPrincipal(),
                saved.getDetails(),
                saved.getRecordedAt()
        );
    }

    @Override
    public List<AuditLogEntry> findRecent(int limit) {
        int queryLimit = (limit > 0 && limit <= 200) ? limit : 50;
        Pageable pageable = PageRequest.of(0, queryLimit);
        List<AuditLogJpaEntity> entities = jpaRepository.findRecentLogs(pageable);
        return entities.stream()
                .map(e -> new AuditLogEntry(
                        e.getId(),
                        e.getEventType(),
                        e.getSourceModule(),
                        e.getPrincipal(),
                        e.getDetails(),
                        e.getRecordedAt()
                ))
                .toList();
    }
}

