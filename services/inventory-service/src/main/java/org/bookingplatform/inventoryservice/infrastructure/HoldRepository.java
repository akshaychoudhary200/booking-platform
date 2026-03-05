package org.bookingplatform.inventoryservice.infrastructure;

import org.bookingplatform.inventoryservice.domain.Hold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoldRepository extends JpaRepository<Hold, UUID> {

  Optional<Hold> findByBookingId(UUID bookingId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select h from Hold h where h.bookingId = :bookingId")
  Optional<Hold> findForUpdateByBookingId(UUID bookingId);

  @Query(value = """
        SELECT * FROM hold
        WHERE status = 'ACTIVE' AND expires_at <= now()
        ORDER BY expires_at ASC
        FOR UPDATE SKIP LOCKED
        LIMIT :limit
      """, nativeQuery = true)
  List<Hold> lockExpiredActiveHolds(int limit);
}