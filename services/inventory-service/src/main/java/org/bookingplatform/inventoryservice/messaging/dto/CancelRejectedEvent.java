package org.bookingplatform.inventoryservice.messaging.dto;

import java.util.UUID;

public record CancelRejectedEvent(
        String type,
        UUID holdId,
        UUID bookingId) {
}
