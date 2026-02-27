package org.bookingplatform.bookingservice.booking.service;

import org.bookingplatform.bookingservice.booking.domain.Booking;
import org.bookingplatform.bookingservice.booking.domain.BookingStatus;
import org.bookingplatform.bookingservice.booking.dto.BookingView;
import org.bookingplatform.bookingservice.booking.dto.CreateBookingRequest;
import org.bookingplatform.bookingservice.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BookingCommandService {

    private final BookingRepository bookingRepository;

    public BookingCommandService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public BookingView create(UUID userId, CreateBookingRequest req) {

        Booking booking = new Booking(
                UUID.randomUUID(),
                userId,
                req.eventId(),
                BookingStatus.CREATED,
                null);

        bookingRepository.save(booking);

        return new BookingView(
                booking.getBookingId(),
                booking.getEventId(),
                booking.getStatus().name(),
                booking.getCreatedAt());
    }
}