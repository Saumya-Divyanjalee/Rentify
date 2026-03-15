package lk.ijse.aad.backend.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AuthResponseDTO {
    private String token;
    private String role;
    private Long userId;
    private String fullName;
    private String email;
    private String username;
}