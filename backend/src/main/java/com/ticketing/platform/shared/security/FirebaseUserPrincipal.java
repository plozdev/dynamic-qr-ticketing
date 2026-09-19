package com.ticketing.platform.shared.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record FirebaseUserPrincipal(
        UUID userId,
        String firebaseUid,
        String email,
        String displayName,
        String avatarUrl,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    public FirebaseUserPrincipal(UUID userId, String firebaseUid, String email, String displayName, String avatarUrl) {
        this(userId, firebaseUid, email, displayName, avatarUrl, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return email != null ? email : (firebaseUid != null ? firebaseUid : userId.toString());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
