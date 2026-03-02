
package org.bookingplatform.bookingservice.booking.dto;

import java.util.UUID;

public record HoldRequestedEvent(
        String type,
        UUID bookingId,
        UUID userId,
        UUID eventId) {
}