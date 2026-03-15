package lk.ijse.aad.backend.controller;

import lk.ijse.aad.backend.dto.AuthResponseDTO;
import lk.ijse.aad.backend.dto.RegisterDTO;
import lk.ijse.aad.backend.dto.SignInDTO;
import lk.ijse.aad.backend.service.AuthService;
import lk.ijse.aad.backend.utill.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/v1/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<APIResponse<String>> signup(@RequestBody RegisterDTO dto) {
        String result = authService.signup(dto);
        return ResponseEntity.ok(new APIResponse<>(200, "OK", result));
    }

    // POST /api/v1/auth/signin
    @PostMapping("/signin")
    public ResponseEntity<APIResponse<AuthResponseDTO>> signin(@RequestBody SignInDTO dto) {
        AuthResponseDTO response = authService.signin(dto);
        return ResponseEntity.ok(new APIResponse<>(200, "OK", response));
    }
}