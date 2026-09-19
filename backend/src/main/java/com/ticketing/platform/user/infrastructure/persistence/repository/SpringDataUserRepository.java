package com.ticketing.platform.user.infrastructure.persistence.repository;

import com.ticketing.platform.user.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByFirebaseUid(String firebaseUid);

    Optional<UserJpaEntity> findByEmail(String email);
}
