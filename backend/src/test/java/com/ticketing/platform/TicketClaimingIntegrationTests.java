package com.ticketing.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.platform.shared.security.AuthSessionService;
import com.ticketing.platform.user.UserExportedService;
import com.ticketing.platform.user.application.service.RoleAssignmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TicketClaimingIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired private UserExportedService users;
    @Autowired private RoleAssignmentService roles;
    @Autowired private AuthSessionService sessions;

    @Test
    @DisplayName("Claim Ticket Test: 1-Click Ticket Claiming decreases available tickets and issues valid ticket")
    void test1ClickTicketClaiming() throws Exception {
        String adminToken = TestAdminSession.create(users, roles, sessions);
        Instant now = Instant.now();
        Instant start = now.plus(7, ChronoUnit.DAYS);
        Instant end = start.plus(4, ChronoUnit.HOURS);

        // 1. Create a new event with total 1000 tickets, 850 available
        Map<String, Object> eventReq = Map.of(
                "name", "V-Pop Super Soundwave 2026",
                "description", "Greatest live music concert",
                "venueName", "Quan Khu 7 Stadium, HCMC",
                "venueAddress", "202 Hoang Van Thu, Tan Binh, HCMC",
                "venueGates", List.of("GATE_1", "GATE_2"),
                "startDateTime", start.toString(),
                "endDateTime", end.toString()
        );

        MvcResult eventResult = mockMvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventReq)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode eventNode = objectMapper.readTree(eventResult.getResponse().getContentAsString());
        String eventId = eventNode.get("id").asText();
        int initialAvailable = eventNode.get("availableTickets").asInt();

        // 2. User claims ticket via 1-Click API: POST /api/v1/events/{eventId}/claim
        String email = "claimer-" + UUID.randomUUID() + "@example.com";
        MvcResult signup = mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "claimer_" + UUID.randomUUID().toString().substring(0, 8),
                                "email", email, "password", "strong-password-123", "displayName", "Phan Thanh Son"))))
                .andExpect(status().isCreated()).andReturn();
        JsonNode account = objectMapper.readTree(signup.getResponse().getContentAsString());
        String token = account.get("token").asText();
        Map<String, Object> claimReq = Map.of(
                "eventId", eventId,
                "userId", account.get("userId").asText(),
                "categoryName", "VIP Diamond",
                "attendeeName", "Phan Thanh Sơn"
        );

        mockMvc.perform(post("/api/v1/events/" + eventId + "/claim")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/admin/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claimReq)))
                .andExpect(status().isForbidden());

        MvcResult claimResult = mockMvc.perform(post("/api/v1/admin/tickets")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claimReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketId").isNotEmpty())
                .andExpect(jsonPath("$.eventName").value("V-Pop Super Soundwave 2026"))
                .andExpect(jsonPath("$.venueName").value("Quan Khu 7 Stadium, HCMC"))
                .andExpect(jsonPath("$.categoryName").value("VIP Diamond"))
                .andExpect(jsonPath("$.seatNumber", startsWith("GA-")))
                .andExpect(jsonPath("$.attendeeName").value("Phan Thanh Sơn"))
                .andExpect(jsonPath("$.secretKeyBase64").isNotEmpty())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();

        JsonNode ticketNode = objectMapper.readTree(claimResult.getResponse().getContentAsString());
        String ticketId = ticketNode.get("ticketId").asText();

        // 3. Verify event available tickets decreased by 1
        mockMvc.perform(get("/api/v1/events/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableTickets").value(initialAvailable - 1));

        // 4. Verify user can list their newly claimed ticket
        mockMvc.perform(get("/api/v1/tickets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ticketId").value(ticketId))
                .andExpect(jsonPath("$[0].eventName").value("V-Pop Super Soundwave 2026"))
                .andExpect(jsonPath("$[0].attendeeName").value("Phan Thanh Sơn"));

        // 5. Verify user can sync their key for mobile offline QR generation
        mockMvc.perform(get("/api/v1/tickets/" + ticketId + "/sync")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secretKeyBase64").isNotEmpty());
    }
}
