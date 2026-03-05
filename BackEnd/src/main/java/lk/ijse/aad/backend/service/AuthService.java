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
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final EmailService emailService;
    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

    //supports login by email or username + password.
    //returns JWT token and role for frontend redirect
    public AuthResponseDTO authenticate(AuthDTO authDTO) {
        User user = userRepo.findByUsername(authDTO.getUsername())
                .or(() -> userRepo.findByUsername(authDTO.getUsername()))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + authDTO.getUsername()));

        //verify password
        if(!passwordEncoder.matches(authDTO.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Incorrect password");
        }

        //generate JWT with role embedded
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponseDTO(token, user.getRole().name());

    }

    //register new user (always USER role from signup page)
    //sends welcome email asynchronously after saving

    public String register(RegisterDTO  dto) {
        //check for duplicate username
        if (userRepo.existsByUsername((dto.getUserName())){
            throw new RuntimeException("Username '"+dto.getUserName()+"' is already taken !");
        }

        //check for duplicate email
        if(dto.getEmail() != null && userRepo.existsByEmail(dto.getEmail())){
            throw new RuntimeException("Email '"+dto.getEmail()+"' is already registered !");
        }

        //determine role
        Role role;
        try {
            role = Role.valueOf(dto.getRole() != null? dto.getRole().toUpperCase() : "USER");

        }catch (IllegalArgumentException e){
            role = Role.USER;
        }

        User user = User.builder()
                .fullName(dto.getFullName())
                .username(dto.getUserName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .password(passwordEncoder.encode((dto.getPassword())))
                .role(role)
                .build();
        userRepo.save(user);

        //send welcome email asynchronously
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            emailService.sendWelcomeEmail(dto.getEmail(),dto.getFullName());

        }
        return "User registered successfully !";
    }

}
