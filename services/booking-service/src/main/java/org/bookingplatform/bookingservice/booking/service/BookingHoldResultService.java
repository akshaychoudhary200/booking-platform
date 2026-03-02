package org.bookingplatform.bookingservice.booking.service;

import org.bookingplatform.bookingservice.booking.domain.BookingStatus;
import org.bookingplatform.bookingservice.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class BookingHoldResultService {

    private final BookingRepository bookingRepository;

    public BookingHoldResultService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public void applyHoldCreated(UUID bookingId, UUID holdId, Instant expiresAt, int seatsHeld) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        // Idempotency: if already applied, ignore
        if (booking.getStatus() == BookingStatus.HOLD_ACTIVE) {
            return;
        }
        // If already rejected, we also ignore (late event). You can choose policy; this
        // is safe.
        if (booking.getStatus() == BookingStatus.HOLD_REJECTED) {
            return;
        }

        booking.markHoldActive(holdId, expiresAt, seatsHeld);
    }

    @Transactional
    public void applyHoldRejected(UUID bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.HOLD_REJECTED) {
            return;
        }
        if (booking.getStatus() == BookingStatus.HOLD_ACTIVE) {
            return; // late reject; ignore safely
        }

        booking.markHoldRejected();
    }
}