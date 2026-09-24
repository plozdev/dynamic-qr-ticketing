package com.ticketing.platform.user.application.service;

import com.ticketing.platform.user.UserExportedService;
import com.ticketing.platform.user.infrastructure.persistence.entity.UserJpaEntity;
import com.ticketing.platform.user.infrastructure.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserExportedService {

    private final SpringDataUserRepository userRepository;
    private final RoleAssignmentService roleAssignments;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDto register(String username, String email, String password, String displayName) {
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsernameIgnoreCase(normalizedUsername)) {
            throw new IllegalArgumentException("Username is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered");
        }
        Instant now = Instant.now();
        UserJpaEntity user = UserJpaEntity.builder()
                .id(UUID.randomUUID())
                .username(normalizedUsername)
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(password))
                .displayName(displayName.trim())
                .createdAt(now)
                .updatedAt(now)
                .role(roleAssignments.resolve("USER"))
                .build();
        try {
            return toDto(userRepository.saveAndFlush(user));
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Username or email is already registered", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> authenticate(String username, String password) {
        return userRepository.findByUsernameIgnoreCase(username.trim())
                .filter(user -> user.getPasswordHash() != null && passwordEncoder.matches(password, user.getPasswordHash()))
                .map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> getUserById(UUID id) {
        return userRepository.findById(id).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    private UserDto toDto(UserJpaEntity entity) {
        return new UserDto(entity.getId(), entity.getUsername(), entity.getEmail(),
                entity.getDisplayName(), entity.getAvatarUrl(),
                List.of(entity.getRole().getName()));
    }
}
