package edu.w04ist.si041g.backend.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Data
public class UserRequestBody {
    @NotBlank(message = "Username is required")
    private String nickname;
    @NotBlank(message = "Email is required")
    private String email;
}
