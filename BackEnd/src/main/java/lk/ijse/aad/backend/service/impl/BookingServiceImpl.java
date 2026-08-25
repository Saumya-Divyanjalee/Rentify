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
import java.util.Map;
import java.util.stream.Collectors;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;


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


    private void checkVehicleAvailability(Vehicle vehicle, LocalDate startDate, LocalDate endDate) {

        // Status
        if (vehicle.getStatus() != VehicleStatus.AVAILABLE) {
            log.warn("Vehicle {} is not AVAILABLE. Current status: {}", vehicle.getId(), vehicle.getStatus());
            throw new VehicleNotAvailableException(
                    "Vehicle '" + vehicle.getModel() + "' is currently " + vehicle.getStatus() +
                            " and cannot be booked."
            );
        }

        // Insurance active
        if (!vehicle.isInsuranceActive()) {
            log.warn("Vehicle {} insurance is not active", vehicle.getId());
            throw new VehicleNotAvailableException(
                    "Vehicle '" + vehicle.getModel() + "' insurance is not active. Cannot book."
            );
        }

        // Insurance expiry date
        if (vehicle.getInsuranceExpiryDate() != null &&
                vehicle.getInsuranceExpiryDate().isBefore(endDate)) {
            log.warn("Vehicle {} insurance expires {} before booking end date {}",
                    vehicle.getId(), vehicle.getInsuranceExpiryDate(), endDate);
            throw new VehicleNotAvailableException(
                    "Vehicle '" + vehicle.getModel() + "' insurance expires on " +
                            vehicle.getInsuranceExpiryDate() + ", before your booking end date."
            );
        }

        // Date overlap with existing bookings
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

        // 4.  Run full availability check (status + insurance + date overlap)
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

    @Override
    public Map<String, Object> getAnalytics() {
        Map<String, Object> result = new HashMap<>();

        LocalDate now = LocalDate.now();

        // Last 3 months date ranges
        LocalDate m1Start = now.minusMonths(3)
                .withDayOfMonth(1);
        LocalDate m1End = now.minusMonths(2)
                .withDayOfMonth(1).minusDays(1);

        LocalDate m2Start = now.minusMonths(2)
                .withDayOfMonth(1);
        LocalDate m2End = now.minusMonths(1)
                .withDayOfMonth(1).minusDays(1);

        LocalDate m3Start = now.minusMonths(1)
                .withDayOfMonth(1);
        LocalDate m3End = now.withDayOfMonth(1)
                .minusDays(1);

        log.info("Month 1: {} to {}", m1Start, m1End);
        log.info("Month 2: {} to {}", m2Start, m2End);
        log.info("Month 3: {} to {}", m3Start, m3End);

        // Load bookings per month
        List<Booking> month1 = bookingRepository
                .findByStartDateBetween(m1Start, m1End);
        List<Booking> month2 = bookingRepository
                .findByStartDateBetween(m2Start, m2End);
        List<Booking> month3 = bookingRepository
                .findByStartDateBetween(m3Start, m3End);

        log.info("Month1 count: {}", month1.size());
        log.info("Month2 count: {}", month2.size());
        log.info("Month3 count: {}", month3.size());

        // Revenue per month (exclude CANCELLED)
        double rev1 = month1.stream()
                .filter(b -> b.getStatus() !=
                        BookingStatus.CANCELLED)
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        double rev2 = month2.stream()
                .filter(b -> b.getStatus() !=
                        BookingStatus.CANCELLED)
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        double rev3 = month3.stream()
                .filter(b -> b.getStatus() !=
                        BookingStatus.CANCELLED)
                .mapToDouble(Booking::getTotalPrice)
                .sum();

        // Booking counts per month
        long bk1 = month1.stream()
                .filter(b -> b.getStatus() !=
                        BookingStatus.CANCELLED)
                .count();

        long bk2 = month2.stream()
                .filter(b -> b.getStatus() !=
                        BookingStatus.CANCELLED)
                .count();

        long bk3 = month3.stream()
                .filter(b -> b.getStatus() !=
                        BookingStatus.CANCELLED)
                .count();

        // Vehicle type breakdown (month 3)
        Map<String, Long> vehicleTypeCounts =
                month3.stream()
                        .filter(b -> b.getStatus() !=
                                BookingStatus.CANCELLED)
                        .filter(b -> b.getVehicle() != null)
                        .collect(Collectors.groupingBy(
                                b -> b.getVehicle()
                                        .getType().name(),
                                Collectors.counting()
                        ));

        // Weekend bookings (month 3)
        long weekendBk = month3.stream()
                .filter(b -> b.getStatus() !=
                        BookingStatus.CANCELLED)
                .filter(b -> {
                    DayOfWeek day = b.getStartDate()
                            .getDayOfWeek();
                    return day == DayOfWeek.FRIDAY
                            || day == DayOfWeek.SATURDAY
                            || day == DayOfWeek.SUNDAY;
                }).count();

        // High frequency vehicle
        Map<String, Long> vehicleFreq = month3.stream()
                .filter(b -> b.getVehicle() != null)
                .collect(Collectors.groupingBy(
                        b -> "Vehicle #" +
                                b.getVehicle().getId(),
                        Collectors.counting()
                ));

        String highFreqVehicle = vehicleFreq
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        // Total vehicles
        long totalVehicles = vehicleRepository.count();

        // Month name helper
        String m1Name = m1Start.getMonth()
                .getDisplayName(TextStyle.SHORT,
                        Locale.ENGLISH)
                + " " + m1Start.getYear();
        String m2Name = m2Start.getMonth()
                .getDisplayName(TextStyle.SHORT,
                        Locale.ENGLISH)
                + " " + m2Start.getYear();
        String m3Name = m3Start.getMonth()
                .getDisplayName(TextStyle.SHORT,
                        Locale.ENGLISH)
                + " " + m3Start.getYear();
        String nextName = now.getMonth()
                .getDisplayName(TextStyle.SHORT,
                        Locale.ENGLISH)
                + " " + now.getYear();

        // Build result map
        result.put("month1Name", m1Name);
        result.put("month2Name", m2Name);
        result.put("month3Name", m3Name);
        result.put("nextMonthName", nextName);
        result.put("rev1", rev1);
        result.put("rev2", rev2);
        result.put("rev3", rev3);
        result.put("bk1", bk1);
        result.put("bk2", bk2);
        result.put("bk3", bk3);
        result.put("totalBk3", bk3);
        result.put("vehicleTypeCounts",
                vehicleTypeCounts);
        result.put("weekendBk", weekendBk);
        result.put("totalVehicles", totalVehicles);
        result.put("highFreqVehicle", highFreqVehicle);

        return result;
    }


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

