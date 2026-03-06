package org.bookingplatform.bookingservice.booking.dto;

import java.util.UUID;

public record CancelBookingCommand(
        String type,
        UUID bookingId) {
}
