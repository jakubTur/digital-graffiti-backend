package edu.w04ist.si041g.backend.user.dto;

import lombok.Data;

@Data
public class UserUpdateBody {
    private String nickname;
    private String email;
    private boolean verified;
    private boolean active;
    private String bio;
}
