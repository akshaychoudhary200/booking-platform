package org.bookingplatform.bookingservice.booking.domain;

public enum BookingStatus {
    HOLD_REQUESTED,
    HOLD_ACTIVE,
    HOLD_REJECTED,
    HOLD_EXPIRED,

    PAYMENT_REQUESTED,
    PAYMENT_AUTHORIZED,
    PAYMENT_FAILED,

    CONFIRM_REQUESTED,
    CONFIRMED,
    CONFIRM_REJECTED
}