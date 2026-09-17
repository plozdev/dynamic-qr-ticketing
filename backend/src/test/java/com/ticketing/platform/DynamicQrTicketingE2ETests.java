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
class DynamicQrTicketingE2ETests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("E2E Test: Full Ticketing Lifecycle - Event Creation -> Issuance -> Mobile Sync -> Web QR -> Gate Validation -> Anti-Replay -> Audit Log")
    void testFullTicketingLifecycle() throws Exception {
        Instant now = Instant.now();
        Instant start = now.plus(10, ChronoUnit.DAYS);
        Instant end = start.plus(4, ChronoUnit.HOURS);

        // 1. Create Event
        Map<String, Object> eventReq = Map.of(
                "name", "Grand Symphony Live",
                "description", "Live Concert 2026",
                "venueName", "My Dinh National Stadium",
                "venueAddress", "1 Le Duc Tho, Hanoi",
                "venueGates", List.of("GATE_A", "GATE_B"),
                "startDateTime", start.toString(),
                "endDateTime", end.toString()
        );

        MvcResult eventResult = mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andReturn();

        JsonNode eventNode = objectMapper.readTree(eventResult.getResponse().getContentAsString());
        String eventId = eventNode.get("id").asText();

        // 2. Get Event By Id
        mockMvc.perform(get("/api/v1/events/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Grand Symphony Live"));

        // 3. Issue Ticket
        UUID userId = UUID.randomUUID();
        Map<String, Object> ticketReq = Map.of(
                "eventId", eventId,
                "userId", userId.toString(),
                "categoryName", "VIP_ZONE",
                "attendeeEmail", "vip_fan@music.com"
        );

        MvcResult ticketResult = mockMvc.perform(post("/api/v1/tickets/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ticketId").isNotEmpty())
                .andReturn();

        JsonNode ticketNode = objectMapper.readTree(ticketResult.getResponse().getContentAsString());
        String ticketId = ticketNode.get("ticketId").asText();

        // 4. Mobile Key Sync (Provisioning)
        mockMvc.perform(get("/api/v1/tickets/" + ticketId + "/sync")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secretKeyBase64").isNotEmpty())
                .andExpect(jsonPath("$.serverTimeEpochSeconds").isNumber());

        // 5. Web Dynamic QR
        MvcResult qrResult = mockMvc.perform(get("/api/v1/tickets/" + ticketId + "/dynamic-qr")
                        .header("X-User-Id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dynamicPayload", startsWith("TICKETING:" + ticketId + ":")))
                .andReturn();

        JsonNode qrNode = objectMapper.readTree(qrResult.getResponse().getContentAsString());
        String dynamicPayload = qrNode.get("dynamicPayload").asText();

        // 6. Gate Validation (First Scan - GRANTED)
        mockMvc.perform(post("/api/v1/gates/GATE_A/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rawQrPayload", dynamicPayload))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value("GRANTED"));

        // 7. Anti-Replay Check (Second Scan of same token - 403 FORBIDDEN)
        mockMvc.perform(post("/api/v1/gates/GATE_A/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rawQrPayload", dynamicPayload))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("DENIED_REPLAY_ATTACK"));

        // 8. Offline Gate Sync
        mockMvc.perform(get("/api/v1/gates/GATE_A/sync-roster"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gateId").value("GATE_A"));

        // 9. Audit Logs
        mockMvc.perform(get("/api/v1/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))));
    }

    @Test
    @DisplayName("Validation Test: Invalid Date Range and Non-existent Resources")
    void testValidationFailures() throws Exception {
        Instant now = Instant.now();

        // Start after End Date -> 400
        Map<String, Object> invalidDates = Map.of(
                "name", "Invalid Event",
                "venueName", "Stadium",
                "startDateTime", now.plus(5, ChronoUnit.DAYS).toString(),
                "endDateTime", now.plus(2, ChronoUnit.DAYS).toString()
        );
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDates)))
                .andExpect(status().isBadRequest());

        // Get Non-existent Event -> 404
        mockMvc.perform(get("/api/v1/events/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Security Test: Expired QR Code Rejection")
    void testExpiredQrRejection() throws Exception {
        UUID fakeTicketId = UUID.randomUUID();
        long expiredTime = Instant.now().minus(60, ChronoUnit.SECONDS).getEpochSecond();
        String expiredPayload = "TICKETING:" + fakeTicketId + ":" + expiredTime + ":dummyToken";

        mockMvc.perform(post("/api/v1/gates/GATE_A/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("rawQrPayload", expiredPayload))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("DENIED_EXPIRED_QR"));
    }
}
