package org.bookingplatform.inventoryservice.infrastructure;

import org.bookingplatform.inventoryservice.domain.Hold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HoldRepository extends JpaRepository<Hold, UUID> {

    Optional<Hold> findByBookingId(UUID bookingId);

    @Query(value = """
              SELECT * FROM hold
              WHERE status = 'ACTIVE' AND expires_at <= now()
              ORDER BY expires_at ASC
              FOR UPDATE SKIP LOCKED
              LIMIT :limit
            """, nativeQuery = true)
    List<Hold> lockExpiredActiveHolds(int limit);
}