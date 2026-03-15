package lk.ijse.aad.backend.service.custom;

import lk.ijse.aad.backend.dto.ChangePasswordDTO;
import lk.ijse.aad.backend.dto.UserDTO;

import java.util.List;

public interface UserService {

    // ── User self-service ──────────────────────────────────────────────────────
    UserDTO getProfile(String username);
    UserDTO updateProfile(String username, UserDTO dto);
    void    changePassword(String username, ChangePasswordDTO dto);

    // ── Admin operations on users ──────────────────────────────────────────────
    List<UserDTO> getAllUsers();
    UserDTO       getUserById(Integer id);
    UserDTO       updateUser(Integer id, UserDTO dto);
    void          deleteUser(Integer id);
}