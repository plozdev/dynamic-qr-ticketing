package com.ticketing.platform.shared.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.ticketing.platform.user.UserExportedService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final FirebaseConfig firebaseConfig;
    private final UserExportedService userExportedService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String xUserIdHeader = request.getHeader("X-User-Id");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String idToken = authHeader.substring(7).trim();
            if (!idToken.isEmpty()) {
                try {
                    if (firebaseConfig.isFirebaseInitialized()) {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        FirebaseToken decodedToken = auth.verifyIdToken(idToken);

                        String uid = decodedToken.getUid();
                        String email = decodedToken.getEmail();
                        String name = decodedToken.getName();
                        String picture = decodedToken.getPicture();

                        var userDto = userExportedService.getOrCreateUser(uid, email, name, picture);
                        FirebaseUserPrincipal principal = new FirebaseUserPrincipal(
                                userDto.id(),
                                userDto.firebaseUid(),
                                userDto.email(),
                                userDto.displayName(),
                                userDto.avatarUrl()
                        );

                        var authentication = new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        // Mock/Dev mode token handling (e.g. dev-user tokens)
                        handleDevToken(idToken);
                    }
                } catch (Exception e) {
                    log.warn("Invalid Firebase ID token received: {}", e.getMessage());
                    // Allow filterChain to proceed so Spring Security handles unauthorized status
                }
            }
        } else if (xUserIdHeader != null && !xUserIdHeader.isBlank()) {
            // Backward-compatibility / Dev / E2E test support
            try {
                UUID parsedUserId = UUID.fromString(xUserIdHeader.trim());
                FirebaseUserPrincipal principal = new FirebaseUserPrincipal(
                        parsedUserId,
                        "dev-" + parsedUserId,
                        "dev-user@" + parsedUserId + ".local",
                        "Dev User " + parsedUserId.toString().substring(0, 8),
                        null
                );
                var authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid X-User-Id header format: {}", xUserIdHeader);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleDevToken(String idToken) {
        UUID devUserId;
        try {
            devUserId = UUID.fromString(idToken);
        } catch (Exception e) {
            devUserId = SecurityUtils.DEFAULT_DEMO_USER_ID;
        }

        var userDto = userExportedService.getOrCreateUser(
                "dev-firebase-" + devUserId,
                "demo@dynamic-qr.vn",
                "Nguyễn Hoàng Long (Demo)",
                null
        );

        FirebaseUserPrincipal principal = new FirebaseUserPrincipal(
                userDto.id(),
                userDto.firebaseUid(),
                userDto.email(),
                userDto.displayName(),
                userDto.avatarUrl()
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
