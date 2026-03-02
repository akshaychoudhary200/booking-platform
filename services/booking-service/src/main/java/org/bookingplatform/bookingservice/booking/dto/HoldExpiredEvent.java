package org.bookingplatform.bookingservice.booking.dto;

import java.time.Instant;
import java.util.UUID;

public record HoldExpiredEvent(
        String type,
        UUID bookingId,
        UUID userId,
        UUID eventId,
        UUID holdId,
        Instant expiresAt) {
}