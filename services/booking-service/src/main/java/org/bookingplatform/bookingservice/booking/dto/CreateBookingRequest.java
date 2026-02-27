package org.bookingplatform.bookingservice.booking.dto;

import java.util.UUID;

public record CreateBookingRequest(UUID eventId) {
}