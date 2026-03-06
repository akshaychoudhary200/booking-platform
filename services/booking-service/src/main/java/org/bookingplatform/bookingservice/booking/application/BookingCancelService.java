package org.bookingplatform.bookingservice.booking.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bookingplatform.bookingservice.booking.domain.BookingStatus;
import org.bookingplatform.bookingservice.booking.dto.CancelRequestedCommand;
import org.bookingplatform.bookingservice.booking.repository.BookingRepository;
import org.bookingplatform.bookingservice.outbox.domain.OutboxEvent;
import org.bookingplatform.bookingservice.outbox.infra.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookingCancelService {

    private final BookingRepository bookingRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public BookingCancelService(
            BookingRepository bookingRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.bookingRepository = bookingRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void requestCancel(UUID userId, UUID bookingId, String cancelIdempotencyKey) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (!booking.getUserId().equals(userId)) {
            throw new IllegalStateException("Not your booking");
        }

        if (cancelIdempotencyKey.equals(booking.getCancelIdempotencyKey())) {
            return;
        }

        if (booking.getHoldId() == null) {
            throw new IllegalStateException("Booking has no hold to cancel");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.CANCEL_REQUESTED) {
            return;
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Confirmed booking cannot be cancelled by hold compensation");
        }

        booking.markCancellationRequested(cancelIdempotencyKey);

        String payload = toJson(new CancelRequestedCommand(
                "CancelRequested",
                booking.getBookingId(),
                booking.getUserId(),
                booking.getEventId(),
                booking.getHoldId(),
                cancelIdempotencyKey));

        outboxRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                "Booking",
                booking.getBookingId(),
                "CancelRequested",
                payload));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize CancelRequestedCommand", e);
        }
    }
}