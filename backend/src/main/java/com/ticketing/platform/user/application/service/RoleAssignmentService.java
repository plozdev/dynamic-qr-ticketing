package com.ticketing.platform.user.application.service;

import com.ticketing.platform.user.infrastructure.persistence.entity.RoleJpaEntity;
import com.ticketing.platform.user.infrastructure.persistence.entity.UserJpaEntity;
import com.ticketing.platform.user.infrastructure.persistence.repository.SpringDataRoleRepository;
import com.ticketing.platform.user.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleAssignmentService {
    private static final UUID USER_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ADMIN_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private final SpringDataRoleRepository roles;
    private final SpringDataUserRepository users;

    public RoleJpaEntity resolve(String name) {
        return roles.findByName(name).orElseGet(() -> {
            UUID seedId = switch (name) {
                case "USER" -> USER_ROLE_ID;
                case "ADMIN" -> ADMIN_ROLE_ID;
                default -> throw new IllegalArgumentException("Unknown role: " + name);
            };
            return roles.saveAndFlush(new RoleJpaEntity(seedId, name));
        });
    }

    @Transactional
    public void assign(UUID userId, String roleName) {
        UserJpaEntity user = users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setRole(resolve(roleName));
    }
}
