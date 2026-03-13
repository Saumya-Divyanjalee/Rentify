package lk.ijse.aad.backend.controller;

import jakarta.validation.Valid;
import lk.ijse.aad.backend.dto.CabDTO;
import lk.ijse.aad.backend.service.custom.CabService;
import lk.ijse.aad.backend.utill.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/cab")
@RequiredArgsConstructor
@CrossOrigin
public class CabController {

    private final CabService cabService;

    // ─── POST /api/v1/cab ──────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<APIResponse<String>> saveCab(
            @Valid @RequestBody CabDTO cabDTO) {

        cabDTO.setCabId(null);
        cabService.saveCab(cabDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new APIResponse<>(201, "Cab added successfully", null));
    }

    // ─── GET /api/v1/cab ───────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<APIResponse<List<CabDTO>>> getAllCabs() {
        return ResponseEntity.ok(
                new APIResponse<>(200, "Success", cabService.getAllCabs()));
    }

    // ─── GET /api/v1/cab/available ─────────────────────────────────────────
    @GetMapping("/available")
    public ResponseEntity<APIResponse<List<CabDTO>>> getAvailableCabs() {
        return ResponseEntity.ok(
                new APIResponse<>(200, "Success", cabService.getAvailableCabs()));
    }

    // ─── GET /api/v1/cab/{id} ──────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<CabDTO>> getCabById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                new APIResponse<>(200, "Success", cabService.getCabById(id)));
    }

    // ─── GET /api/v1/cab/{id}/image ────────────────────────────────────────
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getCabImage(@PathVariable Integer id) {
        byte[] image = cabService.getCabImage(id);
        if (image == null || image.length == 0) {
            return ResponseEntity.notFound().build();
        }
        String imageType = cabService.getCabImageType(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imageType))
                .body(image);
    }

    // ─── PUT /api/v1/cab/{id} ──────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<String>> updateCab(
            @PathVariable Integer id,
            @Valid @RequestBody CabDTO cabDTO) {

        cabService.updateCab(id, cabDTO);
        return ResponseEntity.ok(
                new APIResponse<>(200, "Cab updated successfully", null));
    }

    // ─── DELETE /api/v1/cab/{id} ───────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> deleteCab(
            @PathVariable Integer id) {

        cabService.deleteCab(id);
        return ResponseEntity.ok(
                new APIResponse<>(200, "Cab deleted successfully", null));
    }
}