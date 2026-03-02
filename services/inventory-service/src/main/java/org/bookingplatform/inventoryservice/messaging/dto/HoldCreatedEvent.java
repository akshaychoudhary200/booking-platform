package org.bookingplatform.inventoryservice.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record HoldCreatedEvent(
        String type,
        UUID bookingId,
        UUID userId,
        UUID eventId,
        UUID holdId,
        Instant expiresAt,
        int seatsHeld) {
}