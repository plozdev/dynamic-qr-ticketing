package com.ticketing.platform.ticketissuance.infrastructure.sse;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TicketSseServiceTest {

    @Test
    void sendsOneUpdateAndHeartbeatWhenEmitterIsIndexedByUserAndTicket() throws IOException {
        TicketSseService service = new TicketSseService();
        UUID userId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        SseEmitter emitter = mock(SseEmitter.class);
        index(service, "userEmitters").put(userId, new CopyOnWriteArrayList<>(List.of(emitter)));
        index(service, "ticketEmitters").put(ticketId, new CopyOnWriteArrayList<>(List.of(emitter)));

        service.sendTicketStatusUpdate(userId, ticketId, "CHECKED_IN", "GATE_A", Instant.now());
        service.sendHeartbeat();

        verify(emitter, times(2)).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    void failedEmitterIsRemovedFromBothIndexes() throws IOException {
        TicketSseService service = new TicketSseService();
        UUID userId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        SseEmitter emitter = mock(SseEmitter.class);
        doThrow(new IOException("closed")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));
        index(service, "userEmitters").put(userId, new CopyOnWriteArrayList<>(List.of(emitter)));
        index(service, "ticketEmitters").put(ticketId, new CopyOnWriteArrayList<>(List.of(emitter)));

        service.sendTicketStatusUpdate(userId, ticketId, "CHECKED_IN", "GATE_A", Instant.now());

        verify(emitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
        verify(emitter).complete();
        assertTrue(index(service, "userEmitters").isEmpty());
        assertTrue(index(service, "ticketEmitters").isEmpty());
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> index(
            TicketSseService service, String fieldName) {
        return (ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>>)
                ReflectionTestUtils.getField(service, fieldName);
    }
}
