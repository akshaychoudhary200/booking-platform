package org.bookingplatform.inventoryservice.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class SeatKey implements Serializable {
    private UUID eventId;
    private UUID seatId;

    public SeatKey() {
    }

    public SeatKey(UUID eventId, UUID seatId) {
        this.eventId = eventId;
        this.seatId = seatId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof SeatKey that))
            return false;
        return Objects.equals(eventId, that.eventId) && Objects.equals(seatId, that.seatId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, seatId);
    }
}