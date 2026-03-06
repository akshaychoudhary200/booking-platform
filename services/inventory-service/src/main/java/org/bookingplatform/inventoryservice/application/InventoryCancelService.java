package org.bookingplatform.inventoryservice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bookingplatform.inventoryservice.domain.HoldStatus;
import org.bookingplatform.inventoryservice.infrastructure.HoldRepository;
import org.bookingplatform.inventoryservice.infrastructure.OutboxRepository;
import org.bookingplatform.inventoryservice.infrastructure.SeatRepository;
import org.bookingplatform.inventoryservice.messaging.dto.CancelRejectedEvent;
import org.bookingplatform.inventoryservice.messaging.dto.HoldCancelledEvent;
import org.bookingplatform.inventoryservice.outbox.OutboxEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InventoryCancelService {

    private final HoldRepository holdRepository;
    private final SeatRepository seatRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public InventoryCancelService(
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
    public void cancel(UUID bookingId, UUID holdId, UUID userId, UUID eventId) {
        var hold = holdRepository.findForUpdateByBookingId(bookingId)
                .orElseThrow(() -> new IllegalStateException("Hold not found for booking: " + bookingId));

        if (!hold.getHoldId().equals(holdId)) {
            reject(bookingId, userId, eventId, holdId, "HOLD_ID_MISMATCH");
            return;
        }

        if (hold.getStatus() == HoldStatus.CANCELLED || hold.getStatus() == HoldStatus.EXPIRED) {
            // idempotent success: already gone
            emitCancelled(bookingId, userId, eventId, holdId);
            return;
        }

        if (hold.getStatus() == HoldStatus.CONFIRMED) {
            reject(bookingId, userId, eventId, holdId, "ALREADY_CONFIRMED");
            return;
        }

        if (hold.getStatus() == HoldStatus.REJECTED) {
            emitCancelled(bookingId, userId, eventId, holdId);
            return;
        }

        if (hold.getStatus() == HoldStatus.ACTIVE) {
            seatRepository.releaseSeatsForHold(holdId);
            hold.markCancelled();
            emitCancelled(bookingId, userId, eventId, holdId);
            return;
        }

        reject(bookingId, userId, eventId, holdId, "INVALID_HOLD_STATE");
    }

    private void emitCancelled(UUID bookingId, UUID userId, UUID eventId, UUID holdId) {
        String payload = toJson(new HoldCancelledEvent(
                "HoldCancelled",
                bookingId,
                userId,
                eventId,
                holdId));

        outboxRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                "Hold",
                holdId,
                "HoldCancelled",
                payload));
    }

    private void reject(UUID bookingId, UUID userId, UUID eventId, UUID holdId, String reason) {
        String payload = toJson(new CancelRejectedEvent(
                "CancelRejected",
                bookingId,
                userId,
                eventId,
                holdId,
                reason));

        outboxRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                "Hold",
                holdId,
                "CancelRejected",
                payload));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize cancel event", e);
        }
    }
}