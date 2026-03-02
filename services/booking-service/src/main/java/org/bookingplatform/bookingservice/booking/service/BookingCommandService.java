package org.bookingplatform.bookingservice.booking.service;

import org.bookingplatform.bookingservice.booking.domain.Booking;
import org.bookingplatform.bookingservice.booking.domain.BookingStatus;
import org.bookingplatform.bookingservice.booking.dto.BookingView;
import org.bookingplatform.bookingservice.booking.dto.CreateBookingRequest;
import org.bookingplatform.bookingservice.booking.dto.HoldRequestedEvent;
import org.bookingplatform.bookingservice.booking.repository.BookingRepository;
import org.bookingplatform.bookingservice.outbox.domain.OutboxEvent;
import org.bookingplatform.bookingservice.outbox.infra.OutboxRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
public class BookingCommandService {

    private final BookingRepository bookingRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public BookingCommandService(BookingRepository bookingRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.bookingRepository = bookingRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public BookingView create(UUID userId, CreateBookingRequest req) {

        Booking booking = new Booking(
                UUID.randomUUID(),
                userId,
                req.eventId(),
                BookingStatus.HOLD_REQUESTED,
                null);

        bookingRepository.save(booking);

        return new BookingView(
                booking.getBookingId(),
                booking.getEventId(),
                booking.getStatus().name(),
                booking.getCreatedAt());
    }

    @Transactional
    public BookingView createIdempotent(UUID userId, String idempotencyKey, CreateBookingRequest req) {

        var existing = bookingRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
        if (existing.isPresent()) {
            var b = existing.get();
            return new BookingView(b.getBookingId(), b.getEventId(), b.getStatus().name(), b.getCreatedAt());
        }

        Booking booking = new Booking(
                UUID.randomUUID(),
                userId,
                req.eventId(),
                BookingStatus.HOLD_REQUESTED,
                idempotencyKey);

        try {
            bookingRepository.saveAndFlush(booking);

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

            return new BookingView(booking.getBookingId(), booking.getEventId(), booking.getStatus().name(),
                    booking.getCreatedAt());

        } catch (DataIntegrityViolationException e) {

            Booking winner = bookingRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                    .orElseThrow(() -> e);
            return new BookingView(winner.getBookingId(), winner.getEventId(), winner.getStatus().name(),
                    winner.getCreatedAt());
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize outbox payload", ex);
        }
    }

}