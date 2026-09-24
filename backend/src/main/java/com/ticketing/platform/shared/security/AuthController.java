package com.ticketing.platform.shared.security;

import com.ticketing.platform.user.UserExportedService;
import com.ticketing.platform.user.UserExportedService.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserExportedService users;
    private final AuthSessionService sessions;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        UserDto user = users.register(request.username(), request.email(), request.password(), request.displayName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response(user, sessions.create(user.id())));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        UserDto user = users.authenticate(request.username(), request.password())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        return response(user, sessions.create(user.id()));
    }

    @GetMapping("/me")
    public MeResponse me() {
        UUID id = SecurityUtils.getCurrentUserId();
        UserDto user = users.getUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return new MeResponse(user.id(), user.email(), user.displayName(), user.avatarUrl(), user.username(), "AUTHENTICATED", user.roles());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorization) {
        sessions.revoke(authorization.substring(7));
        return ResponseEntity.noContent().build();
    }

    private AuthResponse response(UserDto user, String token) {
        return new AuthResponse(user.id(), user.email(), user.displayName(), user.username(), token, user.roles());
    }

    public record SignupRequest(@NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,32}") String username,
                                @NotBlank @Email @Size(max = 128) String email,
                                @NotBlank @Size(min = 8, max = 72) String password,
                                @NotBlank @Size(max = 255) String displayName) {}

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record AuthResponse(UUID userId, String email, String displayName, String username, String token,
                               List<String> roles) {}

    public record MeResponse(UUID userId, String email, String displayName, String avatarUrl,
                             String username, String status, List<String> roles) {}
}
