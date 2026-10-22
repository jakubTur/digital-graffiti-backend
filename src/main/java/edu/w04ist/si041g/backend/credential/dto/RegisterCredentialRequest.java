package edu.w04ist.si041g.backend.credential.dto;

import lombok.Data;

@Data
public class RegisterCredentialRequest {
    private String login;
    private String password;
}