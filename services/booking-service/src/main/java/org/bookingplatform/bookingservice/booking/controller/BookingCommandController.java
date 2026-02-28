package org.bookingplatform.bookingservice.booking.controller;

import org.bookingplatform.bookingservice.booking.dto.BookingView;
import org.bookingplatform.bookingservice.booking.dto.CreateBookingRequest;
import org.bookingplatform.bookingservice.booking.service.BookingCommandService;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class BookingCommandController {

    private final BookingCommandService bookingCommandService;

    public BookingCommandController(BookingCommandService bookingCommandService) {
        this.bookingCommandService = bookingCommandService;
    }

    @PostMapping("/bookings/hold")
    public BookingView createBooking(
            Authentication authentication,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Validated @RequestBody CreateBookingRequest req) {
        UUID userId = (UUID) authentication.getPrincipal();
        return bookingCommandService.createIdempotent(userId, idempotencyKey, req);
    }
}