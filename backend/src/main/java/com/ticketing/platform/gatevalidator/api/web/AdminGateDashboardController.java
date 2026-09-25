package com.ticketing.platform.gatevalidator.api.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Read model for the admin console. Every count comes from issued tickets or persisted gate scans. */
@RestController
@RequestMapping("/api/v1/admin/events")
@RequiredArgsConstructor
public class AdminGateDashboardController {
    private final JdbcTemplate jdbc;

    public record GateRow(String gateId, long granted, long scansPerMinute, long denied) {}
    public record ActivityRow(UUID ticketId, String gateId, String status, Instant scannedAt) {}
    public record Dashboard(UUID eventId, long issued, long checkedIn, long replayAlerts,
                            List<GateRow> gates, List<ActivityRow> activity, Instant refreshedAt) {}

    @GetMapping("/{eventId}/gate-dashboard")
    public Dashboard getDashboard(@PathVariable UUID eventId) {
        Long exists = jdbc.queryForObject("SELECT COUNT(*) FROM events WHERE id = ?", Long.class, eventId);
        if (exists == null || exists == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        long[] tickets = jdbc.queryForObject("""
                SELECT COUNT(*), COUNT(*) FILTER (WHERE status = 'USED')
                FROM tickets WHERE event_id = ?
                """, (rs, row) -> new long[]{rs.getLong(1), rs.getLong(2)}, eventId);
        Long replay = jdbc.queryForObject("""
                SELECT COUNT(*) FROM gate_scan_logs g JOIN tickets t ON t.id = g.ticket_id
                WHERE t.event_id = ? AND g.status = 'DENIED_REPLAY_ATTACK'
                """, Long.class, eventId);
        List<GateRow> observedGates = jdbc.query("""
                SELECT g.gate_id,
                       COUNT(*) FILTER (WHERE g.status = 'GRANTED') AS granted,
                       COUNT(*) FILTER (WHERE g.status = 'GRANTED' AND g.scanned_at >= ?) AS per_minute,
                       COUNT(*) FILTER (WHERE g.status <> 'GRANTED') AS denied
                FROM gate_scan_logs g JOIN tickets t ON t.id = g.ticket_id
                WHERE t.event_id = ? GROUP BY g.gate_id ORDER BY g.gate_id
                """, (rs, row) -> new GateRow(rs.getString(1), rs.getLong(2), rs.getLong(3), rs.getLong(4)),
                java.sql.Timestamp.from(Instant.now().minusSeconds(60)), eventId);
        java.util.Map<String, GateRow> gatesById = new java.util.LinkedHashMap<>();
        jdbc.queryForList("SELECT gate_name FROM event_venue_gates WHERE event_id = ? ORDER BY gate_name",
                String.class, eventId).forEach(gate -> gatesById.put(gate, new GateRow(gate, 0, 0, 0)));
        observedGates.forEach(gate -> gatesById.put(gate.gateId(), gate));
        List<ActivityRow> activity = jdbc.query("""
                SELECT g.ticket_id, g.gate_id, g.status, g.scanned_at
                FROM gate_scan_logs g JOIN tickets t ON t.id = g.ticket_id
                WHERE t.event_id = ? ORDER BY g.scanned_at DESC LIMIT 50
                """, (rs, row) -> new ActivityRow((UUID) rs.getObject(1), rs.getString(2), rs.getString(3),
                rs.getTimestamp(4).toInstant()), eventId);
        return new Dashboard(eventId, tickets[0], tickets[1], replay == null ? 0 : replay,
                List.copyOf(gatesById.values()), activity, Instant.now());
    }
}
