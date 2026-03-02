package org.bookingplatform.inventoryservice.infrastructure;

import org.bookingplatform.inventoryservice.domain.Seat;
import org.bookingplatform.inventoryservice.domain.SeatKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, SeatKey> {

  @Query(value = """
        SELECT * FROM seat
        WHERE event_id = :eventId AND status = 'AVAILABLE'
        ORDER BY seat_id
        FOR UPDATE SKIP LOCKED
        LIMIT :limit
      """, nativeQuery = true)
  List<Seat> lockNextAvailableSeats(UUID eventId, int limit);

  @Modifying
  @Query(value = """
        UPDATE seat
        SET status = 'AVAILABLE',
            hold_id = NULL,
            hold_expires_at = NULL,
            version = version + 1
        WHERE hold_id = :holdId
          AND status = 'HELD'
      """, nativeQuery = true)
  int releaseSeatsForHold(UUID holdId);
}