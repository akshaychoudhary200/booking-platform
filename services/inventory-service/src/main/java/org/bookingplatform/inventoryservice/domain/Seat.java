package org.bookingplatform.inventoryservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seat")
@IdClass(SeatKey.class)
public class Seat {

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Id
    @Column(name = "seat_id", nullable = false)
    private UUID seatId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status;

    @Column(name = "hold_id")
    private UUID holdId;

    @Column(name = "hold_expires_at")
    private Instant holdExpiresAt;

    @Column(name = "version", nullable = false)
    private long version;

    protected Seat() {
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getSeatId() {
        return seatId;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void hold(UUID holdId, Instant expiresAt) {
        this.status = SeatStatus.HELD;
        this.holdId = holdId;
        this.holdExpiresAt = expiresAt;
        this.version += 1;
    }
}