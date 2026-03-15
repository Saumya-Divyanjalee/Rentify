package lk.ijse.aad.backend.controller;

import lk.ijse.aad.backend.dto.ChangePasswordDTO;
import lk.ijse.aad.backend.dto.UserDTO;
import lk.ijse.aad.backend.service.custom.UserService;
import lk.ijse.aad.backend.utill.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ── USER SELF-SERVICE ─────────────────────────────────────────────────────

    // GET /api/v1/user/profile
    @GetMapping("/profile")
    public ResponseEntity<APIResponse<UserDTO>> getProfile(Authentication auth) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success",
                userService.getProfile(auth.getName())));
    }

    // PUT /api/v1/user/profile
    @PutMapping("/profile")
    public ResponseEntity<APIResponse<UserDTO>> updateProfile(
            Authentication auth,
            @RequestBody UserDTO dto) {
        return ResponseEntity.ok(new APIResponse<>(200, "Profile updated",
                userService.updateProfile(auth.getName(), dto)));
    }

    // POST /api/v1/user/change-password
    @PostMapping("/change-password")
    public ResponseEntity<APIResponse<String>> changePassword(
            Authentication auth,
            @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(auth.getName(), dto);
        return ResponseEntity.ok(new APIResponse<>(200, "Password changed successfully", null));
    }

    // ── ADMIN: USER CRUD ──────────────────────────────────────────────────────

    // GET /api/v1/user
    @GetMapping
    public ResponseEntity<APIResponse<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(new APIResponse<>(200, "Success",
                userService.getAllUsers()));
    }

    // GET /api/v1/user/{id}
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<UserDTO>> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success",
                userService.getUserById(id)));
    }

    // PUT /api/v1/user/{id}
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<UserDTO>> updateUser(
            @PathVariable Integer id,
            @RequestBody UserDTO dto) {
        return ResponseEntity.ok(new APIResponse<>(200, "User updated",
                userService.updateUser(id, dto)));
    }

    // DELETE /api/v1/user/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new APIResponse<>(200, "User deleted", null));
    }
}