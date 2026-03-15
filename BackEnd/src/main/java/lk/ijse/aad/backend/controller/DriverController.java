package lk.ijse.aad.backend.controller;

import lk.ijse.aad.backend.dto.DriverDTO;
import lk.ijse.aad.backend.service.custom.DriverService;
import lk.ijse.aad.backend.utill.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drivers")
@CrossOrigin(origins = "*")  // Allow frontend jQuery AJAX
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;

    // GET all / search
    @GetMapping
    public ResponseEntity<APIResponse<List<DriverDTO>>> getAll(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String vehicleType) {

        List<DriverDTO> result = driverService.searchDrivers(query, vehicleType);
        return ResponseEntity.ok(new APIResponse<>(200, "Success", result));
    }

    // GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<DriverDTO>> getById(@PathVariable Long id) {
        DriverDTO driver = driverService.getDriver(id);
        return ResponseEntity.ok(new APIResponse<>(200, "Success", driver));
    }

    // POST — Add new driver
    @PostMapping
    public ResponseEntity<APIResponse<DriverDTO>> create(@RequestBody DriverDTO dto) {
        DriverDTO saved = driverService.addDriver(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new APIResponse<>(201, "Driver added successfully", saved));
    }

    // PUT — Update driver
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<DriverDTO>> update(
            @PathVariable Long id, @RequestBody DriverDTO dto) {
        DriverDTO updated = driverService.updateDriver(id, dto);
        return ResponseEntity.ok(new APIResponse<>(200, "Driver updated successfully", updated));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> delete(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return ResponseEntity.ok(new APIResponse<>(200, "Driver deleted successfully", null));
    }
}