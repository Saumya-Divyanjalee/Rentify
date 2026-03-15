package lk.ijse.aad.backend.utill;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class APIResponse<T> {
    private int code;
    private String message;
    private T data;
}