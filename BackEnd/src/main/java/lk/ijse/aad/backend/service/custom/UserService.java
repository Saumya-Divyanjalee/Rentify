package lk.ijse.aad.backend.service.custom;

public interface UserService {
    void changePassword(String username, String currentPassword, String newPassword);

}
