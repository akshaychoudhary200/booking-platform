package org.bookingplatform.bookingservice.payment.dto;

import java.util.UUID;

public record AuthorizePaymentCommand(
        String type,
        UUID bookingId,
        UUID userId,
        long amountCents,
        String paymentIdempotencyKey) {
}