package org.bookingplatform.bookingservice.payment.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bookingplatform.bookingservice.booking.application.BookingPaymentResultService;
import org.bookingplatform.bookingservice.booking.service.BookingConfirmService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventsConsumer {

    private final ObjectMapper objectMapper;
    private final BookingPaymentResultService bookingPaymentResultService;
    private final BookingConfirmService bookingConfirmService;

    public PaymentEventsConsumer(ObjectMapper objectMapper,
            BookingPaymentResultService bookingPaymentResultService,
            BookingConfirmService bookingConfirmService) {
        this.objectMapper = objectMapper;
        this.bookingPaymentResultService = bookingPaymentResultService;
        this.bookingConfirmService = bookingConfirmService;
    }

    @KafkaListener(topics = "payment.events")
    public void onMessage(String message, Acknowledgment ack) throws Exception {
        JsonNode root = objectMapper.readTree(message);
        String type = root.path("type").asText();

        if ("PaymentAuthorized".equals(type)) {
            var evt = objectMapper.treeToValue(root,
                    org.bookingplatform.bookingservice.payment.dto.PaymentAuthorizedEvent.class);

            // DB-first: mark payment authorized idempotently
            boolean shouldConfirm = bookingPaymentResultService.applyPaymentAuthorized(
                    evt.bookingId(), evt.paymentId(), evt.paymentIdempotencyKey());

            // Only request confirm after payment authorized, and only once
            if (shouldConfirm) {
                bookingConfirmService.requestConfirm(evt.userId(), evt.bookingId(), "confirm-" + evt.bookingId());
            }

            ack.acknowledge();
            return;
        }

        if ("PaymentFailed".equals(type)) {
            var evt = objectMapper.treeToValue(root,
                    org.bookingplatform.bookingservice.payment.dto.PaymentFailedEvent.class);
            bookingPaymentResultService.applyPaymentFailed(evt.bookingId(), evt.paymentIdempotencyKey());
            ack.acknowledge();
            return;
        }

        ack.acknowledge();
    }
}