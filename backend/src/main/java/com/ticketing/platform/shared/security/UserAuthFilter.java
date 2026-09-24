package com.ticketing.platform.shared.security;

import com.ticketing.platform.user.UserExportedService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class UserAuthFilter extends OncePerRequestFilter {
    private final AuthSessionService sessions;
    private final UserExportedService users;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            sessions.resolve(authorization.substring(7).trim())
                    .flatMap(users::getUserById)
                    .ifPresent(user -> {
                        UserPrincipal principal = new UserPrincipal(user.id(), user.username(), user.email(),
                                user.displayName(), user.avatarUrl(), user.roles().stream()
                                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList());
                        SecurityContextHolder.getContext().setAuthentication(
                                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
                    });
        }
        chain.doFilter(request, response);
    }
}
