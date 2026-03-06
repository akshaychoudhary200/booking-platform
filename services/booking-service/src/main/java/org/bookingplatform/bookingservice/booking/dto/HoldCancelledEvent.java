package org.bookingplatform.bookingservice.booking.dto;

import java.util.UUID;

public record HoldCancelledEvent(
        String type,
        UUID holdId,
        UUID bookingId) {
}
