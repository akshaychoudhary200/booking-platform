package org.bookingplatform.bookingservice.booking.application;

import org.bookingplatform.bookingservice.booking.domain.BookingStatus;
import org.bookingplatform.bookingservice.booking.repository.BookingRepository;
import org.bookingplatform.bookingservice.waitingroom.application.EventAdmissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookingPaymentResultService {

    private final BookingRepository bookingRepository;
    private final EventAdmissionService eventAdmissionService;

    public BookingPaymentResultService(
            BookingRepository bookingRepository,
            EventAdmissionService eventAdmissionService) {
        this.bookingRepository = bookingRepository;
        this.eventAdmissionService = eventAdmissionService;
    }

    @Transactional
    public boolean applyPaymentAuthorized(UUID bookingId, UUID paymentId, String paymentIdempotencyKey) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.HOLD_EXPIRED
                || booking.getStatus() == BookingStatus.HOLD_REJECTED
                || booking.getStatus() == BookingStatus.CANCELLED) {
            return false;
        }

        if (paymentIdempotencyKey != null
                && paymentIdempotencyKey.equals(booking.getPaymentIdempotencyKey())
                && booking.getStatus() == BookingStatus.PAYMENT_AUTHORIZED) {
            return false;
        }

        if (booking.getStatus() != BookingStatus.PAYMENT_REQUESTED
                && booking.getStatus() != BookingStatus.HOLD_ACTIVE) {
            return false;
        }

        booking.markPaymentAuthorized(paymentId);
        return true;
    }

    @Transactional
    public void applyPaymentFailed(UUID bookingId, String paymentIdempotencyKey) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + bookingId));

        if (booking.getStatus() == BookingStatus.PAYMENT_FAILED) {
            return;
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return;
        }

        booking.markPaymentFailed();
        eventAdmissionService.releaseSlot(booking.getEventId(), booking.getBookingId());
    }
}