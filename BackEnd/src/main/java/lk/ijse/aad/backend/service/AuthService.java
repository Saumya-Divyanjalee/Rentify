package lk.ijse.aad.backend.service;

import lk.ijse.aad.backend.dto.AuthDTO;
import lk.ijse.aad.backend.dto.AuthResponseDTO;
import lk.ijse.aad.backend.dto.RegisterDTO;
import lk.ijse.aad.backend.entity.Role;
import lk.ijse.aad.backend.entity.User;
import lk.ijse.aad.backend.repository.UserRepo;
import lk.ijse.aad.backend.utill.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final EmailService emailService;

    // ─── SIGN IN ────────────────────────────────────────────────
    public AuthResponseDTO authenticate(AuthDTO authDTO) {
        User user = userRepo.findByUsername(authDTO.getUsername())
                .or(() -> userRepo.findByEmail(authDTO.getUsername()))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No account found for: " + authDTO.getUsername()));

        if (!passwordEncoder.matches(authDTO.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Incorrect password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        // ✅ Send login notification email (async – never delays the response)
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            emailService.sendLoginNotificationEmail(user.getEmail(), user.getUsername());
        }

        return new AuthResponseDTO(token, user.getRole().name());
    }

    // ─── SIGN UP ────────────────────────────────────────────────
    public String register(RegisterDTO dto) {
        if (userRepo.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username '" + dto.getUsername() + "' is already taken!");
        }
        if (dto.getEmail() != null && userRepo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email '" + dto.getEmail() + "' is already registered!");
        }

        Role role;
        try {
            role = Role.valueOf(dto.getRole() != null ? dto.getRole().toUpperCase() : "USER");
        } catch (IllegalArgumentException e) {
            role = Role.USER;
        }

        User user = User.builder()
                .fullName(dto.getFullName())
                .username(dto.getUsername())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(role)
                .build();

        userRepo.save(user);

        // ✅ Send welcome email (async – never delays the signup response)
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            emailService.sendWelcomeEmail(dto.getEmail(), dto.getFullName());
        }

        return "User registered successfully!";
    }
}