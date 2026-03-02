package org.bookingplatform.inventoryservice.messaging.dto;

import java.util.UUID;

public record HoldRejectedEvent(
        String type,
        UUID bookingId,
        UUID userId,
        UUID eventId,
        String reason) {
}