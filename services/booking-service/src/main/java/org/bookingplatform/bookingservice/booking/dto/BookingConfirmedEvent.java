package org.bookingplatform.bookingservice.booking.dto;

import java.util.UUID;

public record BookingConfirmedEvent(
        String type,
        UUID bookingId,
        UUID userId,
        UUID eventId,
        UUID holdId) {
}