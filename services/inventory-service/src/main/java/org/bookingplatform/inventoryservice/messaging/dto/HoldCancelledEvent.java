package org.bookingplatform.inventoryservice.messaging.dto;

import java.util.UUID;

public record HoldCancelledEvent(
        String type,
        UUID holdId,
        UUID bookingId) {
}
