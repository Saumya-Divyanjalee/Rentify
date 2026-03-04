package lk.ijse.aad.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String lastName;

    private String username;

    private String email;
    private String mobile;
    private String address;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String password;
}
