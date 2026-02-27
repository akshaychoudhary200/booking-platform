package org.bookingplatform.bookingservice.booking.service;

import org.bookingplatform.bookingservice.booking.dto.BookingView;
import org.bookingplatform.bookingservice.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BookingQueryServiceImpl implements BookingQueryService {

    private final BookingRepository bookingRepository;

    public BookingQueryServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public List<BookingView> listForUser(UUID userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(b -> new BookingView(
                        b.getBookingId(),
                        b.getEventId(),
                        b.getStatus().name(),
                        b.getCreatedAt()))
                .toList();
    }
}
