package org.bookingplatform.bookingservice.booking.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bookingplatform.bookingservice.booking.domain.Booking;
import org.bookingplatform.bookingservice.booking.domain.BookingStatus;
import org.bookingplatform.bookingservice.booking.dto.BookingView;
import org.bookingplatform.bookingservice.booking.dto.CreateBookingRequest;
import org.bookingplatform.bookingservice.booking.dto.HoldRequestedEvent;
import org.bookingplatform.bookingservice.booking.repository.BookingRepository;
import org.bookingplatform.bookingservice.outbox.domain.OutboxEvent;
import org.bookingplatform.bookingservice.outbox.infra.OutboxRepository;
import org.bookingplatform.bookingservice.waitingroom.application.EventAdmissionService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookingCommandService {

    private final BookingRepository bookingRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final EventAdmissionService eventAdmissionService;

    public BookingCommandService(
            BookingRepository bookingRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            EventAdmissionService eventAdmissionService) {
        this.bookingRepository = bookingRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.eventAdmissionService = eventAdmissionService;
    }

    @Transactional
    public BookingView createHoldIdempotent(UUID userId, String idempotencyKey, CreateBookingRequest req) {

        // 1) Idempotent retry: same user + same key => same booking
        var existing = bookingRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (existing.isPresent()) {
            Booking b = existing.get();
            return toView(b);
        }

        // 2) Create booking row first
        Booking booking = new Booking(
                UUID.randomUUID(),
                userId,
                req.eventId(),
                BookingStatus.HOLD_REQUESTED,
                idempotencyKey);

        try {
            bookingRepository.saveAndFlush(booking);
        } catch (DataIntegrityViolationException e) {
            // concurrent duplicate request raced and won
            Booking winner = bookingRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                    .orElseThrow(() -> e);
            return toView(winner);
        }

        // 3) Admission gate: protect hot event from overload
        boolean admitted = eventAdmissionService.tryAcquireSlot(booking.getEventId(), booking.getBookingId());

        if (!admitted) {
            // simplest first version: reject immediately
            booking.setStatus(BookingStatus.HOLD_REJECTED);
            return toView(booking);
        }

        // 4) Admitted => write outbox event in same DB transaction
        String payloadJson = toJson(new HoldRequestedEvent(
                "HoldRequested",
                booking.getBookingId(),
                userId,
                req.eventId()));

        outboxRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                "Booking",
                booking.getBookingId(),
                "HoldRequested",
                payloadJson));

        return toView(booking);
    }

    private BookingView toView(Booking booking) {
        return new BookingView(
                booking.getBookingId(),
                booking.getEventId(),
                booking.getStatus().name(),
                booking.getCreatedAt());
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize outbox payload", ex);
        }
    }
}