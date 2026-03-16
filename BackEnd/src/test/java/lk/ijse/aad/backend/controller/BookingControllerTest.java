package lk.ijse.aad.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lk.ijse.aad.backend.dto.BookingDTO;
import lk.ijse.aad.backend.enums.BookingStatus;
import lk.ijse.aad.backend.service.custom.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@DisplayName("BookingController Web Layer Tests")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    private ObjectMapper objectMapper;
    private BookingDTO sampleBookingDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // For LocalDate serialization

        sampleBookingDTO = new BookingDTO();
        sampleBookingDTO.setId(1L);
        sampleBookingDTO.setUserId(1L);
        sampleBookingDTO.setVehicleId(10L);
        sampleBookingDTO.setVehicleModel("Toyota Prius");
        sampleBookingDTO.setStartDate(LocalDate.now());
        sampleBookingDTO.setEndDate(LocalDate.now().plusDays(3));
        sampleBookingDTO.setTotalPrice(15000.0);
        sampleBookingDTO.setPickupLocation("Colombo");
        sampleBookingDTO.setDropLocation("Kandy");
        sampleBookingDTO.setStatus(BookingStatus.PENDING);
        sampleBookingDTO.setCreatedAt(LocalDateTime.now());
    }

    // ==================== CREATE BOOKING ====================

    @Test
    @DisplayName("✅ POST /api/bookings - USER can create booking")
    @WithMockUser(roles = "USER")
    void createBooking_AsUser_ShouldReturn200() throws Exception {
        when(bookingService.createBooking(any(BookingDTO.class))).thenReturn(sampleBookingDTO);

        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookingDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(15000.0));
    }

    @Test
    @DisplayName("❌ POST /api/bookings - No auth returns 401/403")
    void createBooking_NoAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookingDTO)))
                .andExpect(status().is4xxClientError());
    }

    // ==================== GET MY BOOKINGS ====================

    @Test
    @DisplayName("✅ GET /api/bookings/user/{userId} - USER gets own bookings")
    @WithMockUser(roles = "USER")
    void getMyBookings_AsUser_ShouldReturn200() throws Exception {
        when(bookingService.getBookingsByUser(1L)).thenReturn(List.of(sampleBookingDTO));

        mockMvc.perform(get("/api/bookings/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$").isArray());
    }

    // ==================== GET ALL BOOKINGS (ADMIN) ====================

    @Test
    @DisplayName("✅ GET /api/bookings - ADMIN gets all bookings")
    @WithMockUser(roles = "ADMIN")
    void getAllBookings_AsAdmin_ShouldReturn200() throws Exception {
        when(bookingService.getAllBookings()).thenReturn(List.of(sampleBookingDTO));

        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("❌ GET /api/bookings - USER cannot access all bookings (403)")
    @WithMockUser(roles = "USER")
    void getAllBookings_AsUser_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isForbidden());
    }

    // ==================== UPDATE STATUS (ADMIN) ====================

    @Test
    @DisplayName("✅ PUT /api/bookings/{id}/status - ADMIN updates booking status")
    @WithMockUser(roles = "ADMIN")
    void updateStatus_AsAdmin_ShouldReturn200() throws Exception {
        sampleBookingDTO.setStatus(BookingStatus.CONFIRMED);
        when(bookingService.updateBookingStatus(1L, BookingStatus.CONFIRMED))
                .thenReturn(sampleBookingDTO);

        mockMvc.perform(put("/api/bookings/1/status")
                        .with(csrf())
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    // ==================== CANCEL BOOKING ====================

    @Test
    @DisplayName("✅ DELETE /api/bookings/{id}/cancel - USER can cancel own booking")
    @WithMockUser(roles = "USER")
    void cancelBooking_AsUser_ShouldReturn200() throws Exception {
        doNothing().when(bookingService).cancelBooking(1L, 1L);

        mockMvc.perform(delete("/api/bookings/1/cancel")
                        .with(csrf())
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Booking cancelled."));
    }
}
