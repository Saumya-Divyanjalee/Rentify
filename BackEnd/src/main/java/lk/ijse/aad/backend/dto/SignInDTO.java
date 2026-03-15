package lk.ijse.aad.backend.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SignInDTO {
    private String username;
    private String password;
}