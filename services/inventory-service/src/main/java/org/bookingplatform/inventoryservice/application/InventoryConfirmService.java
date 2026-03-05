package org.bookingplatform.inventoryservice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bookingplatform.inventoryservice.domain.HoldStatus;
import org.bookingplatform.inventoryservice.infrastructure.HoldRepository;
import org.bookingplatform.inventoryservice.infrastructure.OutboxRepository;
import org.bookingplatform.inventoryservice.infrastructure.SeatRepository;
import org.bookingplatform.inventoryservice.messaging.dto.BookingConfirmedEvent;
import org.bookingplatform.inventoryservice.messaging.dto.ConfirmRejectedEvent;
import org.bookingplatform.inventoryservice.outbox.OutboxEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InventoryConfirmService {

    private final HoldRepository holdRepository;
    private final SeatRepository seatRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public InventoryConfirmService(
            HoldRepository holdRepository,
            SeatRepository seatRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.holdRepository = holdRepository;
        this.seatRepository = seatRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void confirm(UUID bookingId, UUID holdId, UUID userId, UUID eventId) {

        var hold = holdRepository.findForUpdateByBookingId(bookingId)
                .orElseThrow(() -> new IllegalStateException("Hold not found for booking: " + bookingId));

        // If already confirmed, idempotent ignore
        if (hold.getStatus() == HoldStatus.CONFIRMED) {
            return;
        }

        // Fence: must be ACTIVE and not expired
        if (hold.getStatus() != HoldStatus.ACTIVE) {
            reject(bookingId, userId, eventId, holdId, "HOLD_NOT_ACTIVE");
            return;
        }
        if (!hold.getHoldId().equals(holdId)) {
            reject(bookingId, userId, eventId, holdId, "HOLD_ID_MISMATCH");
            return;
        }
        if (hold.getExpiresAt().isBefore(java.time.Instant.now())) {
            reject(bookingId, userId, eventId, holdId, "HOLD_EXPIRED");
            return;
        }

        int confirmedSeats = seatRepository.confirmSeatsForHold(holdId);
        if (confirmedSeats <= 0) {
            reject(bookingId, userId, eventId, holdId, "NO_HELD_SEATS_OR_EXPIRED");
            return;
        }

        // Mark hold confirmed (same transaction)
        hold.markConfirmed();

        // Emit fact
        String payload = toJson(new BookingConfirmedEvent(
                "BookingConfirmed",
                bookingId,
                userId,
                eventId,
                holdId));

        outboxRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                "Hold",
                holdId,
                "BookingConfirmed",
                payload));
    }

    private void reject(UUID bookingId, UUID userId, UUID eventId, UUID holdId, String reason) {
        String payload = toJson(new ConfirmRejectedEvent(
                "ConfirmRejected",
                bookingId,
                userId,
                eventId,
                holdId,
                reason));
        outboxRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                "Hold",
                holdId,
                "ConfirmRejected",
                payload));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event", e);
        }
    }
}