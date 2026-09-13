package com.ticketing.platform.eventcatalog.application.service;

import com.ticketing.platform.eventcatalog.EventCatalogExportedService;
import com.ticketing.platform.eventcatalog.application.dto.CreateEventCommand;
import com.ticketing.platform.eventcatalog.application.dto.EventResponse;
import com.ticketing.platform.eventcatalog.application.port.in.CreateEventUseCase;
import com.ticketing.platform.eventcatalog.application.port.in.GetEventQuery;
import com.ticketing.platform.eventcatalog.domain.repository.EventRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Khung sườn Application Service cho Event Catalog.
 * Hiện thực Use Cases và Boundary Interface (SPI) để module khác giao tiếp.
 * Bạn tự hoàn thiện logic nghiệp vụ cụ thể.
 */
@Service
@Transactional
public class EventCatalogService implements CreateEventUseCase, GetEventQuery, EventCatalogExportedService {

    private final EventRepository eventRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EventCatalogService(EventRepository eventRepository, ApplicationEventPublisher eventPublisher) {
        this.eventRepository = eventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EventResponse createEvent(CreateEventCommand command) {
        // TODO: Bạn tự triển khai:
        // 1. Tạo Venue và Event aggregate từ command
        // 2. Lưu thông qua eventRepository
        // 3. Chuyển đổi sang EventResponse
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai createEvent()");
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(UUID eventId) {
        // TODO: Bạn tự triển khai: Truy vấn event qua eventRepository, ném EntityNotFoundException nếu không thấy
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai getEventById()");
    }

    // --- Boundary Service (EventCatalogExportedService implementation) ---

    @Override
    @Transactional(readOnly = true)
    public boolean isEventActive(UUID eventId) {
        // TODO: Bạn tự triển khai: Kiểm tra trạng thái event có PUBLISHED không cho module ticket-issuance gọi sang
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public EventSummaryDto getEventSummary(UUID eventId) {
        // TODO: Bạn tự triển khai: Lấy tóm tắt thông tin event cho module khác gọi
        throw new UnsupportedOperationException("TODO: Bạn tự triển khai getEventSummary()");
    }
}
