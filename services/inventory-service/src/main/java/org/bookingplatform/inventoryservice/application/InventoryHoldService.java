package org.bookingplatform.inventoryservice.application;

import org.bookingplatform.inventoryservice.domain.Hold;
import org.bookingplatform.inventoryservice.domain.HoldStatus;
import org.bookingplatform.inventoryservice.infrastructure.HoldRepository;
import org.bookingplatform.inventoryservice.infrastructure.SeatRepository;
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
    private final int seatsPerHold;
    private final long ttlSeconds;

    public InventoryHoldService(
            HoldRepository holdRepository,
            SeatRepository seatRepository,
            @Value("${app.inventory.hold.seats}") int seatsPerHold,
            @Value("${app.inventory.hold.ttlSeconds}") long ttlSeconds) {
        this.holdRepository = holdRepository;
        this.seatRepository = seatRepository;
        this.seatsPerHold = seatsPerHold;
        this.ttlSeconds = ttlSeconds;
    }

    @Transactional
    public void handleHoldRequested(UUID bookingId, UUID userId, UUID eventId) {

        // Idempotency under Kafka redelivery: if we already created a hold for this
        // booking, do nothing.
        if (holdRepository.findByBookingId(bookingId).isPresent()) {
            return;
        }

        UUID holdId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(ttlSeconds, ChronoUnit.SECONDS);

        // Lock and take seats atomically
        var seats = seatRepository.lockNextAvailableSeats(eventId, seatsPerHold);
        if (seats.size() < seatsPerHold) {
            // Not enough seats. Record a rejected hold (still idempotent) and exit.
            Hold rejected = new Hold(holdId, bookingId, userId, eventId, HoldStatus.REJECTED, expiresAt);
            holdRepository.save(rejected);
            return;
        }

        // Create hold row
        Hold hold = new Hold(holdId, bookingId, userId, eventId, HoldStatus.ACTIVE, expiresAt);
        holdRepository.save(hold);

        // Mark seats held
        for (var seat : seats) {
            seat.hold(holdId, expiresAt);
        }
        // seats are managed entities; changes flush at commit
    }
}