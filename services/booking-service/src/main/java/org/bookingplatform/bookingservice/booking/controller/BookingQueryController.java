package org.bookingplatform.bookingservice.booking.controller;

import org.bookingplatform.bookingservice.booking.dto.BookingView;
import org.bookingplatform.bookingservice.booking.service.BookingQueryService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class BookingQueryController {

    private final BookingQueryService bookingQueryService;

    public BookingQueryController(BookingQueryService bookingQueryService) {
        this.bookingQueryService = bookingQueryService;
    }

    @GetMapping("/bookings")
    public List<BookingView> listMyBookings(Authentication authentication) {
        UUID userId = (UUID) authentication.getPrincipal();
        return bookingQueryService.listForUser(userId);
    }
}