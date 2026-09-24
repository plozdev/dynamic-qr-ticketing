package com.ticketing.platform.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public Boundary Interface (SPI) exposed by the User Management module.
 * Other bounded contexts or security filters invoke this contract to query,
 * bind, or provision user accounts without coupling to external auth providers.
 */
public interface UserExportedService {

    UserDto register(String username, String email, String password, String displayName);

    Optional<UserDto> authenticate(String username, String password);

    Optional<UserDto> getUserById(UUID id);

    Optional<UserDto> findByUsername(String username);

    List<UserDto> getAllUsers();

    record UserDto(
            UUID id,
            String username,
            String email,
            String displayName,
            String avatarUrl,
            List<String> roles
    ) {}
}
