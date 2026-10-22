package edu.w04ist.si041g.backend.credential;

import edu.w04ist.si041g.backend.credential.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credentials")
public class CredentialController {

    private final CredentialService credentialService;

    public CredentialController(CredentialService credentialService) {
        this.credentialService = credentialService;
    }

    @PostMapping
    public ResponseEntity<CredentialResponseBody> createCredential(
            @RequestBody RegisterCredentialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(credentialService.createCredential(request));
    }

    @GetMapping
    public List<CredentialResponseBody> getAllCredentials() {
        return credentialService.getAllCredentials();
    }

    @GetMapping("/{id}")
    public CredentialResponseBody getCredentialById(@PathVariable Long id) {
        return credentialService.getCredentialById(id);
    }

    @PutMapping("/{id}")
    public CredentialResponseBody updateCredential(
            @PathVariable Long id,
            @RequestBody CredentialRequestBody request) {
        return credentialService.updateCredential(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCredential(@PathVariable Long id) {
        credentialService.deleteCredential(id);
        return ResponseEntity.noContent().build();
    }
}
