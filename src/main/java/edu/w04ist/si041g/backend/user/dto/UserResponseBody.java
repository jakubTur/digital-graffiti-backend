package edu.w04ist.si041g.backend.user.dto;

public record UserResponseBody(
        String nickname,
        boolean active,
        boolean verified,
        String email,
        String role
) {}