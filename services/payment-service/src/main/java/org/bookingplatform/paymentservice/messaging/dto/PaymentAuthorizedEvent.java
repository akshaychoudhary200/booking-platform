package org.bookingplatform.paymentservice.messaging.dto;

import java.util.UUID;

public record PaymentAuthorizedEvent(
        String type,
        UUID bookingId,
        UUID userId,
        UUID paymentId,
        String paymentIdempotencyKey) {
}