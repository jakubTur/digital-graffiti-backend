
package edu.w04ist.si041g.backend.credential.dto;

import lombok.Data;

@Data
public class CredentialRequestBody {

    private Long userId;
    private String email;
    private String password;
}
