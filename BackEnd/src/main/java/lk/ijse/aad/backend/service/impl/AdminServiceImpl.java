package lk.ijse.aad.backend.service.impl;

import lk.ijse.aad.backend.dto.AdminDTO;
import lk.ijse.aad.backend.dto.ChangePasswordDTO;
import lk.ijse.aad.backend.entity.Admin;
import lk.ijse.aad.backend.repository.AdminRepository;
import lk.ijse.aad.backend.service.custom.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    // ── Get own profile ───────────────────────────────────────────────────────
    @Override
    public AdminDTO getProfile(String username) {
        return toDTO(findByUsername(username), true);
    }

    // ── Update own profile ────────────────────────────────────────────────────
    @Override
    public AdminDTO updateProfile(String username, AdminDTO dto) {
        Admin admin = findByUsername(username);
        applyUpdates(admin, dto);
        adminRepository.save(admin);
        return toDTO(admin, false);
    }

    // ── Change own password ───────────────────────────────────────────────────
    @Override
    public void changePassword(String username, ChangePasswordDTO dto) {
        Admin admin = findByUsername(username);
        if (!passwordEncoder.matches(dto.getCurrentPassword(), admin.getPassword()))
            throw new RuntimeException("Current password is incorrect");
        admin.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        adminRepository.save(admin);
    }

    // ── Get all admins ────────────────────────────────────────────────────────
    @Override
    public List<AdminDTO> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(a -> toDTO(a, false))
                .collect(Collectors.toList());
    }

    // ── Get admin by id ───────────────────────────────────────────────────────
    @Override
    public AdminDTO getAdminById(Integer id) {
        return toDTO(findById(id), false);
    }

    // ── Update admin ──────────────────────────────────────────────────────────
    @Override
    public AdminDTO updateAdmin(Integer id, AdminDTO dto) {
        Admin admin = findById(id);
        applyUpdates(admin, dto);
        adminRepository.save(admin);
        return toDTO(admin, false);
    }

    // ── Delete admin ──────────────────────────────────────────────────────────
    @Override
    public void deleteAdmin(Integer id) {
        if (!adminRepository.existsById(Long.valueOf(id)))
            throw new RuntimeException("Admin not found with id: " + id);
        adminRepository.deleteById(Long.valueOf(id));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private Admin findByUsername(String username) {
        return adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found: " + username));
    }

    private Admin findById(Integer id) {
        return adminRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + id));
    }

    private void applyUpdates(Admin admin, AdminDTO dto) {
        if (dto.getFullName() != null && !dto.getFullName().isBlank())
            admin.setFullName(dto.getFullName());
        if (dto.getEmail() != null && !dto.getEmail().isBlank())
            admin.setEmail(dto.getEmail());
        if (dto.getPhone() != null && !dto.getPhone().isBlank())
            admin.setPhone(dto.getPhone());
        if (dto.getPassword() != null && !dto.getPassword().isBlank())
            admin.setPassword(passwordEncoder.encode(dto.getPassword()));

        if (dto.getProfileImageBase64() != null && !dto.getProfileImageBase64().isBlank()) {
            String b64 = dto.getProfileImageBase64();
            if (b64.contains(",")) b64 = b64.split(",")[1];
            admin.setProfileImage(Base64.getDecoder().decode(b64));
            admin.setImageType(dto.getImageType());
        }
    }

    private AdminDTO toDTO(Admin a, boolean withImage) {
        String img = (withImage && a.getProfileImage() != null)
                ? Base64.getEncoder().encodeToString(a.getProfileImage()) : null;
        return new AdminDTO(
                Math.toIntExact(a.getAdminId()), a.getFullName(), a.getUsername(),
                a.getEmail(), a.getPhone(),
                null,       // never return password
                img, a.getImageType()
        );
    }
}