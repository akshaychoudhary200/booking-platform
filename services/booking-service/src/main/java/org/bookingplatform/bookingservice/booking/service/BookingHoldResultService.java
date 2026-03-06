package org.bookingplatform.bookingservice.booking.service;

import org.bookingplatform.bookingservice.booking.domain.BookingStatus;
import org.bookingplatform.bookingservice.booking.repository.BookingRepository;
import org.bookingplatform.bookingservice.payment.application.PaymentRequestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class BookingHoldResultService {

    private final BookingRepository bookingRepository;
    private final PaymentRequestService paymentRequestService;

    public BookingHoldResultService(BookingRepository bookingRepository, PaymentRequestService paymentRequestService) {
        this.bookingRepository = bookingRepository;
        this.paymentRequestService = paymentRequestService;
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
        paymentRequestService.requestPaymentIfNeeded(bookingId);
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

    @Transactional
    public void applyHoldExpired(UUID bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        // idempotency + order tolerance:
        // if already expired -> ignore
        if (booking.getStatus() == BookingStatus.HOLD_EXPIRED)
            return;

        // if already rejected -> ignore (expiry after reject doesn't matter)
        if (booking.getStatus() == BookingStatus.HOLD_REJECTED)
            return;

        // if active, we allow expiry to win (safer default). Later confirm flow will
        // fence this properly.
        booking.markHoldExpired();
    }

    @Transactional
    public void applyBookingConfirmed(UUID bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CONFIRMED)
            return;

        booking.markConfirmed();
    }

    @Transactional
    public void applyConfirmRejected(UUID bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CONFIRM_REJECTED)
            return;
        if (booking.getStatus() == BookingStatus.CONFIRMED)
            return;

        booking.markConfirmRejected();
    }
}