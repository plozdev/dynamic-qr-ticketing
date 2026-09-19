package com.ticketing.platform.user.application.service;

import com.ticketing.platform.user.UserExportedService;
import com.ticketing.platform.user.infrastructure.persistence.entity.UserJpaEntity;
import com.ticketing.platform.user.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserExportedService {

    private final SpringDataUserRepository userRepository;

    @Override
    public UserDto getOrCreateUser(String firebaseUid, String email, String displayName, String avatarUrl) {
        return userRepository.findByFirebaseUid(firebaseUid)
                .map(existing -> {
                    boolean modified = false;
                    if (email != null && !email.equals(existing.getEmail())) {
                        existing.setEmail(email);
                        modified = true;
                    }
                    if (displayName != null && !displayName.equals(existing.getDisplayName())) {
                        existing.setDisplayName(displayName);
                        modified = true;
                    }
                    if (avatarUrl != null && !avatarUrl.equals(existing.getAvatarUrl())) {
                        existing.setAvatarUrl(avatarUrl);
                        modified = true;
                    }
                    if (modified) {
                        existing.setUpdatedAt(Instant.now());
                        userRepository.save(existing);
                    }
                    return toDto(existing);
                })
                .orElseGet(() -> {
                    log.info("JIT Provisioning new user for Firebase UID: {}", firebaseUid);
                    UserJpaEntity newUser = UserJpaEntity.builder()
                            .id(UUID.randomUUID())
                            .firebaseUid(firebaseUid)
                            .email(email)
                            .displayName(displayName != null ? displayName : (email != null ? email.split("@")[0] : "Khán Giả"))
                            .avatarUrl(avatarUrl)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    UserJpaEntity saved = userRepository.save(newUser);
                    return toDto(saved);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(UUID id) {
        return userRepository.findById(id).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid).map(this::toDto);
    }

    private UserDto toDto(UserJpaEntity entity) {
        return new UserDto(
                entity.getId(),
                entity.getFirebaseUid(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getAvatarUrl()
        );
    }
}
