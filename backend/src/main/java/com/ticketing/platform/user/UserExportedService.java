package com.ticketing.platform.user;

import java.util.Optional;
import java.util.UUID;

/**
 * Public Boundary Interface (SPI) exposed by the User Management module.
 * Other bounded contexts or security filters invoke this contract to query,
 * bind, or JIT provision user accounts without coupling to internal packages.
 */
public interface UserExportedService {

    UserDto getOrCreateUser(String firebaseUid, String email, String displayName, String avatarUrl);

    Optional<UserDto> getUserById(UUID id);

    Optional<UserDto> findByFirebaseUid(String firebaseUid);

    record UserDto(
            UUID id,
            String firebaseUid,
            String email,
            String displayName,
            String avatarUrl
    ) {}
}
