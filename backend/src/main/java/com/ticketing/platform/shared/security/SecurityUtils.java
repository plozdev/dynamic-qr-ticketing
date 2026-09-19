package com.ticketing.platform.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

public final class SecurityUtils {

    public static final UUID DEFAULT_DEMO_USER_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    private SecurityUtils() {}

    public static Optional<FirebaseUserPrincipal> getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof FirebaseUserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static UUID getCurrentUserId() {
        return getCurrentPrincipal()
                .map(FirebaseUserPrincipal::userId)
                .orElse(DEFAULT_DEMO_USER_ID);
    }

    public static UUID getEffectiveUserId(UUID explicitHeaderOrParam) {
        if (explicitHeaderOrParam != null) {
            return explicitHeaderOrParam;
        }
        return getCurrentUserId();
    }
}
