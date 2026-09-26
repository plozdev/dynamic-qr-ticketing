package com.ticketing.platform.user.api.web;

import com.ticketing.platform.user.UserExportedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUsersController {
    private final UserExportedService users;

    @GetMapping
    public List<UserExportedService.UserDto> list() {
        return users.getAllUsers();
    }
}
