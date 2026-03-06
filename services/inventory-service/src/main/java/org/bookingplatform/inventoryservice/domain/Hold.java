package org.bookingplatform.inventoryservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hold")
public class Hold {

    @Id
    @Column(name = "hold_id", nullable = false)
    private UUID holdId;

    @Column(name = "booking_id", nullable = false, unique = true)
    private UUID bookingId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HoldStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Hold() {
    }

    public Hold(UUID holdId, UUID bookingId, UUID userId, UUID eventId, HoldStatus status, Instant expiresAt) {
        this.holdId = holdId;
        this.bookingId = bookingId;
        this.userId = userId;
        this.eventId = eventId;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public UUID getHoldId() {
        return holdId;
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

    public HoldStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void markRejected() {
        this.status = HoldStatus.REJECTED;
    }

    public void markExpired() {
        this.status = HoldStatus.EXPIRED;
    }

    public void markConfirmed() {
        this.status = HoldStatus.CONFIRMED;
    }

    public void markCancelled() {
        this.status = HoldStatus.CANCELLED;
    }
}