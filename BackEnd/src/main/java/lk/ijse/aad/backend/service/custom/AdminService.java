package lk.ijse.aad.backend.service.custom;

import lk.ijse.aad.backend.dto.AdminDTO;
import lk.ijse.aad.backend.dto.ChangePasswordDTO;

import java.util.List;

public interface AdminService {

    // ── Admin self-service ─────────────────────────────────────────────────────
    AdminDTO getProfile(String username);
    AdminDTO updateProfile(String username, AdminDTO dto);
    void     changePassword(String username, ChangePasswordDTO dto);

    // ── Admin CRUD ─────────────────────────────────────────────────────────────
    List<AdminDTO> getAllAdmins();
    AdminDTO       getAdminById(Integer id);
    AdminDTO       updateAdmin(Integer id, AdminDTO dto);
    void           deleteAdmin(Integer id);
}