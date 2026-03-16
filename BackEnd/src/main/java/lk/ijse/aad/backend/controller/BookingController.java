package lk.ijse.aad.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.ijse.aad.backend.dto.BookingDTO;
import lk.ijse.aad.backend.enums.BookingStatus;
import lk.ijse.aad.backend.service.custom.BookingService;
import lk.ijse.aad.backend.utill.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Bookings", description = "Booking management — create, view, update, cancel")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @Operation(
            summary = "Create a booking",
            description = """
            USER only. System performs 4 availability checks before booking:
            1. Vehicle status must be AVAILABLE
            2. Insurance must be active
            3. Insurance must not expire before booking end date
            4. No overlapping bookings for selected dates
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid dates"),
            @ApiResponse(responseCode = "404", description = "User or vehicle not found"),
            @ApiResponse(responseCode = "409", description = "Vehicle not available or date conflict")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<APIResponse<BookingDTO>> createBooking(
            @Valid @RequestBody BookingDTO dto) {
        return ResponseEntity.ok(new APIResponse<>(200, "Booking created.", bookingService.createBooking(dto)));
    }

    @Operation(summary = "Get bookings for a specific user", description = "USER sees own bookings; ADMIN sees any user's bookings.")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<APIResponse<List<BookingDTO>>> getMyBookings(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success", bookingService.getBookingsByUser(userId)));
    }

    @Operation(summary = "Get all bookings", description = "ADMIN only. Returns all bookings in the system.")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<List<BookingDTO>>> getAllBookings() {
        return ResponseEntity.ok(new APIResponse<>(200, "Success", bookingService.getAllBookings()));
    }

    @Operation(summary = "Get a single booking by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<APIResponse<BookingDTO>> getBookingById(
            @Parameter(description = "Booking ID") @PathVariable Long id) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success", bookingService.getBookingById(id)));
    }

    @Operation(
            summary = "Update booking status",
            description = "ADMIN only. Status transitions: PENDING → CONFIRMED → COMPLETED. CANCELLED frees the vehicle."
    )
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<BookingDTO>> updateStatus(
            @Parameter(description = "Booking ID") @PathVariable Long id,
            @Parameter(description = "New status: PENDING | CONFIRMED | COMPLETED | CANCELLED")
            @RequestParam BookingStatus status) {
        return ResponseEntity.ok(new APIResponse<>(200, "Status updated.", bookingService.updateBookingStatus(id, status)));
    }

    @Operation(summary = "Cancel a booking", description = "USER can only cancel their own booking.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking cancelled, vehicle released"),
            @ApiResponse(responseCode = "403", description = "Cannot cancel another user's booking"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<APIResponse<String>> cancelBooking(
            @Parameter(description = "Booking ID") @PathVariable Long id,
            @Parameter(description = "User ID requesting cancellation") @RequestParam Long userId) {
        bookingService.cancelBooking(id, userId);
        return ResponseEntity.ok(new APIResponse<>(200, "Booking cancelled.", null));
    }
}