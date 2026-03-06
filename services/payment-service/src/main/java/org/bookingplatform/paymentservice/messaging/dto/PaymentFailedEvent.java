package org.bookingplatform.paymentservice.messaging.dto;

import java.util.UUID;

public record PaymentFailedEvent(
        String type,
        UUID bookingId,
        UUID userId,
        String reason,
        String paymentIdempotencyKey) {
}