package org.bookingplatform.bookingservice.booking.domain;

public enum BookingStatus {
    HOLD_REQUESTED,
    HOLD_ACTIVE,
    HOLD_REJECTED,
    HOLD_EXPIRED,
    CONFIRM_REQUESTED,
    CONFIRMED,
    CONFIRM_REJECTED
}