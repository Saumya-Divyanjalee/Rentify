package lk.ijse.aad.backend.dto;

import lombok.Data;

@Data
public class RegisterDTO {

    private String fullName;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String role;   // Lombok @Data auto-generates getRole() for you
}