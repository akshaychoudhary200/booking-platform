package org.bookingplatform.inventoryservice.messaging.dto;

import java.util.UUID;

public record ConfirmRequestedCommand(
        String type,
        UUID bookingId,
        UUID userId,
        UUID eventId,
        UUID holdId,
        String confirmIdempotencyKey) {
}