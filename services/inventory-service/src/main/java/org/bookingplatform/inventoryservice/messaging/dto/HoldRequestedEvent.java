package org.bookingplatform.inventoryservice.messaging.dto;

import java.util.UUID;

public record HoldRequestedEvent(
        String type,
        UUID bookingId,
        UUID userId,
        UUID eventId) {
}