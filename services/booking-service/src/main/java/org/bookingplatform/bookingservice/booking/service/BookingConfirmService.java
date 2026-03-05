package org.bookingplatform.bookingservice.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bookingplatform.bookingservice.booking.domain.BookingStatus;
import org.bookingplatform.bookingservice.booking.dto.ConfirmRequestedCommand;
import org.bookingplatform.bookingservice.booking.repository.BookingRepository;
import org.bookingplatform.bookingservice.outbox.domain.OutboxEvent;
import org.bookingplatform.bookingservice.outbox.infra.OutboxRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookingConfirmService {

    private final BookingRepository bookingRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public BookingConfirmService(BookingRepository bookingRepository, OutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.bookingRepository = bookingRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void requestConfirm(UUID userId, UUID bookingId, String confirmIdempotencyKey) {

        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        // Ownership check (basic)
        if (!booking.getUserId().equals(userId)) {
            throw new IllegalStateException("Not your booking");
        }

        // Idempotent confirm retry
        if (confirmIdempotencyKey.equals(booking.getIdempotencyKey())) {
            return;
        }

        // Local safety gate (Inventory is still the final authority)
        if (booking.getStatus() != BookingStatus.HOLD_ACTIVE) {
            throw new IllegalStateException("Booking not in HOLD_ACTIVE state");
        }

        if (booking.getHoldId() == null) {
            throw new IllegalStateException("No holdId on booking");
        }

        booking.markConfirmRequested(confirmIdempotencyKey);

        String payload = toJson(new ConfirmRequestedCommand(
                "ConfirmRequested",
                booking.getBookingId(),
                booking.getUserId(),
                booking.getEventId(),
                booking.getHoldId(),
                confirmIdempotencyKey));

        try {
            outboxRepository.save(new OutboxEvent(
                    UUID.randomUUID(),
                    "Booking",
                    booking.getBookingId(),
                    "ConfirmRequested",
                    payload));
        } catch (DataIntegrityViolationException e) {
            // unique index (booking_id, confirm_idempotency_key) collision -> treat as
            // idempotent retry
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize ConfirmRequestedCommand", e);
        }
    }
}