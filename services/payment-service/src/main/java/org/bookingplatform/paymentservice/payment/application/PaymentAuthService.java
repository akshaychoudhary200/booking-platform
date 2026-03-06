package org.bookingplatform.paymentservice.payment.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bookingplatform.paymentservice.messaging.dto.AuthorizePaymentCommand;
import org.bookingplatform.paymentservice.messaging.dto.PaymentAuthorizedEvent;
import org.bookingplatform.paymentservice.messaging.dto.PaymentFailedEvent;
import org.bookingplatform.paymentservice.outbox.domain.OutboxEvent;
import org.bookingplatform.paymentservice.outbox.infrastructure.OutboxRepository;
import org.bookingplatform.paymentservice.payment.domain.Payment;
import org.bookingplatform.paymentservice.payment.domain.PaymentStatus;
import org.bookingplatform.paymentservice.payment.infrastructure.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Service
public class PaymentAuthService {

    private final PaymentRepository paymentRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final long callbackDelayMs;
    private final double duplicateChance;
    private final Random random = new Random();

    public PaymentAuthService(
            PaymentRepository paymentRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            @Value("${app.payment.callbackDelayMs}") long callbackDelayMs,
            @Value("${app.payment.duplicateCallbackChance}") double duplicateChance) {
        this.paymentRepository = paymentRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.callbackDelayMs = callbackDelayMs;
        this.duplicateChance = duplicateChance;
    }

    public void handleAuthorize(AuthorizePaymentCommand cmd) {
        // simulate provider delay OUTSIDE transaction
        sleep(callbackDelayMs);

        // 90% authorize, 10% fail (tweak if you want)
        boolean ok = random.nextDouble() < 0.9;

        if (ok) {
            authorizeIdempotent(cmd);
        } else {
            failIdempotent(cmd, "DECLINED");
        }
    }

    @Transactional
    protected void authorizeIdempotent(AuthorizePaymentCommand cmd) {
        // Idempotency: if we already processed this key, do nothing
        if (paymentRepository.findByPaymentIdempotencyKey(cmd.paymentIdempotencyKey()).isPresent()) {
            return;
        }

        UUID paymentId = UUID.randomUUID();
        try {
            paymentRepository.save(new Payment(
                    paymentId,
                    cmd.bookingId(),
                    cmd.userId(),
                    cmd.amountCents(),
                    PaymentStatus.AUTHORIZED,
                    cmd.paymentIdempotencyKey()));
        } catch (DataIntegrityViolationException e) {
            // another duplicate raced; treat as idempotent
            return;
        }

        String payload = toJson(new PaymentAuthorizedEvent(
                "PaymentAuthorized",
                cmd.bookingId(),
                cmd.userId(),
                paymentId,
                cmd.paymentIdempotencyKey()));

        // Write callback event to outbox (reliable)
        outboxRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                "Payment",
                paymentId,
                "PaymentAuthorized",
                payload));

        // Randomly write a duplicate callback (realistic provider behaviour)
        if (random.nextDouble() < duplicateChance) {
            outboxRepository.save(new OutboxEvent(
                    UUID.randomUUID(),
                    "Payment",
                    paymentId,
                    "PaymentAuthorized",
                    payload));
        }
    }

    @Transactional
    protected void failIdempotent(AuthorizePaymentCommand cmd, String reason) {
        if (paymentRepository.findByPaymentIdempotencyKey(cmd.paymentIdempotencyKey()).isPresent()) {
            return;
        }

        UUID paymentId = UUID.randomUUID();
        try {
            paymentRepository.save(new Payment(
                    paymentId,
                    cmd.bookingId(),
                    cmd.userId(),
                    cmd.amountCents(),
                    PaymentStatus.FAILED,
                    cmd.paymentIdempotencyKey()));
        } catch (DataIntegrityViolationException e) {
            return;
        }

        String payload = toJson(new PaymentFailedEvent(
                "PaymentFailed",
                cmd.bookingId(),
                cmd.userId(),
                reason,
                cmd.paymentIdempotencyKey()));

        outboxRepository.save(new OutboxEvent(
                UUID.randomUUID(),
                "Payment",
                paymentId,
                "PaymentFailed",
                payload));

        if (random.nextDouble() < duplicateChance) {
            outboxRepository.save(new OutboxEvent(
                    UUID.randomUUID(),
                    "Payment",
                    paymentId,
                    "PaymentFailed",
                    payload));
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("JSON serialize failed", e);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}