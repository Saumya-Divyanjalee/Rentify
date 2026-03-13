package lk.ijse.aad.backend.controller;

import lk.ijse.aad.backend.service.custom.UserService;
import lk.ijse.aad.backend.utill.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {

    private final UserService userService;

    @PostMapping("change-password")
    public ResponseEntity<APIResponse<String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {

        String currentPassword = body.get("currentPassword");
        String newPassword     = body.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest()
                    .body(new APIResponse<>(400, "Invalid password data", null));
        }

        userService.changePassword(userDetails.getUsername(), currentPassword, newPassword);
        return ResponseEntity.ok(
                new APIResponse<>(200, "Password updated successfully", null));
    }
}