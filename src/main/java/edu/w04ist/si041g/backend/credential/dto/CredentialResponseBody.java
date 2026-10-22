
package edu.w04ist.si041g.backend.credential.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CredentialResponseBody {

    private Long id;
    private Long userId;
    private String login;
}
