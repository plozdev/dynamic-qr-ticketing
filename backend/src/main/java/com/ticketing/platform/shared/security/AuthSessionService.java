package com.ticketing.platform.shared.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthSessionService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final AuthSessionRepository repository;

    public String create(UUID userId) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.save(new AuthSessionEntity(hash(token), userId, Instant.now().plus(30, ChronoUnit.DAYS)));
        return token;
    }

    public Optional<UUID> resolve(String token) {
        if (token == null || token.length() != 43) return Optional.empty();
        return repository.findById(hash(token))
                .filter(session -> session.getExpiresAt().isAfter(Instant.now()))
                .map(AuthSessionEntity::getUserId);
    }

    public void revoke(String token) {
        if (token != null && token.length() == 43) repository.deleteById(hash(token));
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
