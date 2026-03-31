package lk.ijse.aad.backend.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class RegisterDTO {
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String password;
    private String role;
}