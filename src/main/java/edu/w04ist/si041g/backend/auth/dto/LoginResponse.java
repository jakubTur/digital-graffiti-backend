package edu.w04ist.si041g.backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String nickname;
    private String email;
    private String role;
}
