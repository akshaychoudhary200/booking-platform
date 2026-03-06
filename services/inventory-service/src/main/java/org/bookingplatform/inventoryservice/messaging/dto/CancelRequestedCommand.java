package org.bookingplatform.inventoryservice.messaging.dto;

import java.util.UUID;

public record CancelRequestedCommand(
                String type,
                UUID bookingId,
                UUID eventId,
                UUID holdId,
                String cancelIdempotencyKey) {

        public UUID userId() {
                return null;
        }
}
