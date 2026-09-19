package com.ticketing.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Test
    @DisplayName("Claim Ticket Test: 1-Click Ticket Claiming decreases available tickets and issues valid ticket")
    void test1ClickTicketClaiming() throws Exception {
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventReq)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode eventNode = objectMapper.readTree(eventResult.getResponse().getContentAsString());
        String eventId = eventNode.get("id").asText();
        int initialAvailable = eventNode.get("availableTickets").asInt();

        // 2. User claims ticket via 1-Click API: POST /api/v1/events/{eventId}/claim
        UUID claimerUserId = UUID.randomUUID();
        Map<String, Object> claimReq = Map.of(
                "categoryName", "VIP Diamond",
                "attendeeName", "Phan Thanh Sơn"
        );

        MvcResult claimResult = mockMvc.perform(post("/api/v1/events/" + eventId + "/claim")
                        .header("X-User-Id", claimerUserId.toString())
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
                        .header("X-User-Id", claimerUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ticketId").value(ticketId))
                .andExpect(jsonPath("$[0].eventName").value("V-Pop Super Soundwave 2026"))
                .andExpect(jsonPath("$[0].attendeeName").value("Phan Thanh Sơn"));

        // 5. Verify user can sync their key for mobile offline QR generation
        mockMvc.perform(get("/api/v1/tickets/" + ticketId + "/sync")
                        .header("X-User-Id", claimerUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secretKeyBase64").isNotEmpty());
    }
}
