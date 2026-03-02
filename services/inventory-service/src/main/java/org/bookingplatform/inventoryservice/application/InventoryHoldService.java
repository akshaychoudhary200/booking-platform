package org.bookingplatform.inventoryservice.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bookingplatform.inventoryservice.domain.Hold;
import org.bookingplatform.inventoryservice.domain.HoldStatus;
import org.bookingplatform.inventoryservice.infrastructure.HoldRepository;
import org.bookingplatform.inventoryservice.infrastructure.OutboxRepository;
import org.bookingplatform.inventoryservice.infrastructure.SeatRepository;
import org.bookingplatform.inventoryservice.messaging.dto.HoldCreatedEvent;
import org.bookingplatform.inventoryservice.messaging.dto.HoldRejectedEvent;
import org.bookingplatform.inventoryservice.outbox.OutboxEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class InventoryHoldService {

    private final HoldRepository holdRepository;
    private final SeatRepository seatRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final int seatsPerHold;
    private final long ttlSeconds;

    public InventoryHoldService(
            HoldRepository holdRepository,
            SeatRepository seatRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            @Value("${app.inventory.hold.seats}") int seatsPerHold,
            @Value("${app.inventory.hold.ttlSeconds}") long ttlSeconds) {
        this.holdRepository = holdRepository;
        this.seatRepository = seatRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.seatsPerHold = seatsPerHold;
        this.ttlSeconds = ttlSeconds;
    }

    @Transactional
    public void handleHoldRequested(UUID bookingId, UUID userId, UUID eventId) {

        // Idempotency under Kafka redelivery
        if (holdRepository.findByBookingId(bookingId).isPresent()) {
            return;
        }

        UUID holdId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(ttlSeconds, ChronoUnit.SECONDS);

        var seats = seatRepository.lockNextAvailableSeats(eventId, seatsPerHold);

        if (seats.size() < seatsPerHold) {
            Hold rejected = new Hold(holdId, bookingId, userId, eventId, HoldStatus.REJECTED, expiresAt);
            holdRepository.save(rejected);

            String payload = toJson(new HoldRejectedEvent(
                    "HoldRejected",
                    bookingId,
                    userId,
                    eventId,
                    "INSUFFICIENT_CAPACITY"));

            outboxRepository.save(new OutboxEvent(
                    UUID.randomUUID(),
                    "Hold",
                    holdId,
                    "HoldRejected",
                    payload));
            return;
        }

        Hold hold = new Hold(holdId, bookingId, userId, eventId, HoldStatus.ACTIVE, expiresAt);
        holdRepository.save(hold);

        for (var seat : seats) {
            seat.hold(holdId, expiresAt);
        }

        String payload = toJson(new HoldCreatedEvent(
                "HoldCreated",
                bookingId,
                userId,
                eventId,
                holdId,
                expiresAt,
                seatsPerHold));

        outboxRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                "Hold",
                holdId,
                "HoldCreated",
                payload));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }
}