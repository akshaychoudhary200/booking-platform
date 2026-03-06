package org.bookingplatform.bookingservice.booking.dto;

import java.util.UUID;

public record CancelRejectedEvent(
                String type,
                UUID holdId,
                UUID bookingId,
                UUID eventId,
                UUID userId,
                String reason) {
}
