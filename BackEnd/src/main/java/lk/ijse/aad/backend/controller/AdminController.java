package lk.ijse.aad.backend.controller;

import lk.ijse.aad.backend.dto.AdminDTO;
import lk.ijse.aad.backend.dto.ChangePasswordDTO;
import lk.ijse.aad.backend.dto.UserDTO;
import lk.ijse.aad.backend.service.custom.AdminService;
import lk.ijse.aad.backend.service.custom.UserService;
import lk.ijse.aad.backend.utill.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService  userService;

    // ── ADMIN SELF PROFILE ────────────────────────────────────────────────────

    // GET /api/v1/admin/profile
    @GetMapping("/profile")
    public ResponseEntity<APIResponse<AdminDTO>> getProfile(Authentication auth) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success",
                adminService.getProfile(auth.getName())));
    }

    // PUT /api/v1/admin/profile
    @PutMapping("/profile")
    public ResponseEntity<APIResponse<AdminDTO>> updateProfile(
            Authentication auth,
            @RequestBody AdminDTO dto) {
        return ResponseEntity.ok(new APIResponse<>(200, "Profile updated",
                adminService.updateProfile(auth.getName(), dto)));
    }

    // POST /api/v1/admin/change-password
    @PostMapping("/change-password")
    public ResponseEntity<APIResponse<String>> changePassword(
            Authentication auth,
            @RequestBody ChangePasswordDTO dto) {
        adminService.changePassword(auth.getName(), dto);
        return ResponseEntity.ok(new APIResponse<>(200, "Password changed successfully", null));
    }

    // ── ADMIN CRUD (super-admin managing other admins) ────────────────────────

    // GET /api/v1/admin/all
    @GetMapping("/all")
    public ResponseEntity<APIResponse<List<AdminDTO>>> getAllAdmins() {
        return ResponseEntity.ok(new APIResponse<>(200, "Success",
                adminService.getAllAdmins()));
    }

    // GET /api/v1/admin/{id}
    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<AdminDTO>> getAdminById(@PathVariable Integer id) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success",
                adminService.getAdminById(id)));
    }

    // PUT /api/v1/admin/{id}
    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<AdminDTO>> updateAdmin(
            @PathVariable Integer id,
            @RequestBody AdminDTO dto) {
        return ResponseEntity.ok(new APIResponse<>(200, "Admin updated",
                adminService.updateAdmin(id, dto)));
    }

    // DELETE /api/v1/admin/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<String>> deleteAdmin(@PathVariable Integer id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(new APIResponse<>(200, "Admin deleted", null));
    }

    // ── ADMIN: MANAGE ALL USERS ───────────────────────────────────────────────

    // GET /api/v1/admin/users
    @GetMapping("/users")
    public ResponseEntity<APIResponse<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(new APIResponse<>(200, "Success",
                userService.getAllUsers()));
    }

    // GET /api/v1/admin/users/{id}
    @GetMapping("/users/{id}")
    public ResponseEntity<APIResponse<UserDTO>> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(new APIResponse<>(200, "Success",
                userService.getUserById(id)));
    }

    // PUT /api/v1/admin/users/{id}
    @PutMapping("/users/{id}")
    public ResponseEntity<APIResponse<UserDTO>> updateUser(
            @PathVariable Integer id,
            @RequestBody UserDTO dto) {
        return ResponseEntity.ok(new APIResponse<>(200, "User updated",
                userService.updateUser(id, dto)));
    }

    // DELETE /api/v1/admin/users/{id}
    @DeleteMapping("/users/{id}")
    public ResponseEntity<APIResponse<String>> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new APIResponse<>(200, "User deleted", null));
    }
}