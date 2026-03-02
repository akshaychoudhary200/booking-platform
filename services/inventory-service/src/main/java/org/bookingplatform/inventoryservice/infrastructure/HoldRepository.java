package org.bookingplatform.inventoryservice.infrastructure;

import org.bookingplatform.inventoryservice.domain.Hold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface HoldRepository extends JpaRepository<Hold, UUID> {
    Optional<Hold> findByBookingId(UUID bookingId);
}