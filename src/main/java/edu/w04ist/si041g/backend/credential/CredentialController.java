package edu.w04ist.si041g.backend.credential;

import edu.w04ist.si041g.backend.credential.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<List<CredentialResponseBody>> getAllCredentials() {
        return ResponseEntity.ok(credentialService.getAllCredentials());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CredentialResponseBody> getCredentialById(@PathVariable Long id) {
        return ResponseEntity.ok(credentialService.getCredentialById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CredentialResponseBody> updateCredential(
            @PathVariable Long id,
            @RequestBody CredentialRequestBody request,
            Authentication authentication) {

        return ResponseEntity.ok(credentialService.updateCredential(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCredential(@PathVariable Long id) {
        credentialService.deleteCredential(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/email/{userId}")
    public ResponseEntity<Void> updateEmail(@PathVariable Long userId, @RequestBody CredentialRequestBody request, Authentication authentication) {
        if (!Long.valueOf(authentication.getName()).equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        credentialService.requestEmailUpdate(userId, request.getEmail());
        return ResponseEntity.ok().build();
    }
    @GetMapping("verify-change")
    public ResponseEntity<String> verifyChange(@RequestParam("token") String token) {
        String message = credentialService.verifyChange(token);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/password/{userId}")
    public ResponseEntity<Void> updatePassword(@PathVariable Long userId, @RequestBody CredentialRequestBody request, Authentication authentication) {
        if (!Long.valueOf(authentication.getName()).equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        credentialService.requestPasswordUpdate(userId, request.getPassword());
        return ResponseEntity.ok().build();
    }
}
