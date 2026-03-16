package lk.ijse.aad.backend.service;

import lk.ijse.aad.backend.dto.BookingDTO;
import lk.ijse.aad.backend.entity.Booking;
import lk.ijse.aad.backend.entity.User;
import lk.ijse.aad.backend.entity.Vehicle;
import lk.ijse.aad.backend.enums.BookingStatus;
import lk.ijse.aad.backend.enums.VehicleStatus;
import lk.ijse.aad.backend.enums.VehicleType;
import lk.ijse.aad.backend.repository.BookingRepository;
import lk.ijse.aad.backend.repository.UserRepository;
import lk.ijse.aad.backend.repository.VehicleRepository;
import lk.ijse.aad.backend.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private VehicleRepository vehicleRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User sampleUser;
    private Vehicle sampleVehicle;
    private Booking sampleBooking;
    private BookingDTO sampleDTO;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUserId(1L);
        sampleUser.setUsername("Saumyaa");
        sampleUser.setEmail("saumyaa@test.com");

        sampleVehicle = new Vehicle();
        sampleVehicle.setId(10L);
        sampleVehicle.setModel("Toyota Prius");
        sampleVehicle.setPlateNumber("CAB-1234");
        sampleVehicle.setType(VehicleType.CAB);
        sampleVehicle.setStatus(VehicleStatus.AVAILABLE);
        sampleVehicle.setPricePerDay(5000.0);

        sampleDTO = new BookingDTO();
        sampleDTO.setUserId(1L);
        sampleDTO.setVehicleId(10L);
        sampleDTO.setStartDate(LocalDate.now());
        sampleDTO.setEndDate(LocalDate.now().plusDays(3));
        sampleDTO.setPickupLocation("Colombo");
        sampleDTO.setDropLocation("Kandy");

        sampleBooking = new Booking();
        sampleBooking.setId(100L);
        sampleBooking.setUser(sampleUser);
        sampleBooking.setVehicle(sampleVehicle);
        sampleBooking.setStartDate(sampleDTO.getStartDate());
        sampleBooking.setEndDate(sampleDTO.getEndDate());
        sampleBooking.setTotalPrice(15000.0); // 3 days × 5000
        sampleBooking.setPickupLocation("Colombo");
        sampleBooking.setDropLocation("Kandy");
        sampleBooking.setStatus(BookingStatus.PENDING);
        sampleBooking.setCreatedAt(LocalDateTime.now());
    }

    // ==================== CREATE BOOKING ====================

    @Test
    @DisplayName("✅ createBooking - should create booking and set vehicle BOOKED")
    void createBooking_ShouldCreateAndMarkVehicleBooked() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(sampleVehicle));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        BookingDTO result = bookingService.createBooking(sampleDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        // Vehicle should now be BOOKED
        assertThat(sampleVehicle.getStatus()).isEqualTo(VehicleStatus.BOOKED);
        verify(vehicleRepository).save(sampleVehicle);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("✅ createBooking - price calculated correctly (days × pricePerDay)")
    void createBooking_ShouldCalculatePriceCorrectly() {
        // 3 days × 5000 = 15000
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(sampleVehicle));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        BookingDTO result = bookingService.createBooking(sampleDTO);

        assertThat(result.getTotalPrice()).isEqualTo(15000.0);
    }

    @Test
    @DisplayName("❌ createBooking - should throw when vehicle is NOT AVAILABLE")
    void createBooking_WhenVehicleBooked_ShouldThrow() {
        sampleVehicle.setStatus(VehicleStatus.BOOKED); // Already booked
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(sampleVehicle));

        assertThatThrownBy(() -> bookingService.createBooking(sampleDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("❌ createBooking - should throw when user not found")
    void createBooking_WhenUserNotFound_ShouldThrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(sampleDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("❌ createBooking - should throw when end date before start date")
    void createBooking_WhenInvalidDates_ShouldThrow() {
        sampleDTO.setEndDate(LocalDate.now().minusDays(1)); // End date before start
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(vehicleRepository.findById(10L)).thenReturn(Optional.of(sampleVehicle));

        assertThatThrownBy(() -> bookingService.createBooking(sampleDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("End date must be after start date");
    }

    // ==================== GET BOOKINGS ====================

    @Test
    @DisplayName("✅ getBookingsByUser - should return user bookings")
    void getBookingsByUser_ShouldReturnList() {
        when(bookingRepository.findByUserUserId(1L)).thenReturn(List.of(sampleBooking));

        List<BookingDTO> result = bookingService.getBookingsByUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("✅ getAllBookings - should return all bookings")
    void getAllBookings_ShouldReturnAll() {
        when(bookingRepository.findAll()).thenReturn(List.of(sampleBooking));

        List<BookingDTO> result = bookingService.getAllBookings();

        assertThat(result).hasSize(1);
    }

    // ==================== UPDATE STATUS ====================

    @Test
    @DisplayName("✅ updateBookingStatus - COMPLETED should free vehicle")
    void updateBookingStatus_WhenCompleted_ShouldFreeVehicle() {
        sampleVehicle.setStatus(VehicleStatus.BOOKED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        bookingService.updateBookingStatus(100L, BookingStatus.COMPLETED);

        assertThat(sampleVehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
        verify(vehicleRepository).save(sampleVehicle);
    }

    @Test
    @DisplayName("✅ updateBookingStatus - CANCELLED should free vehicle")
    void updateBookingStatus_WhenCancelled_ShouldFreeVehicle() {
        sampleVehicle.setStatus(VehicleStatus.BOOKED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(sampleBooking);

        bookingService.updateBookingStatus(100L, BookingStatus.CANCELLED);

        assertThat(sampleVehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    // ==================== CANCEL BOOKING ====================

    @Test
    @DisplayName("✅ cancelBooking - owner can cancel their booking")
    void cancelBooking_ByOwner_ShouldSucceed() {
        sampleVehicle.setStatus(VehicleStatus.BOOKED);
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(sampleBooking));

        bookingService.cancelBooking(100L, 1L);

        assertThat(sampleBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(sampleVehicle.getStatus()).isEqualTo(VehicleStatus.AVAILABLE);
    }

    @Test
    @DisplayName("❌ cancelBooking - different user cannot cancel")
    void cancelBooking_ByDifferentUser_ShouldThrow() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(sampleBooking));

        // User 99 tries to cancel booking belonging to user 1
        assertThatThrownBy(() -> bookingService.cancelBooking(100L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized");
    }
}
