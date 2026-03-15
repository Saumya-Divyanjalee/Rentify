package lk.ijse.aad.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Integer userId;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String password;          // receive only — never returned
    private String role;
    private String profileImageBase64;
    private String imageType;
}