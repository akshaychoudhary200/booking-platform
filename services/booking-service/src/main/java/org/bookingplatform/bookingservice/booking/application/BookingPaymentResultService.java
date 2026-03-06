package org.bookingplatform.bookingservice.booking.application;

import org.bookingplatform.bookingservice.booking.domain.BookingStatus;
import org.bookingplatform.bookingservice.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookingPaymentResultService {

    private final BookingRepository bookingRepository;

    public BookingPaymentResultService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public boolean applyPaymentAuthorized(UUID bookingId, UUID paymentId, String paymentIdempotencyKey) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        // Late callbacks: if hold expired/rejected, ignore (don’t confirm)
        if (booking.getStatus() == BookingStatus.HOLD_EXPIRED || booking.getStatus() == BookingStatus.HOLD_REJECTED) {
            return false;
        }

        // Idempotent: same key already applied
        if (paymentIdempotencyKey != null && paymentIdempotencyKey.equals(booking.getPaymentIdempotencyKey())
                && booking.getStatus() == BookingStatus.PAYMENT_AUTHORIZED) {
            return false;
        }

        // Only accept auth when we actually requested payment
        if (booking.getStatus() != BookingStatus.PAYMENT_REQUESTED
                && booking.getStatus() != BookingStatus.HOLD_ACTIVE) {
            return false;
        }

        booking.markPaymentAuthorized(paymentId);

        // return "should we proceed to confirm?"
        return true;
    }

    @Transactional
    public void applyPaymentFailed(UUID bookingId, String paymentIdempotencyKey) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.PAYMENT_FAILED)
            return;

        // Late failure after confirm? ignore (safe choice)
        if (booking.getStatus() == BookingStatus.CONFIRMED)
            return;

        booking.markPaymentFailed();
    }
}