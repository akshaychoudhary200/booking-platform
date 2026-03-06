package org.bookingplatform.bookingservice.booking.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "booking")
public class Booking {

    @Id
    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "hold_id")
    private UUID holdId;

    @Column(name = "hold_expires_at")
    private Instant holdExpiresAt;

    @Column(name = "seats_held")
    private Integer seatsHeld;

    @Column(name = "confirm_idempotency_key")
    private String confirmIdempotencyKey;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "payment_idempotency_key")
    private String paymentIdempotencyKey;

    @Column(name = "cancel_idempotency_key")
    private String cancelIdempotencyKey;

    protected Booking() {
    }

    public Booking(UUID bookingId, UUID userId, UUID eventId, BookingStatus status, String idempotencyKey) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.eventId = eventId;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public String getPaymentIdempotencyKey() {
        return paymentIdempotencyKey;
    }

    public void markPaymentRequested(String paymentIdempotencyKey) {
        this.status = BookingStatus.PAYMENT_REQUESTED;
        this.paymentIdempotencyKey = paymentIdempotencyKey;
        this.updatedAt = Instant.now();
    }

    public void markPaymentAuthorized(UUID paymentId) {
        this.status = BookingStatus.PAYMENT_AUTHORIZED;
        this.paymentId = paymentId;
        this.updatedAt = Instant.now();
    }

    public void markPaymentFailed() {
        this.status = BookingStatus.PAYMENT_FAILED;
        this.updatedAt = Instant.now();
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void markHoldActive(UUID holdId, Instant expiresAt, int seatsHeld) {
        this.status = BookingStatus.HOLD_ACTIVE;
        this.holdId = holdId;
        this.holdExpiresAt = expiresAt;
        this.seatsHeld = seatsHeld;
        this.updatedAt = Instant.now();
    }

    public void markHoldRejected() {
        this.status = BookingStatus.HOLD_REJECTED;
        this.updatedAt = Instant.now();
    }

    public void markHoldExpired() {
        this.status = BookingStatus.HOLD_EXPIRED;
        this.updatedAt = Instant.now();
    }

    public void markConfirmRequested(String idempotencyKey) {
        this.status = BookingStatus.CONFIRM_REQUESTED;
        this.confirmIdempotencyKey = idempotencyKey;
        this.updatedAt = Instant.now();
    }

    public void markConfirmed() {
        this.status = BookingStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    public void markConfirmRejected() {
        this.status = BookingStatus.CONFIRM_REJECTED;
        this.updatedAt = Instant.now();
    }

    public UUID getHoldId() {
        return null;
    }

    public void markCancellationRequested() {
    }

    public void markCancellationRequested(String idempotencyKey) {
        this.status = BookingStatus.CANCEL_REQUESTED;
        this.cancelIdempotencyKey = idempotencyKey;
        this.updatedAt = Instant.now();
    }

    public void markCancelled() {
        this.status = BookingStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void markCancelRejected() {
        this.status = BookingStatus.CANCEL_REJECTED;
        this.updatedAt = Instant.now();
    }

    public String getCancelIdempotencyKey() {
        return cancelIdempotencyKey;
    }

}