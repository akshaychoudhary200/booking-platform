package org.bookingplatform.inventoryservice.infrastructure;

import org.bookingplatform.inventoryservice.domain.Seat;
import org.bookingplatform.inventoryservice.domain.SeatKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, SeatKey> {

    // We lock selected seats so concurrent consumers can't grab the same seats.
    // SKIP LOCKED prevents waiting and avoids deadlocks under load.
    @Query(value = """
              SELECT * FROM seat
              WHERE event_id = :eventId AND status = 'AVAILABLE'
              ORDER BY seat_id
              FOR UPDATE SKIP LOCKED
              LIMIT :limit
            """, nativeQuery = true)
    List<Seat> lockNextAvailableSeats(UUID eventId, int limit);
}