package org.bookingplatform.bookingservice.booking.dto;

import java.util.UUID;

public record CancelRequestedCommand(
        String type,
        UUID bookingId,
        UUID userId,
        UUID eventId,
        UUID holdId,
        String cancelIdempotencyKey) {
}