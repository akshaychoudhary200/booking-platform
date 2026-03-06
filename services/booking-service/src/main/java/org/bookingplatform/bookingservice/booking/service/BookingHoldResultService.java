package org.bookingplatform.bookingservice.booking.service;

import org.bookingplatform.bookingservice.booking.domain.BookingStatus;
import org.bookingplatform.bookingservice.booking.repository.BookingRepository;
import org.bookingplatform.bookingservice.waitingroom.application.EventAdmissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class BookingHoldResultService {

    private final BookingRepository bookingRepository;
    private final EventAdmissionService eventAdmissionService;

    public BookingHoldResultService(
            BookingRepository bookingRepository,
            EventAdmissionService eventAdmissionService) {
        this.bookingRepository = bookingRepository;
        this.eventAdmissionService = eventAdmissionService;
    }

    @Transactional
    public void applyHoldCreated(UUID bookingId, UUID holdId, Instant expiresAt, int seatsHeld) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.HOLD_ACTIVE) {
            return;
        }

        if (booking.getStatus() == BookingStatus.HOLD_REJECTED
                || booking.getStatus() == BookingStatus.HOLD_EXPIRED
                || booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.CONFIRMED) {
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

        if (booking.getStatus() == BookingStatus.HOLD_ACTIVE
                || booking.getStatus() == BookingStatus.CONFIRMED) {
            return;
        }

        booking.markHoldRejected();
        eventAdmissionService.releaseSlot(booking.getEventId(), booking.getBookingId());
    }

    @Transactional
    public void applyHoldExpired(UUID bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.HOLD_EXPIRED) {
            return;
        }

        if (booking.getStatus() == BookingStatus.HOLD_REJECTED
                || booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.CONFIRMED) {
            return;
        }

        booking.markHoldExpired();
        eventAdmissionService.releaseSlot(booking.getEventId(), booking.getBookingId());
    }

    @Transactional
    public void applyBookingConfirmed(UUID bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return;
        }

        booking.markConfirmed();
        eventAdmissionService.releaseSlot(booking.getEventId(), booking.getBookingId());
    }

    @Transactional
    public void applyConfirmRejected(UUID bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CONFIRM_REJECTED) {
            return;
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return;
        }

        booking.markConfirmRejected();
        eventAdmissionService.releaseSlot(booking.getEventId(), booking.getBookingId());
    }

    @Transactional
    public void applyHoldCancelled(UUID bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return;
        }

        booking.markCancelled();
        eventAdmissionService.releaseSlot(booking.getEventId(), booking.getBookingId());
    }

    @Transactional
    public void applyCancelRejected(UUID bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCEL_REJECTED) {
            return;
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        booking.markCancelRejected();
        eventAdmissionService.releaseSlot(booking.getEventId(), booking.getBookingId());
    }
}