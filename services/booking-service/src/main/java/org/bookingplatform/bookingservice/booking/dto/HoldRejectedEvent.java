package org.bookingplatform.bookingservice.booking.dto;

import java.util.UUID;

public record HoldRejectedEvent(
        String type,
        UUID bookingId,
        UUID userId,
        UUID eventId,
        String reason) {
}