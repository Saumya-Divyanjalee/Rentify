package lk.ijse.aad.backend.controller;

import lk.ijse.aad.backend.dto.AuthDTO;
import lk.ijse.aad.backend.dto.RegisterDTO;
import lk.ijse.aad.backend.service.AuthService;
import lk.ijse.aad.backend.utill.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/signup
     * Body: { fullName, username, email, phone, password, role }
     */
    @PostMapping("signup")
    public ResponseEntity<APIResponse<String>> registerUser(
            @RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.ok(
                new APIResponse<>(200, "OK", authService.register(registerDTO))
        );
    }

    /**
     * POST /api/v1/auth/signin
     * Body: { username, password }
     * Returns: { accessToken, role }
     */
    @PostMapping("signin")
    public ResponseEntity<APIResponse<Object>> loginUser(
            @RequestBody AuthDTO authDTO) {
        return ResponseEntity.ok(
                new APIResponse<>(200, "OK", authService.authenticate(authDTO))
        );
    }
}