package org.bookingplatform.bookingservice.booking.service;

import org.bookingplatform.bookingservice.booking.dto.BookingView;
import java.util.List;
import java.util.UUID;

public interface BookingQueryService {
    List<BookingView> listForUser(UUID userId);
}