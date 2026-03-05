package org.bookingplatform.inventoryservice.messaging.dto;

import java.util.UUID;

public record BookingConfirmedEvent(
        String type,
        UUID bookingId,
        UUID userId,
        UUID eventId,
        UUID holdId) {
}