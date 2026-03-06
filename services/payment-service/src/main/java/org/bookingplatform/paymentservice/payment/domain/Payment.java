package org.bookingplatform.paymentservice.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "booking_id", nullable = false, unique = true)
    private UUID bookingId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "payment_idempotency_key", nullable = false, unique = true)
    private String paymentIdempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Payment() {
    }

    public Payment(UUID paymentId, UUID bookingId, UUID userId, long amountCents, PaymentStatus status,
            String paymentIdempotencyKey) {
        this.paymentId = paymentId;
        this.bookingId = bookingId;
        this.userId = userId;
        this.amountCents = amountCents;
        this.status = status;
        this.paymentIdempotencyKey = paymentIdempotencyKey;
        this.createdAt = Instant.now();
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public UUID getUserId() {
        return userId;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getPaymentIdempotencyKey() {
        return paymentIdempotencyKey;
    }
}