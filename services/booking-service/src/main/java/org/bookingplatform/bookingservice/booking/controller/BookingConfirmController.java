package org.bookingplatform.bookingservice.booking.controller;

import org.bookingplatform.bookingservice.booking.service.BookingConfirmService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class BookingConfirmController {

    private final BookingConfirmService bookingConfirmService;

    public BookingConfirmController(BookingConfirmService bookingConfirmService) {
        this.bookingConfirmService = bookingConfirmService;
    }

    @PostMapping("/bookings/{bookingId}/confirm")
    public ResponseEntity<?> confirm(
            Authentication authentication,
            @PathVariable UUID bookingId,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        UUID userId = (UUID) authentication.getPrincipal();
        bookingConfirmService.requestConfirm(userId, bookingId, idempotencyKey);
        return ResponseEntity.accepted().build();
    }
}