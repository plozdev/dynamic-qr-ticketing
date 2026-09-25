package com.ticketing.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.platform.shared.security.AuthSessionService;
import com.ticketing.platform.user.UserExportedService;
import com.ticketing.platform.user.application.service.RoleAssignmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MarketplaceFlowIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserExportedService users;
    @Autowired RoleAssignmentService roles;
    @Autowired AuthSessionService sessions;

    @Test
    void draftIsAdminOnlyUntilPublished() throws Exception {
        String adminToken = TestAdminSession.create(users, roles, sessions);
        Instant start = Instant.now().plus(8, ChronoUnit.DAYS);
        JsonNode draft = json.readTree(mvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "name", "Draft " + UUID.randomUUID(), "venueName", "Test Hall",
                                "startDateTime", start.toString(), "endDateTime", start.plus(2, ChronoUnit.HOURS).toString(),
                                "publishNow", false, "basePrice", 250000, "totalTickets", 50))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.availableTickets").value(50))
                .andReturn().getResponse().getContentAsString());
        String id = draft.get("id").asText();
        mvc.perform(get("/api/v1/events/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/admin/events").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mvc.perform(put("/api/v1/events/" + id + "/publish")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));
        mvc.perform(get("/api/v1/events/" + id)).andExpect(status().isOk());
    }

    @Test
    void bookingOwnTicketsAndAdminIssuanceUseRealInventoryAndDashboard() throws Exception {
        String adminToken = TestAdminSession.create(users, roles, sessions);
        Instant start = Instant.now().plus(5, ChronoUnit.DAYS);
        JsonNode event = json.readTree(mvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "name", "Marketplace flow " + UUID.randomUUID(),
                                "venueName", "Test Hall", "venueAddress", "Test Street",
                                "venueGates", List.of("GATE_A1"),
                                "startDateTime", start.toString(),
                                "endDateTime", start.plus(3, ChronoUnit.HOURS).toString()))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        String eventId = event.get("id").asText();
        int before = event.get("availableTickets").asInt();

        String name = "buyer_" + UUID.randomUUID().toString().substring(0, 8);
        JsonNode buyer = json.readTree(mvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", name, "email", name + "@example.com",
                                "password", "strong-password-123", "displayName", "Ticket Buyer"))))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        String buyerToken = buyer.get("token").asText();
        String buyerId = buyer.get("userId").asText();

        mvc.perform(post("/api/v1/tickets/issue").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("eventId", eventId, "userId", buyerId,
                                "categoryName", "VIP", "quantity", 1))))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/tickets/book").header("Authorization", "Bearer " + buyerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("eventId", eventId, "categoryName", "VIP", "quantity", 2))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.ticketIds.length()").value(2));
        JsonNode wallet = json.readTree(mvc.perform(get("/api/v1/tickets")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
                .andReturn().getResponse().getContentAsString());
        String ticketId = wallet.get(0).get("ticketId").asText();
        mvc.perform(post("/api/v1/tickets/issue").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("eventId", eventId, "userId", buyerId,
                                "categoryName", "Standard GA", "quantity", 1))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.ticketIds.length()").value(1));
        mvc.perform(get("/api/v1/events/" + eventId))
                .andExpect(status().isOk()).andExpect(jsonPath("$.availableTickets").value(before - 3));
        mvc.perform(put("/api/v1/events/" + eventId + "/check-in")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":true}"))
                .andExpect(status().isOk());
        JsonNode qr = json.readTree(mvc.perform(get("/api/v1/tickets/" + ticketId + "/dynamic-qr")
                        .header("Authorization", "Bearer " + buyerToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        String scan = json.writeValueAsString(Map.of("rawQrPayload", qr.get("dynamicPayload").asText()));
        mvc.perform(post("/api/v1/gates/GATE_A1/validate")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(scan))
                .andExpect(status().isOk());
        mvc.perform(post("/api/v1/gates/GATE_A1/validate")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(scan))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/events/" + eventId + "/gate-dashboard")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.issued").value(3))
                .andExpect(jsonPath("$.checkedIn").value(1))
                .andExpect(jsonPath("$.replayAlerts").value(1))
                .andExpect(jsonPath("$.gates[0].gateId").value("GATE_A1"))
                .andExpect(jsonPath("$.gates[0].granted").value(1))
                .andExpect(jsonPath("$.activity.length()").value(2));
    }
}
