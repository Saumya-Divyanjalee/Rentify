package lk.ijse.aad.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.ijse.aad.backend.dto.InsuranceDTO;
import lk.ijse.aad.backend.enums.InsuranceStatus;
import lk.ijse.aad.backend.service.custom.InsuranceService;
import lk.ijse.aad.backend.utill.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/insurances")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Insurance", description = "Vehicle insurance policy management")
public class InsuranceController {

    private final InsuranceService insuranceService;

    // ── Public / Authenticated reads ─────────────────────────────────────────

    @Operation(summary = "Get all insurance policies",
            description = "Returns all policies. ADMIN only.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<List<InsuranceDTO>>> getAllPolicies() {
        return ResponseEntity.ok(new APIResponse<>(200, "Success", insuranceService.getAllPolicies()));
    }

    @Operation(summary = "Get a single policy by ID",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy found"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<InsuranceDTO>> getPolicyById(
            @Parameter(description = "Insurance policy ID") @PathVariable Long id) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success", insuranceService.getPolicyById(id)));
    }

    @Operation(summary = "Get all policies for a vehicle",
            description = "Returns all insurance policies linked to a specific vehicle.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<List<InsuranceDTO>>> getPoliciesByVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long vehicleId) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success",
                insuranceService.getPoliciesByVehicle(vehicleId)));
    }

    @Operation(summary = "Get active policies for a vehicle",
            description = "Returns only ACTIVE policies for the given vehicle.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/vehicle/{vehicleId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<List<InsuranceDTO>>> getActivePoliciesByVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long vehicleId) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success",
                insuranceService.getActivePoliciesByVehicle(vehicleId)));
    }

    @Operation(summary = "Get policies filtered by status",
            description = "Returns policies with the specified status: ACTIVE | EXPIRED | CANCELLED",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<List<InsuranceDTO>>> getPoliciesByStatus(
            @Parameter(description = "Policy status: ACTIVE | EXPIRED | CANCELLED")
            @PathVariable InsuranceStatus status) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success",
                insuranceService.getPoliciesByStatus(status)));
    }

    @Operation(summary = "Get policies expiring soon",
            description = "Returns ACTIVE policies expiring within the next N days.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<List<InsuranceDTO>>> getExpiringPolicies(
            @Parameter(description = "Number of days ahead to check (default 30)")
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success",
                insuranceService.getExpiringPolicies(days)));
    }

    // ── Admin write operations ────────────────────────────────────────────────

    @Operation(summary = "Add a new insurance policy",
            description = "ADMIN only. Links a policy to an existing vehicle.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy created"),
            @ApiResponse(responseCode = "400", description = "Validation failed or duplicate policy number"),
            @ApiResponse(responseCode = "403", description = "Admin access required"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<InsuranceDTO>> addPolicy(
            @Valid @RequestBody InsuranceDTO dto) {
        return ResponseEntity.ok(new APIResponse<>(200, "Insurance policy added successfully.",
                insuranceService.addPolicy(dto)));
    }

    @Operation(summary = "Update an insurance policy",
            description = "ADMIN only. Full update of a policy.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Admin access required"),
            @ApiResponse(responseCode = "404", description = "Policy or Vehicle not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<InsuranceDTO>> updatePolicy(
            @Parameter(description = "Insurance policy ID") @PathVariable Long id,
            @Valid @RequestBody InsuranceDTO dto) {
        return ResponseEntity.ok(new APIResponse<>(200, "Insurance policy updated.",
                insuranceService.updatePolicy(id, dto)));
    }

    @Operation(summary = "Cancel an insurance policy",
            description = "ADMIN only. Sets status to CANCELLED without deleting the record.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy cancelled"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<InsuranceDTO>> cancelPolicy(
            @Parameter(description = "Insurance policy ID") @PathVariable Long id) {
        return ResponseEntity.ok(new APIResponse<>(200, "Insurance policy cancelled.",
                insuranceService.cancelPolicy(id)));
    }

    @Operation(summary = "Delete an insurance policy",
            description = "ADMIN only. Permanently removes the policy.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy deleted"),
            @ApiResponse(responseCode = "404", description = "Policy not found"),
            @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<String>> deletePolicy(
            @Parameter(description = "Insurance policy ID") @PathVariable Long id) {
        insuranceService.deletePolicy(id);
        return ResponseEntity.ok(new APIResponse<>(200, "Insurance policy deleted successfully.", null));
    }
}

