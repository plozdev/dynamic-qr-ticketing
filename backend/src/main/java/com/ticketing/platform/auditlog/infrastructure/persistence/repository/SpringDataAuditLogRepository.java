package com.ticketing.platform.auditlog.infrastructure.persistence.repository;

import com.ticketing.platform.auditlog.infrastructure.persistence.entity.AuditLogJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringDataAuditLogRepository extends JpaRepository<AuditLogJpaEntity, UUID> {

    @Query("SELECT a FROM AuditLogJpaEntity a ORDER BY a.recordedAt DESC")
    List<AuditLogJpaEntity> findRecentLogs(Pageable pageable);
}
