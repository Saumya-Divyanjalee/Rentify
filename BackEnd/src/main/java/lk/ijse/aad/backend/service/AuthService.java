package lk.ijse.aad.backend.service;

import lk.ijse.aad.backend.dto.AuthResponseDTO;
import lk.ijse.aad.backend.dto.RegisterDTO;
import lk.ijse.aad.backend.dto.SignInDTO;
import lk.ijse.aad.backend.entity.Admin;
import lk.ijse.aad.backend.enums.Role;
import lk.ijse.aad.backend.entity.User;
import lk.ijse.aad.backend.repository.AdminRepository;
import lk.ijse.aad.backend.repository.UserRepository;
import lk.ijse.aad.backend.service.EmailService;
import lk.ijse.aad.backend.utill.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JWTUtil jwtUtil;
    private final EmailService emailService;


    public String signup(RegisterDTO dto) {
        String role = (dto.getRole() != null) ? dto.getRole().toUpperCase() : "USER";

        if ("ADMIN".equals(role)) {
            if (adminRepository.existsByUsername(dto.getUsername())) throw new RuntimeException("Admin Username taken");

            Admin admin = new Admin();
            admin.setFullName(dto.getFullName());
            admin.setUsername(dto.getUsername());
            admin.setEmail(dto.getEmail());
            admin.setPhone(dto.getPhone());
            admin.setPassword(passwordEncoder.encode(dto.getPassword()));
            admin.setRole(Role.ADMIN);
            adminRepository.save(admin);

            emailService.sendWelcomeEmail(dto.getEmail(), dto.getFullName());
            return "Admin registered successfully!";
        } else {
            if (userRepository.existsByUsername(dto.getUsername())) throw new RuntimeException("User Username taken");

            User user = new User();
            user.setFullName(dto.getFullName());
            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());
            user.setPhone(dto.getPhone());
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setRole(Role.USER);
            userRepository.save(user);

            emailService.sendWelcomeEmail(dto.getEmail(), dto.getFullName());
            return "User registered successfully!";
        }
    }


    public AuthResponseDTO signin(SignInDTO dto) {
         authManager.authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));

         var adminOpt = adminRepository.findByUsername(dto.getUsername());
        if (adminOpt.isPresent()) {
            Admin a = adminOpt.get();
            String token = jwtUtil.generateToken(a.getUsername(), "ADMIN");
            emailService.sendLoginNotificationEmail(a.getEmail(), a.getUsername());
            return new AuthResponseDTO(token, "ADMIN", a.getAdminId(), a.getFullName(), a.getEmail(), a.getUsername());
        }

         var userOpt = userRepository.findByUsername(dto.getUsername());
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            String token = jwtUtil.generateToken(u.getUsername(), "USER");
            emailService.sendLoginNotificationEmail(u.getEmail(), u.getUsername());
            return new AuthResponseDTO(token, "USER", u.getUserId(), u.getFullName(), u.getEmail(), u.getUsername());
        }

        throw new RuntimeException("User not found after authentication");
    }
}