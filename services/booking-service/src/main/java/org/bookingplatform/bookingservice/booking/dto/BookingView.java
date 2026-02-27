package org.bookingplatform.bookingservice.booking.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingView(
        UUID bookingId,
        UUID eventId,
        String status,
        Instant createdAt) {
}