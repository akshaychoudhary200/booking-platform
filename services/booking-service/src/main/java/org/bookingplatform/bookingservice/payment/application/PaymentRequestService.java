package org.bookingplatform.bookingservice.payment.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bookingplatform.bookingservice.booking.domain.BookingStatus;
import org.bookingplatform.bookingservice.booking.repository.BookingRepository;
import org.bookingplatform.bookingservice.outbox.domain.OutboxEvent;
import org.bookingplatform.bookingservice.outbox.infra.OutboxRepository;
import org.bookingplatform.bookingservice.payment.dto.AuthorizePaymentCommand;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentRequestService {

    private final BookingRepository bookingRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public PaymentRequestService(BookingRepository bookingRepository, OutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.bookingRepository = bookingRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void requestPaymentIfNeeded(UUID bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        // Only request payment once, and only if hold is active
        if (booking.getStatus() == BookingStatus.PAYMENT_REQUESTED
                || booking.getStatus() == BookingStatus.PAYMENT_AUTHORIZED
                || booking.getStatus() == BookingStatus.PAYMENT_FAILED
                || booking.getStatus() == BookingStatus.CONFIRM_REQUESTED
                || booking.getStatus() == BookingStatus.CONFIRMED
                || booking.getStatus() == BookingStatus.CONFIRM_REJECTED) {
            return;
        }

        if (booking.getStatus() != BookingStatus.HOLD_ACTIVE) {
            return;
        }

        // Payment idempotency key: stable per booking (simple, deterministic)
        String paymentKey = "pay-" + bookingId;

        booking.markPaymentRequested(paymentKey);

        // For now fixed price; later derive from seatsHeld
        long amountCents = 5000;

        String payload = toJson(new AuthorizePaymentCommand(
                "AuthorizePayment",
                booking.getBookingId(),
                booking.getUserId(),
                amountCents,
                paymentKey));

        outboxRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                "Booking",
                booking.getBookingId(),
                "AuthorizePayment",
                payload));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize AuthorizePaymentCommand", e);
        }
    }
}