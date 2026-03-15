package lk.ijse.aad.backend.service.impl;

import lk.ijse.aad.backend.dto.ChangePasswordDTO;
import lk.ijse.aad.backend.dto.UserDTO;
import lk.ijse.aad.backend.entity.User;
import lk.ijse.aad.backend.repository.UserRepository;
import lk.ijse.aad.backend.service.custom.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Get own profile ───────────────────────────────────────────────────────
    @Override
    public UserDTO getProfile(String username) {
        return toDTO(findByUsername(username), true);
    }

    // ── Update own profile ────────────────────────────────────────────────────
    @Override
    public UserDTO updateProfile(String username, UserDTO dto) {
        User user = findByUsername(username);
        applyUpdates(user, dto);
        userRepository.save(user);
        return toDTO(user, false);
    }

    // ── Change own password ───────────────────────────────────────────────────
    @Override
    public void changePassword(String username, ChangePasswordDTO dto) {
        User user = findByUsername(username);
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword()))
            throw new RuntimeException("Current password is incorrect");
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    // ── Admin: get all users ──────────────────────────────────────────────────
    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(u -> toDTO(u, false))
                .collect(Collectors.toList());
    }

    // ── Admin: get one user ───────────────────────────────────────────────────
    @Override
    public UserDTO getUserById(Integer id) {
        return toDTO(findById(id), false);
    }

    // ── Admin: update any user ────────────────────────────────────────────────
    @Override
    public UserDTO updateUser(Integer id, UserDTO dto) {
        User user = findById(id);
        applyUpdates(user, dto);
        userRepository.save(user);
        return toDTO(user, false);
    }

    // ── Admin: delete user ────────────────────────────────────────────────────
    @Override
    public void deleteUser(Integer id) {
        if (!userRepository.existsById(Long.valueOf(id)))
            throw new RuntimeException("User not found with id: " + id);
        userRepository.deleteById(Long.valueOf(id));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private User findById(Integer id) {
        return userRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    private void applyUpdates(User user, UserDTO dto) {
        if (dto.getFullName() != null && !dto.getFullName().isBlank())
            user.setFullName(dto.getFullName());
        if (dto.getEmail() != null && !dto.getEmail().isBlank())
            user.setEmail(dto.getEmail());
        if (dto.getPhone() != null && !dto.getPhone().isBlank())
            user.setPhone(dto.getPhone());
        if (dto.getPassword() != null && !dto.getPassword().isBlank())
            user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // Profile image (accepts base64 data URL or pure base64)
        if (dto.getProfileImageBase64() != null && !dto.getProfileImageBase64().isBlank()) {
            String b64 = dto.getProfileImageBase64();
            if (b64.contains(",")) b64 = b64.split(",")[1];
            user.setProfileImage(Base64.getDecoder().decode(b64));
            user.setImageType(dto.getImageType());
        }
    }

    private UserDTO toDTO(User u, boolean withImage) {
        String img = (withImage && u.getProfileImage() != null)
                ? Base64.getEncoder().encodeToString(u.getProfileImage()) : null;
        return new UserDTO(
                Math.toIntExact(u.getUserId()), u.getFullName(), u.getUsername(),
                u.getEmail(), u.getPhone(),
                null,                // never return password
                u.getRole().name(),
                img, u.getImageType()
        );
    }
}