package org.bookingplatform.bookingservice.payment.dto;

import java.util.UUID;

public record PaymentAuthorizedEvent(
        String type,
        UUID bookingId,
        UUID userId,
        UUID paymentId,
        String paymentIdempotencyKey) {
}