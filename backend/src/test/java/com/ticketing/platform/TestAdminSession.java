package com.ticketing.platform;

import com.ticketing.platform.shared.security.AuthSessionService;
import com.ticketing.platform.user.UserExportedService;
import com.ticketing.platform.user.application.service.RoleAssignmentService;

import java.util.UUID;

final class TestAdminSession {
    private TestAdminSession() {}

    static String create(UserExportedService users, RoleAssignmentService roles, AuthSessionService sessions) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        var user = users.register("admin_" + suffix, "admin-" + suffix + "@example.com",
                "strong-password-123", "Test Admin");
        roles.assign(user.id(), "ADMIN");
        return sessions.create(user.id());
    }
}
