package lk.ijse.aad.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDTO {

    private Integer adminId;
    private String fullName;
    private String username;
    private String email;
    private String phone;


    private String password;

    private String profileImageBase64;
    private String imageType;

}