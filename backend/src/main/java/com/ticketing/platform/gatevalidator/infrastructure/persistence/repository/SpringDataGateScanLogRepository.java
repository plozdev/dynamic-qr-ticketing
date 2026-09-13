package com.ticketing.platform.gatevalidator.infrastructure.persistence.repository;

import com.ticketing.platform.gatevalidator.infrastructure.persistence.entity.GateScanLogJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataGateScanLogRepository extends JpaRepository<GateScanLogJpaEntity, UUID> {
}
