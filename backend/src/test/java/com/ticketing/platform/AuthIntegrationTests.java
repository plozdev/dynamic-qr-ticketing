package com.ticketing.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.platform.user.application.service.RoleAssignmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired RoleAssignmentService roles;

    @Test
    void signupLoginAndRejectSpoofedIdentity() throws Exception {
        String email = "auth-" + UUID.randomUUID() + "@example.com";
        String username = "auth_" + UUID.randomUUID().toString().substring(0, 8);
        String signup = json.writeValueAsString(Map.of("username", username, "email", email, "password", "strong-password-123", "displayName", "Test User"));
        String login = json.writeValueAsString(Map.of("username", username.toUpperCase(), "password", "strong-password-123"));
        JsonNode created = json.readTree(mvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON).content(signup))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andReturn().getResponse().getContentAsString());

        mvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + created.get("token").asText()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + created.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/gates/GATE_A/validate")
                        .header("Authorization", "Bearer " + created.get("token").asText())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        roles.grant(UUID.fromString(created.get("userId").asText()), "ADMIN");

        mvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON).content(signup))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username.toUpperCase(),
                                "email", "other-" + UUID.randomUUID() + "@example.com",
                                "password", "strong-password-123", "displayName", "Other User"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", username, "password", "wrong-password"))))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/auth/me").header("X-User-Id", created.get("userId").asText()))
                .andExpect(status().isUnauthorized());

        JsonNode loggedIn = json.readTree(mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(login))
                .andExpect(status().isOk()).andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andReturn().getResponse().getContentAsString());
        String token = loggedIn.get("token").asText();
        mvc.perform(get("/api/v1/admin/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token)
                        .header("X-User-Id", UUID.randomUUID().toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(created.get("userId").asText()));
        mvc.perform(post("/api/v1/auth/logout").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}
