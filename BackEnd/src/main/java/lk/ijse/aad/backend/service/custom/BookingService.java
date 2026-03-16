package lk.ijse.aad.backend.service.custom;

import lk.ijse.aad.backend.dto.BookingDTO;
import lk.ijse.aad.backend.enums.BookingStatus;

import java.util.List;

public interface BookingService {

    // User: Create a new booking
    BookingDTO createBooking(BookingDTO dto);

    // User: Get my bookings
    List<BookingDTO> getBookingsByUser(Long userId);

    // Admin: Get all bookings
    List<BookingDTO> getAllBookings();

    // Admin: Update booking status
    BookingDTO updateBookingStatus(Long bookingId, BookingStatus status);

    // User: Cancel booking
    void cancelBooking(Long bookingId, Long userId);

    // Get single booking
    BookingDTO getBookingById(Long id);
}

