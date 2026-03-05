package lk.ijse.aad.backend.dto;

import lombok.Data;

@Data
public class RegisterDTO {

    private String fullName;
    private String userName;
    private String password;
    private String email;
}
