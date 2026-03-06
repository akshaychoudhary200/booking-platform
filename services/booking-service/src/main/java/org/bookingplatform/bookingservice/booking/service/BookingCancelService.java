package org.bookingplatform.bookingservice.booking.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bookingplatform.bookingservice.booking.domain.BookingStatus;
import org.bookingplatform.bookingservice.booking.dto.CancelBookingCommand;
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

    public BookingCancelService(BookingRepository bookingRepository, OutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.bookingRepository = bookingRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void requestCancel(UUID bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.CANCEL_REJECTED) {
            return;
        }

        booking.markCancellationRequested();

        String payload;
        try {
            payload = objectMapper.writeValueAsString(new CancelBookingCommand("CancelRequested", bookingId));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize CancelBookingCommand", e);
        }

        outboxRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                "Booking",
                bookingId,
                "CancelRequested",
                payload));
    }
}
