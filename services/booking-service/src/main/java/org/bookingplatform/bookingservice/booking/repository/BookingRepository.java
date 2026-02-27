package org.bookingplatform.bookingservice.booking.repository;

import org.bookingplatform.bookingservice.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Booking> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
}