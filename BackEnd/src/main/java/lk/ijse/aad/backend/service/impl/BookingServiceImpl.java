package lk.ijse.aad.backend.service.impl;

import lk.ijse.aad.backend.dto.BookingDTO;
import lk.ijse.aad.backend.entity.Booking;
import lk.ijse.aad.backend.entity.User;
import lk.ijse.aad.backend.entity.Vehicle;
import lk.ijse.aad.backend.enums.BookingStatus;
import lk.ijse.aad.backend.enums.VehicleStatus;
import lk.ijse.aad.backend.exception.DuplicateBookingException;
import lk.ijse.aad.backend.exception.ResourceNotFoundException;
import lk.ijse.aad.backend.exception.VehicleNotAvailableException;
import lk.ijse.aad.backend.repository.BookingRepository;
import lk.ijse.aad.backend.repository.UserRepository;
import lk.ijse.aad.backend.repository.VehicleRepository;
import lk.ijse.aad.backend.service.custom.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    // ─────────────────────────────────────────────────────────────
    // HELPER: Entity → DTO
    // ─────────────────────────────────────────────────────────────
    private BookingDTO toDTO(Booking b) {
        BookingDTO dto = new BookingDTO();
        dto.setId(b.getId());
        dto.setUserId(b.getUser().getUserId());
        dto.setVehicleId(b.getVehicle().getId());
        dto.setVehicleModel(b.getVehicle().getModel());
        dto.setVehiclePlate(b.getVehicle().getPlateNumber());
        dto.setStartDate(b.getStartDate());
        dto.setEndDate(b.getEndDate());
        dto.setTotalPrice(b.getTotalPrice());
        dto.setPickupLocation(b.getPickupLocation());
        dto.setDropLocation(b.getDropLocation());
        dto.setStatus(b.getStatus());
        dto.setCreatedAt(b.getCreatedAt());
        return dto;
    }

    // ─────────────────────────────────────────────────────────────
    // VEHICLE AVAILABILITY CHECK  ← Core professional logic
    // Checks 3 things:
    //   1. Vehicle status is AVAILABLE
    //   2. Insurance is active and not expired
    //   3. No date overlap with existing bookings
    // ─────────────────────────────────────────────────────────────
    private void checkVehicleAvailability(Vehicle vehicle, LocalDate startDate, LocalDate endDate) {

        // ── Check 1: Status ──────────────────────────────────────
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            log.warn("Vehicle {} is not AVAILABLE. Current status: {}", vehicle.getId(), vehicle.getStatus());
            throw new VehicleNotAvailableException(
                    "Vehicle '" + vehicle.getModel() + "' is currently " + vehicle.getStatus() +
                            " and cannot be booked."
            );
        }

        // ── Check 2: Insurance active ────────────────────────────
        if (!vehicle.isInsuranceActive()) {
            log.warn("Vehicle {} insurance is not active", vehicle.getId());
            throw new VehicleNotAvailableException(
                    "Vehicle '" + vehicle.getModel() + "' insurance is not active. Cannot book."
            );
        }

        // ── Check 3: Insurance expiry date ───────────────────────
        if (vehicle.getInsuranceExpiryDate() != null &&
                vehicle.getInsuranceExpiryDate().isBefore(endDate)) {
            log.warn("Vehicle {} insurance expires {} before booking end date {}",
                    vehicle.getId(), vehicle.getInsuranceExpiryDate(), endDate);
            throw new VehicleNotAvailableException(
                    "Vehicle '" + vehicle.getModel() + "' insurance expires on " +
                            vehicle.getInsuranceExpiryDate() + ", before your booking end date."
            );
        }

        // ── Check 4: Date overlap with existing bookings ─────────
        boolean hasOverlap = bookingRepository.existsOverlappingBooking(
                vehicle.getId(), startDate, endDate,
                BookingStatus.CANCELLED, BookingStatus.COMPLETED
        );
        if (hasOverlap) {
            log.warn("Vehicle {} already has an overlapping booking for {} → {}", vehicle.getId(), startDate, endDate);
            throw new DuplicateBookingException(
                    "Vehicle '" + vehicle.getModel() + "' is already booked for the selected dates (" +
                            startDate + " → " + endDate + "). Please choose different dates."
            );
        }

        log.info("Vehicle {} passed all availability checks ✓", vehicle.getId());
    }

    // ─────────────────────────────────────────────────────────────
    // CREATE BOOKING
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public BookingDTO createBooking(BookingDTO dto) {
        log.info("Creating booking: userId={}, vehicleId={}, {} → {}",
                dto.getUserId(), dto.getVehicleId(), dto.getStartDate(), dto.getEndDate());

        // 1. Validate date range
        if (!dto.getEndDate().isAfter(dto.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date.");
        }

        // 2. Find user
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getUserId()));

        // 3. Find vehicle
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + dto.getVehicleId()));

        // 4. ✅ Run full availability check (status + insurance + date overlap)
        checkVehicleAvailability(vehicle, dto.getStartDate(), dto.getEndDate());

        // 5. Calculate total price
        long days = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate());
        double totalPrice = days * vehicle.getPricePerDay();
        log.debug("Price: {} days × LKR {} = LKR {}", days, vehicle.getPricePerDay(), totalPrice);

        // 6. Build booking entity
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setVehicle(vehicle);
        booking.setStartDate(dto.getStartDate());
        booking.setEndDate(dto.getEndDate());
        booking.setTotalPrice(totalPrice);
        booking.setPickupLocation(dto.getPickupLocation());
        booking.setDropLocation(dto.getDropLocation());
        booking.setStatus(BookingStatus.PENDING);

        // 7. Mark vehicle BOOKED
        vehicle.setStatus(VehicleStatus.BOOKED);
        vehicleRepository.save(vehicle);

        // 8. Save and return
        Booking saved = bookingRepository.save(booking);
        log.info("Booking created id={}, totalPrice=LKR{}", saved.getId(), totalPrice);
        return toDTO(saved);
    }

    // ─────────────────────────────────────────────────────────────
    // GET BOOKINGS
    // ─────────────────────────────────────────────────────────────
    @Override
    public List<BookingDTO> getBookingsByUser(Long userId) {
        log.debug("Getting bookings for userId={}", userId);
        return bookingRepository.findByUserUserId(userId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<BookingDTO> getAllBookings() {
        log.debug("Getting all bookings");
        return bookingRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public BookingDTO getBookingById(Long id) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
        return toDTO(b);
    }

    // ─────────────────────────────────────────────────────────────
    // UPDATE STATUS (Admin)
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public BookingDTO updateBookingStatus(Long bookingId, BookingStatus status) {
        log.info("Updating booking {} status to {}", bookingId, status);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        booking.setStatus(status);

        // Release vehicle if booking is finished
        if (status == BookingStatus.COMPLETED || status == BookingStatus.CANCELLED) {
            Vehicle vehicle = booking.getVehicle();
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicleRepository.save(vehicle);
            log.info("Vehicle {} released back to AVAILABLE", vehicle.getId());
        }

        return toDTO(bookingRepository.save(booking));
    }

    // ─────────────────────────────────────────────────────────────
    // CANCEL BOOKING (User)
    // ─────────────────────────────────────────────────────────────
    @Override
    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        log.info("Cancel request: bookingId={}, userId={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        if (!booking.getUser().getUserId().equals(userId)) {
            log.warn("Unauthorized cancel: bookingId={} does not belong to userId={}", bookingId, userId);
            throw new SecurityException("You are not authorized to cancel this booking.");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Vehicle vehicle = booking.getVehicle();
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);
        bookingRepository.save(booking);

        log.info("Booking {} cancelled. Vehicle {} now AVAILABLE", bookingId, vehicle.getId());
    }
}

