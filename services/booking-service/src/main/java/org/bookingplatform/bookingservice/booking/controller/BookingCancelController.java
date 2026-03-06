package org.bookingplatform.bookingservice.booking.controller;

import org.bookingplatform.bookingservice.booking.application.BookingCancelService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class BookingCancelController {

    private final BookingCancelService bookingCancelService;

    public BookingCancelController(BookingCancelService bookingCancelService) {
        this.bookingCancelService = bookingCancelService;
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<?> cancel(
            Authentication authentication,
            @PathVariable UUID bookingId,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        UUID userId = (UUID) authentication.getPrincipal();
        bookingCancelService.requestCancel(userId, bookingId, idempotencyKey);
        return ResponseEntity.accepted().build();
    }
}