package lk.ijse.aad.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.ijse.aad.backend.dto.VehicleDTO;
import lk.ijse.aad.backend.enums.VehicleType;
import lk.ijse.aad.backend.service.custom.VehicleService;
import lk.ijse.aad.backend.utill.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/vehicles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Vehicles", description = "Vehicle management — browse, add, edit, delete")
public class VehicleController {

    private final VehicleService vehicleService;


    @Operation(summary = "Get all vehicles", description = "Returns all vehicles regardless of status. Public access.")
    @GetMapping
    public ResponseEntity<APIResponse<List<VehicleDTO>>> getAllVehicles() {
        return ResponseEntity.ok(new APIResponse<>(200, "Success", vehicleService.getAllVehicles()));
    }

    @Operation(summary = "Get available vehicles", description = "Returns only AVAILABLE vehicles. Public access.")
    @GetMapping("/available")
    public ResponseEntity<APIResponse<List<VehicleDTO>>> getAvailableVehicles() {
        return ResponseEntity.ok(new APIResponse<>(200, "Success", vehicleService.getAvailableVehicles()));
    }

    @Operation(summary = "Filter vehicles by type", description = "Returns vehicles of a specific type: CAB, VAN, BUS, WHEEL")
    @GetMapping("/type/{type}")
    public ResponseEntity<APIResponse<List<VehicleDTO>>> getByType(
            @Parameter(description = "Vehicle type: CAB | VAN | BUS | WHEEL") @PathVariable VehicleType type) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success", vehicleService.getVehiclesByType(type)));
    }

    @Operation(summary = "Get available vehicles by type")
    @GetMapping("/type/{type}/available")
    public ResponseEntity<APIResponse<List<VehicleDTO>>> getAvailableByType(
            @Parameter(description = "Vehicle type: CAB | VAN | BUS | WHEEL") @PathVariable VehicleType type) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success", vehicleService.getAvailableVehiclesByType(type)));
    }

    @Operation(summary = "Get a single vehicle by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle found"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<VehicleDTO>> getById(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success", vehicleService.getVehicleById(id)));
    }

    @Operation(summary = "Add a new vehicle", description = "ADMIN only. Adds a vehicle to the fleet.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle added successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<VehicleDTO>> addVehicle(@Valid @RequestBody VehicleDTO dto) {
        return ResponseEntity.ok(new APIResponse<>(200, "Vehicle added successfully.", vehicleService.addVehicle(dto)));
    }

    @Operation(summary = "Update a vehicle", description = "ADMIN only.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<VehicleDTO>> updateVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Valid @RequestBody VehicleDTO dto) {
        return ResponseEntity.ok(new APIResponse<>(200, "Vehicle updated.", vehicleService.updateVehicle(id, dto)));
    }

    @Operation(summary = "Delete a vehicle", description = "ADMIN only. Permanently removes from fleet.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle deleted"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> deleteVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.ok(new APIResponse<>(200, "Vehicle deleted successfully.", null));
    }
}
