package org.bookingplatform.bookingservice.inventory.consumer;

import java.util.UUID;

public record ConfirmRejectedEvent(
        String type,
        UUID bookingId,
        UUID userId,
        UUID eventId,
        UUID holdId,
        String reason) {
}