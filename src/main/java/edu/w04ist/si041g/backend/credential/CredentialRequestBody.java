
package edu.w04ist.si041g.backend.credential;

import lombok.Data;

@Data
public class CredentialRequestBody {

    private Long userId;
    private String login;
    private String password;
}
